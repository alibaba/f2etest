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

package org.glyptodon.guacamole.net.basic.crud.connections;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.Connection;
import org.glyptodon.guacamole.net.auth.Directory;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.net.basic.AuthenticatingHttpServlet;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;

/**
 * Simple HttpServlet which handles connection update.
 *
 * @author Michael Jumper
 */
public class Update extends AuthenticatingHttpServlet {

    /**
     * Prefix given to a parameter name when that parameter is a protocol-
     * specific parameter meant for the configuration.
     */
    public static final String PARAMETER_PREFIX = "_";

    @Override
    protected void authenticatedService(
            UserContext context,
            HttpServletRequest request, HttpServletResponse response)
    throws GuacamoleException {

        // Get ID, name, and protocol
        String identifier = request.getParameter("id");
        String name       = request.getParameter("name");
        String protocol   = request.getParameter("protocol");

        // Attempt to get connection directory
        Directory<String, Connection> directory =
                context.getRootConnectionGroup().getConnectionDirectory();

        // Create config
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol(protocol);

        // Load parameters into config
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {

            // If parameter starts with prefix, load corresponding parameter
            // value into config
            String param = params.nextElement();
            if (param.startsWith(PARAMETER_PREFIX))
                config.setParameter(
                    param.substring(PARAMETER_PREFIX.length()),
                    request.getParameter(param));

        }

        // Create connection skeleton
        Connection connection = directory.get(identifier);
        connection.setName(name);
        connection.setConfiguration(config);

        // Update connection
        directory.update(connection);

    }

}

