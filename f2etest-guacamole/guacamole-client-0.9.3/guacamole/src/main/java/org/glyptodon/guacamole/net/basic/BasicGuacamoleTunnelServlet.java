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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.GuacamoleTunnel;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.servlet.GuacamoleHTTPTunnelServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connects users to a tunnel associated with the authorized connection
 * having the given ID.
 *
 * @author Michael Jumper
 */
public class BasicGuacamoleTunnelServlet extends AuthenticatingHttpServlet {

    /**
     * Logger for this class.
     */
    private static Logger logger = LoggerFactory.getLogger(BasicGuacamoleTunnelServlet.class);

    /**
     * All supported identifier types.
     */
    public static enum IdentifierType {

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
    
    @Override
    protected void authenticatedService(
            UserContext context,
            HttpServletRequest request, HttpServletResponse response)
    throws GuacamoleException {

        try {

            // If authenticated, respond as tunnel
            tunnelServlet.service(request, response);
        }

        catch (ServletException e) {
            logger.info("Error from tunnel (see previous log messages): {}",
                    e.getMessage());
        }

        catch (IOException e) {
            logger.info("I/O error from tunnel (see previous log messages): {}",
                    e.getMessage());
        }

    }

    /**
     * Wrapped GuacamoleHTTPTunnelServlet which will handle all authenticated
     * requests.
     */
    private GuacamoleHTTPTunnelServlet tunnelServlet = new GuacamoleHTTPTunnelServlet() {

        @Override
        protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException {
            return BasicTunnelRequestUtility.createTunnel(request);
        }

    };

    @Override
    protected boolean hasNewCredentials(HttpServletRequest request) {

        String query = request.getQueryString();
        if (query == null)
            return false;

        // Only connections are given new credentials
        return query.equals("connect");

    }

}

