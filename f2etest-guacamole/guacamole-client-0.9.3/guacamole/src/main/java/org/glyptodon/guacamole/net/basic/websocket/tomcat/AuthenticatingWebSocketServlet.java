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

package org.glyptodon.guacamole.net.basic.websocket.tomcat;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.net.basic.AuthenticatingHttpServlet;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A WebSocket servlet wrapped around an AuthenticatingHttpServlet.
 *
 * @author Michael Jumper
 */
public abstract class AuthenticatingWebSocketServlet extends WebSocketServlet {

    /**
     * Logger for this class.
     */
    private Logger logger = LoggerFactory.getLogger(AuthenticatingWebSocketServlet.class);

    /**
     * Wrapped authenticating servlet.
     */
    private AuthenticatingHttpServlet auth_servlet =
            new AuthenticatingHttpServlet() {

        @Override
        protected void authenticatedService(UserContext context,
            HttpServletRequest request, HttpServletResponse response)
            throws GuacamoleException {

            try {
                // If authenticated, service request
                service_websocket_request(request, response);
            }
            catch (IOException e) {
                throw new GuacamoleServerException(
                        "Cannot service WebSocket request (I/O error).", e);
            }
            catch (ServletException e) {
                throw new GuacamoleServerException(
                        "Cannot service WebSocket request (internal error).", e);
            }

        }

    };

    @Override
    public void init() throws ServletException {
        auth_servlet.init();
    }

    @Override
    protected void service(HttpServletRequest request,
        HttpServletResponse response)
        throws IOException, ServletException {

        // Authenticate all inbound requests
        auth_servlet.service(request, response);

    }

    /**
     * Actually services the given request, bypassing the service() override
     * and the authentication scheme.
     *
     * @param request The HttpServletRequest to service.
     * @param response The associated HttpServletResponse.
     * @throws IOException If an I/O error occurs while handling the request.
     * @throws ServletException If an internal error occurs while handling the
     *                          request.
     */
    private void service_websocket_request(HttpServletRequest request,
        HttpServletResponse response)
        throws IOException, ServletException {

        // Bypass override and service WebSocket request
        super.service(request, response);

    }

    @Override
    protected String selectSubProtocol(List<String> subProtocols) {

        // Search for expected protocol
        for (String protocol : subProtocols)
            if ("guacamole".equals(protocol))
                return "guacamole";
        
        // Otherwise, fail
        return null;

    }

    @Override
    public StreamInbound createWebSocketInbound(String protocol,
        HttpServletRequest request) {

        // Get session and user context
        HttpSession session = request.getSession(true);
        UserContext context = AuthenticatingHttpServlet.getUserContext(session);

        // Ensure user logged in
        if (context == null) {
            logger.warn("User no longer logged in upon WebSocket connect.");
            return null;
        }

        // Connect WebSocket
        return authenticatedConnect(context, request, protocol);

    }

    /**
     * Function called after the credentials given in the request (if any)
     * are authenticated. If the current session is not associated with
     * valid credentials, this function will not be called.
     *
     * @param context The current UserContext.
     * @param request The HttpServletRequest being serviced.
     * @param protocol The protocol being used over the WebSocket connection.
     */
    protected abstract StreamInbound authenticatedConnect(
            UserContext context,
            HttpServletRequest request, String protocol);

}
