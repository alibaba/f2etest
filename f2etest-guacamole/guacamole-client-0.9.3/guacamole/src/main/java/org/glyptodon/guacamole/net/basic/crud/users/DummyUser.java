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

package org.glyptodon.guacamole.net.basic.crud.users;

import java.util.HashSet;
import java.util.Set;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.AbstractUser;
import org.glyptodon.guacamole.net.auth.permission.Permission;

/**
 * Basic User skeleton, providing a means of storing User data prior to CRUD
 * operations. This User does not promote any of the semantics that would
 * otherwise be present because of the authentication provider. It is up to the
 * authentication provider to create a new User based on the information
 * contained herein.
 *
 * @author Michael Jumper
 */
public class DummyUser extends AbstractUser {

    /**
     * Set of all available permissions.
     */
    private Set<Permission> permissions = new HashSet<Permission>();

    @Override
    public Set<Permission> getPermissions() throws GuacamoleException {
        return permissions;
    }

    @Override
    public boolean hasPermission(Permission permission) throws GuacamoleException {
        return permissions.contains(permission);
    }

    @Override
    public void addPermission(Permission permission) throws GuacamoleException {
        permissions.add(permission);
    }

    @Override
    public void removePermission(Permission permission) throws GuacamoleException {
        permissions.remove(permission);
    }

}
