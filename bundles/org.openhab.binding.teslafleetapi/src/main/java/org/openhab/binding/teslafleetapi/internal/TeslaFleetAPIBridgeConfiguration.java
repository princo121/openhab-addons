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
package org.openhab.binding.teslafleetapi.internal;

import org.eclipse.jdt.annotation.Nullable;

public class TeslaFleetAPIBridgeConfiguration {

    /**
     * Region of the Tesla Fleet API cluster. Typical values:
     * - "na": North America
     * - "eu": Europe
     * - "cn": China
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
