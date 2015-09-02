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
 * Provides abstract and raw character write access to a stream of Guacamole
 * instructions.
 *
 * @author Michael Jumper
 */
public interface GuacamoleWriter {

    /**
     * Writes a portion of the given array of characters to the Guacamole
     * instruction stream. The portion must contain only complete Guacamole
     * instructions.
     *
     * @param chunk An array of characters containing Guacamole instructions.
     * @param off The start offset of the portion of the array to write.
     * @param len The length of the portion of the array to write.
     * @throws GuacamoleException If an error occurred while writing the
     *                            portion of the array specified.
     */
    public void write(char[] chunk, int off, int len) throws GuacamoleException;

    /**
     * Writes the entire given array of characters to the Guacamole instruction
     * stream. The array must consist only of complete Guacamole instructions.
     *
     * @param chunk An array of characters consisting only of complete
     *              Guacamole instructions.
     * @throws GuacamoleException If an error occurred while writing the
     *                            the specified array.
     */
    public void write(char[] chunk) throws GuacamoleException;

    /**
     * Writes the given fully parsed instruction to the Guacamole instruction
     * stream.
     *
     * @param instruction The Guacamole instruction to write.
     * @throws GuacamoleException If an error occurred while writing the
     *                            instruction.
     */
    public void writeInstruction(GuacamoleInstruction instruction) throws GuacamoleException;

}
