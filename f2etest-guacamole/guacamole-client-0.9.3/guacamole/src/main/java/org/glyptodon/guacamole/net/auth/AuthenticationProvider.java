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

import org.glyptodon.guacamole.GuacamoleException;

/**
 * Provides means of accessing and managing the available
 * GuacamoleConfiguration objects and User objects. Access to each configuration
 * and each user is limited by a given Credentials object.
 *
 * @author Michael Jumper
 */
public interface AuthenticationProvider {

    /**
     * Returns the UserContext of the user authorized by the given credentials.
     *
     * @param credentials The credentials to use to retrieve the environment.
     * @return The UserContext of the user authorized by the given credentials,
     *         or null if the credentials are not authorized.
     *
     * @throws GuacamoleException If an error occurs while creating the
     *                            UserContext.
     */
    UserContext getUserContext(Credentials credentials)
            throws GuacamoleException;

    /**
     * Returns a new or updated UserContext for the user authorized by the
     * give credentials and having the given existing UserContext. Note that
     * because this function will be called for all future requests after
     * initial authentication, including tunnel requests, care must be taken
     * to avoid using functions of HttpServletRequest which invalidate the
     * entire request body, such as getParameter().
     * 
     * @param context The existing UserContext belonging to the user in
     *                question.
     * @param credentials The credentials to use to retrieve or update the
     *                    environment.
     * @return The updated UserContext, which need not be the same as the
     *         UserContext given, or null if the user is no longer authorized.
     *         
     * @throws GuacamoleException If an error occurs while updating the
     *                            UserContext.
     */
    UserContext updateUserContext(UserContext context, Credentials credentials)
            throws GuacamoleException;
    
}
