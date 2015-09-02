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

import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleSecurityException;
import org.glyptodon.guacamole.net.GuacamoleSocket;
import org.glyptodon.guacamole.net.auth.AbstractConnectionGroup;
import org.glyptodon.guacamole.net.auth.Connection;
import org.glyptodon.guacamole.net.auth.ConnectionGroup;
import org.glyptodon.guacamole.net.auth.Directory;
import org.glyptodon.guacamole.protocol.GuacamoleClientInformation;

/**
 * An extremely simple read-only implementation of a ConnectionGroup which
 * returns the connection and connection group directories it was constructed
 * with. Load balancing across this connection group is not allowed.
 * 
 * @author James Muehlner
 */
public class SimpleConnectionGroup extends AbstractConnectionGroup {

    /**
     * Underlying connection directory, containing all connections within this
     * group.
     */
    private final Directory<String, Connection> connectionDirectory;

    /**
     * Underlying connection group directory, containing all connections within
     * this group.
     */
    private final Directory<String, ConnectionGroup> connectionGroupDirectory;
    
    /**
     * Creates a new SimpleConnectionGroup having the given name and identifier
     * which will expose the given directories as its contents.
     * 
     * @param name The name to associate with this connection.
     * @param identifier The identifier to associate with this connection.
     * @param connectionDirectory The connection directory to expose when
     *                            requested.
     * @param connectionGroupDirectory The connection group directory to expose
     *                                 when requested.
     */
    public SimpleConnectionGroup(String name, String identifier,
            Directory<String, Connection> connectionDirectory, 
            Directory<String, ConnectionGroup> connectionGroupDirectory) {

        // Set name
        setName(name);

        // Set identifier
        setIdentifier(identifier);
        
        // Set group type
        setType(ConnectionGroup.Type.ORGANIZATIONAL);

        // Assign directories
        this.connectionDirectory = connectionDirectory;
        this.connectionGroupDirectory = connectionGroupDirectory;

    }
    
    @Override
    public Directory<String, Connection> getConnectionDirectory() 
            throws GuacamoleException {
        return connectionDirectory;
    }

    @Override
    public Directory<String, ConnectionGroup> getConnectionGroupDirectory() 
            throws GuacamoleException {
        return connectionGroupDirectory;
    }

    @Override
    public GuacamoleSocket connect(GuacamoleClientInformation info) 
            throws GuacamoleException {
        throw new GuacamoleSecurityException("Permission denied.");
    }

}
