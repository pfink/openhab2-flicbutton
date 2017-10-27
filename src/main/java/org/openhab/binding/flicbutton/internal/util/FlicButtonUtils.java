/**
 * Copyright (c) 2016-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton.internal.util;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.flicbutton.FlicButtonBindingConstants;

import io.flic.fliclib.javaclient.Bdaddr;

/**
 *
 * @author Patrick Fink
 *
 */
public class FlicButtonUtils {

    public static ThingUID getThingUIDFromBdAddr(Bdaddr bdaddr, ThingUID bridgeUID) {
        String thingID = bdaddr.toString().replace(":", "-");
        return new ThingUID(FlicButtonBindingConstants.FLICBUTTON_THING_TYPE, bridgeUID, thingID);
    }

}
