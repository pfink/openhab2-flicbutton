/**
 * Copyright (c) 2016-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton.handler;

import static org.openhab.binding.flicbutton.FlicButtonBindingConstants.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.flicbutton.internal.util.FlicButtonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.Bdaddr;
import io.flic.fliclib.javaclient.ButtonConnectionChannel;
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
    private ScheduledFuture delayedDisconnect;
    private DisconnectReason latestDisconnectReason;
    private FlicDaemonBridgeHandler bridge;
    private ButtonConnectionChannel connectionChannel;

    public FlicButtonHandler(Thing thing) {
        super(thing);
        bridge = (FlicDaemonBridgeHandler) getBridge();
    }

    public Bdaddr getBdaddr() {
        return FlicButtonUtils.getBdAddrFromThingUID(getThing().getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Pure sensor -> no commands have to be handled
    }

    @Override
    public void initialize() {
        try {
            FlicButtonEventListener eventListener = new FlicButtonEventListener(this);
            connectionChannel = new ButtonConnectionChannel(getBdaddr(), eventListener);
            bridge.getFlicClient().addConnectionChannel(connectionChannel);
            connectionChannel.wait(5000);
        } catch (IOException | InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void handleRemoval() {
        try {
            bridge.getFlicClient().removeConnectionChannel(connectionChannel);
        } catch (IOException e) {
            logger.error("Error occured while removing button channel: {}", e);
        }

        super.handleRemoval();
    }

    void flicConnectionStatusChanged(ConnectionStatus connectionStatus, DisconnectReason disconnectReason) {
        if (connectionStatus == ConnectionStatus.Disconnected) {
            // Status change to offline have to be scheduled to improve stability, see issue #2
            latestDisconnectReason = disconnectReason;
            scheduleStatusChangeToOffline();
        } else {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Button reconnected.");
        }
    }

    private void scheduleStatusChangeToOffline() {
        if (delayedDisconnect == null) {
            delayedDisconnect = scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "Disconnect Reason: " + Objects.toString(latestDisconnectReason));
                }
            }, BUTTON_OFFLINE_GRACE_PERIOD_SECONDS, TimeUnit.SECONDS);
        }
    }

    // Cleanup delayedDisconnect on status change to online
    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        if (status == ThingStatus.ONLINE && delayedDisconnect != null) {
            delayedDisconnect.cancel(false);
            delayedDisconnect = null;
        }
        super.updateStatus(status, statusDetail, description);
    }

    void flicButtonRemoved() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                "Button was removed/detached from flicd (e.g. by simpleclient).");
    }

    void fireTriggerEvent(String event) {
        String channelID = event == CommonTriggerEvents.PRESSED || event == CommonTriggerEvents.RELEASED
                ? CHANNEL_ID_RAWBUTTON_EVENTS
                : CHANNEL_ID_BUTTON_EVENTS;
        updateStatus(ThingStatus.ONLINE);
        ChannelUID channelUID = thing.getChannel(channelID).getUID();
        triggerChannel(channelUID, event);
    }
}
