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

package org.glyptodon.guacamole.net.auth.simple;

import java.util.Collections;
import java.util.Set;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleSecurityException;
import org.glyptodon.guacamole.net.auth.Directory;
import org.glyptodon.guacamole.net.auth.User;

/**
 * An extremely simple read-only implementation of a Directory of Users which
 * provides access to a single pre-defined User.
 *
 * @author Michael Jumper
 */
public class SimpleUserDirectory implements Directory<String, User> {

    /**
     * The only user to be contained within this directory.
     */
    private User user;

    /**
     * Creates a new SimpleUserDirectory which provides access to the single
     * user provided.
     *
     * @param user The user to provide access to.
     */
    public SimpleUserDirectory(User user) {
        this.user = user;
    }

    @Override
    public User get(String username) throws GuacamoleException {

        // If username matches, return the user
        if (user.getUsername().equals(username))
            return user;

        // Otherwise, not found
        return null;

    }

    @Override
    public Set<String> getIdentifiers() throws GuacamoleException {
        return Collections.singleton(user.getUsername());
    }

    @Override
    public void add(User user) throws GuacamoleException {
        throw new GuacamoleSecurityException("Permission denied.");
    }

    @Override
    public void update(User user) throws GuacamoleException {
        throw new GuacamoleSecurityException("Permission denied.");
    }

    @Override
    public void remove(String username) throws GuacamoleException {
        throw new GuacamoleSecurityException("Permission denied.");
    }

    @Override
    public void move(String identifier, Directory<String, User> directory) 
            throws GuacamoleException {
        throw new GuacamoleSecurityException("Permission denied.");
    }

}
