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

import java.util.List;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.GuacamoleSocket;
import org.glyptodon.guacamole.protocol.GuacamoleClientInformation;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;

/**
 * Represents a pairing of a GuacamoleConfiguration with a unique,
 * human-readable identifier, and abstracts the connection process. The
 * backing GuacamoleConfiguration may be intentionally obfuscated or tokenized
 * to protect sensitive configuration information.
 *
 * @author Michael Jumper
 */
public interface Connection {

    /**
     * Returns the name assigned to this Connection.
     * @return The name assigned to this Connection.
     */
    public String getName();

    /**
     * Sets the name assigned to this Connection.
     *
     * @param name The name to assign.
     */
    public void setName(String name);

    /**
     * Returns the unique identifier assigned to this Connection.
     * @return The unique identifier assigned to this Connection.
     */
    public String getIdentifier();

    /**
     * Sets the identifier assigned to this Connection.
     *
     * @param identifier The identifier to assign.
     */
    public void setIdentifier(String identifier);

    /**
     * Returns the unique identifier of the parent ConnectionGroup for
     * this Connection.
     * 
     * @return The unique identifier of the parent ConnectionGroup for
     * this Connection.
     */
    public String getParentIdentifier();

    /**
     * Sets the unique identifier of the parent ConnectionGroup for
     * this Connection.
     * 
     * @param parentIdentifier The unique identifier of the parent 
     * ConnectionGroup for this Connection.
     */
    public void setParentIdentifier(String parentIdentifier);

    /**
     * Returns the GuacamoleConfiguration associated with this Connection. Note
     * that because configurations may contain sensitive information, some data
     * in this configuration may be omitted or tokenized.
     *
     * @return The GuacamoleConfiguration associated with this Connection.
     */
    public GuacamoleConfiguration getConfiguration();

    /**
     * Sets the GuacamoleConfiguration associated with this Connection.
     *
     * @param config The GuacamoleConfiguration to associate with this
     *               Connection.
     */
    public void setConfiguration(GuacamoleConfiguration config);

    /**
     * Establishes a connection to guacd using the GuacamoleConfiguration
     * associated with this Connection, and returns the resulting, connected
     * GuacamoleSocket. The GuacamoleSocket will be pre-configured and will
     * already have passed the handshake stage.
     *
     * @param info Information associated with the connecting client.
     * @return A fully-established GuacamoleSocket.
     *
     * @throws GuacamoleException If an error occurs while connecting to guacd,
     *                            or if permission to connect is denied.
     */
    public GuacamoleSocket connect(GuacamoleClientInformation info)
            throws GuacamoleException;

    /**
     * Returns a list of ConnectionRecords representing the usage history
     * of this Connection, including any active users. ConnectionRecords
     * in this list will be sorted in descending order of end time (active
     * connections are first), and then in descending order of start time
     * (newer connections are first).
     *
     * @return A list of ConnectionRecrods representing the usage history
     *         of this Connection.
     *
     * @throws GuacamoleException If an error occurs while reading the history
     *                            of this connection, or if permission is
     *                            denied.
     */
    public List<? extends ConnectionRecord> getHistory() throws GuacamoleException;

}
