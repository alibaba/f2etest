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

package org.glyptodon.guacamole.net.basic.properties;

import org.glyptodon.guacamole.properties.BooleanGuacamoleProperty;
import org.glyptodon.guacamole.properties.FileGuacamoleProperty;
import org.glyptodon.guacamole.properties.LongGuacamoleProperty;

/**
 * Properties used by the default Guacamole web application.
 *
 * @author Michael Jumper
 */
public class BasicGuacamoleProperties {

    /**
     * This class should not be instantiated.
     */
    private BasicGuacamoleProperties() {}

    /**
     * The authentication provider to user when retrieving the authorized
     * configurations of a user.
     */
    public static final AuthenticationProviderProperty AUTH_PROVIDER = new AuthenticationProviderProperty() {

        @Override
        public String getName() { return "auth-provider"; }

    };

    /**
     * Whether HTTP "Authorization" headers should be taken into account when
     * authenticating the user. By default, "Authorization" headers are
     * ignored.
     */
    public static final BooleanGuacamoleProperty ENABLE_HTTP_AUTH = new BooleanGuacamoleProperty() {

        @Override
        public String getName() { return "enable-http-auth"; }

    };

    /**
     * The directory to search for authentication provider classes.
     */
    public static final FileGuacamoleProperty LIB_DIRECTORY = new FileGuacamoleProperty() {

        @Override
        public String getName() { return "lib-directory"; }

    };

    /**
     * The comma-separated list of all classes to use as event listeners.
     */
    public static final EventListenersProperty EVENT_LISTENERS = new EventListenersProperty() {

        @Override
        public String getName() { return "event-listeners"; }

    };

    /**
     * The session timeout for the API, in milliseconds.
     */
    public static final LongGuacamoleProperty API_SESSION_TIMEOUT = new LongGuacamoleProperty() {

        @Override
        public String getName() { return "api-session-timeout"; }

    };

}
