/**
 * Copyright (c) 2016-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton.internal.discovery;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.flicbutton.FlicButtonBindingConstants;
import org.openhab.binding.flicbutton.handler.FlicButtonEventListener;
import org.openhab.binding.flicbutton.handler.FlicDaemonBridgeHandler;
import org.openhab.binding.flicbutton.internal.util.FlicButtonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.Bdaddr;
import io.flic.fliclib.javaclient.FlicClient;
import io.flic.fliclib.javaclient.GeneralCallbacks;
import io.flic.fliclib.javaclient.GetInfoResponseCallback;
import io.flic.fliclib.javaclient.enums.BdAddrType;
import io.flic.fliclib.javaclient.enums.BluetoothControllerState;

/**
 * For each configured flicd service, there is a {@link FlicButtonDiscoveryServiceImpl} which will be initialized by
 * {@link FlicDaemonBridgeHandler}.
 *
 * This Discovery Service will be called by {@link FlicButtonEventListener}, if new buttons are appearing.
 * That's why it's called "Passive"DiscoveryService, it does not actively scan for new Flic Buttons and
 * do not support adding new ones on it's own. Currently, new buttons have to be added e.g. via simpleclient by Shortcut
 * Labs.
 *
 * @author Patrick Fink - Initial contribution
 */
public class FlicButtonDiscoveryServiceImpl extends AbstractDiscoveryService implements FlicButtonDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(FlicButtonDiscoveryServiceImpl.class);

    private boolean activated = false;
    private ThingUID bridgeUID;
    private FlicClient flicClient;

    public FlicButtonDiscoveryServiceImpl(@NonNull ThingUID bridgeUID) {
        super(FlicButtonBindingConstants.SUPPORTED_THING_TYPES_UIDS, 2, true);
        this.bridgeUID = bridgeUID;
    }

    @Override
    public void activate(@NonNull FlicClient flicClient) {
        this.flicClient = flicClient;
        activated = true;
        super.activate(null);
    }

    @Override
    public void deactivate() {
        activated = false;
        super.deactivate();
    }

    @Override
    protected void startScan() {
        try {
            if (activated) {
                discoverVerifiedButtons();
            }

        } catch (IOException e) {
            logger.warn("Error occured during button discovery: {}", e);
            scanListener.onErrorOccurred(e);
        }
    }

    protected void discoverVerifiedButtons() throws IOException {
        // Register FlicButtonEventListener to all already existing Flic buttons
        flicClient.getInfo(new GetInfoResponseCallback() {
            @Override
            public void onGetInfoResponse(BluetoothControllerState bluetoothControllerState, Bdaddr myBdAddr,
                    BdAddrType myBdAddrType, int maxPendingConnections, int maxConcurrentlyConnectedButtons,
                    int currentPendingConnections, boolean currentlyNoSpaceForNewConnection, Bdaddr[] verifiedButtons)
                    throws IOException {

                for (final Bdaddr bdaddr : verifiedButtons) {
                    flicButtonDiscovered(bdaddr);
                }
            }
        });
    }

    @Override
    protected void startBackgroundDiscovery() {
        super.startBackgroundDiscovery();
        flicClient.setGeneralCallbacks(new GeneralCallbacks() {
            @Override
            public void onNewVerifiedButton(Bdaddr bdaddr) throws IOException {
                logger.info("A new Flic button was added by an external flicd client: {}", bdaddr);
                flicButtonDiscovered(bdaddr);
            }
        });
    }

    @Override
    protected void stopBackgroundDiscovery() {
        super.stopBackgroundDiscovery();
        flicClient.setGeneralCallbacks(null);
    }

    @Override
    public ThingUID flicButtonDiscovered(Bdaddr bdaddr) {
        logger.info("Flic Button {} discovered!", bdaddr);
        ThingUID flicButtonUID = FlicButtonUtils.getThingUIDFromBdAddr(bdaddr, bridgeUID);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(flicButtonUID).withBridge(bridgeUID).build();
        this.thingDiscovered(discoveryResult);
        return flicButtonUID;
    }
}