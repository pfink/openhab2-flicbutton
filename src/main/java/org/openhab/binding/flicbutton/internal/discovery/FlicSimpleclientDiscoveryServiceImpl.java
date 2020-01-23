/**
 * Copyright (c) 2016 - 2020 Patrick Fink
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 3
 * with the GNU Classpath Exception 2.0 which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-3.0 WITH Classpath-exception-2.0
 */
package org.openhab.binding.flicbutton.internal.discovery;

import io.flic.fliclib.javaclient.Bdaddr;
import io.flic.fliclib.javaclient.FlicClient;
import io.flic.fliclib.javaclient.GeneralCallbacks;
import io.flic.fliclib.javaclient.GetInfoResponseCallback;
import io.flic.fliclib.javaclient.enums.BdAddrType;
import io.flic.fliclib.javaclient.enums.BluetoothControllerState;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.flicbutton.FlicButtonBindingConstants;
import org.openhab.binding.flicbutton.internal.FlicButtonHandlerFactory;
import org.openhab.binding.flicbutton.internal.util.FlicButtonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * For each configured flicd service, there is a {@link FlicSimpleclientDiscoveryServiceImpl} which will be initialized by
 * {@link FlicButtonHandlerFactory}.
 *
 * It can scan for Flic Buttons already that are already added to fliclib-linux-hci ("verified" buttons), *
 * but it does not support adding and verify new buttons on it's own.
 * New buttons have to be added (verified) e.g. via simpleclient by Shortcut Labs.
 * Background discovery listens for new buttons that are getting verified.
 *
 * @author Patrick Fink - Initial contribution
 */
public class FlicSimpleclientDiscoveryServiceImpl extends AbstractDiscoveryService implements FlicButtonDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(FlicSimpleclientDiscoveryServiceImpl.class);

    private boolean activated = false;
    private ThingUID bridgeUID;
    private FlicClient flicClient;

    public FlicSimpleclientDiscoveryServiceImpl(@NonNull ThingUID bridgeUID) {
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
            logger.warn("Error occured during button discovery", e);
            if (this.scanListener != null) {
                scanListener.onErrorOccurred(e);
            }
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

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(flicButtonUID).withBridge(bridgeUID).withLabel("Flic Button " + bdaddr.toString().replace(":", "")).build();
        this.thingDiscovered(discoveryResult);
        return flicButtonUID;
    }
}
