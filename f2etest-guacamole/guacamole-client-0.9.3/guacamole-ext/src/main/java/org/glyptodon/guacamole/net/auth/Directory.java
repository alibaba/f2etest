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

import java.util.Set;
import org.glyptodon.guacamole.GuacamoleException;

/**
 * Provides access to a collection of all objects with associated identifiers,
 * and allows user manipulation and removal. Objects stored within a
 * Directory are not necessarily returned to the use as references to
 * the stored objects, thus updating an object requires calling an update
 * function.
 *
 * @author Michael Jumper
 * @param <IdentifierType> The type of identifier used to identify objects
 *                         stored within this Directory.
 * @param <ObjectType> The type of objects stored within this Directory.
 */
public interface Directory<IdentifierType, ObjectType> {

    /**
     * Returns the object having the given identifier. Note that changes to
     * the object returned will not necessarily affect the object stored within
     * the Directory. To update an object stored within an
     * Directory such that future calls to get() will return the updated
     * object, you must call update() on the object after modification.
     *
     * @param identifier The identifier to use when locating the object to
     *                   return.
     * @return The object having the given identifier, or null if no such object
     *         exists.
     *
     * @throws GuacamoleException If an error occurs while retrieving the
     *                            object, or if permission for retrieving the
     *                            object is denied.
     */
    ObjectType get(IdentifierType identifier) throws GuacamoleException;

    /**
     * Returns a Set containing all identifiers for all objects within this
     * Directory.
     *
     * @return A Set of all identifiers.
     * @throws GuacamoleException If an error occurs while retrieving
     *                            the identifiers.
     */
    Set<IdentifierType> getIdentifiers() throws GuacamoleException;

    /**
     * Adds the given object to the overall set.
     *
     * @param object The object to add.
     *
     * @throws GuacamoleException If an error occurs while adding the object , or
     *                            if adding the object is not allowed.
     */
    void add(ObjectType object)
            throws GuacamoleException;

    /**
     * Updates the stored object with the data contained in the given object.
     *
     * @param object The object which will supply the data for the update.
     *
     * @throws GuacamoleException If an error occurs while updating the object,
     *                            or if updating the object is not allowed.
     */
    void update(ObjectType object)
            throws GuacamoleException;

    /**
     * Removes the object with the given identifier from the overall set.
     *
     * @param identifier The identifier of the object to remove.
     *
     * @throws GuacamoleException If an error occurs while removing the object,
     *                            or if removing object is not allowed.
     */
    void remove(IdentifierType identifier) throws GuacamoleException;

    /**
     * Moves the object with the given identifier to the given directory.
     *
     * @param identifier The identifier of the object to remove.
     * @param directory The directory to move the object to.
     *
     * @throws GuacamoleException If an error occurs while moving the object,
     *                            or if moving object is not allowed.
     */
    void move(IdentifierType identifier, Directory<IdentifierType, ObjectType> directory) 
            throws GuacamoleException;

}
