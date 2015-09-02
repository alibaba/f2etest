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


import com.google.inject.Inject;
import java.util.Set;
import org.glyptodon.guacamole.GuacamoleClientException;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.ConnectionGroup;
import org.glyptodon.guacamole.net.auth.ConnectionGroup.Type;
import org.glyptodon.guacamole.net.auth.Directory;
import net.sourceforge.guacamole.net.auth.mysql.dao.ConnectionGroupPermissionMapper;
import net.sourceforge.guacamole.net.auth.mysql.model.ConnectionGroupPermissionKey;
import net.sourceforge.guacamole.net.auth.mysql.service.ConnectionGroupService;
import net.sourceforge.guacamole.net.auth.mysql.service.PermissionCheckService;
import org.glyptodon.guacamole.GuacamoleResourceNotFoundException;
import org.glyptodon.guacamole.GuacamoleUnsupportedException;
import org.mybatis.guice.transactional.Transactional;

/**
 * A MySQL-based implementation of the connection group directory.
 *
 * @author James Muehlner
 */
public class ConnectionGroupDirectory implements Directory<String, ConnectionGroup>{

    /**
     * The ID of the user who this connection directory belongs to.
     * Access is based on his/her permission settings.
     */
    private int user_id;

    /**
     * The ID of the parent connection group.
     */
    private Integer parentID;

    /**
     * Service for checking permissions.
     */
    @Inject
    private PermissionCheckService permissionCheckService;

    /**
     * Service managing connection groups.
     */
    @Inject
    private ConnectionGroupService connectionGroupService;

    /**
     * Service for manipulating connection group permissions in the database.
     */
    @Inject
    private ConnectionGroupPermissionMapper connectionGroupPermissionDAO;

    /**
     * Set the user and parentID for this directory.
     *
     * @param user_id The ID of the user owning this connection group directory.
     * @param parentID The ID of the parent connection group.
     */
    public void init(int user_id, Integer parentID) {
        this.parentID = parentID;
        this.user_id = user_id;
    }

    @Transactional
    @Override
    public ConnectionGroup get(String identifier) throws GuacamoleException {

        // Get connection
        MySQLConnectionGroup connectionGroup =
                connectionGroupService.retrieveConnectionGroup(identifier, user_id);
        
        if(connectionGroup == null)
            return null;
        
        // Verify permission to use the parent connection group for organizational purposes
        permissionCheckService.verifyConnectionGroupUsageAccess
                (connectionGroup.getParentID(), user_id, MySQLConstants.CONNECTION_GROUP_ORGANIZATIONAL);

        // Verify access is granted
        permissionCheckService.verifyConnectionGroupAccess(
                this.user_id,
                connectionGroup.getConnectionGroupID(),
                MySQLConstants.CONNECTION_GROUP_READ);

        // Return connection group
        return connectionGroup;

    }

    @Transactional
    @Override
    public Set<String> getIdentifiers() throws GuacamoleException {
        
        // Verify permission to use the connection group for organizational purposes
        permissionCheckService.verifyConnectionGroupUsageAccess
                (parentID, user_id, MySQLConstants.CONNECTION_GROUP_ORGANIZATIONAL);
        
        return permissionCheckService.retrieveConnectionGroupIdentifiers(user_id, 
                parentID, MySQLConstants.CONNECTION_GROUP_READ);
    }

    @Transactional
    @Override
    public void add(ConnectionGroup object) throws GuacamoleException {

        String name = object.getName().trim();
        if(name.isEmpty())
            throw new GuacamoleClientException("The connection group name cannot be blank.");
        
        Type type = object.getType();
        
        String mySQLType = MySQLConstants.getConnectionGroupTypeConstant(type);
        
        // Verify permission to create
        permissionCheckService.verifySystemAccess(this.user_id,
                MySQLConstants.SYSTEM_CONNECTION_GROUP_CREATE);
        
        // Verify permission to edit the parent connection group
        permissionCheckService.verifyConnectionGroupAccess(this.user_id, 
                this.parentID, MySQLConstants.CONNECTION_GROUP_UPDATE);
        
        // Verify permission to use the parent connection group for organizational purposes
        permissionCheckService.verifyConnectionGroupUsageAccess
                (parentID, user_id, MySQLConstants.CONNECTION_GROUP_ORGANIZATIONAL);

        // Verify that no connection already exists with this name.
        MySQLConnectionGroup previousConnectionGroup =
                connectionGroupService.retrieveConnectionGroup(name, parentID, user_id);
        if(previousConnectionGroup != null)
            throw new GuacamoleClientException("That connection group name is already in use.");

        // Create connection group
        MySQLConnectionGroup connectionGroup = connectionGroupService
                .createConnectionGroup(name, user_id, parentID, mySQLType);
        
        // Set the connection group ID
        object.setIdentifier(connectionGroup.getIdentifier());

        // Finally, give the current user full access to the newly created
        // connection group.
        ConnectionGroupPermissionKey newConnectionGroupPermission = new ConnectionGroupPermissionKey();
        newConnectionGroupPermission.setUser_id(this.user_id);
        newConnectionGroupPermission.setConnection_group_id(connectionGroup.getConnectionGroupID());

        // Read permission
        newConnectionGroupPermission.setPermission(MySQLConstants.CONNECTION_GROUP_READ);
        connectionGroupPermissionDAO.insert(newConnectionGroupPermission);

        // Update permission
        newConnectionGroupPermission.setPermission(MySQLConstants.CONNECTION_GROUP_UPDATE);
        connectionGroupPermissionDAO.insert(newConnectionGroupPermission);

        // Delete permission
        newConnectionGroupPermission.setPermission(MySQLConstants.CONNECTION_GROUP_DELETE);
        connectionGroupPermissionDAO.insert(newConnectionGroupPermission);

        // Administer permission
        newConnectionGroupPermission.setPermission(MySQLConstants.CONNECTION_GROUP_ADMINISTER);
        connectionGroupPermissionDAO.insert(newConnectionGroupPermission);

    }

    @Transactional
    @Override
    public void update(ConnectionGroup object) throws GuacamoleException {

        // If connection not actually from this auth provider, we can't handle
        // the update
        if (!(object instanceof MySQLConnectionGroup))
            throw new GuacamoleUnsupportedException("Connection not from database.");

        MySQLConnectionGroup mySQLConnectionGroup = (MySQLConnectionGroup) object;

        // Verify permission to update
        permissionCheckService.verifyConnectionAccess(this.user_id,
                mySQLConnectionGroup.getConnectionGroupID(),
                MySQLConstants.CONNECTION_UPDATE);

        // Perform update
        connectionGroupService.updateConnectionGroup(mySQLConnectionGroup);
    }

    @Transactional
    @Override
    public void remove(String identifier) throws GuacamoleException {

        // Get connection
        MySQLConnectionGroup mySQLConnectionGroup =
                connectionGroupService.retrieveConnectionGroup(identifier, user_id);
        
        if(mySQLConnectionGroup == null)
            throw new GuacamoleResourceNotFoundException("Connection group not found.");
        
        // Verify permission to use the parent connection group for organizational purposes
        permissionCheckService.verifyConnectionGroupUsageAccess
                (mySQLConnectionGroup.getParentID(), user_id, MySQLConstants.CONNECTION_GROUP_ORGANIZATIONAL);

        // Verify permission to delete
        permissionCheckService.verifyConnectionGroupAccess(this.user_id,
                mySQLConnectionGroup.getConnectionGroupID(),
                MySQLConstants.CONNECTION_GROUP_DELETE);

        // Delete the connection group itself
        connectionGroupService.deleteConnectionGroup
                (mySQLConnectionGroup.getConnectionGroupID());

    }

    @Override
    public void move(String identifier, Directory<String, ConnectionGroup> directory) 
            throws GuacamoleException {
        
        if(MySQLConstants.CONNECTION_GROUP_ROOT_IDENTIFIER.equals(identifier))
            throw new GuacamoleUnsupportedException("The root connection group cannot be moved.");
        
        if(!(directory instanceof ConnectionGroupDirectory))
            throw new GuacamoleUnsupportedException("Directory not from database");
        
        Integer toConnectionGroupID = ((ConnectionGroupDirectory)directory).parentID;

        // Get connection group
        MySQLConnectionGroup mySQLConnectionGroup =
                connectionGroupService.retrieveConnectionGroup(identifier, user_id);
        
        if(mySQLConnectionGroup == null)
            throw new GuacamoleResourceNotFoundException("Connection group not found.");

        // Verify permission to update the connection
        permissionCheckService.verifyConnectionAccess(this.user_id,
                mySQLConnectionGroup.getConnectionGroupID(),
                MySQLConstants.CONNECTION_GROUP_UPDATE);
        
        // Verify permission to use the from connection group for organizational purposes
        permissionCheckService.verifyConnectionGroupUsageAccess
                (mySQLConnectionGroup.getParentID(), user_id, MySQLConstants.CONNECTION_GROUP_ORGANIZATIONAL);

        // Verify permission to update the from connection group
        permissionCheckService.verifyConnectionGroupAccess(this.user_id,
                mySQLConnectionGroup.getParentID(), MySQLConstants.CONNECTION_GROUP_UPDATE);
        
        // Verify permission to use the to connection group for organizational purposes
        permissionCheckService.verifyConnectionGroupUsageAccess
                (toConnectionGroupID, user_id, MySQLConstants.CONNECTION_GROUP_ORGANIZATIONAL);

        // Verify permission to update the to connection group
        permissionCheckService.verifyConnectionGroupAccess(this.user_id,
                toConnectionGroupID, MySQLConstants.CONNECTION_GROUP_UPDATE);

        // Verify that no connection already exists with this name.
        MySQLConnectionGroup previousConnectionGroup =
                connectionGroupService.retrieveConnectionGroup(mySQLConnectionGroup.getName(), 
                toConnectionGroupID, user_id);
        if(previousConnectionGroup != null)
            throw new GuacamoleClientException("That connection group name is already in use.");
        
        // Verify that moving this connectionGroup would not cause a cycle
        Integer relativeParentID = toConnectionGroupID;
        while(relativeParentID != null) {
            if(relativeParentID == mySQLConnectionGroup.getConnectionGroupID())
                throw new GuacamoleUnsupportedException("Connection group cycle detected.");
            
            MySQLConnectionGroup relativeParentGroup = connectionGroupService.
                    retrieveConnectionGroup(relativeParentID, user_id);
            
            relativeParentID = relativeParentGroup.getParentID();
        }
        
        // Update the connection
        mySQLConnectionGroup.setParentID(toConnectionGroupID);
        connectionGroupService.updateConnectionGroup(mySQLConnectionGroup);
    }

}
