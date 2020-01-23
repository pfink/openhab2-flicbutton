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
package org.openhab.binding.flicbutton.handler;

import io.flic.fliclib.javaclient.FlicClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.flicbutton.FlicButtonBindingConstants;
import org.openhab.binding.flicbutton.internal.discovery.FlicButtonDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The {@link FlicDaemonBridgeHandler} handles a running instance of the fliclib-linux-hci server (flicd).
 *
 * @author Patrick Fink - Initial contribution
 */
public class FlicDaemonBridgeHandler extends BaseBridgeHandler {
    private static final Logger logger = LoggerFactory.getLogger(FlicDaemonBridgeHandler.class);
    private static final long REINITIALIZE_DELAY_SECONDS = 10;
    // Config parameters
    private FlicDaemonBridgeConfiguration cfg;
    // Services
    private FlicButtonDiscoveryService buttonDiscoveryService;
    private Future<?> flicClientFuture;
    // For disposal
    private Collection<Future<?>> startedTasks = new ArrayList<>(2);
    private FlicClient flicClient;

    public FlicDaemonBridgeHandler(Bridge bridge, FlicButtonDiscoveryService buttonDiscoveryService) {
        super(bridge);
        this.buttonDiscoveryService = buttonDiscoveryService;
    }

    public FlicClient getFlicClient() {
        return flicClient;
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Fliclib bridge");

        try {
            initConfigParameters();
            startFlicdClientAsync();
            activateButtonDiscoveryService();
            initThingStatus();
        } catch (UnknownHostException ignored) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname wrong or unknown!");
        } catch(IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration (hostname, port) is invalid and cannot be parsed.");
        }
        catch (IOException e) {
            logger.warn("Error occured while connecting to flicd", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error connecting to flicd!");
        }
    }

    private void initConfigParameters() throws UnknownHostException {
        Object hostConfigRaw = thing.getConfiguration().get(FlicButtonBindingConstants.CONFIG_HOST_NAME);
        Object portConfigRaw = thing.getConfiguration().get(FlicButtonBindingConstants.CONFIG_PORT);
        cfg = new FlicDaemonBridgeConfiguration(hostConfigRaw, portConfigRaw);
    }

    private void activateButtonDiscoveryService() {
        buttonDiscoveryService.activate(flicClient);
    }

    private void startFlicdClientAsync() throws IOException {
        flicClient = new FlicClient(cfg.getHostname().getHostAddress(), cfg.getPort());
        Runnable flicClientService = () -> {
            try {
                flicClient.handleEvents();
                logger.info("Listening to flicd unexpectedly ended");
            }
            catch (Exception e) {
                logger.info("Error occured while listening to flicd", e);
            } finally {
                onClientFailure();
            }
        };

        flicClientFuture = scheduler.submit(flicClientService);
        startedTasks.add(flicClientFuture);
    }

    private void onClientFailure() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "flicd client terminated, probably flicd is not reachable.");
        dispose();
        scheduleReinitialize();
    }

    private void initThingStatus() {
        if (!flicClientFuture.isDone()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "flicd client could not be started, probably flicd is not reachable.");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Future<?> startedTask : startedTasks) {
            if (!startedTask.isDone()) {
                startedTask.cancel(true);
            }
        }
        startedTasks = new ArrayList<>(2);
        buttonDiscoveryService.deactivate();
    }

    private void scheduleReinitialize() {
        startedTasks.add(scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                initialize();
            }
        }, REINITIALIZE_DELAY_SECONDS, TimeUnit.SECONDS));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands to the fliclib-linux-hci are supported.
        // So there is nothing to handle in the bridge handler
    }
}
