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

public class TeslaFleetApi {

    private String baseUrl;
    private HttpClient client = HttpClient.newHttpClient();

    public void configure(TeslaFleetAPIBridgeConfiguration cfg) {
        switch (cfg.region) {
            case "eu":
                baseUrl = "https://fleet-api.prd.eu.vn.cloud.tesla.com";
                break;
            case "cn":
                baseUrl = "https://fleet-api.prd.cn.vn.cloud.tesla.cn";
                break;
            default:
                baseUrl = "https://fleet-api.prd.na.vn.cloud.tesla.com";
        }
    }

    public void pingVehicles(String accessToken) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/1/vehicles"))
                .header("Authorization", "Bearer " + accessToken).GET().build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Ping failed: HTTP " + resp.statusCode());
        }
    }
}
