package org.openhab.binding.flicbutton.internal.discovery;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.framework.BundleContext;

import io.flic.fliclib.javaclient.Bdaddr;

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
