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

package org.glyptodon.guacamole.net.basic;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.basic.properties.BasicGuacamoleProperties;
import org.glyptodon.guacamole.properties.GuacamoleProperties;

/**
 * A ClassLoader implementation which finds classes within a configurable
 * directory. This directory is set within guacamole.properties.
 *
 * @author Michael Jumper
 */
public class GuacamoleClassLoader extends ClassLoader {

    /**
     * Class loader which will load classes from the classpath specified
     * in guacamole.properties.
     */
    private URLClassLoader classLoader = null;

    /**
     * Any exception that occurs while the class loader is being instantiated.
     */
    private static GuacamoleException exception = null;

    /**
     * Singleton instance of the GuacamoleClassLoader.
     */
    private static GuacamoleClassLoader instance = null;

    static {

        try {
            // Attempt to create singleton classloader which loads classes from
            // all .jar's in the lib directory defined in guacamole.properties
            instance = AccessController.doPrivileged(new PrivilegedExceptionAction<GuacamoleClassLoader>() {

                @Override
                public GuacamoleClassLoader run() throws GuacamoleException {
                    return new GuacamoleClassLoader(
                        GuacamoleProperties.getProperty(BasicGuacamoleProperties.LIB_DIRECTORY)
                    );
                }

            });
        }

        catch (PrivilegedActionException e) {
            // On error, record exception
            exception = (GuacamoleException) e.getException();
        }

    }

    /**
     * Creates a new GuacamoleClassLoader which reads classes from the given
     * directory.
     *
     * @param libDirectory The directory to load classes from.
     * @throws GuacamoleException If the file given is not a director, or if
     *                            an error occurs while constructing the URL
     *                            for the backing classloader.
     */
    private GuacamoleClassLoader(File libDirectory) throws GuacamoleException {

        // If no directory provided, just direct requests to parent classloader
        if (libDirectory == null)
            return;

        // Validate directory is indeed a directory
        if (!libDirectory.isDirectory())
            throw new GuacamoleException(libDirectory + " is not a directory.");

        // Get list of URLs for all .jar's in the lib directory
        Collection<URL> jarURLs = new ArrayList<URL>();
        for (File file : libDirectory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {

                // If it ends with .jar, accept the file
                return name.endsWith(".jar");

            }

        })) {

            try {

                // Add URL for the .jar to the jar URL list
                jarURLs.add(file.toURI().toURL());

            }
            catch (MalformedURLException e) {
                throw new GuacamoleException(e);
            }

        }

        // Set delegate classloader to new URLClassLoader which loads from the
        // .jars found above.

        URL[] urls = new URL[jarURLs.size()];
        classLoader = new URLClassLoader(
            jarURLs.toArray(urls),
            getClass().getClassLoader()
        );

    }

    /**
     * Returns an instance of a GuacamoleClassLoader which finds classes
     * within the directory configured in guacamole.properties.
     *
     * @return An instance of a GuacamoleClassLoader.
     * @throws GuacamoleException If no instance could be returned due to an
     *                            error.
     */
    public static GuacamoleClassLoader getInstance() throws GuacamoleException {

        // If instance could not be created, rethrow original exception
        if (exception != null) throw exception;

        return instance;

    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        // If no classloader, use default loader
        if (classLoader == null)
            return Class.forName(name);

        // Otherwise, delegate
        return classLoader.loadClass(name);

    }

}
