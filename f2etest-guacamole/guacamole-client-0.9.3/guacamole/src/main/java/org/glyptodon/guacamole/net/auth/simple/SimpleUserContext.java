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
import java.util.Map;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.ConnectionGroup;
import org.glyptodon.guacamole.net.auth.Directory;
import org.glyptodon.guacamole.net.auth.User;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;

/**
 * An extremely simple UserContext implementation which provides access to
 * a defined and restricted set of GuacamoleConfigurations. Access to
 * querying or modifying either users or permissions is denied.
 *
 * @author Michael Jumper
 */
public class SimpleUserContext implements UserContext {

    /**
     * Reference to the user whose permissions dictate the configurations
     * accessible within this UserContext.
     */
    private final User self;

    /**
     * The Directory with access only to the User associated with this
     * UserContext.
     */
    private final Directory<String, User> userDirectory;

    /**
     * The ConnectionGroup with access only to those Connections that the User
     * associated with this UserContext has access to.
     */
    private final ConnectionGroup connectionGroup;

    /**
     * Creates a new SimpleUserContext which provides access to only those
     * configurations within the given Map.
     * 
     * @param configs A Map of all configurations for which the user associated
     *                with this UserContext has read access.
     */
    public SimpleUserContext(Map<String, GuacamoleConfiguration> configs) {

        // Add root group that contains only configurations
        this.connectionGroup = new SimpleConnectionGroup("ROOT", "ROOT",
                new SimpleConnectionDirectory(configs),
                new SimpleConnectionGroupDirectory(Collections.EMPTY_LIST));

        // Build new user from credentials, giving the user an arbitrary name
        this.self = new SimpleUser("user",
                configs, Collections.singleton(connectionGroup));

        // Create user directory for new user
        this.userDirectory = new SimpleUserDirectory(self);
        
    }

    @Override
    public User self() {
        return self;
    }

    @Override
    public Directory<String, User> getUserDirectory()
            throws GuacamoleException {
        return userDirectory;
    }

    @Override
    public ConnectionGroup getRootConnectionGroup() throws GuacamoleException {
        return connectionGroup;
    }

}
