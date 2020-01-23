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
package org.openhab.binding.flicbutton.internal;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.flicbutton.FlicButtonBindingConstants;
import org.openhab.binding.flicbutton.handler.FlicButtonHandler;
import org.openhab.binding.flicbutton.handler.FlicDaemonBridgeHandler;
import org.openhab.binding.flicbutton.internal.discovery.FlicButtonDiscoveryService;
import org.openhab.binding.flicbutton.internal.discovery.FlicSimpleclientDiscoveryServiceImpl;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@link FlicButtonHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Patrick Fink - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.flicbutton")
public class FlicButtonHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.concat(
            FlicButtonBindingConstants.BRIDGE_THING_TYPES_UIDS.stream(), FlicButtonBindingConstants.SUPPORTED_THING_TYPES_UIDS.stream()).collect(Collectors.toSet());
    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(FlicButtonBindingConstants.FLICBUTTON_THING_TYPE)) {
            return new FlicButtonHandler(thing);
        } else if (thingTypeUID.equals(FlicButtonBindingConstants.BRIDGE_THING_TYPE)) {
            FlicButtonDiscoveryService discoveryService = new FlicSimpleclientDiscoveryServiceImpl(thing.getUID());
            FlicDaemonBridgeHandler bridgeHandler = new FlicDaemonBridgeHandler((Bridge) thing, discoveryService);
            registerDiscoveryService(discoveryService, thing.getUID());

            return bridgeHandler;
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof FlicDaemonBridgeHandler) {
            unregisterDiscoveryService(thingHandler.getThing().getUID());
        }
        super.removeHandler(thingHandler);
    }

    private synchronized void registerDiscoveryService(FlicButtonDiscoveryService discoveryService,
                                                       ThingUID bridgeUID) {
        this.discoveryServiceRegs.put(bridgeUID, getBundleContext().registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<String, Object>()));
    }

    private synchronized void unregisterDiscoveryService(ThingUID bridgeUID) {
        ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(bridgeUID);
        if (serviceReg != null) {
            FlicButtonDiscoveryService service = (FlicButtonDiscoveryService) getBundleContext()
                    .getService(serviceReg.getReference());
            if (service != null) {
                service.deactivate();
            }
            serviceReg.unregister();
            discoveryServiceRegs.remove(bridgeUID);
        }
    }
}