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
import org.glyptodon.guacamole.net.event.TunnelConnectEvent;

/**
 * A listener whose tunnelConnected() hook will fire immediately after a new
 * tunnel is connected.
 *
 * @author Michael Jumper
 */
public interface TunnelConnectListener {

   /**
     * Event hook which fires immediately after a new tunnel is connected.
     * The return value of this hook dictates whether the tunnel is made visible
     * to the session.
     *
     * @param e The TunnelConnectEvent describing the tunnel being connected and
     *          any associated credentials.
     * @return true if the tunnel should be allowed to be connected, or false
     *         if the attempt should be denied, causing the attempt to
     *         effectively fail.
     * @throws GuacamoleException If an error occurs while handling the
     *                            tunnel connect event. Throwing an exception
     *                            will also stop the tunnel from being made
     *                            visible to the session.
     */
    boolean tunnelConnected(TunnelConnectEvent e)
            throws GuacamoleException;

}
