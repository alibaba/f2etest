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


/**
 * A service to perform password encryption and checking.
 * @author James Muehlner
 */
public interface PasswordEncryptionService {

    /**
     * Checks whether the provided, unhashed password matches the given
     * hash/salt pair.
     *
     * @param password The unhashed password to validate.
     * @param hashedPassword The hashed password to compare the given password
     *                       against.
     * @param salt The salt used when the hashed password given was created.
     * @return true if the provided credentials match the values given, false
     *         otherwise.
     */
    public boolean checkPassword(String password, byte[] hashedPassword,
            byte[] salt);

    /**
     * Creates a password hash based on the provided username, password, and
     * salt.
     *
     * @param password The password to hash.
     * @param salt The salt to use when hashing the password.
     * @return The generated password hash.
     */
    public byte[] createPasswordHash(String password, byte[] salt);
}
