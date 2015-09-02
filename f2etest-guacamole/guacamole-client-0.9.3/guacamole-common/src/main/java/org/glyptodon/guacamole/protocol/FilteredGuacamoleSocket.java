/*
 * Copyright (C) 2014 Glyptodon LLC
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

package org.glyptodon.guacamole.protocol;

import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.io.GuacamoleReader;
import org.glyptodon.guacamole.io.GuacamoleWriter;
import org.glyptodon.guacamole.net.GuacamoleSocket;

/**
 * Implementation of GuacamoleSocket which allows individual instructions to be
 * intercepted, overridden, etc.
 *
 * @author Michael Jumper
 */
public class FilteredGuacamoleSocket implements GuacamoleSocket {

    /**
     * Wrapped GuacamoleSocket.
     */
    private final GuacamoleSocket socket;

    /**
     * A reader for the wrapped GuacamoleSocket which may be filtered.
     */
    private final GuacamoleReader reader;
    
    /**
     * A writer for the wrapped GuacamoleSocket which may be filtered.
     */
    private final GuacamoleWriter writer;
    
    /**
     * Creates a new FilteredGuacamoleSocket which uses the given filters to
     * determine whether instructions read/written are allowed through,
     * modified, etc. If reads or writes should be unfiltered, simply specify
     * null rather than a particular filter.
     *
     * @param socket The GuacamoleSocket to wrap.
     * @param readFilter The GuacamoleFilter to apply to all read instructions,
     *                   if any.
     * @param writeFilter The GuacamoleFilter to apply to all written 
     *                    instructions, if any.
     */
    public FilteredGuacamoleSocket(GuacamoleSocket socket, GuacamoleFilter readFilter, GuacamoleFilter writeFilter) {
        this.socket = socket;

        // Apply filter to reader
        if (readFilter != null)
            reader = new FilteredGuacamoleReader(socket.getReader(), readFilter);
        else
            reader = socket.getReader();

        // Apply filter to writer
        if (writeFilter != null)
            writer = new FilteredGuacamoleWriter(socket.getWriter(), writeFilter);
        else
            writer = socket.getWriter();

    }
    
    @Override
    public GuacamoleReader getReader() {
        return reader;
    }

    @Override
    public GuacamoleWriter getWriter() {
        return writer;
    }

    @Override
    public void close() throws GuacamoleException {
        socket.close();
    }

    @Override
    public boolean isOpen() {
        return socket.isOpen();
    }
    
}
