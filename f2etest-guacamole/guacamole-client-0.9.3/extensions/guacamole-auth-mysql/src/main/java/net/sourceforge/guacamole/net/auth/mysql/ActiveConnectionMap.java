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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.glyptodon.guacamole.GuacamoleException;
import net.sourceforge.guacamole.net.auth.mysql.dao.ConnectionHistoryMapper;
import net.sourceforge.guacamole.net.auth.mysql.model.ConnectionHistory;
import org.glyptodon.guacamole.GuacamoleResourceNotFoundException;

/**
 * Represents the map of currently active Connections to the count of the number
 * of current users. Whenever a socket is opened, the connection count should be
 * incremented, and whenever a socket is closed, the connection count should be 
 * decremented.
 *
 * @author James Muehlner
 */
public class ActiveConnectionMap {
    
    /**
     * Represents the count of users currently using a MySQL connection.
     */
    public class Connection {
        
        /**
         * The ID of the MySQL connection that this Connection represents.
         */
        private int connectionID;
        
        /**
         * The number of users currently using this connection.
         */
        private int currentUserCount;
        
        /**
         * Returns the ID of the MySQL connection that this Connection 
         * represents.
         * 
         * @return the ID of the MySQL connection that this Connection 
         * represents.
         */
        public int getConnectionID() {
            return connectionID;
        }
        
        /**
         * Returns the number of users currently using this connection.
         * 
         * @return the number of users currently using this connection.
         */
        public int getCurrentUserCount() {
            return currentUserCount;
        }
        
        /**
         * Set the current user count for this connection.
         * 
         * @param currentUserCount The new user count for this Connection.
         */
        public void setCurrentUserCount(int currentUserCount) {
            this.currentUserCount = currentUserCount;
        }
        
        /**
         * Create a new Connection for the given connectionID with a zero
         * current user count.
         * 
         * @param connectionID The ID of the MySQL connection that this 
         *                     Connection represents.
         */
        public Connection(int connectionID) {
            this.connectionID = connectionID;
            this.currentUserCount = 0;
        }
    }
    
    /*
     * Represents a user connected to a connection or BALANCING connection group.
     */
    public class ConnectionUser {
        /**
         * The ID of the connection or connection group that this ConnectionUser refers to.
         */
        private int identifier; 
        
        /**
         * The user that this ConnectionUser refers to.
         */
        private int userID;

        /**
         * Returns ID of the connection or connection group that this ConnectionUser refers to.
         * @return ID of the connection or connection group that this ConnectionUser refers to.
         */
        public int getIdentifier() {
            return identifier;
        }

        /**
         * Returns the user ID that this ConnectionUser refers to.
         * @return the user ID that this ConnectionUser refers to.
         */
        public int getUserID() {
            return userID;
        }
        
        /**
         * Create a ConnectionUser with the given connection or connection group
         * ID and user ID.
         * 
         * @param identifier The connection or connection group ID that this 
         *                   ConnectionUser refers to.
         * @param userID The user ID that this ConnectionUser refers to.
         */
        public ConnectionUser(int identifier, int userID) {
            this.identifier = identifier;
            this.userID = userID;
        }
        
        @Override
        public boolean equals(Object other) {
            
            // Only another ConnectionUser can equal this ConnectionUser
            if(!(other instanceof ConnectionUser))
                return false;
            
            ConnectionUser otherConnectionGroupUser = 
                    (ConnectionUser)other;
            
            /* 
             * Two ConnectionGroupUsers are equal iff they represent the exact 
             * same pairing of connection or connection group and user.
             */
            return this.identifier == otherConnectionGroupUser.identifier
                    && this.userID == otherConnectionGroupUser.userID;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 23 * hash + this.identifier;
            hash = 23 * hash + this.userID;
            return hash;
        }
    }

    /**
     * DAO for accessing connection history.
     */
    @Inject
    private ConnectionHistoryMapper connectionHistoryDAO;

    /**
     * Map of all the connections that are currently active to the
     * count of current users.
     */
    private Map<Integer, Connection> activeConnectionMap =
            new HashMap<Integer, Connection>();

    /**
     * Map of all the connection group users to the count of current usages.
     */
    private Map<ConnectionUser, Integer> activeConnectionGroupUserMap =
            new HashMap<ConnectionUser, Integer>();

    /**
     * Map of all the connection users to the count of current usages.
     */
    private Map<ConnectionUser, Integer> activeConnectionUserMap =
            new HashMap<ConnectionUser, Integer>();
    
    /**
     * Returns the number of connections opened by the given user using 
     * the given ConnectionGroup.
     * 
     * @param connectionGroupID The connection group ID that this 
     *                          ConnectionUser refers to.
     * @param userID The user ID that this ConnectionUser refers to.
     * 
     * @return The number of connections opened by the given user to the given
     *         ConnectionGroup.
     */
    public int getConnectionGroupUserCount(int connectionGroupID, int userID) {
        Integer count = activeConnectionGroupUserMap.get
                (new ConnectionUser(connectionGroupID, userID));
        
        // No ConnectionUser found means this combination was never used
        if(count == null)
            return 0;
        
        return count;
    }
    
    /**
     * Checks if the given user is currently connected to the given BALANCING
     * connection group.
     * 
     * @param connectionGroupID The connection group ID that this 
     *                          ConnectionUser refers to.
     * @param userID The user ID that this ConnectionUser refers to.
     * 
     * @return True if the given user is currently connected to the given 
     *         BALANCING connection group, false otherwise.
     */
    public boolean isConnectionGroupUserActive(int connectionGroupID, int userID) {
        Integer count = activeConnectionGroupUserMap.get
                (new ConnectionUser(connectionGroupID, userID));
        
        // The connection group is in use if the ConnectionUser count > 0
        return count != null && count > 0;
    }
    
    /**
     * Increment the count of the number of connections opened by the given user
     * to the given ConnectionGroup.
     * 
     * @param connectionGroupID The connection group ID that this 
     *                          ConnectionUser refers to.
     * @param userID The user ID that this ConnectionUser refers to.
     */
    private void incrementConnectionGroupUserCount(int connectionGroupID, int userID) {
        int currentCount = getConnectionGroupUserCount(connectionGroupID, userID);
        
        activeConnectionGroupUserMap.put
                (new ConnectionUser(connectionGroupID, userID), currentCount + 1);
    }
    
    /**
     * Decrement the count of the number of connections opened by the given user
     * to the given ConnectionGroup.
     * 
     * @param connectionGroupID The connection group ID that this 
     *                          ConnectionUser refers to.
     * @param userID The user ID that this ConnectionUser refers to.
     */
    private void decrementConnectionGroupUserCount(int connectionGroupID, int userID) {
        int currentCount = getConnectionGroupUserCount(connectionGroupID, userID);
        
        activeConnectionGroupUserMap.put
                (new ConnectionUser(connectionGroupID, userID), currentCount - 1);
    }
    
    /**
     * Returns the number of connections opened by the given user using 
     * the given Connection.
     * 
     * @param connectionID The connection ID that this ConnectionUser refers to.
     * @param userID The user ID that this ConnectionUser refers to.
     * 
     * @return The number of connections opened by the given user to the given
     *         connection.
     */
    public int getConnectionUserCount(int connectionID, int userID) {
        Integer count = activeConnectionUserMap.get
                (new ConnectionUser(connectionID, userID));
        
        // No ConnectionUser found means this combination was never used
        if(count == null)
            return 0;
        
        return count;
    }
    
    /**
     * Checks if the given user is currently connected to the given connection.
     * 
     * @param connectionID The connection ID that this ConnectionUser refers to.
     * @param userID The user ID that this ConnectionUser refers to.
     * 
     * @return True if the given user is currently connected to the given 
     *         connection, false otherwise.
     */
    public boolean isConnectionUserActive(int connectionID, int userID) {
        Integer count = activeConnectionUserMap.get
                (new ConnectionUser(connectionID, userID));
        
        // The connection is in use if the ConnectionUser count > 0
        return count != null && count > 0;
    }
    
    /**
     * Increment the count of the number of connections opened by the given user
     * to the given Connection.
     * 
     * @param connectionID The connection ID that this ConnectionUser refers to.
     * @param userID The user ID that this ConnectionUser refers to.
     */
    private void incrementConnectionUserCount(int connectionID, int userID) {
        int currentCount = getConnectionUserCount(connectionID, userID);
        
        activeConnectionUserMap.put
                (new ConnectionUser(connectionID, userID), currentCount + 1);
    }
    
    /**
     * Decrement the count of the number of connections opened by the given user
     * to the given Connection.
     * 
     * @param connectionID The connection ID that this ConnectionUser refers to.
     * @param userID The user ID that this ConnectionUser refers to.
     */
    private void decrementConnectionUserCount(int connectionID, int userID) {
        int currentCount = getConnectionUserCount(connectionID, userID);
        
        activeConnectionUserMap.put
                (new ConnectionUser(connectionID, userID), currentCount - 1);
    }
    
    /**
     * Returns the ID of the connection with the lowest number of current
     * active users, if found.
     * 
     * @param connectionIDs The subset of connection IDs to find the least
     *                      used connection within.
     * 
     * @return The ID of the connection with the lowest number of current
     *         active users, if found.
     */
    public Integer getLeastUsedConnection(Collection<Integer> connectionIDs) {
        
        if(connectionIDs.isEmpty())
            return null;
        
        int minUserCount = Integer.MAX_VALUE;
        Integer minConnectionID = null;
        
        for(Integer connectionID : connectionIDs) {
            Connection connection = activeConnectionMap.get(connectionID);
            
            /*
             * If the connection is not found in the map, it has not been used,
             * and therefore will be count 0.
             */
            if(connection == null) {
                minUserCount = 0;
                minConnectionID = connectionID;
            }
            // If this is the least active connection
            else if(connection.getCurrentUserCount() < minUserCount) {
                minUserCount = connection.getCurrentUserCount();
                minConnectionID = connection.getConnectionID();
            }
        }
        
        return minConnectionID;
    }
    
    /**
     * Returns the count of currently active users for the given connectionID.
     * @return the count of currently active users for the given connectionID.
     */
    public int getCurrentUserCount(int connectionID) {
        Connection connection = activeConnectionMap.get(connectionID);
        
        if(connection == null)
            return 0;
        
        return connection.getCurrentUserCount();
    }
    
    /**
     * Decrement the current user count for this Connection.
     * 
     * @param connectionID The ID of the MySQL connection that this 
     *                     Connection represents.
     * 
     * @throws GuacamoleException If the connection is not found.
     */
    private void decrementUserCount(int connectionID)
            throws GuacamoleException {
        Connection connection = activeConnectionMap.get(connectionID);
        
        if(connection == null)
            throw new GuacamoleResourceNotFoundException
                    ("Connection to decrement does not exist.");
        
        // Decrement the current user count
        connection.setCurrentUserCount(connection.getCurrentUserCount() - 1);
    }
    
    /**
     * Increment the current user count for this Connection.
     * 
     * @param connectionID The ID of the MySQL connection that this 
     *                     Connection represents.
     * 
     * @throws GuacamoleException If the connection is not found.
     */
    private void incrementUserCount(int connectionID) {
        Connection connection = activeConnectionMap.get(connectionID);
        
        // If the Connection does not exist, it should be created
        if(connection == null) {
            connection = new Connection(connectionID);
            activeConnectionMap.put(connectionID, connection);
        }
        
        // Increment the current user count
        connection.setCurrentUserCount(connection.getCurrentUserCount() + 1);
    }

    /**
     * Check if a connection is currently in use.
     * @param connectionID The connection to check the status of.
     * @return true if the connection is currently in use.
     */
    public boolean isActive(int connectionID) {
        return getCurrentUserCount(connectionID) > 0;
    }

    /**
     * Set a connection as open.
     * @param connectionID The ID of the connection that is being opened.
     * @param userID The ID of the user who is opening the connection.
     * @param connectionGroupID The ID of the BALANCING connection group that is
     *                          being connected to; null if not used.
     * @return The ID of the history record created for this open connection.
     */
    public int openConnection(int connectionID, int userID, Integer connectionGroupID) {

        // Create the connection history record
        ConnectionHistory connectionHistory = new ConnectionHistory();
        connectionHistory.setConnection_id(connectionID);
        connectionHistory.setUser_id(userID);
        connectionHistory.setStart_date(new Date());
        connectionHistoryDAO.insert(connectionHistory);

        // Increment the user count
        incrementUserCount(connectionID);
        
        // Increment the connection user count
        incrementConnectionUserCount(connectionID, userID);
        
        // If this is a connection to a BALANCING ConnectionGroup, increment the count
        if(connectionGroupID != null)
            incrementConnectionGroupUserCount(connectionGroupID, userID);

        return connectionHistory.getHistory_id();
    }

    /**
     * Set a connection as closed.
     * @param historyID The ID of the history record about the open connection.
     * @param connectionGroupID The ID of the BALANCING connection group that is
     *                          being connected to; null if not used.
     * @throws GuacamoleException If the open connection history is not found.
     */
    public void closeConnection(int historyID, Integer connectionGroupID) 
            throws GuacamoleException {

        // Get the existing history record
        ConnectionHistory connectionHistory =
                connectionHistoryDAO.selectByPrimaryKey(historyID);

        if(connectionHistory == null)
            throw new GuacamoleResourceNotFoundException("History record not found.");
        
        // Get the connection and user IDs
        int connectionID = connectionHistory.getConnection_id();
        int userID = connectionHistory.getUser_id();

        // Update the connection history record to mark that it is now closed
        connectionHistory.setEnd_date(new Date());
        connectionHistoryDAO.updateByPrimaryKey(connectionHistory);

        // Decrement the user count.
        decrementUserCount(connectionID);
        
        // Decrement the connection user count
        decrementConnectionUserCount(connectionID, userID);
        
        // If this is a connection to a BALANCING ConnectionGroup, decrement the count
        if(connectionGroupID != null)
            decrementConnectionGroupUserCount(connectionGroupID, userID);
    }
}
