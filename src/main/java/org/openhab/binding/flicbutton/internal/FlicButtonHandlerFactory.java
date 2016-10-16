/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton.internal;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.flicbutton.FlicButtonBindingConstants;
import org.openhab.binding.flicbutton.handler.FlicButtonHandler;
import org.openhab.binding.flicbutton.handler.FlicDaemonBridgeHandler;

import com.google.common.collect.Sets;

/**
 * The {@link FlicButtonHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Patrick Fink - Initial contribution
 */
public class FlicButtonHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.union(
            FlicButtonBindingConstants.BRIDGE_THING_TYPES_UIDS, FlicButtonBindingConstants.SUPPORTED_THING_TYPES_UIDS);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(FlicButtonBindingConstants.FLICBUTTON_THING_TYPE)) {
            return new FlicButtonHandler(thing);
        } else if (thingTypeUID.equals(FlicButtonBindingConstants.BRIDGE_THING_TYPE)) {
            return new FlicDaemonBridgeHandler((Bridge) thing);
        }

        return null;
    }
}
