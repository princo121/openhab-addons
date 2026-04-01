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

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.teslafleetapi.internal.TeslaFleetAPIAuthService;
import org.openhab.binding.teslafleetapi.internal.TeslaFleetAPIBridgeConfiguration;
import org.openhab.binding.teslafleetapi.internal.TeslaFleetApi;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
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
    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;

    private @Nullable ScheduledFuture<?> pollingJob;

    private volatile @Nullable String accessToken;
    private volatile long accessTokenExpiresAtEpochSec = 0L;

    private volatile @Nullable String refreshToken;

    private volatile int pollingIntervalSeconds = 60;

    public TeslaFleetAPIBridgeHandler(Bridge bridge, OAuthFactory oAuthFactory, HttpClient httpClient,
            TeslaFleetAPIAuthService authService) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
    }

    // ------------------------------------------
    // Lifecycle
    // ------------------------------------------

    @Override
    public void initialize() {
        logger.debug("Initializing TeslaFleetBridgeHandler for thing {}", getThing().getUID());
        updateStatus(ThingStatus.UNKNOWN);
        active = true;
        configuration = getConfigAs(TeslaFleetAPIBridgeConfiguration.class);
        OAuthClientService oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(),
                TESLA_AUTHORIZE_URL, null, configuration.clientId, configuration.clientSecret, SPOTIFY_SCOPES, true);
        this.oAuthService = oAuthService;
        oAuthService.addAccessTokenRefreshListener(TeslaFleetAPIBridgeHandler.this);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing TeslaFleetBridgeHandler {}", getThing().getUID());
        OAuthClientService oAuthService = this.oAuthService;
        if (oAuthService != null) {
            oAuthService.removeAccessTokenRefreshListener(this);
            oAuthFactory.ungetOAuthService(thing.getUID().getAsString());
            this.oAuthService = null;
        }
    }

    @Override
    public boolean isOnline() {
        return thing.getStatus() == ThingStatus.ONLINE;
    }

    @Override
    public String formatAuthorizationUrl(String redirectUri) {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            if (oAuthService == null) {
                throw new OAuthException("OAuth service is not initialized");
            }
            return oAuthService.getAuthorizationUrl(redirectUri, null, thing.getUID().getAsString());
        } catch (final OAuthException e) {
            logger.debug("Error constructing AuthorizationUrl: ", e);
            return "";
        }
    }

    @Override
    public String authorize(String redirectUri, String reqCode) {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            if (oAuthService == null) {
                throw new OAuthException("OAuth service is not initialized");
            }
            logger.debug("Make call to Tesla to get access token.");
            final AccessTokenResponse credentials = oAuthService.getAccessTokenResponseByAuthorizationCode(reqCode,
                    redirectUri);
            final String user = updateProperties(credentials);
            logger.debug("Authorized for user: {}", user);
            startPolling();
            return user;
        } catch (RuntimeException | OAuthException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw new SpotifyException(e.getMessage(), e);
        } catch (final OAuthResponseException e) {
            throw new SpotifyAuthorizationException(e.getMessage(), e);
        }
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
