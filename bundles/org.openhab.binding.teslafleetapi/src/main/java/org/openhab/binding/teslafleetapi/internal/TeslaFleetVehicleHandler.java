package org.openhab.binding.teslafleet.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;

@NonNullByDefault
public class TeslaFleetVehicleHandler extends BaseThingHandler {

    private final TeslaFleetBridgeHandler bridge;

    public TeslaFleetVehicleHandler(Thing thing) {
        super(thing);
        this.bridge = (TeslaFleetBridgeHandler) getBridge().getHandler();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String token = bridge.getAccessToken();
        if (token == null) return;

        // Qui implementerai le chiamate a TeslaFleetApi
        logger.debug("Command {} on {}", command, channelUID);
    }
}
