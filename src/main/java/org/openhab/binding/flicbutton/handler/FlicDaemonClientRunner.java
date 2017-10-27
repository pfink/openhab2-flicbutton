/**
 * Copyright (c) 2016-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton.handler;

import java.io.IOException;
import java.net.InetAddress;

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
 *
 * @author Patrick Fink
 *
 */
public class FlicDaemonClientRunner implements Runnable {
    private Logger logger = LoggerFactory.getLogger(FlicDaemonClientRunner.class);

    private final InetAddress flicDaemonHostname;
    private final int flicDaemonPort;
    private final ButtonConnectionChannel.Callbacks eventListener;

    FlicDaemonClientRunner(ButtonConnectionChannel.Callbacks eventListener, InetAddress flicDaemonHostname,
            int flicDaemonPort) {
        this.flicDaemonHostname = flicDaemonHostname;
        this.flicDaemonPort = flicDaemonPort;
        this.eventListener = eventListener;
    }

    @Override
    public void run() {
        try {

            FlicClient client = new FlicClient(flicDaemonHostname.getHostAddress(), flicDaemonPort);
            registerFlicClientEventListener(client);
            client.handleEvents();

        } catch (IOException e) {
            logger.error("Error occured while listening to flicd: {}", e);
        }
    }

    private void registerFlicClientEventListener(final FlicClient client) throws IOException {

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
                logger.info("A new Flic button was added by an external flicd client: {}. Now connecting to it...",
                        bdaddr);
                client.addConnectionChannel(new ButtonConnectionChannel(bdaddr, eventListener));
            }
        });
    }

}
