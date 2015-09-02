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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleSecurityException;
import org.glyptodon.guacamole.net.auth.AbstractUser;
import org.glyptodon.guacamole.net.auth.ConnectionGroup;
import org.glyptodon.guacamole.net.auth.permission.ConnectionGroupPermission;
import org.glyptodon.guacamole.net.auth.permission.ConnectionPermission;
import org.glyptodon.guacamole.net.auth.permission.ObjectPermission;
import org.glyptodon.guacamole.net.auth.permission.Permission;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;

/**
 * An extremely basic User implementation.
 *
 * @author Michael Jumper
 */
public class SimpleUser extends AbstractUser {

    /**
     * The set of all permissions available to this user.
     */
    private Set<Permission> permissions = new HashSet<Permission>();

    /**
     * Creates a completely uninitialized SimpleUser.
     */
    public SimpleUser() {
    }

    /**
     * Creates a new SimpleUser having the given username.
     *
     * @param username The username to assign to this SimpleUser.
     * @param configs All configurations this user has read access to.
     * @param groups All groups this user has read access to.
     */
    public SimpleUser(String username,
            Map<String, GuacamoleConfiguration> configs,
            Collection<ConnectionGroup> groups) {

        // Set username
        setUsername(username);

        // Add connection permissions
        for (String identifier : configs.keySet()) {

            // Create permission
            Permission permission = new ConnectionPermission(
                ObjectPermission.Type.READ,
                identifier
            );

            // Add to set
            permissions.add(permission);

        }

        // Add group permissions
        for (ConnectionGroup group : groups) {

            // Create permission
            Permission permission = new ConnectionGroupPermission(
                ObjectPermission.Type.READ,
                group.getIdentifier()
            );

            // Add to set
            permissions.add(permission);

        }

    }

    @Override
    public Set<Permission> getPermissions() throws GuacamoleException {
        return permissions;
    }

    @Override
    public boolean hasPermission(Permission permission) throws GuacamoleException {
        return permissions.contains(permission);
    }

    @Override
    public void addPermission(Permission permission) throws GuacamoleException {
        throw new GuacamoleSecurityException("Permission denied.");
    }

    @Override
    public void removePermission(Permission permission) throws GuacamoleException {
        throw new GuacamoleSecurityException("Permission denied.");
    }

}
