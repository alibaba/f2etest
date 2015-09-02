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

package org.glyptodon.guacamole.io;


import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.protocol.GuacamoleInstruction;

/**
 * Provides abstract and raw character read access to a stream of Guacamole
 * instructions.
 *
 * @author Michael Jumper
 */
public interface GuacamoleReader {

    /**
     * Returns whether instruction data is available for reading. Note that
     * this does not guarantee an entire instruction is available. If a full
     * instruction is not available, this function can return true, and a call
     * to read() will still block.
     *
     * @return true if instruction data is available for reading, false
     *         otherwise.
     * @throws GuacamoleException If an error occurs while checking for
     *                            available data.
     */
    public boolean available() throws GuacamoleException;

    /**
     * Reads at least one complete Guacamole instruction, returning a buffer
     * containing one or more complete Guacamole instructions and no
     * incomplete Guacamole instructions. This function will block until at
     * least one complete instruction is available.
     *
     * @return A buffer containing at least one complete Guacamole instruction,
     *         or null if no more instructions are available for reading.
     * @throws GuacamoleException If an error occurs while reading from the
     *                            stream.
     */
    public char[] read() throws GuacamoleException;

    /**
     * Reads exactly one complete Guacamole instruction and returns the fully
     * parsed instruction.
     *
     * @return The next complete instruction from the stream, fully parsed, or
     *         null if no more instructions are available for reading.
     * @throws GuacamoleException If an error occurs while reading from the
     *                            stream, or if the instruction cannot be
     *                            parsed.
     */
    public GuacamoleInstruction readInstruction() throws GuacamoleException;

}
