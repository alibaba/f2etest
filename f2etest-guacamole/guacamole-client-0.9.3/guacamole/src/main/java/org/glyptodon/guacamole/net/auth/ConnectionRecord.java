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

import java.util.Date;

/**
 * A logging record describing when a user started and ended usage of a
 * particular connection.
 *
 * @author Michael Jumper
 */
public interface ConnectionRecord {

    /**
     * Returns the date and time the connection began.
     *
     * @return The date and time the connection began.
     */
    public Date getStartDate();

    /**
     * Returns the date and time the connection ended, if applicable.
     *
     * @return The date and time the connection ended, or null if the
     *         connection is still running or if the end time is unknown.
     */
    public Date getEndDate();

    /**
     * Returns the name of the user who used or is using the connection at the
     * times given by this connection record.
     *
     * @return The name of the user who used or is using the associated
     *         connection.
     */
    public String getUsername();

    /**
     * Returns whether the connection associated with this record is still
     * active.
     *
     * @return true if the connection associated with this record is still
     *         active, false otherwise.
     */
    public boolean isActive();

}
