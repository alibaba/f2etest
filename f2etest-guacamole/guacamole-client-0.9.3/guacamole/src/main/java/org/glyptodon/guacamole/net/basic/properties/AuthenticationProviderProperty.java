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

import java.lang.reflect.InvocationTargetException;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.AuthenticationProvider;
import org.glyptodon.guacamole.net.basic.GuacamoleClassLoader;
import org.glyptodon.guacamole.properties.GuacamoleProperty;

/**
 * A GuacamoleProperty whose value is the name of a class to use to
 * authenticate users. This class must implement AuthenticationProvider.
 *
 * @author Michael Jumper
 */
public abstract class AuthenticationProviderProperty implements GuacamoleProperty<AuthenticationProvider> {

    @Override
    public AuthenticationProvider parseValue(String authProviderClassName) throws GuacamoleException {

        // If no property provided, return null.
        if (authProviderClassName == null)
            return null;

        // Get auth provider instance
        try {

            Object obj = GuacamoleClassLoader.getInstance().loadClass(authProviderClassName)
                            .getConstructor().newInstance();

            if (!(obj instanceof AuthenticationProvider))
                throw new GuacamoleException("Specified authentication provider class is not a AuthenticationProvider.");

            return (AuthenticationProvider) obj;

        }
        catch (ClassNotFoundException e) {
            throw new GuacamoleException("Authentication provider class not found", e);
        }
        catch (NoSuchMethodException e) {
            throw new GuacamoleException("Default constructor for authentication provider not present", e);
        }
        catch (SecurityException e) {
            throw new GuacamoleException("Creation of authentication provider disallowed; check your security settings", e);
        }
        catch (InstantiationException e) {
            throw new GuacamoleException("Unable to instantiate authentication provider", e);
        }
        catch (IllegalAccessException e) {
            throw new GuacamoleException("Unable to access default constructor of authentication provider", e);
        }
        catch (InvocationTargetException e) {
            throw new GuacamoleException("Internal error in constructor of authentication provider", e.getTargetException());
        }

    }

}

