/**
 * Copyright (c) 2016-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton.internal.util;

import java.util.Map;

import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.flicbutton.FlicButtonBindingConstants;

import com.google.common.collect.ImmutableMap;

import io.flic.fliclib.javaclient.Bdaddr;

/**
 *
 * @author Patrick Fink
 *
 */
public class FlicButtonUtils {
    public static final Map<String, String> flicOpenhabTriggerEventMap = ImmutableMap.<String, String> builder()
            .put("ButtonSingleClick", CommonTriggerEvents.SHORT_PRESSED)
            .put("ButtonDoubleClick", CommonTriggerEvents.DOUBLE_PRESSED)
            .put("ButtonHold", CommonTriggerEvents.LONG_PRESSED).put("ButtonDown", CommonTriggerEvents.PRESSED)
            .put("ButtonUp", CommonTriggerEvents.RELEASED).build();

    public static ThingUID getThingUIDFromBdAddr(Bdaddr bdaddr, ThingUID bridgeUID) {
        String thingID = bdaddr.toString().replace(":", "-");
        return new ThingUID(FlicButtonBindingConstants.FLICBUTTON_THING_TYPE, bridgeUID, thingID);
    }

    public static Bdaddr getBdAddrFromThingUID(ThingUID thingUID) {
        String bdaddrRaw = thingUID.getId().replace("-", ":");
        return new Bdaddr(bdaddrRaw);
    }

}
