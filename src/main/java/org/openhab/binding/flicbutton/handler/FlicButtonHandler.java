/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.flicbutton.FlicButtonBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.enums.ConnectionStatus;
import io.flic.fliclib.javaclient.enums.DisconnectReason;

/**
 * The {@link FlicButtonHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrick Fink - Initial contribution
 */
public class FlicButtonHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(FlicButtonHandler.class);

    public FlicButtonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Pure sensor -> no commands have to be handled
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");

    }

    void flicConnectionStatusChanged(ConnectionStatus connectionStatus, DisconnectReason disconnectReason) {
        if (connectionStatus == ConnectionStatus.Disconnected) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    disconnectReason.toString());
        } else {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Button reconnected.");
        }
    }

    void flicButtonDown() {
        ChannelUID channelUID = thing.getChannel(FlicButtonBindingConstants.CHANNEL_ID_BUTTON_PRESSED).getUID();
        updateState(channelUID, OnOffType.ON);
    }

    void flicButtonUp() {
        ChannelUID channelUID = thing.getChannel(FlicButtonBindingConstants.CHANNEL_ID_BUTTON_PRESSED).getUID();
        updateState(channelUID, OnOffType.OFF);
    }
}
