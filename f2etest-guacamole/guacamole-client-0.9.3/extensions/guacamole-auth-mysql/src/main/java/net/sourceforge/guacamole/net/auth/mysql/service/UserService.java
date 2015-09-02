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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.Credentials;
import net.sourceforge.guacamole.net.auth.mysql.MySQLUser;
import net.sourceforge.guacamole.net.auth.mysql.dao.UserMapper;
import net.sourceforge.guacamole.net.auth.mysql.model.User;
import net.sourceforge.guacamole.net.auth.mysql.model.UserExample;
import net.sourceforge.guacamole.net.auth.mysql.model.UserWithBLOBs;

/**
 * Service which provides convenience methods for creating, retrieving, and
 * manipulating users.
 *
 * @author Michael Jumper, James Muehlner
 */
public class UserService {

    /**
     * DAO for accessing users.
     */
    @Inject
    private UserMapper userDAO;

    /**
     * Provider for creating users.
     */
    @Inject
    private Provider<MySQLUser> mySQLUserProvider;

    /**
     * Service for checking permissions.
     */
    @Inject
    private PermissionCheckService permissionCheckService;

    /**
     * Service for encrypting passwords.
     */
    @Inject
    private PasswordEncryptionService passwordService;

    /**
     * Service for generating random salts.
     */
    @Inject
    private SaltService saltService;

    /**
     * Create a new MySQLUser based on the provided User.
     *
     * @param user The User to use when populating the data of the given
     *             MySQLUser.
     * @return A new MySQLUser object, populated with the data of the given
     *         user.
     *
     * @throws GuacamoleException If an error occurs while reading the data
     *                            of the provided User.
     */
    public MySQLUser toMySQLUser(org.glyptodon.guacamole.net.auth.User user) throws GuacamoleException {
        MySQLUser mySQLUser = mySQLUserProvider.get();
        mySQLUser.init(user);
        return mySQLUser;
    }

    /**
     * Create a new MySQLUser based on the provided database record.
     *
     * @param user The database record describing the user.
     * @return A new MySQLUser object, populated with the data of the given
     *         database record.
     */
    private MySQLUser toMySQLUser(UserWithBLOBs user) {

        // Retrieve user from provider
        MySQLUser mySQLUser = mySQLUserProvider.get();

        // Init with data from given database user
        mySQLUser.init(
            user.getUser_id(),
            user.getUsername(),
            null,
            permissionCheckService.retrieveAllPermissions(user.getUser_id())
        );

        // Return new user
        return mySQLUser;

    }

    /**
     * Retrieves the user having the given ID from the database.
     *
     * @param id The ID of the user to retrieve.
     * @return The existing MySQLUser object if found, null otherwise.
     */
    public MySQLUser retrieveUser(int id) {

        // Query user by ID
        UserWithBLOBs user = userDAO.selectByPrimaryKey(id);

        // If no user found, return null
        if(user == null)
            return null;

        // Otherwise, return found user
        return toMySQLUser(user);

    }

    /**
     * Retrieves the user having the given username from the database.
     *
     * @param name The username of the user to retrieve.
     * @return The existing MySQLUser object if found, null otherwise.
     */
    public MySQLUser retrieveUser(String name) {

        // Query user by ID
        UserExample example = new UserExample();
        example.createCriteria().andUsernameEqualTo(name);
        List<UserWithBLOBs> users = userDAO.selectByExampleWithBLOBs(example);

        // If no user found, return null
        if(users.isEmpty())
            return null;

        // Otherwise, return found user
        return toMySQLUser(users.get(0));

    }

    /**
     * Retrieves the user corresponding to the given credentials from the
     * database.
     *
     * @param credentials The credentials to use when locating the user.
     * @return The existing MySQLUser object if the credentials given are
     *         valid, null otherwise.
     */
    public MySQLUser retrieveUser(Credentials credentials) {

        // No null users in database
        if (credentials.getUsername() == null)
            return null;

        // Query user
        UserExample userExample = new UserExample();
        userExample.createCriteria().andUsernameEqualTo(credentials.getUsername());
        List<UserWithBLOBs> users = userDAO.selectByExampleWithBLOBs(userExample);

        // Check that a user was found
        if (users.isEmpty())
            return null;

        // Assert only one user found
        assert users.size() == 1 : "Multiple users with same username.";

        // Get first (and only) user
        UserWithBLOBs user = users.get(0);

        // Check password, if invalid return null
        if (!passwordService.checkPassword(credentials.getPassword(),
                user.getPassword_hash(), user.getPassword_salt()))
            return null;

        // Return found user
        return toMySQLUser(user);

    }

    /**
     * Retrieves a translation map of usernames to their corresponding IDs.
     *
     * @param ids The IDs of the users to retrieve the usernames of.
     * @return A map containing the names of all users and their corresponding
     *         IDs.
     */
    public Map<String, Integer> translateUsernames(List<Integer> ids) {

        // If no IDs given, just return empty map
        if (ids.isEmpty())
            return Collections.EMPTY_MAP;

        // Map of all names onto their corresponding IDs
        Map<String, Integer> names = new HashMap<String, Integer>();

        // Get all users having the given IDs
        UserExample example = new UserExample();
        example.createCriteria().andUser_idIn(ids);
        List<User> users =
                userDAO.selectByExample(example);

        // Produce set of names
        for (User user : users)
            names.put(user.getUsername(), user.getUser_id());

        return names;

    }

    /**
     * Retrieves a map of all usernames for the given IDs.
     *
     * @param ids The IDs of the users to retrieve the usernames of.
     * @return A map containing the names of all users and their corresponding
     *         IDs.
     */
    public Map<Integer, String> retrieveUsernames(Collection<Integer> ids) {

        // If no IDs given, just return empty map
        if (ids.isEmpty())
            return Collections.EMPTY_MAP;

        // Map of all names onto their corresponding IDs
        Map<Integer, String> names = new HashMap<Integer, String>();

        // Get all users having the given IDs
        UserExample example = new UserExample();
        example.createCriteria().andUser_idIn(Lists.newArrayList(ids));
        List<User> users =
                userDAO.selectByExample(example);

        // Produce set of names
        for (User user : users)
            names.put(user.getUser_id(), user.getUsername());

        return names;

    }

    /**
     * Creates a new user having the given username and password.
     *
     * @param username The username to assign to the new user.
     * @param password The password to assign to the new user.
     * @return A new MySQLUser containing the data of the newly created
     *         user.
     */
    public MySQLUser createUser(String username, String password) {

        // Initialize database user
        UserWithBLOBs user = new UserWithBLOBs();
        user.setUsername(username);

        // Set password if specified
        if (password != null) {
            byte[] salt = saltService.generateSalt();
            user.setPassword_salt(salt);
            user.setPassword_hash(
                passwordService.createPasswordHash(password, salt));
        }

        // Create user
        userDAO.insert(user);
        return toMySQLUser(user);

    }

    /**
     * Deletes the user having the given ID from the database.
     * @param user_id The ID of the user to delete.
     */
    public void deleteUser(int user_id) {
        userDAO.deleteByPrimaryKey(user_id);
    }

    /**
     * Updates the user in the database corresponding to the given MySQLUser.
     *
     * @param mySQLUser The MySQLUser to update (save) to the database. This
     *                  user must already exist.
     */
    public void updateUser(MySQLUser mySQLUser) {

        UserWithBLOBs user = new UserWithBLOBs();
        user.setUser_id(mySQLUser.getUserID());
        user.setUsername(mySQLUser.getUsername());

        // Set password if specified
        if (mySQLUser.getPassword() != null) {
            byte[] salt = saltService.generateSalt();
            user.setPassword_salt(salt);
            user.setPassword_hash(
                passwordService.createPasswordHash(mySQLUser.getPassword(), salt));
        }

        // Update the user in the database
        userDAO.updateByPrimaryKeySelective(user);

    }

    /**
     * Get the usernames of all the users defined in the system.
     *
     * @return A Set of usernames of all the users defined in the system.
     */
    public Set<String> getAllUsernames() {

        // Set of all present usernames
        Set<String> usernames = new HashSet<String>();

        // Query all usernames
        List<User> users =
                userDAO.selectByExample(new UserExample());
        for (User user : users)
            usernames.add(user.getUsername());

        return usernames;

    }

    /**
     * Get the user IDs of all the users defined in the system.
     *
     * @return A list of user IDs of all the users defined in the system.
     */
    public List<Integer> getAllUserIDs() {

        // Set of all present user IDs
        List<Integer> userIDs = new ArrayList<Integer>();

        // Query all user IDs
        List<User> users =
                userDAO.selectByExample(new UserExample());
        for (User user : users)
            userIDs.add(user.getUser_id());

        return userIDs;

    }

}
