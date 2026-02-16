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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teslafleetapi.internal.handler.TeslaFleetAPIBridgeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class TeslaFleetAPIVehicleHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private @Nullable TeslaFleetAPIBridgeHandler bridge;

    public TeslaFleetAPIVehicleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Bridge bridgeThing = getBridge();
        if (bridgeThing != null && bridgeThing.getHandler() instanceof TeslaFleetAPIBridgeHandler) {
            this.bridge = (TeslaFleetAPIBridgeHandler) bridgeThing.getHandler();
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (bridge == null) {
            return;
        }

        String token = bridge.getAccessToken();
        if (token == null) {
            return;
        }

        logger.debug("Command {} on {}", command, channelUID);
    }
}
