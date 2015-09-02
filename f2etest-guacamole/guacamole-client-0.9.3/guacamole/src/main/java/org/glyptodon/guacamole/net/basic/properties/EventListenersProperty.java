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

import java.util.ArrayList;
import java.util.Collection;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.basic.GuacamoleClassLoader;
import org.glyptodon.guacamole.properties.GuacamoleProperty;

/**
 * A GuacamoleProperty whose value is a comma-separated list of class names,
 * where each class will be used as a listener for events.
 *
 * @author Michael Jumper
 */
public abstract class EventListenersProperty implements GuacamoleProperty<Collection<Class>> {

    @Override
    public Collection<Class> parseValue(String classNameList) throws GuacamoleException {

        // If no property provided, return null.
        if (classNameList == null)
            return null;

        // Parse list
        String[] classNames = classNameList.split(",[\\s]*");

        // Fill list of classes
        Collection<Class> listeners = new ArrayList<Class>();
        try {

            // Load all classes in list
            for (String className : classNames) {
                Class clazz = GuacamoleClassLoader.getInstance().loadClass(className);
                listeners.add(clazz);
            }

        }
        catch (ClassNotFoundException e) {
            throw new GuacamoleException("Listener class not found.", e);
        }
        catch (SecurityException e) {
            throw new GuacamoleException("Security settings prevent loading of listener class.", e);
        }

        return listeners;

    }

}

