package org.openhab.binding.teslafleet.internal;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration class for the Tesla Fleet API Bridge.
 *
 * All fields map 1:1 to the configuration parameters of the Bridge Thing.
 * These values are injected automatically by openHAB from the thing configuration.
 *
 * Typical fields:
 *   - region:            API region ("na", "eu", "cn")
 *   - clientId:          OAuth client id
 *   - clientSecret:      OAuth client secret
 *   - redirectUri:       Redirect URL used during authorization
 *   - refreshToken:      OAuth refresh token (persisted once obtained)
 *   - pollingIntervalSeconds: how often the bridge polls Tesla for sanity/online checks
 *
 * This class must be a pure POJO: getters/setters are not required.
 */
public class TeslaFleetBridgeConfiguration {

    /**
     * Region of the Tesla Fleet API cluster. Typical values:
     *   - "na": North America
     *   - "eu": Europe
     *   - "cn": China
     */
    public @Nullable String region;

    /**
     * OAuth Client ID of the registered Tesla application.
     */
    public @Nullable String clientId;

    /**
     * OAuth Client Secret of the registered Tesla application.
     */
    public @Nullable String clientSecret;

    /**
     * Redirect URI configured in the Tesla OAuth application,
     * used during the authorization flow.
     */
    public @Nullable String redirectUri;

    /**
     * Refresh token obtained after completing the OAuth authorization flow.
     * Stored persistently once the user authorizes the bridge.
     */
    public @Nullable String refreshToken;

    /**
     * Polling interval for internal status checks (seconds).
     * Default is 60 if not specified.
     */
    public int pollingIntervalSeconds = 60;
}
