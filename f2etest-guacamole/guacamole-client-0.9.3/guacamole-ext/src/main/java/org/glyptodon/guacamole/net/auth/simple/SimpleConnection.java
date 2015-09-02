/*
 * Copyright (C) 2013 Glyptodon LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.glyptodon.guacamole.net.auth.simple;

import java.util.Collections;
import java.util.List;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.GuacamoleSocket;
import org.glyptodon.guacamole.net.InetGuacamoleSocket;
import org.glyptodon.guacamole.net.SSLGuacamoleSocket;
import org.glyptodon.guacamole.net.auth.AbstractConnection;
import org.glyptodon.guacamole.net.auth.ConnectionRecord;
import org.glyptodon.guacamole.properties.GuacamoleProperties;
import org.glyptodon.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.glyptodon.guacamole.protocol.GuacamoleClientInformation;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;

/**
 * An extremely basic Connection implementation.
 *
 * @author Michael Jumper
 */
public class SimpleConnection extends AbstractConnection {

    /**
     * Backing configuration, containing all sensitive information.
     */
    private GuacamoleConfiguration config;

    /**
     * Creates a completely uninitialized SimpleConnection.
     */
    public SimpleConnection() {
    }

    /**
     * Creates a new SimpleConnection having the given identifier and
     * GuacamoleConfiguration.
     *
     * @param name The name to associate with this connection.
     * @param identifier The identifier to associate with this connection.
     * @param config The configuration describing how to connect to this
     *               connection.
     */
    public SimpleConnection(String name, String identifier,
            GuacamoleConfiguration config) {
        
        // Set name
        setName(name);

        // Set identifier
        setIdentifier(identifier);

        // Set config
        setConfiguration(config);
        this.config = config;

    }

    @Override
    public GuacamoleSocket connect(GuacamoleClientInformation info)
            throws GuacamoleException {

        // Get guacd connection parameters
        String hostname = GuacamoleProperties.getProperty(GuacamoleProperties.GUACD_HOSTNAME);
        int port = GuacamoleProperties.getProperty(GuacamoleProperties.GUACD_PORT);

        // If guacd requires SSL, use it
        if (GuacamoleProperties.getProperty(GuacamoleProperties.GUACD_SSL, false))
            return new ConfiguredGuacamoleSocket(
                new SSLGuacamoleSocket(hostname, port),
                config, info
            );

        // Return connected socket
        return new ConfiguredGuacamoleSocket(
            new InetGuacamoleSocket(hostname, port),
            config, info
        );

    }

    @Override
    public List<ConnectionRecord> getHistory() throws GuacamoleException {
        return Collections.EMPTY_LIST;
    }

}
