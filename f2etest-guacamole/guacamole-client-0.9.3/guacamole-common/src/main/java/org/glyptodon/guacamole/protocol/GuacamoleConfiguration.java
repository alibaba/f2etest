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

package org.glyptodon.guacamole.protocol;


import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * All information necessary to complete the initial protocol handshake of a
 * Guacamole session.
 *
 * @author Michael Jumper
 */
public class GuacamoleConfiguration implements Serializable {

    /**
     * Identifier unique to this version of GuacamoleConfiguration.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ID of the connection being joined. If this value is present,
     * the protocol need not be specified.
     */
    private String connectionID;
    
    /**
     * The name of the protocol associated with this configuration.
     */
    private String protocol;

    /**
     * Map of all associated parameter values, indexed by parameter name.
     */
    private Map<String, String> parameters = new HashMap<String, String>();

    /**
     * Returns the ID of the connection being joined, if any. If no connection
     * is being joined, this returns null, and the protocol must be set.
     *
     * @return The ID of the connection being joined, or null if no connection
     *         is being joined.
     */
    public String getConnectionID() {
        return connectionID;
    }

    /**
     * Sets the ID of the connection being joined, if any. If no connection
     * is being joined, this value must be omitted, and the protocol must be
     * set instead.
     *
     * @param connectionID The ID of the connection being joined.
     */
    public void setConnectionID(String connectionID) {
        this.connectionID = connectionID;
    }

    /**
     * Returns the name of the protocol to be used.
     * @return The name of the protocol to be used.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the name of the protocol to be used.
     * @param protocol The name of the protocol to be used.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Returns the value set for the parameter with the given name, if any.
     * @param name The name of the parameter to return the value for.
     * @return The value of the parameter with the given name, or null if
     *         that parameter has not been set.
     */
    public String getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * Sets the value for the parameter with the given name.
     *
     * @param name The name of the parameter to set the value for.
     * @param value The value to set for the parameter with the given name.
     */
    public void setParameter(String name, String value) {
        parameters.put(name, value);
    }

    /**
     * Removes the value set for the parameter with the given name.
     *
     * @param name The name of the parameter to remove the value of.
     */
    public void unsetParameter(String name) {
        parameters.remove(name);
    }

    /**
     * Returns a set of all currently defined parameter names. Each name
     * corresponds to a parameter that has a value set on this
     * GuacamoleConfiguration via setParameter().
     *
     * @return A set of all currently defined parameter names.
     */
    public Set<String> getParameterNames() {
        return Collections.unmodifiableSet(parameters.keySet());
    }

}
