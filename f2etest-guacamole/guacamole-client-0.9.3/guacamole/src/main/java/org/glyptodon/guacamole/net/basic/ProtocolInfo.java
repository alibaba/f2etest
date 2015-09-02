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

package org.glyptodon.guacamole.net.basic;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Describes a protocol and all parameters associated with it, as required by
 * a protocol plugin for guacd. This class allows known parameters for a
 * protocol to be exposed to the user as friendly fields.
 *
 * @author Michael Jumper
 */
public class ProtocolInfo {

    /**
     * The human-readable title associated with this protocol.
     */
    private String title;

    /**
     * The unique name associated with this protocol.
     */
    private String name;

    /**
     * A collection of all associated protocol parameters.
     */
    private Collection<ProtocolParameter> parameters =
            new ArrayList<ProtocolParameter>();

    /**
     * Returns the human-readable title associated with this protocol.
     *
     * @return The human-readable title associated with this protocol.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the human-readable title associated with this protocol.
     *
     * @param title The human-readable title to associate with this protocol.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the unique name of this protocol. The protocol name is the
     * value required by the corresponding protocol plugin for guacd.
     *
     * @return The unique name of this protocol.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique name of this protocol. The protocol name is the value
     * required by the corresponding protocol plugin for guacd.
     *
     * @param name The unique name of this protocol.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a mutable collection of the protocol parameters associated with
     * this protocol. Changes to this collection affect the parameters exposed
     * to the user.
     *
     * @return A mutable collection of protocol parameters.
     */
    public Collection<ProtocolParameter> getParameters() {
        return parameters;
    }

}
