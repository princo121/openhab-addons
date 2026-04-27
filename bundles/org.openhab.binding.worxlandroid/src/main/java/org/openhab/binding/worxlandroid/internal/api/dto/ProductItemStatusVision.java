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

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * The {@link ProductItemStatusVision} class
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class ProductItemStatusVision extends ProductItemStatus {

    public class Accessories {
        public boolean ultrasonic;
    }

    public class MqttTopics {
        public String commandIn;
        public String commandOut;
    }

    public class SetupLocation {
        public double latitude;
        public double longitude;
    }

    public class AppSettings {
        boolean cellularSetupCompleted;
    }

    public class City {
        public int id;
        public int countryId;
        public String name;
        public double latitude;
        public double longitude;
        public String createdAt;
        public String updatedAt;
    }

    public class Sim {
        public int id;
        public String iccid;
        public String simStatus;
        public boolean pendingActivation;
        public Instant contractStartsAt;
        public Instant contractEndsAt;
        public Instant createdAt;
        public Instant updatedAt;
    }

    public class AutoSchedule {
        public int boost;
        public String grassType;
        public boolean irrigation;
        public Map<String, String> nutrition;
        public String soilType;
    }

    // public String id;
    // public String uuid;
    // public int productId;
    // public StringsrId;
    public String serialNumber;
    public String macAddress;
    public boolean locked;
    public String firmwareVersion;
    public boolean firmwareAutoUpgrade;
    public Sim sim;
    public boolean test;
    public boolean iotRegistered;
    public boolean mqttRegistered;
    public String registeredAt;
    // public boolean online;
    public int protocol;
    public String pendingRadioLinkValidation;
    public List<String> capabilities;
    public List<String> capabilitiesAvailable;
    // public String mqttEndpoint;
    // public MqttTopics mqttTopics;
    public String name;
    public long bladeHeightShift;
    public boolean pushNotifications;
    public String pushNotificationsLevel;
    public String pinCode;
    public AppSettings appSettings;
    public boolean warrantyRegistered;

    public Accessories accessories;

    public String purchasedAt;
    public String warrantyExpiresAt;
    public SetupLocation setupLocation;
    public City city;
    public ZoneId timeZone;
    public double lawnSize;
    public double lawnPerimeter;
    public AutoSchedule autoScheduleSettings;
    public boolean autoSchedule;
    public boolean improvement;
    public boolean diagnostic;
    public long distanceCovered;
    public long mowerWorkTime;

    public long bladeWorkTime;
    public long bladeWorkTimeReset;
    public Instant bladeWorkTimeResetAt;

    public int batteryChargeCycles;
    public int batteryChargeCyclesReset;
    public Instant batteryChargeCyclesResetAt;

    public Instant createdAt;
    public Instant updatedAt;
    public LastStatus lastStatus;
}
