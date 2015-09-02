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


import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.glyptodon.guacamole.GuacamoleClientException;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleSecurityException;
import org.glyptodon.guacamole.net.auth.Directory;
import org.glyptodon.guacamole.net.auth.User;
import net.sourceforge.guacamole.net.auth.mysql.dao.ConnectionGroupPermissionMapper;
import net.sourceforge.guacamole.net.auth.mysql.dao.ConnectionPermissionMapper;
import net.sourceforge.guacamole.net.auth.mysql.dao.SystemPermissionMapper;
import net.sourceforge.guacamole.net.auth.mysql.dao.UserPermissionMapper;
import net.sourceforge.guacamole.net.auth.mysql.model.ConnectionGroupPermissionExample;
import net.sourceforge.guacamole.net.auth.mysql.model.ConnectionGroupPermissionKey;
import net.sourceforge.guacamole.net.auth.mysql.model.ConnectionPermissionExample;
import net.sourceforge.guacamole.net.auth.mysql.model.ConnectionPermissionKey;
import net.sourceforge.guacamole.net.auth.mysql.model.SystemPermissionExample;
import net.sourceforge.guacamole.net.auth.mysql.model.SystemPermissionKey;
import net.sourceforge.guacamole.net.auth.mysql.model.UserPermissionExample;
import net.sourceforge.guacamole.net.auth.mysql.model.UserPermissionKey;
import net.sourceforge.guacamole.net.auth.mysql.service.ConnectionGroupService;
import net.sourceforge.guacamole.net.auth.mysql.service.ConnectionService;
import net.sourceforge.guacamole.net.auth.mysql.service.PermissionCheckService;
import net.sourceforge.guacamole.net.auth.mysql.service.UserService;
import org.glyptodon.guacamole.GuacamoleUnsupportedException;
import org.glyptodon.guacamole.net.auth.permission.ConnectionGroupPermission;
import org.glyptodon.guacamole.net.auth.permission.ConnectionPermission;
import org.glyptodon.guacamole.net.auth.permission.Permission;
import org.glyptodon.guacamole.net.auth.permission.SystemPermission;
import org.glyptodon.guacamole.net.auth.permission.UserPermission;
import org.mybatis.guice.transactional.Transactional;

/**
 * A MySQL based implementation of the User Directory.
 * @author James Muehlner
 */
public class UserDirectory implements Directory<String, User> {

    /**
     * The ID of the user who this user directory belongs to.
     * Access is based on his/her permission settings.
     */
    private int user_id;

    /**
     * Service for accessing users.
     */
    @Inject
    private UserService userService;

    /**
     * Service for accessing connections.
     */
    @Inject
    private ConnectionService connectionService;

    /**
     * Service for accessing connection groups.
     */
    @Inject
    private ConnectionGroupService connectionGroupService;

    /**
     * DAO for accessing user permissions, which will be injected.
     */
    @Inject
    private UserPermissionMapper userPermissionDAO;

    /**
     * DAO for accessing connection permissions, which will be injected.
     */
    @Inject
    private ConnectionPermissionMapper connectionPermissionDAO;

    /**
     * DAO for accessing connection group permissions, which will be injected.
     */
    @Inject
    private ConnectionGroupPermissionMapper connectionGroupPermissionDAO;

    /**
     * DAO for accessing system permissions, which will be injected.
     */
    @Inject
    private SystemPermissionMapper systemPermissionDAO;

    /**
     * Service for checking various permissions, which will be injected.
     */
    @Inject
    private PermissionCheckService permissionCheckService;

    /**
     * Set the user for this directory.
     *
     * @param user_id The ID of the user whose permissions define the visibility
     *                of other users in this directory.
     */
    public void init(int user_id) {
        this.user_id = user_id;
    }

    @Transactional
    @Override
    public org.glyptodon.guacamole.net.auth.User get(String identifier)
            throws GuacamoleException {

        // Get user
        MySQLUser user = userService.retrieveUser(identifier);
        
        if(user == null)
            return null;

        // Verify access is granted
        permissionCheckService.verifyUserAccess(this.user_id,
                user.getUserID(),
                MySQLConstants.USER_READ);

        // Return user
        return user;

    }

    @Transactional
    @Override
    public Set<String> getIdentifiers() throws GuacamoleException {
        return permissionCheckService.retrieveUsernames(user_id,
                MySQLConstants.USER_READ);
    }

    @Override
    @Transactional
    public void add(org.glyptodon.guacamole.net.auth.User object)
            throws GuacamoleException {

        String username = object.getUsername().trim();
        if(username.isEmpty())
            throw new GuacamoleClientException("The username cannot be blank.");

        // Verify current user has permission to create users
        permissionCheckService.verifySystemAccess(this.user_id,
                MySQLConstants.SYSTEM_USER_CREATE);
        Preconditions.checkNotNull(object);

        // Verify that no user already exists with this username.
        MySQLUser previousUser = userService.retrieveUser(username);
        if(previousUser != null)
            throw new GuacamoleClientException("That username is already in use.");

        // Create new user
        MySQLUser user = userService.createUser(username, object.getPassword());

        // Create permissions of new user in database
        createPermissions(user.getUserID(), object.getPermissions());

        // Give the current user full access to the newly created user.
        UserPermissionKey newUserPermission = new UserPermissionKey();
        newUserPermission.setUser_id(this.user_id);
        newUserPermission.setAffected_user_id(user.getUserID());

        // READ permission on new user
        newUserPermission.setPermission(MySQLConstants.USER_READ);
        userPermissionDAO.insert(newUserPermission);

        // UPDATE permission on new user
        newUserPermission.setPermission(MySQLConstants.USER_UPDATE);
        userPermissionDAO.insert(newUserPermission);

        // DELETE permission on new user
        newUserPermission.setPermission(MySQLConstants.USER_DELETE);
        userPermissionDAO.insert(newUserPermission);

        // ADMINISTER permission on new user
        newUserPermission.setPermission(MySQLConstants.USER_ADMINISTER);
        userPermissionDAO.insert(newUserPermission);

    }

    /**
     * Add the given permissions to the given user.
     *
     * @param user_id The ID of the user whose permissions should be updated.
     * @param permissions The permissions to add.
     * @throws GuacamoleException If an error occurs while updating the
     *                            permissions of the given user.
     */
    private void createPermissions(int user_id, Set<Permission> permissions) throws GuacamoleException {

        // Partition given permissions by permission type
        List<UserPermission> newUserPermissions = new ArrayList<UserPermission>();
        List<ConnectionPermission> newConnectionPermissions = new ArrayList<ConnectionPermission>();
        List<ConnectionGroupPermission> newConnectionGroupPermissions = new ArrayList<ConnectionGroupPermission>();
        List<SystemPermission> newSystemPermissions = new ArrayList<SystemPermission>();

        for (Permission permission : permissions) {

            if (permission instanceof UserPermission)
                newUserPermissions.add((UserPermission) permission);

            else if (permission instanceof ConnectionPermission)
                newConnectionPermissions.add((ConnectionPermission) permission);

            else if (permission instanceof ConnectionGroupPermission)
                newConnectionGroupPermissions.add((ConnectionGroupPermission) permission);

            else if (permission instanceof SystemPermission)
                newSystemPermissions.add((SystemPermission) permission);
        }

        // Create the new permissions
        createUserPermissions(user_id, newUserPermissions);
        createConnectionPermissions(user_id, newConnectionPermissions);
        createConnectionGroupPermissions(user_id, newConnectionGroupPermissions);
        createSystemPermissions(user_id, newSystemPermissions);

    }

    /**
     * Remove the given permissions from the given user.
     *
     * @param user_id The ID of the user whose permissions should be updated.
     * @param permissions The permissions to remove.
     * @throws GuacamoleException If an error occurs while updating the
     *                            permissions of the given user.
     */
    private void removePermissions(int user_id, Set<Permission> permissions)
            throws GuacamoleException {

        // Partition given permissions by permission type
        List<UserPermission> removedUserPermissions = new ArrayList<UserPermission>();
        List<ConnectionPermission> removedConnectionPermissions = new ArrayList<ConnectionPermission>();
        List<ConnectionGroupPermission> removedConnectionGroupPermissions = new ArrayList<ConnectionGroupPermission>();
        List<SystemPermission> removedSystemPermissions = new ArrayList<SystemPermission>();

        for (Permission permission : permissions) {

            if (permission instanceof UserPermission)
                removedUserPermissions.add((UserPermission) permission);

            else if (permission instanceof ConnectionPermission)
                removedConnectionPermissions.add((ConnectionPermission) permission);

            else if (permission instanceof ConnectionGroupPermission)
                removedConnectionGroupPermissions.add((ConnectionGroupPermission) permission);

            else if (permission instanceof SystemPermission)
                removedSystemPermissions.add((SystemPermission) permission);
        }

        // Delete the removed permissions.
        deleteUserPermissions(user_id, removedUserPermissions);
        deleteConnectionPermissions(user_id, removedConnectionPermissions);
        deleteConnectionGroupPermissions(user_id, removedConnectionGroupPermissions);
        deleteSystemPermissions(user_id, removedSystemPermissions);

    }

    /**
     * Create the given user permissions for the given user.
     *
     * @param user_id The ID of the user to change the permissions of.
     * @param permissions The new permissions the given user should have when
     *                    this operation completes.
     * @throws GuacamoleException If permission to alter the access permissions
     *                            of affected objects is denied.
     */
    private void createUserPermissions(int user_id,
            Collection<UserPermission> permissions)
            throws GuacamoleException {

        // If no permissions given, stop now
        if(permissions.isEmpty())
            return;

        // Get list of administerable user IDs
        List<Integer> administerableUserIDs =
            permissionCheckService.retrieveUserIDs(this.user_id,
                MySQLConstants.USER_ADMINISTER);

        // Get set of usernames corresponding to administerable users
        Map<String, Integer> administerableUsers =
                userService.translateUsernames(administerableUserIDs);

        // Insert all given permissions
        for (UserPermission permission : permissions) {

            // Get original ID
            Integer affected_id =
                    administerableUsers.get(permission.getObjectIdentifier());

            // Verify that the user actually has permission to administrate
            // every one of these users
            if (affected_id == null)
                throw new GuacamoleSecurityException(
                      "User #" + this.user_id
                    + " does not have permission to administrate user "
                    + permission.getObjectIdentifier());

            // Create new permission
            UserPermissionKey newPermission = new UserPermissionKey();
            newPermission.setUser_id(user_id);
            newPermission.setPermission(MySQLConstants.getUserConstant(permission.getType()));
            newPermission.setAffected_user_id(affected_id);
            userPermissionDAO.insert(newPermission);

         }

    }

    /**
     * Delete permissions having to do with users for a given user.
     *
     * @param user_id The ID of the user to change the permissions of.
     * @param permissions The permissions the given user should no longer have
     *                    when this operation completes.
     * @throws GuacamoleException If permission to alter the access permissions
     *                            of affected objects is denied.
     */
    private void deleteUserPermissions(int user_id,
            Collection<UserPermission> permissions)
            throws GuacamoleException {

        // If no permissions given, stop now
        if(permissions.isEmpty())
            return;

        // Get list of administerable user IDs
        List<Integer> administerableUserIDs =
            permissionCheckService.retrieveUserIDs(this.user_id,
                MySQLConstants.USER_ADMINISTER);

        // Get set of usernames corresponding to administerable users
        Map<String, Integer> administerableUsers =
                userService.translateUsernames(administerableUserIDs);

        // Delete requested permissions
        for (UserPermission permission : permissions) {

            // Get original ID
            Integer affected_id =
                    administerableUsers.get(permission.getObjectIdentifier());

            // Verify that the user actually has permission to administrate
            // every one of these users
            if (affected_id == null)
                throw new GuacamoleSecurityException(
                      "User #" + this.user_id
                    + " does not have permission to administrate user "
                    + permission.getObjectIdentifier());

            // Delete requested permission
            UserPermissionExample userPermissionExample = new UserPermissionExample();
            userPermissionExample.createCriteria()
                .andUser_idEqualTo(user_id)
                .andPermissionEqualTo(MySQLConstants.getUserConstant(permission.getType()))
                .andAffected_user_idEqualTo(affected_id);
            userPermissionDAO.deleteByExample(userPermissionExample);

        }

    }

    /**
     * Create any new permissions having to do with connections for a given
     * user.
     *
     * @param user_id The ID of the user to assign or remove permissions from.
     * @param permissions The new permissions the user should have after this
     *                    operation completes.
     * @throws GuacamoleException If permission to alter the access permissions
     *                            of affected objects is deniedD
     */
    private void createConnectionPermissions(int user_id,
            Collection<ConnectionPermission> permissions)
            throws GuacamoleException {

        // If no permissions given, stop now
        if(permissions.isEmpty())
            return;

        // Get list of administerable connection IDs
        Set<Integer> administerableConnectionIDs = Sets.<Integer>newHashSet(
            permissionCheckService.retrieveConnectionIDs(this.user_id,
                MySQLConstants.CONNECTION_ADMINISTER));

        // Insert all given permissions
        for (ConnectionPermission permission : permissions) {

            // Get original ID
            Integer connection_id = Integer.valueOf(permission.getObjectIdentifier());

            // Throw exception if permission to administer this connection
            // is not granted
            if (!administerableConnectionIDs.contains(connection_id))
                throw new GuacamoleSecurityException(
                      "User #" + this.user_id
                    + " does not have permission to administrate connection "
                    + permission.getObjectIdentifier());

            // Create new permission
            ConnectionPermissionKey newPermission = new ConnectionPermissionKey();
            newPermission.setUser_id(user_id);
            newPermission.setPermission(MySQLConstants.getConnectionConstant(permission.getType()));
            newPermission.setConnection_id(connection_id);
            connectionPermissionDAO.insert(newPermission);

        }
    }

    /**
     * Create any new permissions having to do with connection groups 
     * for a given user.
     *
     * @param user_id The ID of the user to assign or remove permissions from.
     * @param permissions The new permissions the user should have after this
     *                    operation completes.
     * @throws GuacamoleException If permission to alter the access permissions
     *                            of affected objects is deniedD
     */
    private void createConnectionGroupPermissions(int user_id,
            Collection<ConnectionGroupPermission> permissions)
            throws GuacamoleException {

        // If no permissions given, stop now
        if(permissions.isEmpty())
            return;

        // Get list of administerable connection group IDs
        Set<Integer> administerableConnectionGroupIDs = Sets.<Integer>newHashSet(
            permissionCheckService.retrieveConnectionGroupIDs(this.user_id,
                MySQLConstants.CONNECTION_GROUP_ADMINISTER));

        // Insert all given permissions
        for (ConnectionGroupPermission permission : permissions) {

            // Get original ID
            Integer connection_group_id = Integer.valueOf(permission.getObjectIdentifier());

            // Throw exception if permission to administer this connection group
            // is not granted
            if (!administerableConnectionGroupIDs.contains(connection_group_id))
                throw new GuacamoleSecurityException(
                      "User #" + this.user_id
                    + " does not have permission to administrate connection group"
                    + permission.getObjectIdentifier());

            // Create new permission
            ConnectionGroupPermissionKey newPermission = new ConnectionGroupPermissionKey();
            newPermission.setUser_id(user_id);
            newPermission.setPermission(MySQLConstants.getConnectionGroupConstant(permission.getType()));
            newPermission.setConnection_group_id(connection_group_id);
            connectionGroupPermissionDAO.insert(newPermission);

        }
    }

    /**
     * Delete permissions having to do with connections for a given user.
     *
     * @param user_id The ID of the user to change the permissions of.
     * @param permissions The permissions the given user should no longer have
     *                    when this operation completes.
     * @throws GuacamoleException If permission to alter the access permissions
     *                            of affected objects is denied.
     */
    private void deleteConnectionPermissions(int user_id,
            Collection<ConnectionPermission> permissions)
            throws GuacamoleException {

        // If no permissions given, stop now
        if(permissions.isEmpty())
            return;

        // Get list of administerable connection IDs
        Set<Integer> administerableConnectionIDs = Sets.<Integer>newHashSet(
            permissionCheckService.retrieveConnectionIDs(this.user_id,
                MySQLConstants.CONNECTION_ADMINISTER));

        // Delete requested permissions
        for (ConnectionPermission permission : permissions) {

            // Get original ID
            Integer connection_id = Integer.valueOf(permission.getObjectIdentifier());

            // Verify that the user actually has permission to administrate
            // every one of these connections
            if (!administerableConnectionIDs.contains(connection_id))
                throw new GuacamoleSecurityException(
                      "User #" + this.user_id
                    + " does not have permission to administrate connection "
                    + permission.getObjectIdentifier());

            ConnectionPermissionExample connectionPermissionExample = new ConnectionPermissionExample();
            connectionPermissionExample.createCriteria()
                .andUser_idEqualTo(user_id)
                .andPermissionEqualTo(MySQLConstants.getConnectionConstant(permission.getType()))
                .andConnection_idEqualTo(connection_id);
            connectionPermissionDAO.deleteByExample(connectionPermissionExample);

        }

    }

    /**
     * Delete permissions having to do with connection groups for a given user.
     *
     * @param user_id The ID of the user to change the permissions of.
     * @param permissions The permissions the given user should no longer have
     *                    when this operation completes.
     * @throws GuacamoleException If permission to alter the access permissions
     *                            of affected objects is denied.
     */
    private void deleteConnectionGroupPermissions(int user_id,
            Collection<ConnectionGroupPermission> permissions)
            throws GuacamoleException {

        // If no permissions given, stop now
        if(permissions.isEmpty())
            return;

        // Get list of administerable connection group IDs
        Set<Integer> administerableConnectionGroupIDs = Sets.<Integer>newHashSet(
            permissionCheckService.retrieveConnectionGroupIDs(this.user_id,
                MySQLConstants.CONNECTION_GROUP_ADMINISTER));

        // Delete requested permissions
        for (ConnectionGroupPermission permission : permissions) {

            // Get original ID
            Integer connection_group_id = Integer.valueOf(permission.getObjectIdentifier());

            // Verify that the user actually has permission to administrate
            // every one of these connection groups
            if (!administerableConnectionGroupIDs.contains(connection_group_id))
                throw new GuacamoleSecurityException(
                      "User #" + this.user_id
                    + " does not have permission to administrate connection group"
                    + permission.getObjectIdentifier());

            ConnectionGroupPermissionExample connectionGroupPermissionExample = new ConnectionGroupPermissionExample();
            connectionGroupPermissionExample.createCriteria()
                .andUser_idEqualTo(user_id)
                .andPermissionEqualTo(MySQLConstants.getConnectionGroupConstant(permission.getType()))
                .andConnection_group_idEqualTo(connection_group_id);
            connectionGroupPermissionDAO.deleteByExample(connectionGroupPermissionExample);

        }

    }

    /**
     * Create any new system permissions for a given user. All permissions in
     * the given list will be inserted.
     *
     * @param user_id The ID of the user whose permissions should be updated.
     * @param permissions The new system permissions that the given user should
     *                    have when this operation completes.
     * @throws GuacamoleException If permission to administer system permissions
     *                            is denied.
     */
    private void createSystemPermissions(int user_id,
            Collection<SystemPermission> permissions) throws GuacamoleException {

        // If no permissions given, stop now
        if(permissions.isEmpty())
            return;

        // Only a system administrator can add system permissions.
        permissionCheckService.verifySystemAccess(
                this.user_id, SystemPermission.Type.ADMINISTER.name());

        // Insert all requested permissions
        for (SystemPermission permission : permissions) {

            // Insert permission
            SystemPermissionKey newSystemPermission = new SystemPermissionKey();
            newSystemPermission.setUser_id(user_id);
            newSystemPermission.setPermission(MySQLConstants.getSystemConstant(permission.getType()));
            systemPermissionDAO.insert(newSystemPermission);

        }

    }

    /**
     * Delete system permissions for a given user. All permissions in
     * the given list will be removed from the user.
     *
     * @param user_id The ID of the user whose permissions should be updated.
     * @param permissions The permissions the given user should no longer have
     *                    when this operation completes.
     * @throws GuacamoleException If the permissions specified could not be
     *                            removed due to system restrictions.
     */
    private void deleteSystemPermissions(int user_id,
            Collection<SystemPermission> permissions)
            throws GuacamoleException {

        // If no permissions given, stop now
        if (permissions.isEmpty())
            return;

        // Prevent self-de-adminifying
        if (user_id == this.user_id)
            throw new GuacamoleUnsupportedException("Removing your own administrative permissions is not allowed.");

        // Build list of requested system permissions
        List<String> systemPermissionTypes = new ArrayList<String>();
        for (SystemPermission permission : permissions)
            systemPermissionTypes.add(MySQLConstants.getSystemConstant(permission.getType()));

        // Delete the requested system permissions for this user
        SystemPermissionExample systemPermissionExample = new SystemPermissionExample();
        systemPermissionExample.createCriteria().andUser_idEqualTo(user_id)
                .andPermissionIn(systemPermissionTypes);
        systemPermissionDAO.deleteByExample(systemPermissionExample);

    }

    @Override
    @Transactional
    public void update(org.glyptodon.guacamole.net.auth.User object)
            throws GuacamoleException {

        // If user not actually from this auth provider, we can't handle updated
        // permissions.
        if (!(object instanceof MySQLUser))
            throw new GuacamoleUnsupportedException("User not from database.");

        MySQLUser mySQLUser = (MySQLUser) object;

        // Validate permission to update this user is granted
        permissionCheckService.verifyUserAccess(this.user_id,
                mySQLUser.getUserID(),
                MySQLConstants.USER_UPDATE);

        // Update the user in the database
        userService.updateUser(mySQLUser);

        // Update permissions in database
        createPermissions(mySQLUser.getUserID(), mySQLUser.getNewPermissions());
        removePermissions(mySQLUser.getUserID(), mySQLUser.getRemovedPermissions());

        // The appropriate permissions have been inserted and deleted, so
        // reset the new and removed permission sets.
        mySQLUser.resetPermissions();

    }

    @Override
    @Transactional
    public void remove(String identifier) throws GuacamoleException {

        // Get user pending deletion
        MySQLUser user = userService.retrieveUser(identifier);

        // Prevent self-deletion
        if (user.getUserID() == this.user_id)
            throw new GuacamoleUnsupportedException("Deleting your own user is not allowed.");

        // Validate current user has permission to remove the specified user
        permissionCheckService.verifyUserAccess(this.user_id,
                user.getUserID(),
                MySQLConstants.USER_DELETE);

        // Delete specified user
        userService.deleteUser(user.getUserID());

    }

    @Override
    public void move(String identifier, Directory<String, User> groupIdentifier) 
            throws GuacamoleException {
        throw new GuacamoleSecurityException("Permission denied.");
    }

}
