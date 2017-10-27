/**
 * Copyright (c) 2016-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.flicbutton.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author Patrick Fink
 *
 */
public class FlicDaemonBridgeConfiguration {

    private final InetAddress hostname;
    private final int port;

    FlicDaemonBridgeConfiguration(InetAddress hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    FlicDaemonBridgeConfiguration(Object rawHostname, Object rawPort) throws UnknownHostException {
        this.hostname = parseBridgeHostname(rawHostname);
        this.port = parseBridgePort(rawPort);
    }

    private InetAddress parseBridgeHostname(Object rawHostname) throws UnknownHostException {
        String host_config = ((rawHostname instanceof String) ? (String) rawHostname
                : (rawHostname instanceof InetAddress) ? ((InetAddress) rawHostname).getHostAddress() : null);

        return InetAddress.getByName(host_config);
    }

    private int parseBridgePort(Object rawPort) {
        return Integer.parseInt(rawPort.toString());
    }

    public InetAddress getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }
}
