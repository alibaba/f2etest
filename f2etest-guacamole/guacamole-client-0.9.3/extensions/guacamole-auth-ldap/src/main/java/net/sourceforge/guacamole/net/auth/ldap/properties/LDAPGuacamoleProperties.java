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

package net.sourceforge.guacamole.net.auth.ldap.properties;

import org.glyptodon.guacamole.properties.IntegerGuacamoleProperty;
import org.glyptodon.guacamole.properties.StringGuacamoleProperty;


/**
 * Provides properties required for use of the LDAP authentication provider.
 * These properties will be read from guacamole.properties when the LDAP
 * authentication provider is used.
 *
 * @author Michael Jumper
 */
public class LDAPGuacamoleProperties {

    /**
     * This class should not be instantiated.
     */
    private LDAPGuacamoleProperties() {}

    /**
     * The base DN to search for Guacamole configurations.
     */
    public static final StringGuacamoleProperty LDAP_CONFIG_BASE_DN = new StringGuacamoleProperty() {

        @Override
        public String getName() { return "ldap-config-base-dn"; }

    };

    /**
     * The base DN of users. All users must be direct children of this DN,
     * varying only by LDAP_USERNAME_ATTRIBUTE.
     */
    public static final StringGuacamoleProperty LDAP_USER_BASE_DN = new StringGuacamoleProperty() {

        @Override
        public String getName() { return "ldap-user-base-dn"; }

    };

    /**
     * The attribute which identifies users. This attribute must be part of
     * each user's DN such that the concatenation of this attribute and
     * LDAP_USER_BASE_DN equals the users full DN.
     */
    public static final StringGuacamoleProperty LDAP_USERNAME_ATTRIBUTE = new StringGuacamoleProperty() {

        @Override
        public String getName() { return "ldap-username-attribute"; }

    };

    /**
     * The port on the LDAP server to connect to when authenticating users.
     */
    public static final IntegerGuacamoleProperty LDAP_PORT = new IntegerGuacamoleProperty() {

        @Override
        public String getName() { return "ldap-port"; }

    };

    /**
     * The hostname of the LDAP server to connect to when authenticating users.
     */
    public static final StringGuacamoleProperty LDAP_HOSTNAME = new StringGuacamoleProperty() {

        @Override
        public String getName() { return "ldap-hostname"; }

    };

}
