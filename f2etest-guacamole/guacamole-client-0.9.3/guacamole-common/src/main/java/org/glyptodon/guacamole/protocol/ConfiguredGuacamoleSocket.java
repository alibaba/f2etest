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

package org.glyptodon.guacamole.protocol;


import java.util.List;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.io.GuacamoleReader;
import org.glyptodon.guacamole.io.GuacamoleWriter;
import org.glyptodon.guacamole.net.GuacamoleSocket;

/**
 * A GuacamoleSocket which pre-configures the connection based on a given
 * GuacamoleConfiguration, completing the initial protocol handshake before
 * accepting data for read or write.
 *
 * This is useful for forcing a connection to the Guacamole proxy server with
 * a specific configuration while disallowing the client that will be using
 * this GuacamoleSocket from manually controlling the initial protocol
 * handshake.
 *
 * @author Michael Jumper
 */
public class ConfiguredGuacamoleSocket implements GuacamoleSocket {

    /**
     * The wrapped socket.
     */
    private GuacamoleSocket socket;

    /**
     * The configuration to use when performing the Guacamole protocol
     * handshake.
     */
    private GuacamoleConfiguration config;

    /**
     * The unique identifier associated with this connection, as determined
     * by the "ready" instruction received from the Guacamole proxy.
     */
    private String id;
    
    /**
     * Waits for the instruction having the given opcode, returning that
     * instruction once it has been read. If the instruction is never read,
     * an exception is thrown.
     * 
     * @param reader The reader to read instructions from.
     * @param opcode The opcode of the instruction we are expecting.
     * @return The instruction having the given opcode.
     * @throws GuacamoleException If an error occurs while reading, or if
     *                            the expected instruction is not read.
     */
    private GuacamoleInstruction expect(GuacamoleReader reader, String opcode)
        throws GuacamoleException {

        // Wait for an instruction
        GuacamoleInstruction instruction = reader.readInstruction();
        if (instruction == null)
            throw new GuacamoleServerException("End of stream while waiting for \"" + opcode + "\".");

        // Ensure instruction has expected opcode
        if (!instruction.getOpcode().equals(opcode))
            throw new GuacamoleServerException("Expected \"" + opcode + "\" instruction but instead received \"" + instruction.getOpcode() + "\".");

        return instruction;

    }
 
    /**
     * Creates a new ConfiguredGuacamoleSocket which uses the given
     * GuacamoleConfiguration to complete the initial protocol handshake over
     * the given GuacamoleSocket. A default GuacamoleClientInformation object
     * is used to provide basic client information.
     *
     * @param socket The GuacamoleSocket to wrap.
     * @param config The GuacamoleConfiguration to use to complete the initial
     *               protocol handshake.
     * @throws GuacamoleException If an error occurs while completing the
     *                            initial protocol handshake.
     */
    public ConfiguredGuacamoleSocket(GuacamoleSocket socket,
            GuacamoleConfiguration config) throws GuacamoleException {
        this(socket, config, new GuacamoleClientInformation());
    }

    /**
     * Creates a new ConfiguredGuacamoleSocket which uses the given
     * GuacamoleConfiguration and GuacamoleClientInformation to complete the
     * initial protocol handshake over the given GuacamoleSocket.
     *
     * @param socket The GuacamoleSocket to wrap.
     * @param config The GuacamoleConfiguration to use to complete the initial
     *               protocol handshake.
     * @param info The GuacamoleClientInformation to use to complete the initial
     *             protocol handshake.
     * @throws GuacamoleException If an error occurs while completing the
     *                            initial protocol handshake.
     */
    public ConfiguredGuacamoleSocket(GuacamoleSocket socket,
            GuacamoleConfiguration config,
            GuacamoleClientInformation info) throws GuacamoleException {

        this.socket = socket;
        this.config = config;

        // Get reader and writer
        GuacamoleReader reader = socket.getReader();
        GuacamoleWriter writer = socket.getWriter();

        // Get protocol / connection ID
        String select_arg = config.getConnectionID();
        if (select_arg == null)
            select_arg = config.getProtocol();

        // Send requested protocol or connection ID
        writer.writeInstruction(new GuacamoleInstruction("select", select_arg));

        // Wait for server args
        GuacamoleInstruction args = expect(reader, "args");

        // Build args list off provided names and config
        List<String> arg_names = args.getArgs();
        String[] arg_values = new String[arg_names.size()];
        for (int i=0; i<arg_names.size(); i++) {

            // Retrieve argument name
            String arg_name = arg_names.get(i);

            // Get defined value for name
            String value = config.getParameter(arg_name);

            // If value defined, set that value
            if (value != null) arg_values[i] = value;

            // Otherwise, leave value blank
            else arg_values[i] = "";

        }

        // Send size
        writer.writeInstruction(
            new GuacamoleInstruction(
                "size",
                Integer.toString(info.getOptimalScreenWidth()),
                Integer.toString(info.getOptimalScreenHeight()),
                Integer.toString(info.getOptimalResolution())
            )
        );

        // Send supported audio formats
        writer.writeInstruction(
                new GuacamoleInstruction(
                    "audio",
                    info.getAudioMimetypes().toArray(new String[0])
                ));

        // Send supported video formats
        writer.writeInstruction(
                new GuacamoleInstruction(
                    "video",
                    info.getVideoMimetypes().toArray(new String[0])
                ));

        // Send args
        writer.writeInstruction(new GuacamoleInstruction("connect", arg_values));

        // Wait for ready, store ID
        GuacamoleInstruction ready = expect(reader, "ready");

        List<String> ready_args = ready.getArgs();
        if (ready_args.isEmpty())
            throw new GuacamoleServerException("No connection ID received");

        id = ready.getArgs().get(0);

    }

    /**
     * Returns the GuacamoleConfiguration used to configure this
     * ConfiguredGuacamoleSocket.
     *
     * @return The GuacamoleConfiguration used to configure this
     *         ConfiguredGuacamoleSocket.
     */
    public GuacamoleConfiguration getConfiguration() {
        return config;
    }

    /**
     * Returns the unique ID associated with the Guacamole connection
     * negotiated by this ConfiguredGuacamoleSocket. The ID is provided by
     * the "ready" instruction returned by the Guacamole proxy.
     * 
     * @return The ID of the negotiated Guacamole connection.
     */
    public String getConnectionID() {
        return id;
    }

    @Override
    public GuacamoleWriter getWriter() {
        return socket.getWriter();
    }

    @Override
    public GuacamoleReader getReader() {
        return socket.getReader();
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
