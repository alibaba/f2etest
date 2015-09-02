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
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.ConnectionGroup;
import org.glyptodon.guacamole.net.auth.Directory;
import org.glyptodon.guacamole.net.auth.User;
import org.glyptodon.guacamole.net.auth.UserContext;
import net.sourceforge.guacamole.net.auth.mysql.service.UserService;

/**
 * The MySQL representation of a UserContext.
 * @author James Muehlner
 */
public class MySQLUserContext implements UserContext {

    /**
     * The ID of the user owning this context. The permissions of this user
     * dictate the access given via the user and connection directories.
     */
    private int user_id;

    /**
     * User directory restricted by the permissions of the user associated
     * with this context.
     */
    @Inject
    private UserDirectory userDirectory;
    
    /**
     * The root connection group.
     */
    @Inject
    private MySQLConnectionGroup rootConnectionGroup;

    /**
     * Service for accessing users.
     */
    @Inject
    private UserService userService;

    /**
     * Initializes the user and directories associated with this context.
     *
     * @param user_id The ID of the user owning this context.
     */
    public void init(int user_id) {
        this.user_id = user_id;
        userDirectory.init(user_id);
        rootConnectionGroup.init(null, null, 
                MySQLConstants.CONNECTION_GROUP_ROOT_IDENTIFIER, 
                MySQLConstants.CONNECTION_GROUP_ROOT_IDENTIFIER, 
                ConnectionGroup.Type.ORGANIZATIONAL, user_id);
    }

    @Override
    public User self() {
        return userService.retrieveUser(user_id);
    }

    @Override
    public Directory<String, User> getUserDirectory() throws GuacamoleException {
        return userDirectory;
    }

    @Override
    public ConnectionGroup getRootConnectionGroup() throws GuacamoleException {
        return rootConnectionGroup;
    }

}
