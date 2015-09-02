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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleSecurityException;
import org.glyptodon.guacamole.net.auth.ConnectionGroup;
import org.glyptodon.guacamole.net.auth.Directory;

/**
 * An extremely simple read-only implementation of a Directory of
 * ConnectionGroup which provides which provides access to a pre-defined
 * Collection of ConnectionGroups.
 *
 * @author James Muehlner
 */
public class SimpleConnectionGroupDirectory
    implements Directory<String, ConnectionGroup> {

    /**
     * The Map of ConnectionGroups to provide access to.
     */
    private Map<String, ConnectionGroup> connectionGroups =
            new HashMap<String, ConnectionGroup>();

    /**
     * Creates a new SimpleConnectionGroupDirectory which contains the given
     * groups.
     * 
     * @param groups A Collection of all groups that should be present in this
     *               connection group directory.
     */
    public SimpleConnectionGroupDirectory(Collection<ConnectionGroup> groups) {

        // Add all given groups
        for (ConnectionGroup group : groups)
            connectionGroups.put(group.getIdentifier(), group);

    }

    @Override
    public ConnectionGroup get(String identifier)
            throws GuacamoleException {
        return connectionGroups.get(identifier);
    }

    @Override
    public Set<String> getIdentifiers() throws GuacamoleException {
        return connectionGroups.keySet();
    }

    @Override
    public void add(ConnectionGroup connectionGroup)
            throws GuacamoleException {
        throw new GuacamoleSecurityException("Permission denied.");
    }

    @Override
    public void update(ConnectionGroup connectionGroup)
            throws GuacamoleException {
        throw new GuacamoleSecurityException("Permission denied.");
    }

    @Override
    public void remove(String identifier) throws GuacamoleException {
        throw new GuacamoleSecurityException("Permission denied.");
    }

    @Override
    public void move(String identifier, Directory<String, ConnectionGroup> directory) 
            throws GuacamoleException {
        throw new GuacamoleSecurityException("Permission denied.");
    }

    /**
     * An internal method for modifying the ConnectionGroups in this Directory.
     * Returns the previous connection group for the given identifier, if found.
     * 
     * @param connectionGroup The connection group to add or update the
     *                        Directory with.
     * @return The previous connection group for the connection group
     *         identifier, if found.
     */
    public ConnectionGroup putConnectionGroup(ConnectionGroup connectionGroup) {
        return connectionGroups.put(connectionGroup.getIdentifier(), connectionGroup);
    }
    
    /**
     * An internal method for removing a ConnectionGroup from this Directory.
     * 
     * @param identifier The identifier of the ConnectionGroup to remove.
     * @return The previous connection group for the given identifier, if found.
     */
    public ConnectionGroup removeConnectionGroup(String identifier) {
        return connectionGroups.remove(identifier);
    }

}
