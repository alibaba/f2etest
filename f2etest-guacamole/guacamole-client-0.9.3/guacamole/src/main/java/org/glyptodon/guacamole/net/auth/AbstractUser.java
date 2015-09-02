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


/**
 * Basic implementation of a Guacamole user which uses the username to
 * determine equality. Username comparison is case-sensitive.
 *
 * @author Michael Jumper
 */
public abstract class AbstractUser implements User {

    /**
     * The name of this user.
     */
    private String username;

    /**
     * This user's password. Note that while this provides a means for the
     * password to be set, the data stored in this String is not necessarily
     * the user's actual password. It may be hashed, it may be arbitrary.
     */
    private String password;

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int hashCode() {
        if (username == null) return 0;
        return username.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        // Not equal if null or not a User
        if (obj == null) return false;
        if (!(obj instanceof AbstractUser)) return false;

        // Get username
        String objUsername = ((AbstractUser) obj).username;

        // If null, equal only if this username is null
        if (objUsername == null) return username == null;

        // Otherwise, equal only if strings are identical
        return objUsername.equals(username);

    }

}
