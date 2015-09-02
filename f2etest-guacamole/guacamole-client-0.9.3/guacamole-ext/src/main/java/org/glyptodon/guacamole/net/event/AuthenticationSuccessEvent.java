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

package org.glyptodon.guacamole.net.event;

import org.glyptodon.guacamole.net.auth.Credentials;
import org.glyptodon.guacamole.net.auth.UserContext;

/**
 * An event which is triggered whenever a user's credentials pass
 * authentication. The credentials that passed authentication are included
 * within this event, and can be retrieved using getCredentials().
 * 
 * @author Michael Jumper
 */
public class AuthenticationSuccessEvent implements UserEvent, CredentialEvent {

    /**
     * The UserContext associated with the request that is connecting the
     * tunnel, if any.
     */
    private UserContext context;

    /**
     * The credentials which passed authentication.
     */
    private Credentials credentials;

    /**
     * Creates a new AuthenticationSuccessEvent which represents a successful
     * authentication attempt with the given credentials.
     *
     * @param context The UserContext created as a result of successful
     *                authentication.
     * @param credentials The credentials which passed authentication.
     */
    public AuthenticationSuccessEvent(UserContext context, Credentials credentials) {
        this.context = context;
        this.credentials = credentials;
    }

    @Override
    public UserContext getUserContext() {
        return context;
    }

    @Override
    public Credentials getCredentials() {
        return credentials;
    }

}
