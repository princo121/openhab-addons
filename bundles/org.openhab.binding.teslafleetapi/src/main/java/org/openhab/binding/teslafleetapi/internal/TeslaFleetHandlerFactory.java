package org.openhab.binding.teslafleet.internal;

import org.openhab.binding.teslafleet.internal.handler.*;
import org.openhab.binding.teslafleet.internal.auth.*;
import org.openhab.binding.teslafleet.internal.api.*;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

@Component(service = ThingHandlerFactory.class, configurationPid = "binding.teslafleet")
public class TeslaFleetHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID type) {
        return type.equals(TeslaFleetBindingConstants.THING_TYPE_BRIDGE) ||
               type.equals(TeslaFleetBindingConstants.THING_TYPE_VEHICLE);
    }

    @Override
    public ThingHandler createHandler(Thing thing) {
        ThingTypeUID type = thing.getThingTypeUID();

        if (type.equals(TeslaFleetBindingConstants.THING_TYPE_BRIDGE)) {
            return new TeslaFleetBridgeHandler(
                    (Bridge) thing,
                    new TeslaFleetAuthService(),
                    new TeslaFleetApi()
            );
        }

        if (type.equals(TeslaFleetBindingConstants.THING_TYPE_VEHICLE)) {
            return new TeslaFleetVehicleHandler(thing);
        }

        return null;
    }
}
