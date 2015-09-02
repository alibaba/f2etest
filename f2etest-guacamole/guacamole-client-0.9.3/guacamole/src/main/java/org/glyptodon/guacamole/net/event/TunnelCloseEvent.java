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

import org.glyptodon.guacamole.net.GuacamoleTunnel;
import org.glyptodon.guacamole.net.auth.Credentials;
import org.glyptodon.guacamole.net.auth.UserContext;

/**
 * An event which is triggered whenever a tunnel is being closed. The tunnel
 * being closed can be accessed through getTunnel(), and the UserContext
 * associated with the request which is closing the tunnel can be retrieved
 * with getUserContext().
 *
 * @author Michael Jumper
 */
public class TunnelCloseEvent implements UserEvent, CredentialEvent, TunnelEvent {

    /**
     * The UserContext associated with the request that is closing the
     * tunnel, if any.
     */
    private UserContext context;

    /**
     * The credentials associated with the request that connected the
     * tunnel, if any.
     */
    private Credentials credentials;

    /**
     * The tunnel being closed.
     */
    private GuacamoleTunnel tunnel;

    /**
     * Creates a new TunnelCloseEvent which represents the closing of the
     * given tunnel via a request associated with the given credentials.
     *
     * @param context The UserContext associated with the request closing 
     *                the tunnel.
     * @param credentials The credentials associated with the request that 
     *                    connected the tunnel.
     * @param tunnel The tunnel being closed.
     */
    public TunnelCloseEvent(UserContext context, Credentials credentials,
            GuacamoleTunnel tunnel) {
        this.context = context;
        this.credentials = credentials;
        this.tunnel = tunnel;
    }

    @Override
    public UserContext getUserContext() {
        return context;
    }

    @Override
    public Credentials getCredentials() {
        return credentials;
    }

    @Override
    public GuacamoleTunnel getTunnel() {
        return tunnel;
    }

}
