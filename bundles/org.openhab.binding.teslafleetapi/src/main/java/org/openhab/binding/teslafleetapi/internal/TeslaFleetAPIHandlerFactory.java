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

import org.openhab.binding.teslafleetapi.internal.handler.*;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

@Component(service = ThingHandlerFactory.class, configurationPid = "binding.teslafleetapi")
public class TeslaFleetAPIHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID type) {
        return type.equals(TeslaFleetAPIBindingConstants.THING_TYPE_BRIDGE)
                || type.equals(TeslaFleetAPIBindingConstants.THING_TYPE_VEHICLE);
    }

    @Override
    public ThingHandler createHandler(Thing thing) {
        ThingTypeUID type = thing.getThingTypeUID();

        if (type.equals(TeslaFleetAPIBindingConstants.THING_TYPE_BRIDGE)) {
            return new TeslaFleetAPIBridgeHandler((Bridge) thing, new TeslaFleetAPIAuthService(), new TeslaFleetApi());
        }

        if (type.equals(TeslaFleetAPIBindingConstants.THING_TYPE_VEHICLE)) {
            return new TeslaFleetAPIVehicleHandler(thing);
        }

        return null;
    }
}
