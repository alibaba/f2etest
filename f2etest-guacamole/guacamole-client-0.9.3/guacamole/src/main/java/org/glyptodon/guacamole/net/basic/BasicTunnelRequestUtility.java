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

import java.util.Arrays;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.glyptodon.guacamole.GuacamoleClientException;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleSecurityException;
import org.glyptodon.guacamole.io.GuacamoleReader;
import org.glyptodon.guacamole.net.GuacamoleSocket;
import org.glyptodon.guacamole.net.GuacamoleTunnel;
import org.glyptodon.guacamole.net.auth.Connection;
import org.glyptodon.guacamole.net.auth.ConnectionGroup;
import org.glyptodon.guacamole.net.auth.Credentials;
import org.glyptodon.guacamole.net.auth.Directory;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.net.basic.event.SessionListenerCollection;
import org.glyptodon.guacamole.net.event.TunnelCloseEvent;
import org.glyptodon.guacamole.net.event.TunnelConnectEvent;
import org.glyptodon.guacamole.net.event.listener.TunnelCloseListener;
import org.glyptodon.guacamole.net.event.listener.TunnelConnectListener;
import org.glyptodon.guacamole.properties.GuacamoleProperties;
import org.glyptodon.guacamole.protocol.GuacamoleClientInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that takes a standard request from the Guacamole JavaScript
 * client and produces the corresponding GuacamoleTunnel. The implementation
 * of this utility is specific to the form of request used by the upstream
 * Guacamole web application, and is not necessarily useful to applications
 * that use purely the Guacamole API.
 *
 * @author Michael Jumper
 */
public class BasicTunnelRequestUtility {

    /**
     * Logger for this class.
     */
    private static Logger logger = LoggerFactory.getLogger(BasicTunnelRequestUtility.class);

    /**
     * All supported identifier types.
     */
    private static enum IdentifierType {

        /**
         * The unique identifier of a connection.
         */
        CONNECTION("c/"),

        /**
         * The unique identifier of a connection group.
         */
        CONNECTION_GROUP("g/");
        
        /**
         * The prefix which precedes an identifier of this type.
         */
        final String PREFIX;
        
        /**
         * Defines an IdentifierType having the given prefix.
         * @param prefix The prefix which will precede any identifier of this
         *               type, thus differentiating it from other identifier
         *               types.
         */
        IdentifierType(String prefix) {
            PREFIX = prefix;
        }

        /**
         * Given an identifier, determines the corresponding identifier type.
         * 
         * @param identifier The identifier whose type should be identified.
         * @return The identified identifier type.
         */
        static IdentifierType getType(String identifier) {

            // If null, no known identifier
            if (identifier == null)
                return null;

            // Connection identifiers
            if (identifier.startsWith(CONNECTION.PREFIX))
                return CONNECTION;
            
            // Connection group identifiers
            if (identifier.startsWith(CONNECTION_GROUP.PREFIX))
                return CONNECTION_GROUP;
            
            // Otherwise, unknown
            return null;
            
        }
        
    };
    
    /**
     * Notifies all listeners in the given collection that a tunnel has been
     * connected.
     *
     * @param listeners A collection of all listeners that should be notified.
     * @param context The UserContext associated with the current session.
     * @param credentials The credentials associated with the current session.
     * @param tunnel The tunnel being connected.
     * @return true if all listeners are allowing the tunnel to connect,
     *         or if there are no listeners, and false if any listener is
     *         canceling the connection. Note that once one listener cancels,
     *         no other listeners will run.
     * @throws GuacamoleException If any listener throws an error while being
     *                            notified. Note that if any listener throws an
     *                            error, the connect is canceled, and no other
     *                            listeners will run.
     */
    private static boolean notifyConnect(Collection listeners, UserContext context,
            Credentials credentials, GuacamoleTunnel tunnel)
            throws GuacamoleException {

        // Build event for auth success
        TunnelConnectEvent event = new TunnelConnectEvent(context,
                credentials, tunnel);

        // Notify all listeners
        for (Object listener : listeners) {
            if (listener instanceof TunnelConnectListener) {

                // Cancel immediately if hook returns false
                if (!((TunnelConnectListener) listener).tunnelConnected(event))
                    return false;

            }
        }

        return true;

    }

    /**
     * Notifies all listeners in the given collection that a tunnel has been
     * closed.
     *
     * @param listeners A collection of all listeners that should be notified.
     * @param context The UserContext associated with the current session.
     * @param credentials The credentials associated with the current session.
     * @param tunnel The tunnel being closed.
     * @return true if all listeners are allowing the tunnel to close,
     *         or if there are no listeners, and false if any listener is
     *         canceling the close. Note that once one listener cancels,
     *         no other listeners will run.
     * @throws GuacamoleException If any listener throws an error while being
     *                            notified. Note that if any listener throws an
     *                            error, the close is canceled, and no other
     *                            listeners will run.
     */
    private static boolean notifyClose(Collection listeners, UserContext context,
            Credentials credentials, GuacamoleTunnel tunnel)
            throws GuacamoleException {

        // Build event for auth success
        TunnelCloseEvent event = new TunnelCloseEvent(context,
                credentials, tunnel);

        // Notify all listeners
        for (Object listener : listeners) {
            if (listener instanceof TunnelCloseListener) {

                // Cancel immediately if hook returns false
                if (!((TunnelCloseListener) listener).tunnelClosed(event))
                    return false;

            }
        }

        return true;

    }

    /**
     * Creates a new tunnel using the parameters and credentials present in
     * the given request.
     * 
     * @param request The HttpServletRequest describing the tunnel to create.
     * @return The created tunnel, or null if the tunnel could not be created.
     * @throws GuacamoleException If an error occurs while creating the tunnel.
     */
    public static GuacamoleTunnel createTunnel(HttpServletRequest request)
            throws GuacamoleException {

        HttpSession httpSession = request.getSession(true);

        // Get listeners
        final SessionListenerCollection listeners;
        try {
            listeners = new SessionListenerCollection(httpSession);
        }
        catch (GuacamoleException e) {
            logger.error("Failed to retrieve listeners. Authentication canceled.", e);
            throw e;
        }

        // Get ID of connection
        String id = request.getParameter("id");
        IdentifierType id_type = IdentifierType.getType(id);

        // Do not continue if unable to determine type
        if (id_type == null)
            throw new GuacamoleClientException("Illegal identifier - unknown type.");

        // Remove prefix
        id = id.substring(id_type.PREFIX.length());

        // Get credentials
        final Credentials credentials = AuthenticatingHttpServlet.getCredentials(httpSession);

        // Get context
        final UserContext context = AuthenticatingHttpServlet.getUserContext(httpSession);

        // If no context or no credentials, not logged in
        if (context == null || credentials == null)
            throw new GuacamoleSecurityException("Cannot connect - user not logged in.");

        // Get clipboard 
        final ClipboardState clipboard = AuthenticatingHttpServlet.getClipboardState(httpSession);

        // Get client information
        GuacamoleClientInformation info = new GuacamoleClientInformation();

        // Set width if provided
        String width  = request.getParameter("width");
        if (width != null)
            info.setOptimalScreenWidth(Integer.parseInt(width));

        // Set height if provided
        String height = request.getParameter("height");
        if (height != null)
            info.setOptimalScreenHeight(Integer.parseInt(height));

        // Set resolution if provided
        String dpi = request.getParameter("dpi");
        if (dpi != null)
            info.setOptimalResolution(Integer.parseInt(dpi));

        // Add audio mimetypes
        String[] audio_mimetypes = request.getParameterValues("audio");
        if (audio_mimetypes != null)
            info.getAudioMimetypes().addAll(Arrays.asList(audio_mimetypes));

        // Add video mimetypes
        String[] video_mimetypes = request.getParameterValues("video");
        if (video_mimetypes != null)
            info.getVideoMimetypes().addAll(Arrays.asList(video_mimetypes));

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String program = request.getParameter("program");
		
        // Create connected socket from identifier
        GuacamoleSocket socket;
        switch (id_type) {

            // Connection identifiers
            case CONNECTION: {

                // Get connection directory
                Directory<String, Connection> directory =
                    context.getRootConnectionGroup().getConnectionDirectory();

                // Get authorized connection
                Connection connection = directory.get(id);
                if (connection == null) {
                    logger.warn("Connection id={} not found.", id);
                    throw new GuacamoleSecurityException("Requested connection is not authorized.");
                }

                // Connect socket
                socket = connection.connect(info, username, password, program);
                logger.info("Successful connection from {} to \"{}\".", request.getRemoteAddr(), id);
                break;
            }

            // Connection group identifiers
            case CONNECTION_GROUP: {

                // Get connection group directory
                Directory<String, ConnectionGroup> directory =
                    context.getRootConnectionGroup().getConnectionGroupDirectory();

                // Get authorized connection group
                ConnectionGroup group = directory.get(id);
                if (group == null) {
                    logger.warn("Connection group id={} not found.", id);
                    throw new GuacamoleSecurityException("Requested connection group is not authorized.");
                }

                // Connect socket
                socket = group.connect(info);
                logger.info("Successful connection from {} to group \"{}\".", request.getRemoteAddr(), id);
                break;
            }

            // Fail if unsupported type
            default:
                throw new GuacamoleClientException("Connection not supported for provided identifier type.");

        }

        // Associate socket with tunnel
        GuacamoleTunnel tunnel = new GuacamoleTunnel(socket) {

            @Override
            public GuacamoleReader acquireReader() {

                // Monitor instructions which pertain to server-side events, if necessary
                try {
                    if (GuacamoleProperties.getProperty(CaptureClipboard.INTEGRATION_ENABLED, false))
                        return new MonitoringGuacamoleReader(clipboard, super.acquireReader());
                }
                catch (GuacamoleException e) {
                    logger.warn("Clipboard integration disabled due to error.", e);
                }

                // Pass through by default.
                return super.acquireReader();
                
            }

            @Override
            public void close() throws GuacamoleException {

                // Only close if not canceled
                if (!notifyClose(listeners, context, credentials, this))
                    throw new GuacamoleException("Tunnel close canceled by listener.");

                // Close if no exception due to listener
                super.close();

            }

        };

        // Notify listeners about connection
        if (!notifyConnect(listeners, context, credentials, tunnel)) {
            logger.info("Connection canceled by listener.");
            return null;
        }

        return tunnel;

    }

}

