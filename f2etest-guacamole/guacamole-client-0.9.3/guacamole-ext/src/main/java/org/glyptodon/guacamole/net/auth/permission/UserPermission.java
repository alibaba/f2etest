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

package org.glyptodon.guacamole.net.auth.permission;


/**
 * A permission which controls operations that directly affect a specific
 * User.
 *
 * @author Michael Jumper
 */
public class UserPermission implements ObjectPermission<String> {

    /**
     * The username of the User associated with the operation affected by this
     * permission.
     */
    private String identifier;

    /**
     * The type of operation affected by this permission.
     */
    private Type type;

    /**
     * Creates a new UserPermission having the given type and identifier. The
     * identifier must be the user's username.
     *
     * @param type The type of operation affected by this permission.
     * @param identifier The username of the User associated with the operation
     *                   affected by this permission.
     */
    public UserPermission(Type type, String identifier) {

        this.identifier = identifier;
        this.type = type;

    }

    @Override
    public String getObjectIdentifier() {
        return identifier;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        if (identifier != null) hash = 47 * hash + identifier.hashCode();
        if (type != null)       hash = 47 * hash + type.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {

        // Not equal if null or wrong type
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        final UserPermission other = (UserPermission) obj;

        // Not equal if different type
        if (this.type != other.type)
            return false;

        // If null identifier, equality depends on whether other identifier
        // is null
        if (identifier == null)
            return other.identifier == null;

        // Otherwise, equality depends entirely on identifier
        return identifier.equals(other.identifier);

    }

}
