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

import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;

/**
 * Basic implementation of a Guacamole connection.
 *
 * @author Michael Jumper
 */
public abstract class AbstractConnection implements Connection {

    /**
     * The name associated with this connection.
     */
    private String name;

    /**
     * The unique identifier associated with this connection.
     */
    private String identifier;

    /**
     * The unique identifier of the parent ConnectionGroup for
     * this Connection.
     */
    private String parentIdentifier;

    /**
     * The GuacamoleConfiguration associated with this connection.
     */
    private GuacamoleConfiguration configuration;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getParentIdentifier() {
        return parentIdentifier;
    }

    @Override
    public void setParentIdentifier(String parentIdentifier) {
        this.parentIdentifier = parentIdentifier;
    }

    @Override
    public GuacamoleConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(GuacamoleConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public int hashCode() {
        if (identifier == null) return 0;
        return identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        // Not equal if null or not a Connection
        if (obj == null) return false;
        if (!(obj instanceof AbstractConnection)) return false;

        // Get identifier
        String objIdentifier = ((AbstractConnection) obj).identifier;

        // If null, equal only if this identifier is null
        if (objIdentifier == null) return identifier == null;

        // Otherwise, equal only if strings are identical
        return objIdentifier.equals(identifier);

    }

}
