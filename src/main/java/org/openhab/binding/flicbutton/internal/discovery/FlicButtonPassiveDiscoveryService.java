/**
 * Copyright (c) 2016-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton.internal.discovery;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.flicbutton.FlicButtonBindingConstants;
import org.openhab.binding.flicbutton.handler.FlicDaemonBridgeEventListener;
import org.openhab.binding.flicbutton.handler.FlicDaemonBridgeHandler;
import org.openhab.binding.flicbutton.internal.util.FlicButtonUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.Bdaddr;

/**
 * For each configured flicd service, there is a {@link FlicButtonPassiveDiscoveryService} which will be initialized by
 * {@link FlicDaemonBridgeHandler}.
 *
 * This Discovery Service will be called by {@link FlicDaemonBridgeEventListener}, if new buttons are appearing.
 * That's why it's called "Passive"DiscoveryService, it does not actively scan for new Flic Buttons and
 * do not support adding new ones on it's own. Currently, new buttons have to be added e.g. via simpleclient by Shortcut
 * Labs.
 *
 * @author Patrick Fink - Initial contribution
 */
public class FlicButtonPassiveDiscoveryService extends AbstractDiscoveryService implements FlicButtonDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(FlicButtonPassiveDiscoveryService.class);

    private ServiceRegistration<?> reg = null;
    private ThingUID bridgeUID;

    public FlicButtonPassiveDiscoveryService(ThingUID bridgeUID) {
        super(FlicButtonBindingConstants.SUPPORTED_THING_TYPES_UIDS, 2, true);
        this.bridgeUID = bridgeUID;
    }

    @Override
    public ThingUID flicButtonDiscovered(Bdaddr bdaddr) {
        logger.info("Flic Button {} discovered!", bdaddr);
        ThingUID flicButtonUID = FlicButtonUtils.getThingUIDFromBdAddr(bdaddr, bridgeUID);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(flicButtonUID).withBridge(bridgeUID).build();
        this.thingDiscovered(discoveryResult);
        return flicButtonUID;
    }

    @Override
    protected void startScan() {
        // DCUWCY - Don't call us, we call you
    }

    @Override
    public void start(BundleContext bundleContext) {
        if (reg != null) {
            return;
        }
        reg = bundleContext.registerService(DiscoveryService.class.getName(), this, new Hashtable<String, Object>());
    }

    @Override
    public void stop() {
        if (reg != null) {
            reg.unregister();
        }
        reg = null;
    }
}
