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

package org.glyptodon.guacamole.net.basic.websocket.tomcat;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import javax.servlet.http.HttpServletRequest;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.io.GuacamoleReader;
import org.glyptodon.guacamole.io.GuacamoleWriter;
import org.glyptodon.guacamole.net.GuacamoleTunnel;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;
import org.glyptodon.guacamole.GuacamoleClientException;
import org.glyptodon.guacamole.protocol.GuacamoleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A WebSocketServlet partial re-implementation of GuacamoleTunnelServlet.
 *
 * @author Michael Jumper
 */
public abstract class GuacamoleWebSocketTunnelServlet extends WebSocketServlet {

    /**
     * The default, minimum buffer size for instructions.
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * Logger for this class.
     */
    private final Logger logger = LoggerFactory.getLogger(GuacamoleWebSocketTunnelServlet.class);

    /**
     * Sends the given status on the given WebSocket connection and closes the
     * connection.
     *
     * @param outbound The outbound WebSocket connection to close.
     * @param guac_status The status to send.
     * @throws IOException If an error prevents proper closure of the WebSocket
     *                     connection.
     */
    public static void closeConnection(WsOutbound outbound,
            GuacamoleStatus guac_status) throws IOException {

        byte[] message = Integer.toString(guac_status.getGuacamoleStatusCode()).getBytes("UTF-8");
        outbound.close(guac_status.getWebSocketCode(), ByteBuffer.wrap(message));

    }

    @Override
    public StreamInbound createWebSocketInbound(String protocol, HttpServletRequest request) {

        // Get tunnel
        final GuacamoleTunnel tunnel;

        try {
            tunnel = doConnect(request);
        }
        catch (GuacamoleException e) {
            logger.error("Error connecting WebSocket tunnel.", e);
            return null;
        }

        // Return new WebSocket which communicates through tunnel
        return new StreamInbound() {

            @Override
            protected void onTextData(Reader reader) throws IOException {

                GuacamoleWriter writer = tunnel.acquireWriter();

                // Write all available data
                try {

                    char[] buffer = new char[BUFFER_SIZE];

                    int num_read;
                    while ((num_read = reader.read(buffer)) > 0)
                        writer.write(buffer, 0, num_read);

                }
                catch (GuacamoleException e) {
                    logger.debug("Tunnel write failed.", e);
                }

                tunnel.releaseWriter();
            }

            @Override
            public void onOpen(final WsOutbound outbound) {

                Thread readThread = new Thread() {

                    @Override
                    public void run() {

                        StringBuilder buffer = new StringBuilder(BUFFER_SIZE);
                        GuacamoleReader reader = tunnel.acquireReader();
                        char[] readMessage;

                        try {

                            try {

                                // Attempt to read
                                while ((readMessage = reader.read()) != null) {

                                    // Buffer message
                                    buffer.append(readMessage);

                                    // Flush if we expect to wait or buffer is getting full
                                    if (!reader.available() || buffer.length() >= BUFFER_SIZE) {
                                        outbound.writeTextMessage(CharBuffer.wrap(buffer));
                                        buffer.setLength(0);
                                    }

                                }

                                // No more data
                                closeConnection(outbound, GuacamoleStatus.SUCCESS);

                            }

                            // Catch any thrown guacamole exception and attempt
                            // to pass within the WebSocket connection, logging
                            // each error appropriately.
                            catch (GuacamoleClientException e) {
                                logger.warn("Client request rejected: {}", e.getMessage());
                                closeConnection(outbound, e.getStatus());
                            }
                            catch (GuacamoleException e) {
                                logger.error("Internal server error.", e);
                                closeConnection(outbound, e.getStatus());
                            }

                        }
                        catch (IOException e) {
                            logger.debug("I/O error prevents further reads.", e);
                        }

                    }

                };

                readThread.start();

            }

            @Override
            public void onClose(int i) {
                try {
                    tunnel.close();
                }
                catch (GuacamoleException e) {
                    logger.debug("Unable to close WebSocket tunnel.", e);
                }
            }

            @Override
            protected void onBinaryData(InputStream in) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        };

    }

    protected abstract GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException;

}

