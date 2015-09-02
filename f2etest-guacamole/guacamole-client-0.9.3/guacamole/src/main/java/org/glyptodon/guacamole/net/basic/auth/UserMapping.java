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

package org.glyptodon.guacamole.net.basic.auth;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapping of all usernames to corresponding authorizations.
 *
 * @author Mike Jumper
 */
public class UserMapping {

    /**
     * All authorizations, indexed by username.
     */
    private Map<String, Authorization> authorizations =
            new HashMap<String, Authorization>();

    /**
     * Adds the given authorization to the user mapping.
     *
     * @param authorization The authorization to add to the user mapping.
     */
    public void addAuthorization(Authorization authorization) {
        authorizations.put(authorization.getUsername(), authorization);
    }

    /**
     * Returns the authorization corresponding to the user having the given
     * username, if any.
     *
     * @param username The username to find the authorization for.
     * @return The authorization corresponding to the user having the given
     *         username, or null if no such authorization exists.
     */
    public Authorization getAuthorization(String username) {
        return authorizations.get(username);
    }

}
