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

package org.glyptodon.guacamole.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleServerException;

/**
 * Simple utility class for reading properties from the guacamole.properties
 * file. The guacamole.properties file is preferably located in the servlet
 * container's user's home directory, in a subdirectory called .guacamole, or
 * in the directory set by the system property: guacamole.home.
 *
 * If none of those locations are possible, guacamole.properties will also
 * be read from the root of the classpath.
 *
 * @author Michael Jumper
 */
public class GuacamoleProperties {

    /**
     * GuacamoleProperties is a utility class and cannot be instantiated.
     */
    private GuacamoleProperties() {}

    /**
     * The hostname of the server where guacd (the Guacamole proxy server) is
     * running.
     */
    public static final StringGuacamoleProperty GUACD_HOSTNAME = new StringGuacamoleProperty() {

        @Override
        public String getName() { return "guacd-hostname"; }

    };

    /**
     * The port that guacd (the Guacamole proxy server) is listening on.
     */
    public static final IntegerGuacamoleProperty GUACD_PORT = new IntegerGuacamoleProperty() {

        @Override
        public String getName() { return "guacd-port"; }

    };

    /**
     * Whether guacd requires SSL/TLS on connections.
     */
    public static final BooleanGuacamoleProperty GUACD_SSL = new BooleanGuacamoleProperty() {

        @Override
        public String getName() { return "guacd-ssl"; }

    };

    /**
     * All properties read from guacamole.properties when this class was first
     * used.
     */
    private static final Properties properties;

    /**
     * Any error encountered when reading guacamole.properties was last
     * attempted.
     */
    private static GuacamoleException exception;

    static {

        properties = new Properties();

        try {

            // Attempt to find Guacamole home
            File guacHome = GuacamoleHome.getDirectory();

            InputStream stream;

            // If not a directory, load from classpath
            if (!guacHome.isDirectory()) {

                // Read from classpath
                stream = GuacamoleProperties.class.getResourceAsStream("/guacamole.properties");
                if (stream == null)
                    throw new IOException(
                        "guacamole.properties not loaded from " + guacHome
                      + " (not a directory), and guacamole.properties could"
                      + " not be found as a resource in the classpath.");

            }

            // Otherwise, try to load from file
            else
                stream = new FileInputStream(new File(guacHome, "guacamole.properties"));

            // Load properties, always close stream
            try { properties.load(stream); }
            finally { stream.close(); }

        }
        catch (IOException e) {
            exception = new GuacamoleServerException("Error reading guacamole.properties", e);
        }

    }

    /**
     * Given a GuacamoleProperty, parses and returns the value set for that
     * property in guacamole.properties, if any.
     *
     * @param <Type> The type that the given property is parsed into.
     * @param property The property to read from guacamole.properties.
     * @return The parsed value of the property as read from
     *         guacamole.properties.
     * @throws GuacamoleException If an error occurs while parsing the value
     *                            for the given property in
     *                            guacamole.properties.
     */
    public static <Type> Type getProperty(GuacamoleProperty<Type> property) throws GuacamoleException {

        if (exception != null)
            throw exception;

        return property.parseValue(properties.getProperty(property.getName()));

    }

    /**
     * Given a GuacamoleProperty, parses and returns the value set for that
     * property in guacamole.properties, if any. If no value is found, the
     * provided default value is returned.
     *
     * @param <Type> The type that the given property is parsed into.
     * @param property The property to read from guacamole.properties.
     * @param defaultValue The value to return if no value was given in
     *                     guacamole.properties.
     * @return The parsed value of the property as read from
     *         guacamole.properties, or the provided default value if no value
     *         was found.
     * @throws GuacamoleException If an error occurs while parsing the value
     *                            for the given property in
     *                            guacamole.properties.
     */
    public static <Type> Type getProperty(GuacamoleProperty<Type> property,
            Type defaultValue) throws GuacamoleException {

        Type value = getProperty(property);
        if (value == null)
            return defaultValue;

        return value;

    }

    /**
     * Given a GuacamoleProperty, parses and returns the value set for that
     * property in guacamole.properties. An exception is thrown if the value
     * is not provided.
     *
     * @param <Type> The type that the given property is parsed into.
     * @param property The property to read from guacamole.properties.
     * @return The parsed value of the property as read from
     *         guacamole.properties.
     * @throws GuacamoleException If an error occurs while parsing the value
     *                            for the given property in
     *                            guacamole.properties, or if the property is
     *                            not specified.
     */
    public static <Type> Type getRequiredProperty(GuacamoleProperty<Type> property)
            throws GuacamoleException {

        Type value = getProperty(property);
        if (value == null)
            throw new GuacamoleServerException("Property " + property.getName() + " is required.");

        return value;

    }
}
