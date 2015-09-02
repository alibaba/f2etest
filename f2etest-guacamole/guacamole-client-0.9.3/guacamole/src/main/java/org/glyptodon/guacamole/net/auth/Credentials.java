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

package org.glyptodon.guacamole.net.auth;

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * Simple arbitrary set of credentials, including a username/password pair,
 * the HttpServletRequest associated with the request for authorization
 * (if any) and the HttpSession associated with that request.
 *
 * This class is used along with AuthenticationProvider to provide arbitrary
 * HTTP-based authentication for Guacamole.
 *
 * @author Michael Jumper
 */
public class Credentials implements Serializable {

    /**
     * Unique identifier associated with this specific version of Credentials.
     */
    private static final long serialVersionUID = 1L;

    /**
     * An arbitrary username.
     */
    private String username;

    /**
     * An arbitrary password.
     */
    private String password;

    /**
     * The HttpServletRequest carrying additional credentials, if any.
     */
    private transient HttpServletRequest request;

    /**
     * The HttpSession carrying additional credentials, if any.
     */
    private transient HttpSession session;

    /**
     * Returns the password associated with this set of credentials.
     * @return The password associated with this username/password pair, or
     *         null if no password has been set.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password associated with this set of credentials.
     * @param password The password to associate with this username/password
     *                 pair.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the username associated with this set of credentials.
     * @return The username associated with this username/password pair, or
     *         null if no username has been set.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username associated with this set of credentials.
     * @param username The username to associate with this username/password
     *                 pair.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the HttpServletRequest associated with this set of credentials.
     * @return The HttpServletRequest associated with this set of credentials,
     *         or null if no such request exists.
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Sets the HttpServletRequest associated with this set of credentials.
     * @param request  The HttpServletRequest to associated with this set of
     *                 credentials.
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Returns the HttpSession associated with this set of credentials.
     * @return The HttpSession associated with this set of credentials, or null
     *         if no such request exists.
     */
    public HttpSession getSession() {
        return session;
    }

    /**
     * Sets the HttpSession associated with this set of credentials.
     * @param session The HttpSession to associated with this set of
     *                credentials.
     */
    public void setSession(HttpSession session) {
        this.session = session;
    }

}
