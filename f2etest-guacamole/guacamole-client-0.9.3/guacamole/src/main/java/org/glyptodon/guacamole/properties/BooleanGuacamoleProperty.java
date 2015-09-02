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
import org.glyptodon.guacamole.GuacamoleServerException;

/**
 * A GuacamoleProperty whose value is an boolean. Legal true values are "true",
 * or "false". Case does not matter.
 *
 * @author Michael Jumper
 */
public abstract class BooleanGuacamoleProperty implements GuacamoleProperty<Boolean> {

    @Override
    public Boolean parseValue(String value) throws GuacamoleException {

        // If no property provided, return null.
        if (value == null)
            return null;

        // If "true", return true
        if (value.equalsIgnoreCase("true"))
            return true;

        // If "false", return false
        if (value.equalsIgnoreCase("false"))
            return false;

        // Otherwise, fail
        throw new GuacamoleServerException("Property \"" + getName()
                + "\" must be either \"true\" or \"false\".");

    }

}
