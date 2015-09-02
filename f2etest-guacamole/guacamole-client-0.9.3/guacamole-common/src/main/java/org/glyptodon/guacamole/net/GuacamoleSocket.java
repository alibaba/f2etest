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

package org.glyptodon.guacamole.net;


import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.io.GuacamoleReader;
import org.glyptodon.guacamole.io.GuacamoleWriter;

/**
 * Provides abstract socket-like access to a Guacamole connection.
 *
 * @author Michael Jumper
 */
public interface GuacamoleSocket {

    /**
     * Returns a GuacamoleReader which can be used to read from the
     * Guacamole instruction stream associated with the connection
     * represented by this GuacamoleSocket.
     *
     * @return A GuacamoleReader which can be used to read from the
     *         Guacamole instruction stream.
     */
    public GuacamoleReader getReader();

    /**
     * Returns a GuacamoleWriter which can be used to write to the
     * Guacamole instruction stream associated with the connection
     * represented by this GuacamoleSocket.
     *
     * @return A GuacamoleWriter which can be used to write to the
     *         Guacamole instruction stream.
     */
    public GuacamoleWriter getWriter();

    /**
     * Releases all resources in use by the connection represented by this
     * GuacamoleSocket.
     *
     * @throws GuacamoleException If an error occurs while releasing resources.
     */
    public void close() throws GuacamoleException;

    /**
     * Returns whether this GuacamoleSocket is open and can be used for reading
     * and writing.
     *
     * @return true if this GuacamoleSocket is open, false otherwise.
     */
    public boolean isOpen();

}
