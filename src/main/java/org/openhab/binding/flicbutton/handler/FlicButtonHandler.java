/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton.handler;

import java.util.Objects;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
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
        // TODO: Currently, just online is assumed. To be really correct, we have to ask Flic Daemon if button is
        // currently connected
        updateStatus(ThingStatus.ONLINE);
    }

    void flicConnectionStatusChanged(ConnectionStatus connectionStatus, DisconnectReason disconnectReason) {
        if (connectionStatus == ConnectionStatus.Disconnected) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Disconnect Reason: " + Objects.toString(disconnectReason));
        } else {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Button reconnected.");
        }
    }

    void flicButtonDown() {
        ChannelUID channelUID = thing.getChannel(FlicButtonBindingConstants.CHANNEL_ID_BUTTON_PRESSED_SWITCH).getUID();
        updateState(channelUID, OnOffType.ON);
        fireTriggerEvent(CommonTriggerEvents.PRESSED);
    }

    void flicButtonUp() {
        ChannelUID channelUID = thing.getChannel(FlicButtonBindingConstants.CHANNEL_ID_BUTTON_PRESSED_SWITCH).getUID();
        updateState(channelUID, OnOffType.OFF);
        fireTriggerEvent(CommonTriggerEvents.RELEASED);
    }

    void flicButtonClickedSingle() {
        fireTriggerEvent(CommonTriggerEvents.SHORT_PRESSED);
    }

    void flicButtonClickedDouble() {
        fireTriggerEvent(CommonTriggerEvents.DOUBLE_PRESSED);
    }

    void flicButtonClickedHold() {
        fireTriggerEvent(CommonTriggerEvents.LONG_PRESSED);
    }

    private void fireTriggerEvent(String event) {
        ChannelUID channelUID = thing.getChannel(FlicButtonBindingConstants.CHANNEL_ID_BUTTON_EVENTS).getUID();
        triggerChannel(channelUID, event);
    }
}
