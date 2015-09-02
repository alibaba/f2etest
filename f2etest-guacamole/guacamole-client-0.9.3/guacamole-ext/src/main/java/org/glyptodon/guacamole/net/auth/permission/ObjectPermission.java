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
 * A permission which affects a specific object, rather than the system as a
 * whole.
 *
 * @author Michael Jumper
 * @param <T> The type of identifier used by the object this permission affects.
 */
public interface ObjectPermission<T> extends Permission<ObjectPermission.Type> {

    /**
     * Specific types of object-level permissions. Each permission type is
     * related to a specific class of object-level operation.
     */
    public enum Type {

        /**
         * Read data within an object.
         */
        READ,

        /**
         * Update data within an object.
         */
        UPDATE,

        /**
         * Delete an object.
         */
        DELETE,

        /**
         * Change who has access to an object.
         */
        ADMINISTER

    }

    /**
     * Returns the identifier of the specific object affected by this
     * permission.
     *
     * @return The identifier of the specific object affected by this
     *         permission.
     */
    public T getObjectIdentifier();

}
