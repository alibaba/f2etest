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

package net.sourceforge.guacamole.net.auth.mysql.service;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.GuacamoleSocket;
import net.sourceforge.guacamole.net.auth.mysql.ActiveConnectionMap;
import net.sourceforge.guacamole.net.auth.mysql.MySQLConnection;
import net.sourceforge.guacamole.net.auth.mysql.MySQLConnectionGroup;
import net.sourceforge.guacamole.net.auth.mysql.MySQLConstants;
import net.sourceforge.guacamole.net.auth.mysql.dao.ConnectionGroupMapper;
import net.sourceforge.guacamole.net.auth.mysql.model.ConnectionGroup;
import net.sourceforge.guacamole.net.auth.mysql.model.ConnectionGroupExample;
import net.sourceforge.guacamole.net.auth.mysql.model.ConnectionGroupExample.Criteria;
import net.sourceforge.guacamole.net.auth.mysql.properties.MySQLGuacamoleProperties;
import org.glyptodon.guacamole.GuacamoleClientTooManyException;
import org.glyptodon.guacamole.GuacamoleResourceNotFoundException;
import org.glyptodon.guacamole.GuacamoleServerBusyException;
import org.glyptodon.guacamole.properties.GuacamoleProperties;
import org.glyptodon.guacamole.protocol.GuacamoleClientInformation;

/**
 * Service which provides convenience methods for creating, retrieving, and
 * manipulating connection groups.
 *
 * @author James Muehlner
 */
public class ConnectionGroupService {
    
    /**
     * Service for managing connections.
     */
    @Inject
    private ConnectionService connectionService;
    
    /**
     * DAO for accessing connection groups.
     */
    @Inject
    private ConnectionGroupMapper connectionGroupDAO;

    /**
     * Provider which creates MySQLConnectionGroups.
     */
    @Inject
    private Provider<MySQLConnectionGroup> mysqlConnectionGroupProvider;
    
    /**
     * The map of all active connections.
     */
    @Inject
    private ActiveConnectionMap activeConnectionMap;
    

    /**
     * Retrieves the connection group having the given 
     * name from the database.
     *
     * @param name The name of the connection to return.
     * @param parentID The ID of the parent connection group.
     * @param userID The ID of the user who queried this connection group.
     * @return The connection having the given name, or null if no such
     *         connection group could be found.
     */
    public MySQLConnectionGroup retrieveConnectionGroup(String name, Integer parentID,
            int userID) {

        // Create criteria
        ConnectionGroupExample example = new ConnectionGroupExample();
        Criteria criteria = example.createCriteria().andConnection_group_nameEqualTo(name);
        if(parentID != null)
            criteria.andParent_idEqualTo(parentID);
        else
            criteria.andParent_idIsNull();
        
        // Query connection group by name and parentID
        List<ConnectionGroup> connectionGroups =
                connectionGroupDAO.selectByExample(example);

        // If no connection group found, return null
        if(connectionGroups.isEmpty())
            return null;

        // Otherwise, return found connection
        return toMySQLConnectionGroup(connectionGroups.get(0), userID);

    }

    /**
     * Retrieves the connection group having the given unique identifier 
     * from the database.
     *
     * @param uniqueIdentifier The unique identifier of the connection group to retrieve.
     * @param userID The ID of the user who queried this connection group.
     * @return The connection group having the given unique identifier, 
     *         or null if no such connection group was found.
     */
    public MySQLConnectionGroup retrieveConnectionGroup(String uniqueIdentifier, 
            int userID) throws GuacamoleException {

        // The unique identifier for a MySQLConnectionGroup is the database ID
        Integer connectionGroupID = null;
        
        // Try to parse the connectionID if it's not the root group
        if(!MySQLConstants.CONNECTION_GROUP_ROOT_IDENTIFIER.equals(uniqueIdentifier)) {
            try {
                connectionGroupID = Integer.parseInt(uniqueIdentifier);
            } catch(NumberFormatException e) {
                throw new GuacamoleResourceNotFoundException("Invalid connection group ID.");
            }
        }
        
        return retrieveConnectionGroup(connectionGroupID, userID);
    }
    
    /**
     * Retrieves the connection group having the given ID from the database.
     *
     * @param id The ID of the connection group to retrieve.
     * @param userID The ID of the user who queried this connection.
     * @return The connection group having the given ID, or null if no such
     *         connection was found.
     */
    public MySQLConnectionGroup retrieveConnectionGroup(Integer id, int userID) {

        // This is the root connection group, so just create it here
        if(id == null) {
            MySQLConnectionGroup connectionGroup = mysqlConnectionGroupProvider.get();
            connectionGroup.init(null, null, 
                    MySQLConstants.CONNECTION_GROUP_ROOT_IDENTIFIER, 
                    MySQLConstants.CONNECTION_GROUP_ROOT_IDENTIFIER, 
                    org.glyptodon.guacamole.net.auth.ConnectionGroup.Type.ORGANIZATIONAL, 
                    userID);
            
            return connectionGroup;
        }
        
        // Query connection by ID
        ConnectionGroup connectionGroup = connectionGroupDAO.selectByPrimaryKey(id);

        // If no connection found, return null
        if(connectionGroup == null)
            return null;

        // Otherwise, return found connection
        return toMySQLConnectionGroup(connectionGroup, userID);
    }

    /**
     * Connect to the connection within the given group with the lowest number
     * of currently active users.
     *
     * @param group The group to load balance across.
     * @param info The information to use when performing the connection
     *             handshake.
     * @param userID The ID of the user who is connecting to the socket.
     * @return The connected socket.
     * @throws GuacamoleException If an error occurs while connecting the
     *                            socket.
     */
    public GuacamoleSocket connect(MySQLConnectionGroup group, 
            GuacamoleClientInformation info, int userID) throws GuacamoleException {
       
        // Get all connections in the group.
        List<Integer> connectionIDs = connectionService.getAllConnectionIDs
                (group.getConnectionGroupID());
        
        synchronized (activeConnectionMap) {

            // Get the least used connection.
            Integer leastUsedConnectionID = 
                    activeConnectionMap.getLeastUsedConnection(connectionIDs);
            
            if(leastUsedConnectionID == null)
                throw new GuacamoleResourceNotFoundException("No connections found in group.");
            
            if(GuacamoleProperties.getProperty(
                    MySQLGuacamoleProperties.MYSQL_DISALLOW_SIMULTANEOUS_CONNECTIONS, false)
                    && activeConnectionMap.isActive(leastUsedConnectionID))
                throw new GuacamoleServerBusyException
                        ("Cannot connect. All connections are in use.");
            
            if(GuacamoleProperties.getProperty(
                    MySQLGuacamoleProperties.MYSQL_DISALLOW_DUPLICATE_CONNECTIONS, true)
                    && activeConnectionMap.isConnectionGroupUserActive(group.getConnectionGroupID(), userID))
                throw new GuacamoleClientTooManyException
                        ("Cannot connect. Connection group already in use by this user.");

            // Get the connection 
            MySQLConnection connection = connectionService
                    .retrieveConnection(leastUsedConnectionID, userID);
            
            // Connect to the connection
            return connectionService.connect(connection, info, userID, group.getConnectionGroupID());

        }
            
    }
    
    /**
     * Returns a list of the IDs of all connection groups with a given parent ID.
     * @param parentID The ID of the parent for all the queried connection groups.
     * @return a list of the IDs of all connection groups with a given parent ID.
     */
    public List<Integer> getAllConnectionGroupIDs(Integer parentID) {
        
        // Create criteria
        ConnectionGroupExample example = new ConnectionGroupExample();
        Criteria criteria = example.createCriteria();
        
        if(parentID != null)
            criteria.andParent_idEqualTo(parentID);
        else
            criteria.andParent_idIsNull();
        
        // Query the connections
        List<ConnectionGroup> connectionGroups = connectionGroupDAO.selectByExample(example);
        
        // List of IDs of connections with the given parent
        List<Integer> connectionGroupIDs = new ArrayList<Integer>();
        
        for(ConnectionGroup connectionGroup : connectionGroups) {
            connectionGroupIDs.add(connectionGroup.getConnection_group_id());
        }
        
        return connectionGroupIDs;
    }

    /**
     * Get the identifiers of all the connection groups defined in the system 
     * with a certain parentID.
     *
     * @return A Set of identifiers of all the connection groups defined 
     * in the system with the given parentID.
     */
    public Set<String> getAllConnectionGroupIdentifiers(Integer parentID) {

        // Set of all present connection identifiers
        Set<String> identifiers = new HashSet<String>();
        
        // Set up Criteria
        ConnectionGroupExample example = new ConnectionGroupExample();
        Criteria criteria = example.createCriteria();
        if(parentID != null)
            criteria.andParent_idEqualTo(parentID);
        else
            criteria.andParent_idIsNull();

        // Query connection identifiers
        List<ConnectionGroup> connectionGroups =
                connectionGroupDAO.selectByExample(example);
        for (ConnectionGroup connectionGroup : connectionGroups)
            identifiers.add(String.valueOf(connectionGroup.getConnection_group_id()));

        return identifiers;

    }

    /**
     * Convert the given database-retrieved Connection into a MySQLConnection.
     * The parameters of the given connection will be read and added to the
     * MySQLConnection in the process.
     *
     * @param connection The connection to convert.
     * @param userID The user who queried this connection.
     * @return A new MySQLConnection containing all data associated with the
     *         specified connection.
     */
    private MySQLConnectionGroup toMySQLConnectionGroup(ConnectionGroup connectionGroup, int userID) {

        // Create new MySQLConnection from retrieved data
        MySQLConnectionGroup mySQLConnectionGroup = mysqlConnectionGroupProvider.get();
        
        String mySqlType = connectionGroup.getType();
        org.glyptodon.guacamole.net.auth.ConnectionGroup.Type authType;
        
        if(mySqlType.equals(MySQLConstants.CONNECTION_GROUP_ORGANIZATIONAL))
            authType = org.glyptodon.guacamole.net.auth.ConnectionGroup.Type.ORGANIZATIONAL;
        else
            authType = org.glyptodon.guacamole.net.auth.ConnectionGroup.Type.BALANCING;
        
        mySQLConnectionGroup.init(
            connectionGroup.getConnection_group_id(),
            connectionGroup.getParent_id(),
            connectionGroup.getConnection_group_name(),
            Integer.toString(connectionGroup.getConnection_group_id()),
            authType,
            userID
        );

        return mySQLConnectionGroup;

    }

    /**
     * Get the connection group IDs of all the connection groups defined in the system.
     *
     * @return A list of connection group IDs of all the connection groups defined in the system.
     */
    public List<Integer> getAllConnectionGroupIDs() {

        // Set of all present connection group IDs
        List<Integer> connectionGroupIDs = new ArrayList<Integer>();

        // Query all connection IDs
        List<ConnectionGroup> connections =
                connectionGroupDAO.selectByExample(new ConnectionGroupExample());
        for (ConnectionGroup connection : connections)
            connectionGroupIDs.add(connection.getConnection_group_id());

        return connectionGroupIDs;

    }

    /**
     * Creates a new connection group having the given name and type.
     *
     * @param name The name to assign to the new connection group.
     * @param userID The ID of the user who created this connection group.
     * @param Type The type of the new connection group.
     * @return A new MySQLConnectionGroup containing the data of the newly created
     *         connection group.
     */
    public MySQLConnectionGroup createConnectionGroup(String name, int userID, 
            Integer parentID, String type) {

        // Initialize database connection
        ConnectionGroup connectionGroup = new ConnectionGroup();
        connectionGroup.setConnection_group_name(name);
        connectionGroup.setParent_id(parentID);
        connectionGroup.setType(type);

        // Create connection
        connectionGroupDAO.insert(connectionGroup);
        return toMySQLConnectionGroup(connectionGroup, userID);

    }

    /**
     * Updates the connection group in the database corresponding to the given
     * MySQLConnectionGroup.
     *
     * @param mySQLConnectionGroup The MySQLConnectionGroup to update (save) 
     *                             to the database. 
     *                             This connection must already exist.
     */
    public void updateConnectionGroup(MySQLConnectionGroup mySQLConnectionGroup) {

        // Populate connection
        ConnectionGroup connectionGroup = new ConnectionGroup();
        connectionGroup.setConnection_group_id(mySQLConnectionGroup.getConnectionGroupID());
        connectionGroup.setParent_id(mySQLConnectionGroup.getParentID());
        connectionGroup.setConnection_group_name(mySQLConnectionGroup.getName());
        
        switch(mySQLConnectionGroup.getType()) {
            case BALANCING :
                connectionGroup.setType(MySQLConstants.CONNECTION_GROUP_BALANCING);
                break;
            case ORGANIZATIONAL:
                connectionGroup.setType(MySQLConstants.CONNECTION_GROUP_ORGANIZATIONAL);
                break;
        }

        // Update the connection group in the database
        connectionGroupDAO.updateByPrimaryKey(connectionGroup);

    }

    /**
     * Deletes the connection group having the given ID from the database.
     * @param id The ID of the connection group to delete.
     */
    public void deleteConnectionGroup(int id) {
        connectionGroupDAO.deleteByPrimaryKey(id);
    }
}
