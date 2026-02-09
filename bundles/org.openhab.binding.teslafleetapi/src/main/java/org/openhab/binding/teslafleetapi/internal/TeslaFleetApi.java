package org.openhab.binding.teslafleet.internal.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.openhab.binding.teslafleet.internal.TeslaFleetBridgeConfiguration;

public class TeslaFleetApi {

    private String baseUrl;
    private HttpClient client = HttpClient.newHttpClient();

    public void configure(TeslaFleetBridgeConfiguration cfg) {
        switch (cfg.region) {
            case "eu": baseUrl = "https://fleet-api.prd.eu.vn.cloud.tesla.com"; break;
            case "cn": baseUrl = "https://fleet-api.prd.cn.vn.cloud.tesla.cn"; break;
            default:   baseUrl = "https://fleet-api.prd.na.vn.cloud.tesla.com";
        }
    }

    public void pingVehicles(String accessToken) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/1/vehicles"))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Ping failed: HTTP " + resp.statusCode());
        }
    }
}
