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

package org.glyptodon.guacamole.servlet;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.http.HttpSession;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleSecurityException;
import org.glyptodon.guacamole.net.GuacamoleTunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides abstract access to the tunnels associated with a Guacamole session.
 *
 * @author Michael Jumper
 */
public class GuacamoleSession {

    /**
     * Logger for this class.
     */
    private Logger logger = LoggerFactory.getLogger(GuacamoleSession.class);

    /**
     * Map of all currently attached tunnels, indexed by tunnel UUID.
     */
    private ConcurrentMap<String, GuacamoleTunnel> tunnels;

    /**
     * Creates a new GuacamoleSession, storing and retrieving tunnels from the
     * given HttpSession. Note that the true Guacamole session is tied to the
     * HttpSession provided, thus creating a new GuacamoleSession does not
     * create a new Guacamole session; it merely creates a new object for
     * accessing the tunnels of an existing Guacamole session represented by
     * the provided HttpSession.
     *
     * @param session The HttpSession to use as tunnel storage.
     * @throws GuacamoleException If session is null.
     */
    @SuppressWarnings("unchecked")
    public GuacamoleSession(HttpSession session) throws GuacamoleException {

        if (session == null)
            throw new GuacamoleSecurityException("User has no session.");

        synchronized (session) {

            tunnels = (ConcurrentMap<String, GuacamoleTunnel>) session.getAttribute("GUAC_TUNNELS");
            if (tunnels == null) {
                tunnels = new ConcurrentHashMap<String, GuacamoleTunnel>();
                session.setAttribute("GUAC_TUNNELS", tunnels);
            }

        }

    }

    /**
     * Attaches the given tunnel to this GuacamoleSession.
     * @param tunnel The tunnel to attach to this GucacamoleSession.
     */
    public void attachTunnel(GuacamoleTunnel tunnel) {
        tunnels.put(tunnel.getUUID().toString(), tunnel);
        logger.debug("Attached tunnel {}.", tunnel.getUUID());
    }

    /**
     * Detaches the given tunnel to this GuacamoleSession.
     * @param tunnel The tunnel to detach to this GucacamoleSession.
     */
    public void detachTunnel(GuacamoleTunnel tunnel) {
        tunnels.remove(tunnel.getUUID().toString());
        logger.debug("Detached tunnel {}.", tunnel.getUUID());
    }

    /**
     * Returns the tunnel with the given UUID attached to this GuacamoleSession,
     * if any.
     *
     * @param tunnelUUID The UUID of an attached tunnel.
     * @return The tunnel corresponding to the given UUID, if attached, or null
     *         if no such tunnel is attached.
     */
    public GuacamoleTunnel getTunnel(String tunnelUUID) {
        return tunnels.get(tunnelUUID);
    }

}
