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
package org.openhab.binding.worxlandroid.internal.model;

public class MowerStatus {

    private final String id;
    private final String name;
    private final String serialNumber;
    private final boolean online;
    private final MowerType type;

    public MowerStatus(String id, String name, String serialNumber, boolean online, MowerType type) {
        this.id = id;
        this.name = name;
        this.serialNumber = serialNumber;
        this.online = online;
        this.type = type;
    }

    public boolean isOnline() {
        return online;
    }

    public MowerType getType() {
        return type;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
}
