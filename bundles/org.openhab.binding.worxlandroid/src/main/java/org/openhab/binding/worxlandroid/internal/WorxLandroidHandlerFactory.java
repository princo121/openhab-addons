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
package org.openhab.binding.worxlandroid.internal;

import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.THING_TYPE_BRIDGE;
import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.THING_TYPE_MOWER;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.worxlandroid.internal.api.WorxApiDeserializer;
import org.openhab.binding.worxlandroid.internal.api.WorxApiHandler;
import org.openhab.binding.worxlandroid.internal.discovery.MowerDiscoveryService;
import org.openhab.binding.worxlandroid.internal.handler.WorxLandroidBridgeHandler;
import org.openhab.binding.worxlandroid.internal.handler.WorxLandroidMowerHandler;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WorxLandroidHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Nils Billing - Initial contribution
 * @author Gaël L'hopital - Added oAuthFactory
 */
@NonNullByDefault
@Component(configurationPid = "binding.worxlandroid", service = ThingHandlerFactory.class)
public class WorxLandroidHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_MOWER, THING_TYPE_BRIDGE);

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final OAuthFactory oAuthFactory;
    private final WorxApiHandler worxApiHandler;
    private final TimeZoneProvider timeZoneProvider;
    private final Logger logger = LoggerFactory.getLogger(WorxLandroidHandlerFactory.class);

    @Reference
    private WorxApiDeserializer deserializer;

    @Activate
    public WorxLandroidHandlerFactory(final @Reference OAuthFactory oAuthFactory,
            final @Reference WorxApiHandler worxApiHandler, final @Reference TimeZoneProvider timeZoneProvider,
            final @Reference WorxApiDeserializer deserializer) {

        logger.debug("WorxLandroidHandlerFactory - 1");
        this.oAuthFactory = oAuthFactory;
        logger.debug("WorxLandroidHandlerFactory - 2");
        this.worxApiHandler = worxApiHandler;
        logger.debug("WorxLandroidHandlerFactory - 3");
        this.timeZoneProvider = timeZoneProvider;
        logger.debug("WorxLandroidHandlerFactory - 4");
        this.deserializer = deserializer;
        logger.debug("WorxLandroidHandlerFactory - 5");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.debug("WorxLandroidHandlerFactory - 6");
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("WorxLandroidHandlerFactory - 7");

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            logger.debug("WorxLandroidHandlerFactory - 8");
            WorxLandroidBridgeHandler bridgeHandler = new WorxLandroidBridgeHandler((Bridge) thing, worxApiHandler,
                    oAuthFactory);
            logger.debug("WorxLandroidHandlerFactory - 9");
            MowerDiscoveryService discoveryService = new MowerDiscoveryService(bridgeHandler);
            logger.debug("WorxLandroidHandlerFactory - 10");
            discoveryServiceRegs.put(thing.getUID(), bundleContext.registerService(DiscoveryService.class.getName(),
                    discoveryService, new Hashtable<>()));

            return bridgeHandler;
        } else if (THING_TYPE_MOWER.equals(thingTypeUID)) {
            return new WorxLandroidMowerHandler(thing, deserializer, timeZoneProvider);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler handler) {
        if (handler instanceof WorxLandroidBridgeHandler) {
            ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(handler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
        super.removeHandler(handler);
    }
}
