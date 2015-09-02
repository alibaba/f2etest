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

/**
 * Abstract representation of the Guacamole configuration directory.
 *
 * @author Michael Jumper
 */
public class GuacamoleHome {

    /**
     * GuacamoleHome is a utility class and cannot be instantiated.
     */
    private GuacamoleHome() {}

    /**
     * Returns the Guacamole home directory by checking, in order:
     * the guacamole.home system property, the GUACAMOLE_HOME environment
     * variable, and finally the .guacamole directory in the home directory of
     * the user running the servlet container.
     *
     * @return The File representing the Guacamole home directory, which may
     *         or may not exist, and may turn out to not be a directory.
     */
    public static File getDirectory() {

        // Attempt to find Guacamole home
        File guacHome;

        // Use system property by default
        String desiredDir = System.getProperty("guacamole.home");

        // Failing that, try the GUACAMOLE_HOME environment variable
        if (desiredDir == null) desiredDir = System.getenv("GUACAMOLE_HOME");

        // If successful, use explicitly specified directory
        if (desiredDir != null)
            guacHome = new File(desiredDir);

        // If not explicitly specified, use ~/.guacamole
        else
            guacHome = new File(System.getProperty("user.home"), ".guacamole");

        // Return discovered directory
        return guacHome;

    }

}
