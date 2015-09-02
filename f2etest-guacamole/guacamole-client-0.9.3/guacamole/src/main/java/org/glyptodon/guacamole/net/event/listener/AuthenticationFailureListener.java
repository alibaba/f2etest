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
import org.glyptodon.guacamole.net.event.AuthenticationFailureEvent;

/**
 * A listener whose authenticationFailed() hook will fire immediately
 * after a user's authentication attempt fails. Note that this hook cannot
 * be used to cancel the authentication failure.
 *
 * @author Michael Jumper
 */
public interface AuthenticationFailureListener  {

    /**
     * Event hook which fires immediately after a user's authentication attempt
     * fails.
     *
     * @param e The AuthenticationFailureEvent describing the authentication
     *          failure that just occurred.
     * @throws GuacamoleException If an error occurs while handling the
     *                            authentication failure event. Note that
     *                            throwing an exception will NOT cause the
     *                            authentication failure to be canceled.
     */
    void authenticationFailed(AuthenticationFailureEvent e)
            throws GuacamoleException;

}
