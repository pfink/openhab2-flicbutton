/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FlicButtonBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrick Fink - Initial contribution
 */
public class FlicButtonBindingConstants {

    public static final String BINDING_ID = "flicbutton";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_FLICBUTTON = new ThingTypeUID(BINDING_ID, "flicbutton-thingtype");

    // List of all Channel ids
    public final static String CHANNEL_1 = "channel1";

}
