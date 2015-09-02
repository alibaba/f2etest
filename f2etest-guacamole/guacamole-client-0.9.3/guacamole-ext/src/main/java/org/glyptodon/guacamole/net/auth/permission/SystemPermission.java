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
 * A permission which affects the system as a whole, rather than an individual
 * object.
 *
 * @author Michael Jumper
 */
public class SystemPermission implements Permission<SystemPermission.Type> {

    /**
     * Specific types of system-level permissions. Each permission type is
     * related to a specific class of system-level operation.
     */
    public enum Type {

        /**
         * Create users.
         */
        CREATE_USER,

        /**
         * Create connections.
         */
        CREATE_CONNECTION,

        /**
         * Create connection groups.
         */
        CREATE_CONNECTION_GROUP,

        /**
         * Administer the system in general, including adding permissions
         * which affect the system (like user creation, connection creation,
         * and system administration).
         */
        ADMINISTER

    }

    /**
     * The type of operation affected by this permission.
     */
    private Type type;

    /**
     * Creates a new SystemPermission with the given
     * type.
     *
     * @param type The type of operation controlled by this permission.
     */
    public SystemPermission(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        // Not equal if null or wrong type
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        final SystemPermission other = (SystemPermission) obj;

        // Compare types
        if (type != other.type)
            return false;

        return true;
    }

}
