/**
 * Copyright (c) 2016-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton.internal.discovery;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.framework.BundleContext;

import io.flic.fliclib.javaclient.Bdaddr;

/**
 *
 * @author Patrick Fink
 *
 */
public interface FlicButtonDiscoveryService {

    public void start(BundleContext bundleContext);

    public void stop();

    /**
     *
     * @param bdaddr Bluetooth address of the discovered Flic button
     * @return UID that was created by the discovery service
     */
    public ThingUID flicButtonDiscovered(Bdaddr bdaddr);
}
