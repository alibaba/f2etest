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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.glyptodon.guacamole.GuacamoleClientException;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.Directory;
import org.glyptodon.guacamole.net.auth.User;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.net.auth.permission.ConnectionGroupPermission;
import org.glyptodon.guacamole.net.auth.permission.ConnectionPermission;
import org.glyptodon.guacamole.net.auth.permission.ObjectPermission;
import org.glyptodon.guacamole.net.auth.permission.Permission;
import org.glyptodon.guacamole.net.auth.permission.SystemPermission;
import org.glyptodon.guacamole.net.auth.permission.UserPermission;
import org.glyptodon.guacamole.net.basic.AuthenticatingHttpServlet;

/**
 * Simple HttpServlet which handles user update.
 *
 * @author Michael Jumper
 */
public class Update extends AuthenticatingHttpServlet {

    /**
     * String given for user creation permission.
     */
    private static final String CREATE_USER_PERMISSION = "create-user";

    /**
     * String given for connection creation permission.
     */
    private static final String CREATE_CONNECTION_PERMISSION = "create-connection";

    /**
     * String given for connection group creation permission.
     */
    private static final String CREATE_CONNECTION_GROUP_PERMISSION = "create-connection-group";

    /**
     * String given for system administration permission.
     */
    private static final String ADMIN_PERMISSION = "admin";

    /**
     * Prefix given before an object identifier for read permission.
     */
    private static final String READ_PREFIX   = "read:";

    /**
     * Prefix given before an object identifier for delete permission.
     */
    private static final String DELETE_PREFIX = "delete:";

    /**
     * Prefix given before an object identifier for update permission.
     */
    private static final String UPDATE_PREFIX = "update:";

    /**
     * Prefix given before an object identifier for administration permission.
     */
    private static final String ADMIN_PREFIX  = "admin:";

    /**
     * Given a permission string, returns the corresponding system permission.
     *
     * @param str The permission string to parse.
     * @return The parsed system permission.
     * @throws GuacamoleException If the given string could not be parsed.
     */
    private Permission parseSystemPermission(String str)
            throws GuacamoleException {

        // Create user
        if (str.equals(CREATE_USER_PERMISSION))
            return new SystemPermission(SystemPermission.Type.CREATE_USER);

        // Create connection
        if (str.equals(CREATE_CONNECTION_PERMISSION))
            return new SystemPermission(SystemPermission.Type.CREATE_CONNECTION);

        // Create connection group
        if (str.equals(CREATE_CONNECTION_GROUP_PERMISSION))
            return new SystemPermission(SystemPermission.Type.CREATE_CONNECTION_GROUP);

        // Administration
        if (str.equals(ADMIN_PERMISSION))
            return new SystemPermission(SystemPermission.Type.ADMINISTER);

        throw new GuacamoleException("Invalid permission string.");

    }

    /**
     * Given a permission string, returns the corresponding user permission.
     *
     * @param str The permission string to parse.
     * @return The parsed user permission.
     * @throws GuacamoleException If the given string could not be parsed.
     */
    private Permission parseUserPermission(String str)
            throws GuacamoleException {

        // Read
        if (str.startsWith(READ_PREFIX))
            return new UserPermission(ObjectPermission.Type.READ,
                    str.substring(READ_PREFIX.length()));

        // Update
        if (str.startsWith(UPDATE_PREFIX))
            return new UserPermission(ObjectPermission.Type.UPDATE,
                    str.substring(UPDATE_PREFIX.length()));

        // Delete
        if (str.startsWith(DELETE_PREFIX))
            return new UserPermission(ObjectPermission.Type.DELETE,
                    str.substring(DELETE_PREFIX.length()));

        // Administration
        if (str.startsWith(ADMIN_PREFIX))
            return new UserPermission(ObjectPermission.Type.ADMINISTER,
                    str.substring(ADMIN_PREFIX.length()));

        throw new GuacamoleException("Invalid permission string.");

    }

    /**
     * Given a permission string, returns the corresponding connection
     * permission.
     *
     * @param str The permission string to parse.
     * @return The parsed connection permission.
     * @throws GuacamoleException If the given string could not be parsed.
     */
    private Permission parseConnectionPermission(String str)
            throws GuacamoleException {

        // Read
        if (str.startsWith(READ_PREFIX))
            return new ConnectionPermission(ObjectPermission.Type.READ,
                    str.substring(READ_PREFIX.length()));

        // Update
        if (str.startsWith(UPDATE_PREFIX))
            return new ConnectionPermission(ObjectPermission.Type.UPDATE,
                    str.substring(UPDATE_PREFIX.length()));

        // Delete
        if (str.startsWith(DELETE_PREFIX))
            return new ConnectionPermission(ObjectPermission.Type.DELETE,
                    str.substring(DELETE_PREFIX.length()));

        // Administration
        if (str.startsWith(ADMIN_PREFIX))
            return new ConnectionPermission(ObjectPermission.Type.ADMINISTER,
                    str.substring(ADMIN_PREFIX.length()));

        throw new GuacamoleClientException("Invalid permission string.");

    }

    /**
     * Given a permission string, returns the corresponding connection group
     * permission.
     *
     * @param str The permission string to parse.
     * @return The parsed connection group permission.
     * @throws GuacamoleException If the given string could not be parsed.
     */
    private Permission parseConnectionGroupPermission(String str)
            throws GuacamoleException {

        // Read
        if (str.startsWith(READ_PREFIX))
            return new ConnectionGroupPermission(ObjectPermission.Type.READ,
                    str.substring(READ_PREFIX.length()));

        // Update
        if (str.startsWith(UPDATE_PREFIX))
            return new ConnectionGroupPermission(ObjectPermission.Type.UPDATE,
                    str.substring(UPDATE_PREFIX.length()));

        // Delete
        if (str.startsWith(DELETE_PREFIX))
            return new ConnectionGroupPermission(ObjectPermission.Type.DELETE,
                    str.substring(DELETE_PREFIX.length()));

        // Administration
        if (str.startsWith(ADMIN_PREFIX))
            return new ConnectionGroupPermission(ObjectPermission.Type.ADMINISTER,
                    str.substring(ADMIN_PREFIX.length()));

        throw new GuacamoleClientException("Invalid permission string.");

    }

    @Override
    protected void authenticatedService(
            UserContext context,
            HttpServletRequest request, HttpServletResponse response)
    throws GuacamoleException {

        // Create user as specified
        String username = request.getParameter("name");
        String password = request.getParameter("password");

        // Attempt to get user directory
        Directory<String, User> directory =
                context.getUserDirectory();

        // Get user data, setting password if given
        User user = directory.get(username);
        user.setUsername(username);
        if (password != null)
            user.setPassword(password);

        /*
         * NEW PERMISSIONS
         */

        // Set added system permissions
        String[] add_sys_permission = request.getParameterValues("+sys");
        if (add_sys_permission != null) {
            for (String str : add_sys_permission)
                user.addPermission(parseSystemPermission(str));
        }

        // Set added user permissions
        String[] add_user_permission = request.getParameterValues("+user");
        if (add_user_permission != null) {
            for (String str : add_user_permission)
                user.addPermission(parseUserPermission(str));
        }

        // Set added connection permissions
        String[] add_connection_permission = request.getParameterValues("+connection");
        if (add_connection_permission != null) {
            for (String str : add_connection_permission)
                user.addPermission(parseConnectionPermission(str));
        }

        // Set added connection group permissions
        String[] add_connection_group_permission = request.getParameterValues("+connection-group");
        if (add_connection_group_permission != null) {
            for (String str : add_connection_group_permission)
                user.addPermission(parseConnectionGroupPermission(str));
        }

        /*
         * REMOVED PERMISSIONS
         */

        // Unset removed system permissions
        String[] remove_sys_permission = request.getParameterValues("-sys");
        if (remove_sys_permission != null) {
            for (String str : remove_sys_permission)
                user.removePermission(parseSystemPermission(str));
        }

        // Unset removed user permissions
        String[] remove_user_permission = request.getParameterValues("-user");
        if (remove_user_permission != null) {
            for (String str : remove_user_permission)
                user.removePermission(parseUserPermission(str));
        }

        // Unset removed connection permissions
        String[] remove_connection_permission = request.getParameterValues("-connection");
        if (remove_connection_permission != null) {
            for (String str : remove_connection_permission)
                user.removePermission(parseConnectionPermission(str));
        }

        // Unset removed connection group permissions
        String[] remove_connection_group_permission = request.getParameterValues("-connection-group");
        if (remove_connection_group_permission != null) {
            for (String str : remove_connection_group_permission)
                user.removePermission(parseConnectionGroupPermission(str));
        }

        // Update user
        directory.update(user);

    }

}

