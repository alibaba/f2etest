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

package net.sourceforge.guacamole.net.auth.mysql;


import com.google.inject.Inject;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.io.GuacamoleReader;
import org.glyptodon.guacamole.io.GuacamoleWriter;
import org.glyptodon.guacamole.net.GuacamoleSocket;

/**
 * A MySQL specific wrapper around a ConfiguredGuacamoleSocket.
 * @author James Muehlner
 */
public class MySQLGuacamoleSocket implements GuacamoleSocket {

    /**
     * Injected ActiveConnectionMap which will contain all active connections.
     */
    @Inject
    private ActiveConnectionMap activeConnectionMap;

    /**
     * The wrapped socket.
     */
    private GuacamoleSocket socket;

    /**
     * The ID of the history record associated with this instance of the
     * connection.
     */
    private int historyID;

    /**
     * The ID of the balancing connection group that is being connected to; 
     * null if not used.
     */
    private Integer connectionGroupID;

    /**
     * Initialize this MySQLGuacamoleSocket with the provided GuacamoleSocket.
     *
     * @param socket The ConfiguredGuacamoleSocket to wrap.
     * @param historyID The ID of the history record associated with this
     *                  instance of the connection.
     * @param connectionGroupID The ID of the balancing connection group that is
     *                          being connected to; null if not used.
     */
    public void init(GuacamoleSocket socket,
            int historyID, Integer connectionGroupID) {
        this.socket = socket;
        this.historyID = historyID;
        this.connectionGroupID = connectionGroupID;
    }

    @Override
    public GuacamoleReader getReader() {
        return socket.getReader();
    }

    @Override
    public GuacamoleWriter getWriter() {
        return socket.getWriter();
    }

    @Override
    public void close() throws GuacamoleException {

        // Mark this connection as inactive
        synchronized (activeConnectionMap) {

            if (isOpen())
                activeConnectionMap.closeConnection(historyID, connectionGroupID);

            // Close socket
            socket.close();

        }

    }

    @Override
    public boolean isOpen() {
        return socket.isOpen();
    }
}
