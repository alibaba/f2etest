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

/**
 * Describes an available legal value for an enumerated protocol parameter.
 *
 * @author Michael Jumper
 */
public class ProtocolParameterOption {

    /**
     * The value that will be sent to the client plugin if this option is
     * chosen.
     */
    private String value;

    /**
     * A human-readable title describing the effect of the value.
     */
    private String title;

    /**
     * Returns the value that will be sent to the client plugin if this option
     * is chosen.
     *
     * @return The value that will be sent if this option is chosen.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value that will be sent to the client plugin if this option is
     * chosen.
     *
     * @param value The value to send if this option is chosen.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the human-readable title describing the effect of this option.
     * @return The human-readable title describing the effect of this option.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the human-readable title describing the effect of this option.
     * @param title A human-readable title describing the effect of this option.
     */
    public void setTitle(String title) {
        this.title = title;
    }

}
