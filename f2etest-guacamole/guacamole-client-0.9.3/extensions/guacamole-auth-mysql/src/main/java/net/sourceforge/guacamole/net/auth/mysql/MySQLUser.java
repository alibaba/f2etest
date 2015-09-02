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

package net.sourceforge.guacamole.net.auth.mysql;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.AbstractUser;
import org.glyptodon.guacamole.net.auth.User;
import org.glyptodon.guacamole.net.auth.permission.Permission;

/**
 * A MySQL based implementation of the User object.
 * @author James Muehlner
 */
public class MySQLUser extends AbstractUser {

    /**
     * The ID of this user in the database, if any.
     */
    private Integer userID;

    /**
     * The set of current permissions a user has.
     */
    private Set<Permission> permissions = new HashSet<Permission>();

    /**
     * Any newly added permissions that have yet to be committed.
     */
    private Set<Permission> newPermissions = new HashSet<Permission>();

    /**
     * Any newly deleted permissions that have yet to be deleted.
     */
    private Set<Permission> removedPermissions = new HashSet<Permission>();

    /**
     * Creates a new, empty MySQLUser.
     */
    public MySQLUser() {
    }

    /**
     * Initializes a new MySQLUser having the given username.
     *
     * @param name The name to assign to this MySQLUser.
     */
    public void init(String name) {
        init(null, name, null, Collections.EMPTY_SET);
    }

    /**
     * Initializes a new MySQLUser, copying all data from the given user
     * object.
     *
     * @param user The user object to copy.
     * @throws GuacamoleException If an error occurs while reading the user
     *                            data in the given object.
     */
    public void init(User user) throws GuacamoleException {
        init(null, user.getUsername(), user.getPassword(), user.getPermissions());
    }

    /**
     * Initializes a new MySQLUser initialized from the given data from the
     * database.
     *
     * @param userID The ID of the user in the database, if any.
     * @param username The username of this user.
     * @param password The password to assign to this user.
     * @param permissions The permissions to assign to this user, as
     *                    retrieved from the database.
     */
    public void init(Integer userID, String username, String password,
            Set<Permission> permissions) {
        this.userID = userID;
        setUsername(username);
        setPassword(password);
        this.permissions.addAll(permissions);
    }

    /**
     * Get the current set of permissions this user has.
     * @return the current set of permissions.
     */
    public Set<Permission> getCurrentPermissions() {
        return permissions;
    }

    /**
     * Get any new permissions that have yet to be inserted.
     * @return the new set of permissions.
     */
    public Set<Permission> getNewPermissions() {
        return newPermissions;
    }

    /**
     * Get any permissions that have not yet been deleted.
     * @return the permissions that need to be deleted.
     */
    public Set<Permission> getRemovedPermissions() {
        return removedPermissions;
    }

    /**
     * Reset the new and removed permission sets after they are
     * no longer needed.
     */
    public void resetPermissions() {
        newPermissions.clear();
        removedPermissions.clear();
    }

    /**
     * Returns the ID of this user in the database, if it exists.
     *
     * @return The ID of this user in the database, or null if this user
     *         was not retrieved from the database.
     */
    public Integer getUserID() {
        return userID;
    }

    /**
     * Sets the ID of this user to the given value.
     *
     * @param userID The ID to assign to this user.
     */
    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    @Override
    public Set<Permission> getPermissions() throws GuacamoleException {
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public boolean hasPermission(Permission permission) throws GuacamoleException {
        return permissions.contains(permission);
    }

    @Override
    public void addPermission(Permission permission) throws GuacamoleException {
        permissions.add(permission);
        newPermissions.add(permission);
        removedPermissions.remove(permission);
    }

    @Override
    public void removePermission(Permission permission) throws GuacamoleException {
        permissions.remove(permission);
        newPermissions.remove(permission);
        removedPermissions.add(permission);
    }

}
