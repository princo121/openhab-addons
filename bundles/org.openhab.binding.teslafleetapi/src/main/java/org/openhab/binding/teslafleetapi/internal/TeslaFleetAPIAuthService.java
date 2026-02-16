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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TeslaFleetAPIAuthService {

    public static record TokenResponse(@NonNull String accessToken, int expiresInSec, @Nullable String refreshToken) {
    }

    private final HttpClient client = HttpClient.newHttpClient();

    public TokenResponse refreshAccessToken(String refreshToken) throws Exception {

        JsonObject json = new JsonObject();
        json.addProperty("grant_type", "refresh_token");
        json.addProperty("refresh_token", refreshToken);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://fleet-auth.prd.vn.cloud.tesla.com/oauth2/v3/token"))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalStateException("Refresh failed: HTTP " + res.statusCode());
        }

        JsonObject o = JsonParser.parseString(res.body()).getAsJsonObject();
        // JsonObject o = Json.createReader(new java.io.StringReader(res.body())).readObject();

        return new TokenResponse(o.get("access_token").getAsString(), o.get("expires_in").getAsInt(),
                o.has("refresh_token") ? o.get("refresh_token").getAsString() : null);
    }
}
