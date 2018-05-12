/**
 * Copyright (c) 2016-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton.internal.discovery;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;

import io.flic.fliclib.javaclient.Bdaddr;
import io.flic.fliclib.javaclient.FlicClient;

/**
 *
 * @author Patrick Fink
 *
 */
public interface FlicButtonDiscoveryService extends DiscoveryService {

    /**
     *
     * @param bdaddr Bluetooth address of the discovered Flic button
     * @return UID that was created by the discovery service
     */
    public ThingUID flicButtonDiscovered(Bdaddr bdaddr);

    public void activate(@NonNull FlicClient client);

    public void deactivate();
}
