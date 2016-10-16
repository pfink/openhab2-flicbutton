/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton.handler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.flicbutton.FlicButtonBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.Bdaddr;
import io.flic.fliclib.javaclient.ButtonConnectionChannel;
import io.flic.fliclib.javaclient.FlicClient;
import io.flic.fliclib.javaclient.GeneralCallbacks;
import io.flic.fliclib.javaclient.GetInfoResponseCallback;
import io.flic.fliclib.javaclient.enums.BdAddrType;
import io.flic.fliclib.javaclient.enums.BluetoothControllerState;

/**
 * The {@link FlicLibBridgeHandler} handles a running instance of the fliclib-linux-hci server.
 *
 * @author Patrick Fink - Initial contribution
 */
public class FlicLibBridgeHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(FlicLibBridgeHandler.class);
    private FlicClient fliclibClient;

    public FlicLibBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Currently, no commands to the fliclib-linux-hci are supported.
        // So there is nothing to handle in the bridge handler
    }

    @Override
    public void thingUpdated(Thing thing) {
        // TODO: Handle config update
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Fliclib bridge");
        try {

            String bridgeHostname = getAndCheckBridgeHostname();
            int bridgePort = Integer
                    .parseInt(thing.getConfiguration().get(FlicButtonBindingConstants.CONFIG_PORT).toString());

            fliclibClient = new FlicClient(bridgeHostname, bridgePort);
            registerButtonEventListener(fliclibClient);

        } catch (UnknownHostException ignored) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname wrong or unknown!");
            return;
        } catch (IOException ioError) {
            logger.error(ioError.toString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Error while trying to connect to Fliclib!");
            return;
        }
    }

    private String getAndCheckBridgeHostname() throws UnknownHostException {
        Object host_config_obj = thing.getConfiguration().get(FlicButtonBindingConstants.CONFIG_HOST_NAME);
        String host_config = ((host_config_obj instanceof String) ? (String) host_config_obj
                : (host_config_obj instanceof InetAddress) ? ((InetAddress) host_config_obj).getHostAddress() : null);

        // This will throw an exception if something is wrong with the hostname
        InetAddress.getByName(host_config);

        return host_config;
    }

    private void registerButtonEventListener(FlicClient client) throws IOException {
        FlicLibEventListener eventListener = new FlicLibEventListener(this);

        // Register FlicButtonEventListener to all already existing Flic buttons
        client.getInfo(new GetInfoResponseCallback() {
            @Override
            public void onGetInfoResponse(BluetoothControllerState bluetoothControllerState, Bdaddr myBdAddr,
                    BdAddrType myBdAddrType, int maxPendingConnections, int maxConcurrentlyConnectedButtons,
                    int currentPendingConnections, boolean currentlyNoSpaceForNewConnection, Bdaddr[] verifiedButtons)
                    throws IOException {

                for (final Bdaddr bdaddr : verifiedButtons) {
                    client.addConnectionChannel(new ButtonConnectionChannel(bdaddr, eventListener));
                }
            }
        });

        // Register FlicButtonEventListener also to incoming buttons in the future
        client.setGeneralCallbacks(new GeneralCallbacks() {
            @Override
            public void onNewVerifiedButton(Bdaddr bdaddr) throws IOException {
                logger.info("A new Flic button was added by an external fliclib client: " + bdaddr
                        + ". Now connecting to it...");
                client.addConnectionChannel(new ButtonConnectionChannel(bdaddr, eventListener));
            }
        });
        client.handleEvents();

    }
}
