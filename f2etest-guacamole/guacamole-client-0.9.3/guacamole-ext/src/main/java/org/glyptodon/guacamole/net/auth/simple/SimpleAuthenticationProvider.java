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

import java.util.Map;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.AuthenticationProvider;
import org.glyptodon.guacamole.net.auth.Credentials;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;

/**
 * Provides means of retrieving a set of named GuacamoleConfigurations for a
 * given Credentials object. This is a simple AuthenticationProvider
 * implementation intended to be easily extended. It is useful for simple
 * authentication situations where access to web-based administration and
 * complex users and permissions are not required.
 *
 * The interface provided by SimpleAuthenticationProvider is similar to that of
 * the AuthenticationProvider interface of older Guacamole releases.
 *
 * @author Michael Jumper
 */
public abstract class SimpleAuthenticationProvider
    implements AuthenticationProvider {

    /**
     * Given an arbitrary credentials object, returns a Map containing all
     * configurations authorized by those credentials. The keys of this Map
     * are Strings which uniquely identify each configuration.
     *
     * @param credentials The credentials to use to retrieve authorized
     *                    configurations.
     * @return A Map of all configurations authorized by the given credentials,
     *         or null if the credentials given are not authorized.
     * @throws GuacamoleException If an error occurs while retrieving
     *                            configurations.
     */
    public abstract Map<String, GuacamoleConfiguration>
            getAuthorizedConfigurations(Credentials credentials)
            throws GuacamoleException;

    @Override
    public UserContext getUserContext(Credentials credentials)
            throws GuacamoleException {

        // Get configurations
        Map<String, GuacamoleConfiguration> configs =
                getAuthorizedConfigurations(credentials);

        // Return as unauthorized if not authorized to retrieve configs
        if (configs == null)
            return null;

        // Return user context restricted to authorized configs
        return new SimpleUserContext(configs);

    }

    @Override
    public UserContext updateUserContext(UserContext context,
        Credentials credentials) throws GuacamoleException {

        // Simply return the given context, updating nothing
        return context;
        
    }

}
