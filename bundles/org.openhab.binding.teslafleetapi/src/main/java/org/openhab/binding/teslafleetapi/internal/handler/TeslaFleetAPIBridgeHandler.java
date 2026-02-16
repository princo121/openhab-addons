/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.teslafleetapi.internal.handler;

import static org.openhab.binding.teslafleetapi.internal.TeslaFleetAPIBindingConstants.*;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teslafleetapi.internal.TeslaFleetAPIAuthService;
import org.openhab.binding.teslafleetapi.internal.TeslaFleetAPIBridgeConfiguration;
import org.openhab.binding.teslafleetapi.internal.TeslaFleetApi;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TeslaFleetBridgeHandler
 *
 * Gestisce: - Lettura della configurazione del bridge (region, clientId,
 * clientSecret, redirectUri, refreshToken, polling) - Ciclo di polling per
 * "sanity check" con l'API (es. /vehicles) e aggiornamento stato ONLINE/OFFLINE
 * - Refresh automatico dell'access token quando necessario - Un accessor per
 * fornire il bearer token ai child handler (es. VehicleHandler)
 *
 * Requisiti API: - Autenticazione OAuth2 con header "Authorization: Bearer
 * <token>" - Scopes appropriati in base alle funzioni usate (es.
 * vehicle_device_data, vehicle_location, vehicle_cmds)
 *
 * Vedi documentazione Tesla Fleet API per dettagli di auth e scopes.
 *
 * @author You
 */
@NonNullByDefault
public class TeslaFleetAPIBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TeslaFleetAPIBridgeHandler.class);

    private final TeslaFleetAPIAuthService authService;
    private final TeslaFleetApi api;

    private @Nullable ScheduledFuture<?> pollingJob;

    private volatile @Nullable String accessToken;
    private volatile long accessTokenExpiresAtEpochSec = 0L;

    private volatile @Nullable String refreshToken;

    private volatile int pollingIntervalSeconds = 60;

    public TeslaFleetAPIBridgeHandler(Bridge bridge, TeslaFleetAPIAuthService authService, TeslaFleetApi api) {
        super(bridge);
        this.authService = Objects.requireNonNull(authService);
        this.api = Objects.requireNonNull(api);
    }

    // ------------------------------------------
    // Lifecycle
    // ------------------------------------------

    @Override
    public void initialize() {
        logger.debug("Initializing TeslaFleetBridgeHandler for thing {}", getThing().getUID());

        final Thing thing = getThing();
        final TeslaFleetAPIBridgeConfiguration cfg = thing.getConfiguration()
                .as(TeslaFleetAPIBridgeConfiguration.class);

        // Validate mandatory config
        if (cfg == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing configuration");
            return;
        }

        // Pull essentials
        this.refreshToken = trimOrNull(cfg.refreshToken);
        this.pollingIntervalSeconds = cfg.pollingIntervalSeconds > 0 ? cfg.pollingIntervalSeconds : 60;

        // Configure API base (region host, timeouts, etc.)
        try {
            api.configure(cfg); // e.g., set baseUrl by region, client credentials if needed, timeouts, etc.
        } catch (Exception ex) {
            logger.warn("Failed to configure TeslaFleetApi: {}", ex.getMessage(), ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid API configuration");
            return;
        }

        // First token acquisition/refresh (if we already have a refresh token)
        if (this.refreshToken == null) {
            // In molti flussi third‑party sarà la servlet di auth a popolare il
            // refreshToken sul bridge
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Authorization required (no refresh token present)");
            schedulePolling(false); // mantieni polling leggero per check di stato/attesa autorizzazione
            return;
        }

        if (!ensureValidAccessToken()) {
            // Se non riusciamo a generare l’access token, restiamo OFFLINE
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to obtain access token from Tesla");
            // Pianifica retry periodico: l’utente potrebbe completare l’autorizzazione o
            // correggere le credenziali
            schedulePolling(false);
            return;
        }

        // Quick connectivity check: per esempio, prova a leggere la lista veicoli
        if (checkConnectivity()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot reach Tesla Fleet API");
        }

        // Start polling loop
        schedulePolling(true);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing TeslaFleetBridgeHandler {}", getThing().getUID());
        cancelPolling();
        this.accessToken = null;
        this.refreshToken = null;
        this.accessTokenExpiresAtEpochSec = 0L;
        super.dispose();
    }

    // ------------------------------------------
    // Command handling (usually none on a Bridge)
    // ------------------------------------------

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Bridge received command on channel {}: {}", channelUID, command);
        // Tipicamente il bridge non espone canali comandabili; i comandi vanno ai
        // VehicleHandler.
        // Puoi eventualmente gestire un canale ‘refresh’ o simili.
    }

    // ------------------------------------------
    // Polling & Connectivity
    // ------------------------------------------

    private void schedulePolling(boolean verbose) {
        cancelPolling();
        int interval = Math.max(15, this.pollingIntervalSeconds); // imposta un minimo per evitare hammering
        this.pollingJob = scheduler.scheduleWithFixedDelay(this::pollStatus, interval, interval, TimeUnit.SECONDS);
        if (verbose) {
            logger.debug("Scheduled Tesla Fleet polling every {}s", interval);
        }
    }

    private void cancelPolling() {
        final ScheduledFuture<?> job = this.pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        this.pollingJob = null;
    }

    private void pollStatus() {
        try {
            if (!ensureValidAccessToken()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to refresh access token");
                return;
            }

            if (checkConnectivity()) {
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connectivity check failed");
            }
        } catch (Exception ex) {
            logger.info("Unexpected error during polling status: {}", ex.getMessage(), ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        }
    }

    private boolean checkConnectivity() {
        final String token = getAccessToken();
        if (token == null) {
            return false;
        }
        try {
            // Esempio: /api/1/vehicles per validare sessione e latenza
            api.pingVehicles(token); // implementa una chiamata leggera lato API
            return true;
        } catch (Exception ex) {
            logger.debug("Connectivity check failed: {}", ex.getMessage());
            return false;
        }
    }

    // ------------------------------------------
    // Token lifecycle
    // ------------------------------------------

    private boolean ensureValidAccessToken() {
        final long now = Instant.now().getEpochSecond();

        // Se abbiamo un token ancora valido per almeno 30s, mantienilo
        if (accessToken != null && now + 30 < accessTokenExpiresAtEpochSec) {
            return true;
        }

        final String currentRefresh = this.refreshToken;
        if (currentRefresh == null) {
            logger.debug("No refresh token available yet");
            return false;
        }

        try {
            final TeslaFleetAPIAuthService.TokenResponse tr = authService.refreshAccessToken(currentRefresh);
            if (tr == null || tr.accessToken() == null) {
                return false;
            }

            this.accessToken = tr.accessToken();
            this.accessTokenExpiresAtEpochSec = now + Math.max(1, tr.expiresInSec());
            if (tr.refreshToken() != null && !tr.refreshToken().isBlank()) {
                // Alcuni provider ruotano anche il refresh token
                this.refreshToken = tr.refreshToken();
                // salva in thing config (persisti la rotazione)
                final String rotated = tr.refreshToken();
                if (rotated != null && !rotated.isBlank()) {
                    this.refreshToken = rotated;
                    persistRefreshToken(rotated);
                }
                // persistRefreshToken(this.refreshToken);
            }
            logger.debug("Obtained new Tesla Fleet access token; expires in {}s", tr.expiresInSec());
            return true;
        } catch (Exception ex) {
            logger.debug("Failed to refresh access token: {}", ex.getMessage());
            return false;
        }
    }

    private void persistRefreshToken(String newRefreshToken) {
        // Aggiorna la config del thing in modo thread‑safe
        try {
            final var cfg = getThing().getConfiguration();
            cfg.put("refreshToken", newRefreshToken);
            editThing().withConfiguration(cfg).build();
        } catch (Exception e) {
            logger.warn("Failed to persist new refresh token: {}", e.getMessage());
        }
    }

    /**
     * Fornisce ai child handler un access token valido (o null se non disponibile).
     */
    public @Nullable String getAccessToken() {
        if (!ensureValidAccessToken()) {
            return null;
        }
        return this.accessToken;
    }

    // ------------------------------------------
    // Utility
    // ------------------------------------------

    private static @Nullable String trimOrNull(@Nullable String s) {
        if (s == null)
            return null;
        final String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
