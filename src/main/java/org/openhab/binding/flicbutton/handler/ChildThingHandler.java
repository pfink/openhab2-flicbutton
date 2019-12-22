package org.openhab.binding.flicbutton.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class ChildThingHandler<BridgeHandlerType extends BridgeHandler> extends BaseThingHandler {
    private static final Collection<ThingStatus> defaultToleratedBridgeStatuses = Collections.singleton(ThingStatus.ONLINE);
    protected boolean bridgeValid = false;
    protected BridgeHandlerType bridgeHandler;

    public ChildThingHandler(Thing thing) {
        super(thing);
    }

    public void initialize() {
        setStatusBasedOnBridge();
        if(bridgeValid) {
            linkBridge();
        }
    }

    protected void linkBridge() {
        try {
            BridgeHandler bridgeHandlerUncasted = getBridge().getHandler();
            bridgeHandler = (BridgeHandlerType) bridgeHandlerUncasted;
        } catch(ClassCastException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge Type is invalid.");
        }
    }

    protected void setStatusBasedOnBridge() {
        setStatusBasedOnBridge(defaultToleratedBridgeStatuses);
    }

    protected void setStatusBasedOnBridge(Collection<ThingStatus> toleratedBridgeStatuses) {
        if(getBridge() != null) {
            if(!toleratedBridgeStatuses.contains(getBridge().getStatus())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge in unsupported status: " + getBridge().getStatus());
                return;
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Bridge missing.");
            return;
        }

        bridgeValid = true;
    }
}
