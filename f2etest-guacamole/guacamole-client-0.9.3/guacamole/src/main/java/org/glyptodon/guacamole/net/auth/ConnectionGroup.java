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

package org.glyptodon.guacamole.net.auth;

import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.GuacamoleSocket;
import org.glyptodon.guacamole.protocol.GuacamoleClientInformation;

/**
 * Represents a connection group, which can contain both other connection groups
 * as well as connections.
 *
 * @author James Muehlner
 */
public interface ConnectionGroup {
    
    public enum Type {
        ORGANIZATIONAL, BALANCING
    };

    /**
     * Returns the name assigned to this ConnectionGroup.
     * @return The name assigned to this ConnectionGroup.
     */
    public String getName();

    /**
     * Sets the name assigned to this ConnectionGroup.
     *
     * @param name The name to assign.
     */
    public void setName(String name);

    /**
     * Returns the unique identifier assigned to this ConnectionGroup.
     * @return The unique identifier assigned to this ConnectionGroup.
     */
    public String getIdentifier();

    /**
     * Sets the identifier assigned to this ConnectionGroup.
     *
     * @param identifier The identifier to assign.
     */
    public void setIdentifier(String identifier);

    /**
     * Returns the unique identifier of the parent ConnectionGroup for
     * this ConnectionGroup.
     * 
     * @return The unique identifier of the parent ConnectionGroup for
     * this ConnectionGroup.
     */
    public String getParentIdentifier();

    /**
     * Sets the unique identifier of the parent ConnectionGroup for
     * this ConnectionGroup.
     * 
     * @param parentIdentifier The unique identifier of the parent 
     * ConnectionGroup for this ConnectionGroup.
     */
    public void setParentIdentifier(String parentIdentifier);
    
    /**
     * Set the type of this ConnectionGroup.
     *
     * @param type The type of this ConnectionGroup.
     */
    public void setType(Type type);
    
    /**
     * Returns the type of this connection.
     * @return the type of this connection.
     */
    public Type getType();

    /**
     * Retrieves a Directory which can be used to view and manipulate
     * connections and their configurations, but only as allowed by the
     * permissions given to the user.
     *
     * @return A Directory whose operations are bound by the permissions of 
     *         the user.
     *
     * @throws GuacamoleException If an error occurs while creating the
     *                            Directory.
     */
    Directory<String, Connection> getConnectionDirectory()
            throws GuacamoleException;

    /**
     * Retrieves a Directory which can be used to view and manipulate
     * connection groups and their members, but only as allowed by the
     * permissions given to the user.
     *
     * @return A Directory whose operations are bound by the permissions of
     *         the user.
     *
     * @throws GuacamoleException If an error occurs while creating the
     *                            Directory.
     */
    Directory<String, ConnectionGroup> getConnectionGroupDirectory()
            throws GuacamoleException;
    
    /**
     * Establishes a connection to guacd using a connection chosen from among
     * the connections in this ConnectionGroup, and returns the resulting, 
     * connected GuacamoleSocket.
     *
     * @param info Information associated with the connecting client.
     * @return A fully-established GuacamoleSocket.
     *
     * @throws GuacamoleException If an error occurs while connecting to guacd,
     *                            or if permission to connect is denied.
     */
    public GuacamoleSocket connect(GuacamoleClientInformation info)
            throws GuacamoleException;

}
