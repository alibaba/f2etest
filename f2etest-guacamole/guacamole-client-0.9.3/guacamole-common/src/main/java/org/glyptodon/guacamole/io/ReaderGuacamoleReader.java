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


import java.io.IOException;
import java.io.Reader;
import java.net.SocketTimeoutException;
import java.util.Deque;
import java.util.LinkedList;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.GuacamoleUpstreamTimeoutException;
import org.glyptodon.guacamole.protocol.GuacamoleInstruction;

/**
 * A GuacamoleReader which wraps a standard Java Reader, using that Reader as
 * the Guacamole instruction stream.
 *
 * @author Michael Jumper
 */
public class ReaderGuacamoleReader implements GuacamoleReader {

    /**
     * Wrapped Reader to be used for all input.
     */
    private Reader input;

    /**
     * Creates a new ReaderGuacamoleReader which will use the given Reader as
     * the Guacamole instruction stream.
     *
     * @param input The Reader to use as the Guacamole instruction stream.
     */
    public ReaderGuacamoleReader(Reader input) {
        this.input = input;
    }

    /**
     * The location within the received data buffer that parsing should begin
     * when more data is read.
     */
    private int parseStart;

    /**
     * The buffer holding all received, unparsed data.
     */
    private char[] buffer = new char[20480];

    /**
     * The number of characters currently used within the data buffer. All
     * other characters within the buffer are free space available for
     * future reads.
     */
    private int usedLength = 0;

    @Override
    public boolean available() throws GuacamoleException {
        try {
            return input.ready() || usedLength != 0;
        }
        catch (IOException e) {
            throw new GuacamoleServerException(e);
        }
    }

    @Override
    public char[] read() throws GuacamoleException {

        try {

            // While we're blocking, or input is available
            for (;;) {

                // Length of element
                int elementLength = 0;

                // Resume where we left off
                int i = parseStart;

                // Parse instruction in buffer
                while (i < usedLength) {

                    // Read character
                    char readChar = buffer[i++];

                    // If digit, update length
                    if (readChar >= '0' && readChar <= '9')
                        elementLength = elementLength * 10 + readChar - '0';

                    // If not digit, check for end-of-length character
                    else if (readChar == '.') {

                        // Check if element present in buffer
                        if (i + elementLength < usedLength) {

                            // Get terminator
                            char terminator = buffer[i + elementLength];

                            // Move to character after terminator
                            i += elementLength + 1;

                            // Reset length
                            elementLength = 0;

                            // Continue here if necessary
                            parseStart = i;

                            // If terminator is semicolon, we have a full
                            // instruction.
                            if (terminator == ';') {

                                // Copy instruction data
                                char[] instruction = new char[i];
                                System.arraycopy(buffer, 0, instruction, 0, i);

                                // Update buffer
                                usedLength -= i;
                                parseStart = 0;
                                System.arraycopy(buffer, i, buffer, 0, usedLength);

                                return instruction;

                            }

                            // Handle invalid terminator characters
                            else if (terminator != ',')
                                throw new GuacamoleServerException("Element terminator of instruction was not ';' nor ','");

                        }

                        // Otherwise, read more data
                        else
                            break;

                    }

                    // Otherwise, parse error
                    else
                        throw new GuacamoleServerException("Non-numeric character in element length.");

                }

                // If past threshold, resize buffer before reading
                if (usedLength > buffer.length/2) {
                    char[] biggerBuffer = new char[buffer.length*2];
                    System.arraycopy(buffer, 0, biggerBuffer, 0, usedLength);
                    buffer = biggerBuffer;
                }

                // Attempt to fill buffer
                int numRead = input.read(buffer, usedLength, buffer.length - usedLength);
                if (numRead == -1)
                    return null;

                // Update used length
                usedLength += numRead;

            } // End read loop

        }
        catch (SocketTimeoutException e) {
            throw new GuacamoleUpstreamTimeoutException("Connection to guacd timed out.", e);
        }
        catch (IOException e) {
            throw new GuacamoleServerException(e);
        }

    }

    @Override
    public GuacamoleInstruction readInstruction() throws GuacamoleException {

        // Get instruction
        char[] instructionBuffer = read();

        // If EOF, return EOF
        if (instructionBuffer == null)
            return null;

        // Start of element
        int elementStart = 0;

        // Build list of elements
        Deque<String> elements = new LinkedList<String>();
        while (elementStart < instructionBuffer.length) {

            // Find end of length
            int lengthEnd = -1;
            for (int i=elementStart; i<instructionBuffer.length; i++) {
                if (instructionBuffer[i] == '.') {
                    lengthEnd = i;
                    break;
                }
            }

            // read() is required to return a complete instruction. If it does
            // not, this is a severe internal error.
            if (lengthEnd == -1)
                throw new GuacamoleServerException("Read returned incomplete instruction.");

            // Parse length
            int length = Integer.parseInt(new String(
                    instructionBuffer,
                    elementStart,
                    lengthEnd - elementStart
            ));

            // Parse element from just after period
            elementStart = lengthEnd + 1;
            String element = new String(
                    instructionBuffer,
                    elementStart,
                    length
            );

            // Append element to list of elements
            elements.addLast(element);

            // Read terminator after element
            elementStart += length;
            char terminator = instructionBuffer[elementStart];

            // Continue reading instructions after terminator
            elementStart++;

            // If we've reached the end of the instruction
            if (terminator == ';')
                break;

        }

        // Pull opcode off elements list
        String opcode = elements.removeFirst();

        // Create instruction
        GuacamoleInstruction instruction = new GuacamoleInstruction(
                opcode,
                elements.toArray(new String[elements.size()])
        );

        // Return parsed instruction
        return instruction;

    }

}
