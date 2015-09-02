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

package org.glyptodon.guacamole.net.basic.crud.connections;

import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.Connection;
import org.glyptodon.guacamole.net.auth.ConnectionGroup;
import org.glyptodon.guacamole.net.auth.Directory;
import org.glyptodon.guacamole.net.auth.UserContext;

/**
 * A class that provides helper methods for the Connection CRUD servlets.
 * 
 * @author James Muehlner
 */
class ConnectionUtility {
    
    // This class should not be instantiated
    private ConnectionUtility() {}
    
    /**
     * Get the ConnectionDirectory with the parent connection group specified by
     * parentID.
     * 
     * @param context The UserContext to search for the connection directory.
     * @param parentID The ID of the parent connection group to search for.
     * 
     * @return The ConnectionDirectory with the parent connection group,
     *         if found.
     * @throws GuacamoleException If an error is encountered while getting the
     *                            connection directory.
     */
    static Directory<String, Connection> findConnectionDirectory(
            UserContext context, String parentID) throws GuacamoleException {
        
        // Find the correct connection directory
        ConnectionGroup rootGroup = context.getRootConnectionGroup();
        Directory<String, Connection> directory;
        
        Directory<String, ConnectionGroup> connectionGroupDirectory = 
            rootGroup.getConnectionGroupDirectory();

        ConnectionGroup parentGroup = connectionGroupDirectory.get(parentID);

        if(parentGroup == null)
            return null;

        directory = parentGroup.getConnectionDirectory();
        
        return directory;
    }
}
