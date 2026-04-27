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
package org.openhab.binding.worxlandroid.internal.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ProductItemStatusDeserializer.class)
public abstract class AbstractProductItemStatus {

    public String id;
    public Integer product_id;
    public String name;
    public String serialNumber;

    // 🔥 AGGIUNGI QUESTI
    public String mqttEndpoint;
    public String uuid;
    public String userId;
    public MqttTopics mqttTopics;

    public class MqttTopics {
        public String commandIn;
        public String commandOut;
    }

    public boolean online;
}
