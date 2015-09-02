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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import org.glyptodon.guacamole.GuacamoleClientException;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleUnauthorizedException;
import org.glyptodon.guacamole.net.auth.AuthenticationProvider;
import org.glyptodon.guacamole.net.auth.Credentials;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.net.basic.event.SessionListenerCollection;
import org.glyptodon.guacamole.net.basic.properties.BasicGuacamoleProperties;
import org.glyptodon.guacamole.net.event.AuthenticationFailureEvent;
import org.glyptodon.guacamole.net.event.AuthenticationSuccessEvent;
import org.glyptodon.guacamole.net.event.listener.AuthenticationFailureListener;
import org.glyptodon.guacamole.net.event.listener.AuthenticationSuccessListener;
import org.glyptodon.guacamole.properties.GuacamoleProperties;
import org.glyptodon.guacamole.protocol.GuacamoleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract servlet which provides an authenticatedService() function that
 * is only called if the HTTP request is authenticated, or the current
 * HTTP session has already been authenticated.
 *
 * The user context is retrieved using the authentication provider defined in
 * guacamole.properties. The authentication provider has access to the request
 * and session, in addition to any submitted username and password, in order
 * to authenticate the user.
 *
 * The user context will be stored in the current HttpSession.
 *
 * Success and failure are logged.
 *
 * @author Michael Jumper
 */
public abstract class AuthenticatingHttpServlet extends HttpServlet {

    /**
     * Logger for this class.
     */
    private Logger logger = LoggerFactory.getLogger(AuthenticatingHttpServlet.class);

    /**
     * The session attribute holding the current UserContext.
     */
    public static final String CONTEXT_ATTRIBUTE = "GUAC_CONTEXT";

    /**
     * The session attribute holding the credentials authorizing this session.
     */
    public static final String CREDENTIALS_ATTRIBUTE = "GUAC_CREDS";

    /**
     * The session attribute holding the session-scoped clipboard storage.
     */
    public static final String CLIPBOARD_ATTRIBUTE = "GUAC_CLIP";
    
    /**
     * The AuthenticationProvider to use to authenticate all requests.
     */
    private AuthenticationProvider authProvider;

    /**
     * Whether HTTP authentication should be used (the "Authorization" header).
     */
    private boolean useHttpAuthentication;

    @Override
    public void init() throws ServletException {

        // Parse Guacamole configuration
        try {

            // Get auth provider instance
            authProvider = GuacamoleProperties.getRequiredProperty(BasicGuacamoleProperties.AUTH_PROVIDER);

            // Enable HTTP auth, if requested
            useHttpAuthentication = GuacamoleProperties.getProperty(BasicGuacamoleProperties.ENABLE_HTTP_AUTH, false);

        }
        catch (GuacamoleException e) {
            logger.error("Error reading Guacamole configuration.", e);
            throw new ServletException(e);
        }

    }

    /**
     * Notifies all listeners in the given collection that authentication has
     * failed.
     *
     * @param listeners A collection of all listeners that should be notified.
     * @param credentials The credentials associated with the authentication
     *                    request that failed.
     */
    private void notifyFailed(Collection listeners, Credentials credentials) {

        // Build event for auth failure
        AuthenticationFailureEvent event = new AuthenticationFailureEvent(credentials);

        // Notify all listeners
        for (Object listener : listeners) {
            try {
                if (listener instanceof AuthenticationFailureListener)
                    ((AuthenticationFailureListener) listener).authenticationFailed(event);
            }
            catch (GuacamoleException e) {
                logger.error("Error notifying AuthenticationFailureListener.", e);
            }
        }

    }

    /**
     * Notifies all listeners in the given collection that authentication was
     * successful.
     *
     * @param listeners A collection of all listeners that should be notified.
     * @param context The UserContext created as a result of authentication
     *                success.
     * @param credentials The credentials associated with the authentication
     *                    request that succeeded.
     * @return true if all listeners are allowing the authentication success,
     *         or if there are no listeners, and false if any listener is
     *         canceling the authentication success. Note that once one
     *         listener cancels, no other listeners will run.
     * @throws GuacamoleException If any listener throws an error while being
     *                            notified. Note that if any listener throws an
     *                            error, the success is canceled, and no other
     *                            listeners will run.
     */
    private boolean notifySuccess(Collection listeners, UserContext context,
            Credentials credentials) throws GuacamoleException {

        // Build event for auth success
        AuthenticationSuccessEvent event =
                new AuthenticationSuccessEvent(context, credentials);

        // Notify all listeners
        for (Object listener : listeners) {
            if (listener instanceof AuthenticationSuccessListener) {

                // Cancel immediately if hook returns false
                if (!((AuthenticationSuccessListener) listener).authenticationSucceeded(event))
                    return false;

            }
        }

        return true;

    }

    /**
     * Sends an error on the given HTTP response using the information within
     * the given GuacamoleStatus.
     *
     * @param response The HTTP response to use to send the error.
     * @param guac_status The status to send
     * @param message A human-readable message that can be presented to the
     *                user.
     * @throws ServletException If an error prevents sending of the error
     *                          code.
     */
    public static void sendError(HttpServletResponse response,
            GuacamoleStatus guac_status, String message)
            throws ServletException {

        try {

            // If response not committed, send error code and message
            if (!response.isCommitted()) {
                response.addHeader("Guacamole-Status-Code", Integer.toString(guac_status.getGuacamoleStatusCode()));
                response.addHeader("Guacamole-Error-Message", message);
                response.sendError(guac_status.getHttpStatusCode());
            }

        }
        catch (IOException ioe) {

            // If unable to send error at all due to I/O problems,
            // rethrow as servlet exception
            throw new ServletException(ioe);

        }

    }

    /**
     * Returns the credentials associated with the given session.
     *
     * @param session The session to retrieve credentials from.
     * @return The credentials associated with the given session.
     */
    public static Credentials getCredentials(HttpSession session) {
        return (Credentials) session.getAttribute(CREDENTIALS_ATTRIBUTE);
    }

    /**
     * Returns the UserContext associated with the given session.
     *
     * @param session The session to retrieve UserContext from.
     * @return The UserContext associated with the given session.
     */
    public static UserContext getUserContext(HttpSession session) {
        return (UserContext) session.getAttribute(CONTEXT_ATTRIBUTE);
    }

    /**
     * Returns the ClipboardState associated with the given session. If none
     * exists yet, one is created.
     *
     * @param session The session to retrieve the ClipboardState from.
     * @return The ClipboardState associated with the given session.
     */
    public static ClipboardState getClipboardState(HttpSession session) {

        ClipboardState clipboard = (ClipboardState) session.getAttribute(CLIPBOARD_ATTRIBUTE);
        if (clipboard == null) {
            clipboard = new ClipboardState();
            session.setAttribute(CLIPBOARD_ATTRIBUTE, clipboard);
        }

        return clipboard;

    }

    /**
     * Returns whether the request given has updated credentials. If this
     * function returns false, the UserContext will not be updated.
     * 
     * @param request The request to check for credentials.
     * @return true if the request contains credentials, false otherwise.
     */
    protected boolean hasNewCredentials(HttpServletRequest request) {
        return true;
    }
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        
        // Set character encoding to UTF-8 if it's not already set
        if(request.getCharacterEncoding() == null) {
            try {
                request.setCharacterEncoding("UTF-8");
            } catch (UnsupportedEncodingException exception) {
               throw new ServletException(exception);
            }
        }

        try {

            // Obtain context from session
            HttpSession httpSession = request.getSession(true);
            UserContext context = getUserContext(httpSession);

            // If new credentials present, update/create context
            if (hasNewCredentials(request)) {

                // Retrieve username and password from parms
                String username = request.getParameter("username");
                String password = request.getParameter("password");

                // If no username/password given, try Authorization header
                if (useHttpAuthentication && username == null && password == null) {

                    String authorization = request.getHeader("Authorization");
                    if (authorization != null && authorization.startsWith("Basic ")) {

                        // Decode base64 authorization
                        String basicBase64 = authorization.substring(6);
                        String basicCredentials = new String(DatatypeConverter.parseBase64Binary(basicBase64), "UTF-8");

                        // Pull username/password from auth data
                        int colon = basicCredentials.indexOf(':');
                        if (colon != -1) {
                            username = basicCredentials.substring(0, colon);
                            password = basicCredentials.substring(colon+1);
                        }

                        else
                            logger.warn("Invalid HTTP Basic \"Authorization\" header received.");

                    }

                } // end Authorization header fallback
                
                // Build credentials object
                Credentials credentials = new Credentials();
                credentials.setSession(httpSession);
                credentials.setRequest(request);
                credentials.setUsername(username);
                credentials.setPassword(password);

                SessionListenerCollection listeners = new SessionListenerCollection(httpSession);

                // If no cached context, attempt to get new context
                if (context == null) {

                    context = authProvider.getUserContext(credentials);

                    // Log successful authentication
                    if (context != null)
                        logger.info("User \"{}\" successfully authenticated from {}.",
                                context.self().getUsername(), request.getRemoteAddr());
                    
                }

                // Otherwise, update existing context
                else
                    context = authProvider.updateUserContext(context, credentials);

                // If auth failed, notify listeners
                if (context == null) {
                    logger.warn("Authentication attempt from {} for user \"{}\" failed.",
                            request.getRemoteAddr(), credentials.getUsername());

                    notifyFailed(listeners, credentials);
                }

                // If auth succeeded, notify and check with listeners
                else if (!notifySuccess(listeners, context, credentials)) {
                    logger.info("Successful authentication canceled by hook.");
                    context = null;
                }

                // If auth still OK, associate context with session
                else {
                    httpSession.setAttribute(CONTEXT_ATTRIBUTE,     context);
                    httpSession.setAttribute(CREDENTIALS_ATTRIBUTE, credentials);
                }

            } // end if credentials present

            // If no context, no authorizaton present
            if (context == null)
                throw new GuacamoleUnauthorizedException("Not authenticated");

            // Allow servlet to run now that authentication has been validated
            authenticatedService(context, request, response);

        }

        // Catch any thrown guacamole exception and attempt to pass within the
        // HTTP response, logging each error appropriately.
        catch (GuacamoleClientException e) {
            logger.warn("Client request rejected: {}", e.getMessage());
            sendError(response, e.getStatus(), e.getMessage());
        }
        catch (GuacamoleException e) {
            logger.error("Internal server error.", e);
            sendError(response, e.getStatus(), "Internal server error.");
        }

    }

    /**
     * Function called after the credentials given in the request (if any)
     * are authenticated. If the current session is not associated with
     * valid credentials, this function will not be called.
     *
     * @param context The current UserContext.
     * @param request The HttpServletRequest being serviced.
     * @param response An HttpServletResponse which controls the HTTP response
     *                 of this servlet.
     *
     * @throws GuacamoleException If an error occurs that interferes with the
     *                            normal operation of this servlet.
     */
    protected abstract void authenticatedService(
            UserContext context,
            HttpServletRequest request, HttpServletResponse response)
            throws GuacamoleException;

}
