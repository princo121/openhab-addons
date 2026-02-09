package org.openhab.binding.teslafleet.internal.auth;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.json.Json;
import javax.json.JsonObject;

public class TeslaFleetAuthService {

    record TokenResponse(String accessToken, int expiresInSec, String refreshToken) {}

    private final HttpClient client = HttpClient.newHttpClient();

    public TokenResponse refreshAccessToken(String refreshToken) throws Exception {

        var json = Json.createObjectBuilder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://fleet-auth.prd.vn.cloud.tesla.com/oauth2/v3/token"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalStateException("Refresh failed: HTTP " + res.statusCode());
        }

        JsonObject o = Json.createReader(new java.io.StringReader(res.body())).readObject();

        return new TokenResponse(
                o.getString("access_token"),
                o.getInt("expires_in"),
                o.containsKey("refresh_token") ? o.getString("refresh_token") : null
        );
    }
}
