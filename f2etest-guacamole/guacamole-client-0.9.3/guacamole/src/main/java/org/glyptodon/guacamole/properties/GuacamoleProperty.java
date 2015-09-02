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

package org.glyptodon.guacamole.properties;

import org.glyptodon.guacamole.GuacamoleException;

/**
 * An abstract representation of a property in the guacamole.properties file,
 * which parses into a specific type.
 *
 * @author Michael Jumper
 * @param <Type> The type this GuacamoleProperty will parse into.
 */
public interface GuacamoleProperty<Type> {

    /**
     * Returns the name of the property in guacamole.properties that this
     * GuacamoleProperty will parse.
     *
     * @return The name of the property in guacamole.properties that this
     *         GuacamoleProperty will parse.
     */
    public String getName();

    /**
     * Parses the given string value into the type associated with this
     * GuacamoleProperty.
     *
     * @param value The string value to parse.
     * @return The parsed value.
     * @throws GuacamoleException If an error occurs while parsing the
     *                            provided value.
     */
    public Type parseValue(String value) throws GuacamoleException;

}
