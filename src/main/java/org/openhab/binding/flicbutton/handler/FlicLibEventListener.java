package org.openhab.binding.flicbutton.handler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.ButtonConnectionChannel;
import io.flic.fliclib.javaclient.enums.ClickType;
import io.flic.fliclib.javaclient.enums.ConnectionStatus;
import io.flic.fliclib.javaclient.enums.CreateConnectionChannelError;
import io.flic.fliclib.javaclient.enums.DisconnectReason;
import io.flic.fliclib.javaclient.enums.RemovedReason;

public class FlicLibEventListener extends ButtonConnectionChannel.Callbacks {
    private final Logger logger = LoggerFactory.getLogger(FlicLibEventListener.class);

    private final FlicLibBridgeHandler bridgeHandler;

    FlicLibEventListener(FlicLibBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public void onCreateConnectionChannelResponse(ButtonConnectionChannel channel,
            CreateConnectionChannelError createConnectionChannelError, ConnectionStatus connectionStatus) {

        logger.debug("Create response " + channel.getBdaddr() + ": " + createConnectionChannelError + ", "
                + connectionStatus);

    }

    @Override
    public void onRemoved(ButtonConnectionChannel channel, RemovedReason removedReason) {

        logger.debug("Channel removed for " + channel.getBdaddr() + ": " + removedReason);

    }

    @Override
    public void onConnectionStatusChanged(ButtonConnectionChannel channel, ConnectionStatus connectionStatus,
            DisconnectReason disconnectReason) {

        logger.debug("New status for " + channel.getBdaddr() + ": " + connectionStatus
                + (connectionStatus == ConnectionStatus.Disconnected ? ", " + disconnectReason : ""));

    }

    @Override
    public void onButtonUpOrDown(ButtonConnectionChannel channel, ClickType clickType, boolean wasQueued, int timeDiff)
            throws IOException {

        logger.debug(channel.getBdaddr() + " " + (clickType == ClickType.ButtonUp ? "Up" : "Down"));

    }

}
