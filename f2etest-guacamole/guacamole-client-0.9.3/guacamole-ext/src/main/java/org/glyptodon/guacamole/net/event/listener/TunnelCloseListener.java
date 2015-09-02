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

package org.glyptodon.guacamole.net.event.listener;

import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.event.TunnelCloseEvent;

/**
 * A listener whose tunnelClosed() hook will fire immediately after an
 * existing tunnel is closed.
 *
 * @author Michael Jumper
 */
public interface TunnelCloseListener {

    /**
     * Event hook which fires immediately after an existing tunnel is closed.
     * The return value of this hook dictates whether the tunnel is allowed to
     * be closed.
     *
     * @param e The TunnelCloseEvent describing the tunnel being closed and
     *          any associated credentials.
     * @return true if the tunnel should be allowed to be closed, or false
     *         if the attempt should be denied, causing the attempt to
     *         effectively fail.
     * @throws GuacamoleException If an error occurs while handling the
     *                            tunnel close event. Throwing an exception
     *                            will also stop the tunnel from being closed.
     */
    boolean tunnelClosed(TunnelCloseEvent e)
            throws GuacamoleException;

}
