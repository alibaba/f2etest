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

import org.glyptodon.guacamole.net.auth.ConnectionGroup;
import org.glyptodon.guacamole.net.auth.permission.ObjectPermission;
import org.glyptodon.guacamole.net.auth.permission.SystemPermission;


/**
 * A set of constants that are useful for the MySQL-based authentication provider.
 * @author James Muehlner
 */
public final class MySQLConstants {

    /**
     * This class should not be instantiated.
     */
    private MySQLConstants() {}

    /**
     * The string stored in the database to represent READ access to a user.
     */
    public static final String USER_READ = "READ";

    /**
     * The string stored in the database to represent UPDATE access to a user.
     */
    public static final String USER_UPDATE = "UPDATE";

    /**
     * The string stored in the database to represent DELETE access to a user.
     */
    public static final String USER_DELETE = "DELETE";

    /**
     * The string stored in the database to represent ADMINISTER access to a
     * user.
     */
    public static final String USER_ADMINISTER = "ADMINISTER";

    /**
     * The string stored in the database to represent READ access to a
     * connection.
     */
    public static final String CONNECTION_READ = "READ";

    /**
     * The string stored in the database to represent UPDATE access to a
     * connection.
     */
    public static final String CONNECTION_UPDATE = "UPDATE";

    /**
     * The string stored in the database to represent DELETE access to a
     * connection.
     */
    public static final String CONNECTION_DELETE = "DELETE";

    /**
     * The string stored in the database to represent ADMINISTER access to a
     * connection.
     */
    public static final String CONNECTION_ADMINISTER = "ADMINISTER";

    /**
     * The string stored in the database to represent READ access to a
     * connection.
     */
    public static final String CONNECTION_GROUP_READ = "READ";

    /**
     * The string stored in the database to represent UPDATE access to a
     * connection group.
     */
    public static final String CONNECTION_GROUP_UPDATE = "UPDATE";

    /**
     * The string stored in the database to represent DELETE access to a
     * connection group.
     */
    public static final String CONNECTION_GROUP_DELETE = "DELETE";

    /**
     * The string stored in the database to represent ADMINISTER access to a
     * connection group.
     */
    public static final String CONNECTION_GROUP_ADMINISTER = "ADMINISTER";

    /**
     * The string stored in the database to represent a BALANCING
     * connection group.
     */
    public static final String CONNECTION_GROUP_BALANCING = "BALANCING";

    /**
     * The string stored in the database to represent an ORGANIZATIONAL
     * connection group.
     */
    public static final String CONNECTION_GROUP_ORGANIZATIONAL = 
            "ORGANIZATIONAL";
    
    /**
     * The identifier used to mark the root connection group.
     */
    public static final String CONNECTION_GROUP_ROOT_IDENTIFIER = "ROOT";

    /**
     * The string stored in the database to represent permission to create
     * users.
     */
    public static final String SYSTEM_USER_CREATE = "CREATE_USER";

    /**
     * The string stored in the database to represent permission to create
     * connections.
     */
    public static final String SYSTEM_CONNECTION_CREATE = "CREATE_CONNECTION";

    /**
     * The string stored in the database to represent permission to create
     * connection groups.
     */
    public static final String SYSTEM_CONNECTION_GROUP_CREATE = "CREATE_CONNECTION_GROUP";

    /**
     * The string stored in the database to represent permission to administer
     * the system as a whole.
     */
    public static final String SYSTEM_ADMINISTER = "ADMINISTER";

    /**
     * Given the type of a permission affecting a user, returns the MySQL
     * constant representing that permission type.
     *
     * @param type The type of permission to look up.
     * @return The MySQL constant corresponding to the given permission type.
     */
    public static String getUserConstant(ObjectPermission.Type type) {

        // Convert permission type to MySQL constant
        switch (type) {
            case READ:       return USER_READ;
            case UPDATE:     return USER_UPDATE;
            case ADMINISTER: return USER_ADMINISTER;
            case DELETE:     return USER_DELETE;
        }

        // If we get here, permission support was not properly implemented
        throw new UnsupportedOperationException(
            "Unsupported permission type: " + type);

    }

    /**
     * Given the type of a permission affecting a connection, returns the MySQL
     * constant representing that permission type.
     *
     * @param type The type of permission to look up.
     * @return The MySQL constant corresponding to the given permission type.
     */
    public static String getConnectionConstant(ObjectPermission.Type type) {

        // Convert permission type to MySQL constant
        switch (type) {
            case READ:       return CONNECTION_READ;
            case UPDATE:     return CONNECTION_UPDATE;
            case ADMINISTER: return CONNECTION_ADMINISTER;
            case DELETE:     return CONNECTION_DELETE;
        }

        // If we get here, permission support was not properly implemented
        throw new UnsupportedOperationException(
            "Unsupported permission type: " + type);

    }

    /**
     * Given the type of a permission affecting a connection group, 
     * returns the MySQL constant representing that permission type.
     *
     * @param type The type of permission to look up.
     * @return The MySQL constant corresponding to the given permission type.
     */
    public static String getConnectionGroupConstant(ObjectPermission.Type type) {

        // Convert permission type to MySQL constant
        switch (type) {
            case READ:       return CONNECTION_GROUP_READ;
            case UPDATE:     return CONNECTION_GROUP_UPDATE;
            case ADMINISTER: return CONNECTION_GROUP_ADMINISTER;
            case DELETE:     return CONNECTION_GROUP_DELETE;
        }

        // If we get here, permission support was not properly implemented
        throw new UnsupportedOperationException(
            "Unsupported permission type: " + type);

    }

    /**
     * Given the type of a connection group, returns the MySQL constant
     * representing that type.
     *
     * @param type The connection group type to look up.
     * @return The MySQL constant corresponding to the given type.
     */
    public static String getConnectionGroupTypeConstant(ConnectionGroup.Type type) {

        // Convert permission type to MySQL constant
        switch (type) {
            case ORGANIZATIONAL: return CONNECTION_GROUP_ORGANIZATIONAL;
            case BALANCING:      return CONNECTION_GROUP_BALANCING;
        }

        // If we get here, permission support was not properly implemented
        throw new UnsupportedOperationException(
            "Unsupported connection group type: " + type);

    }

    /**
     * Given the type of a permission affecting the system, returns the MySQL
     * constant representing that permission type.
     *
     * @param type The type of permission to look up.
     * @return The MySQL constant corresponding to the given permission type.
     */
    public static String getSystemConstant(SystemPermission.Type type) {

        // Convert permission type to MySQL constant
        switch (type) {
            case CREATE_USER:             return SYSTEM_USER_CREATE;
            case CREATE_CONNECTION:       return SYSTEM_CONNECTION_CREATE;
            case CREATE_CONNECTION_GROUP: return SYSTEM_CONNECTION_GROUP_CREATE;
            case ADMINISTER:              return SYSTEM_ADMINISTER;
        }

        // If we get here, permission support was not properly implemented
        throw new UnsupportedOperationException(
            "Unsupported permission type: " + type);

    }

}
