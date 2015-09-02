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
 * Represents a parameter of a protocol.
 *
 * @author Michael Jumper
 */
public class ProtocolParameter {

    /**
     * All possible types of protocol parameter.
     */
    public enum Type {

        /**
         * A text parameter, accepting arbitrary values.
         */
        TEXT,

        /**
         * A password parameter, whose value is sensitive and must be hidden.
         */
        PASSWORD,

        /**
         * A numeric parameter, whose value must contain only digits.
         */
        NUMERIC,

        /**
         * A boolean parameter, whose value is either blank or "true".
         */
        BOOLEAN,

        /**
         * An enumerated parameter, whose legal values are fully enumerated
         * by a provided, finite list.
         */
        ENUM,

        /**
         * A text parameter that can span more than one line.
         */
        MULTILINE

    }

    /**
     * The unique name that identifies this parameter to the protocol plugin.
     */
    private String name;

    /**
     * A human-readable name to be presented to the user.
     */
    private String title;

    /**
     * The type of this field.
     */
    private Type type;

    /**
     * The value of this parameter, for boolean parameters.
     */
    private String value;

    /**
     * A collection of all associated parameter options.
     */
    private Collection<ProtocolParameterOption> options =
            new ArrayList<ProtocolParameterOption>();

    /**
     * Returns the name associated with this protocol parameter.
     * @return The name associated with this protocol parameter.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name associated with this protocol parameter. This name must
     * uniquely identify this parameter among the others accepted by the
     * corresponding protocol.
     *
     * @param name The name to assign to this protocol parameter.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the title associated with this protocol parameter.
     * @return The title associated with this protocol parameter.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title associated with this protocol parameter. The title must
     * be a human-readable string which describes accurately this parameter.
     *
     * @param title A human-readable string describing this parameter.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the value associated with this protocol parameter.
     * @return The value associated with this protocol parameter.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value associated with this protocol parameter. The value must
     * be a human-readable string which describes accurately this parameter.
     *
     * @param value A human-readable string describing this parameter.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the type of this parameter.
     * @return The type of this parameter.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of this parameter.
     * @param type The type of this parameter.
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Returns a mutable collection of protocol parameter options. Changes to
     * this collection directly affect the available options.
     *
     * @return A mutable collection of parameter options.
     */
    public Collection<ProtocolParameterOption> getOptions() {
        return options;
    }

}
