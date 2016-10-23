/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton;

import java.util.Collections;
import java.util.Set;

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
    public final static ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "flicdaemon-bridge");
    public final static ThingTypeUID FLICBUTTON_THING_TYPE = new ThingTypeUID(BINDING_ID, "flicbutton-thing");

    public final static Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(FLICBUTTON_THING_TYPE);

    // List of all configuration options
    public static final String CONFIG_HOST_NAME = "hostname";
    public static final String CONFIG_PORT = "port";

    // List of all Channel ids
    public final static String CHANNEL_ID_BUTTON_PRESSED = "flicbutton-pressed-channel";
    public final static String CHANNEL_ID_BUTTON_TRIGGER = "button-trigger";

}
