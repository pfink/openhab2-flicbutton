package org.openhab.binding.flicbutton.handler;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.ButtonConnectionChannel;
import io.flic.fliclib.javaclient.enums.ClickType;
import io.flic.fliclib.javaclient.enums.ConnectionStatus;
import io.flic.fliclib.javaclient.enums.CreateConnectionChannelError;
import io.flic.fliclib.javaclient.enums.DisconnectReason;
import io.flic.fliclib.javaclient.enums.RemovedReason;

public class FlicDaemonBridgeEventListener extends ButtonConnectionChannel.Callbacks {
    private final Logger logger = LoggerFactory.getLogger(FlicDaemonBridgeEventListener.class);

    private final FlicDaemonBridgeHandler bridgeHandler;

    FlicDaemonBridgeEventListener(FlicDaemonBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public void onCreateConnectionChannelResponse(ButtonConnectionChannel channel,
            CreateConnectionChannelError createConnectionChannelError, ConnectionStatus connectionStatus) {
        logger.debug("Create response " + channel.getBdaddr() + ": " + createConnectionChannelError + ", "
                + connectionStatus);
        // Handling does not differ from Status change, so redirect
        onConnectionStatusChanged(channel, connectionStatus, null);
    }

    @Override
    public void onRemoved(ButtonConnectionChannel channel, RemovedReason removedReason) {
        // TODO: Handle removed button
        logger.debug("Channel removed for " + channel.getBdaddr() + ": " + removedReason);

    }

    @Override
    public void onConnectionStatusChanged(ButtonConnectionChannel channel, ConnectionStatus connectionStatus,
            DisconnectReason disconnectReason) {
        logger.debug("New status for " + channel.getBdaddr() + ": " + connectionStatus
                + (connectionStatus == ConnectionStatus.Disconnected ? ", " + disconnectReason : ""));

        Thing flicButtonThing = bridgeHandler.getFlicButtonThing(channel.getBdaddr());

        if (flicButtonThing != null) {
            FlicButtonHandler thingHandler = (FlicButtonHandler) flicButtonThing.getHandler();
            thingHandler.flicConnectionStatusChanged(connectionStatus, disconnectReason);

        } else if (connectionStatus != ConnectionStatus.Disconnected) {
            bridgeHandler.getButtonDiscoveryService().flicButtonDiscovered(channel.getBdaddr());
        }
    }

    @Override
    public void onButtonUpOrDown(ButtonConnectionChannel channel, ClickType clickType, boolean wasQueued, int timeDiff)
            throws IOException {

        logger.debug(channel.getBdaddr() + " " + (clickType == ClickType.ButtonUp ? "Up" : "Down"));

        Thing flicButtonThing = bridgeHandler.getFlicButtonThing(channel.getBdaddr());

        if (flicButtonThing != null) {
            FlicButtonHandler thingHandler = (FlicButtonHandler) flicButtonThing.getHandler();

            if (clickType == ClickType.ButtonUp) {
                thingHandler.flicButtonUp();
            } else {
                thingHandler.flicButtonDown();
            }
        }
    }

    @Override
    public void onButtonSingleOrDoubleClickOrHold(ButtonConnectionChannel channel, ClickType clickType,
            boolean wasQueued, int timeDiff) throws IOException {

        logger.debug(channel.getBdaddr() + " " + clickType.name());

        Thing flicButtonThing = bridgeHandler.getFlicButtonThing(channel.getBdaddr());

        if (flicButtonThing != null) {
            FlicButtonHandler thingHandler = (FlicButtonHandler) flicButtonThing.getHandler();

            if (clickType == ClickType.ButtonSingleClick) {
                thingHandler.flicButtonClickedSingle();
            } else if (clickType == ClickType.ButtonDoubleClick) {
                thingHandler.flicButtonClickedDouble();
            } else if (clickType == ClickType.ButtonHold) {
                thingHandler.flicButtonClickedHold();
            }
        }
    }
}
