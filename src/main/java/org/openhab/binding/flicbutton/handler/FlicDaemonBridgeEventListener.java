package org.openhab.binding.flicbutton.handler;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.smarthome.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.Bdaddr;
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

    private Optional<FlicButtonHandler> getFlicButtonHandler(Bdaddr bdaddr) {
        Thing flicButtonThing = bridgeHandler.getFlicButtonThing(bdaddr);

        if (flicButtonThing != null) {
            FlicButtonHandler thingHandler = (FlicButtonHandler) flicButtonThing.getHandler();
            return Optional.of(thingHandler);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void onCreateConnectionChannelResponse(ButtonConnectionChannel channel,
            CreateConnectionChannelError createConnectionChannelError, ConnectionStatus connectionStatus) {
        logger.debug("Create response {}: {}, {}", channel.getBdaddr(), createConnectionChannelError, connectionStatus);
        // Handling does not differ from Status change, so redirect
        onConnectionStatusChanged(channel, connectionStatus, null);
    }

    @Override
    public void onRemoved(ButtonConnectionChannel channel, RemovedReason removedReason) {
        getFlicButtonHandler(channel.getBdaddr()).ifPresent(FlicButtonHandler::flicButtonRemoved);

        logger.debug("Button {} removed. ThingStatus updated to OFFLINE. Reason: {}", channel.getBdaddr(),
                removedReason);

    }

    @Override
    public void onConnectionStatusChanged(ButtonConnectionChannel channel, ConnectionStatus connectionStatus,
            DisconnectReason disconnectReason) {
        logger.debug("New status for {}: {}", channel.getBdaddr(),
                connectionStatus + (connectionStatus == ConnectionStatus.Disconnected ? ", " + disconnectReason : ""));

        Optional<FlicButtonHandler> thingHandler = getFlicButtonHandler(channel.getBdaddr());

        if (thingHandler.isPresent()) {
            thingHandler.get().flicConnectionStatusChanged(connectionStatus, disconnectReason);
        } else if (connectionStatus != ConnectionStatus.Disconnected) {
            bridgeHandler.getButtonDiscoveryService().flicButtonDiscovered(channel.getBdaddr());
        }
    }

    @Override
    public void onButtonUpOrDown(ButtonConnectionChannel channel, ClickType clickType, boolean wasQueued, int timeDiff)
            throws IOException {

        logger.debug("{} {}", channel.getBdaddr(), clickType.name());
        FlicButtonHandler thingHandler = getFlicButtonHandler(channel.getBdaddr()).get();

        if (thingHandler != null) {
            switch (clickType) {
                case ButtonSingleClick:
                    thingHandler.flicButtonClickedSingle();
                    break;
                case ButtonDoubleClick:
                    thingHandler.flicButtonClickedDouble();
                    break;
                case ButtonHold:
                    thingHandler.flicButtonClickedHold();
                    break;
                case ButtonDown:
                    thingHandler.flicButtonDown();
                    break;
                case ButtonUp:
                    thingHandler.flicButtonUp();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onButtonSingleOrDoubleClickOrHold(ButtonConnectionChannel channel, ClickType clickType,
            boolean wasQueued, int timeDiff) throws IOException {
        // Handling does not differ from up/down events, so redirect
        onButtonUpOrDown(channel, clickType, wasQueued, timeDiff);
    }
}
