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

import org.openhab.core.thing.ThingTypeUID;

public final class TeslaFleetAPIBindingConstants {

    // ------------------------------------------------------------------------
    // Binding ID
    // ------------------------------------------------------------------------
    public static final String BINDING_ID = "teslafleet";

    // ------------------------------------------------------------------------
    // Thing Type UIDs
    // ------------------------------------------------------------------------
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    public static final ThingTypeUID THING_TYPE_VEHICLE = new ThingTypeUID(BINDING_ID, "vehicle");

    // ------------------------------------------------------------------------
    // Channel IDs (per i Vehicle Thing)
    // ------------------------------------------------------------------------

    // Stato/Info generali
    public static final String CHANNEL_VEHICLE_NAME = "vehicleName";
    public static final String CHANNEL_VIN = "vin";
    public static final String CHANNEL_SOC = "stateOfCharge";
    public static final String CHANNEL_BATTERY_RANGE = "batteryRangeKm";
    public static final String CHANNEL_ODOMETER = "odometerKm";
    public static final String CHANNEL_STATE = "vehicleState"; // es. online/offline/asleep

    // Posizione
    public static final String CHANNEL_LATITUDE = "lat";
    public static final String CHANNEL_LONGITUDE = "lon";
    public static final String CHANNEL_HEADING = "heading";

    // Charging
    public static final String CHANNEL_CHARGING_STATE = "chargingState";
    public static final String CHANNEL_CHARGING_POWER = "chargingPowerKw";
    public static final String CHANNEL_CHARGING_VOLTAGE = "chargingVoltage";
    public static final String CHANNEL_CHARGING_CURRENT = "chargingCurrent";

    // Climate
    public static final String CHANNEL_INSIDE_TEMP = "insideTemp";
    public static final String CHANNEL_OUTSIDE_TEMP = "outsideTemp";
    public static final String CHANNEL_CLIMATE_ON = "climateOn";

    // Comandi (azioni)
    public static final String CHANNEL_CMD_WAKE_UP = "wakeUp";
    public static final String CHANNEL_CMD_UNLOCK = "unlockDoors";
    public static final String CHANNEL_CMD_LOCK = "lockDoors";
    public static final String CHANNEL_CMD_HONK_HORN = "honkHorn";
    public static final String CHANNEL_CMD_FLASH_LIGHTS = "flashLights";
    public static final String CHANNEL_CMD_START_CLIMATE = "startClimate";
    public static final String CHANNEL_CMD_STOP_CLIMATE = "stopClimate";

    // ------------------------------------------------------------------------
    // Constructor privato per evitare istanziazione
    // ------------------------------------------------------------------------
    private TeslaFleetAPIBindingConstants() {
        // No instance
    }
}
