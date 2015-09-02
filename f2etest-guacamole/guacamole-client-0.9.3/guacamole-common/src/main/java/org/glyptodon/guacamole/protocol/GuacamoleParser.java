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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleServerException;

/**
 * Parser for the Guacamole protocol. Arbitrary instruction data is appended,
 * and instructions are returned as a result. Invalid instructions result in
 * exceptions.
 *
 * @author Michael Jumper
 */
public class GuacamoleParser implements Iterator<GuacamoleInstruction> {

    /**
     * The maximum number of characters per instruction.
     */
    public static final int INSTRUCTION_MAX_LENGTH = 8192;

    /**
     * The maximum number of digits to allow per length prefix.
     */
    public static final int INSTRUCTION_MAX_DIGITS = 5;

    /**
     * The maximum number of elements per instruction, including the opcode.
     */
    public static final int INSTRUCTION_MAX_ELEMENTS = 64;

    /**
     * All possible states of the instruction parser.
     */ 
    private enum State {

        /**
         * The parser is currently waiting for data to complete the length prefix
         * of the current element of the instruction.
         */
        PARSING_LENGTH,

        /**
         * The parser has finished reading the length prefix and is currently
         * waiting for data to complete the content of the instruction.
         */
        PARSING_CONTENT,

        /**
         * The instruction has been fully parsed.
         */
        COMPLETE,

        /**
         * The instruction cannot be parsed because of a protocol error.
         */
        ERROR
            
    }

    /**
     * The latest parsed instruction, if any.
     */
    private GuacamoleInstruction parsedInstruction;

    /**
     * The parse state of the instruction.
     */
    private State state = State.PARSING_LENGTH;

    /**
     * The length of the current element, if known.
     */
    private int elementLength = 0;

    /**
     * The number of elements currently parsed.
     */
    private int elementCount = 0;

    /**
     * All currently parsed elements.
     */
    private final String elements[] = new String[INSTRUCTION_MAX_ELEMENTS];

    /**
     * Appends data from the given buffer to the current instruction.
     * 
     * @param chunk The buffer containing the data to append.
     * @param offset The offset within the buffer where the data begins.
     * @param length The length of the data to append.
     * @return The number of characters appended, or 0 if complete instructions
     *         have already been parsed and must be read via next() before
     *         more data can be appended.
     * @throws GuacamoleException If an error occurs while parsing the new data.
     */
    public int append(char chunk[], int offset, int length) throws GuacamoleException {

        int charsParsed = 0;

        // Do not exceed maximum number of elements
        if (elementCount == INSTRUCTION_MAX_ELEMENTS && state != State.COMPLETE) {
            state = State.ERROR;
            throw new GuacamoleServerException("Instruction contains too many elements.");
        }

        // Parse element length
        if (state == State.PARSING_LENGTH) {

            int parsedLength = elementLength;
            while (charsParsed < length) {

                // Pull next character
                char c = chunk[offset + charsParsed++];

                // If digit, add to length
                if (c >= '0' && c <= '9')
                    parsedLength = parsedLength*10 + c - '0';

                // If period, switch to parsing content
                else if (c == '.') {
                    state = State.PARSING_CONTENT;
                    break;
                }

                // If not digit, parse error
                else {
                    state = State.ERROR;
                    throw new GuacamoleServerException("Non-numeric character in element length.");
                }

            }

            // If too long, parse error
            if (parsedLength > INSTRUCTION_MAX_LENGTH) {
                state = State.ERROR;
                throw new GuacamoleServerException("Instruction exceeds maximum length.");
            }

            // Save length
            elementLength = parsedLength;

        } // end parse length

        // Parse element content, if available
        if (state == State.PARSING_CONTENT && charsParsed + elementLength + 1 <= length) {

            // Read element
            String element = new String(chunk, offset + charsParsed, elementLength);
            charsParsed += elementLength;
            elementLength = 0;

            // Read terminator char following element
            char terminator = chunk[offset + charsParsed++];

            // Add element to currently parsed elements
            elements[elementCount++] = element;
            
            // If semicolon, store end-of-instruction
            if (terminator == ';') {
                state = State.COMPLETE;
                parsedInstruction = new GuacamoleInstruction(elements[0],
                        Arrays.asList(elements).subList(1, elementCount));
            }

            // If comma, move on to next element
            else if (terminator == ',')
                state = State.PARSING_LENGTH;

            // Otherwise, parse error
            else {
                state = State.ERROR;
                throw new GuacamoleServerException("Element terminator of instruction was not ';' nor ','");
            }

        } // end parse content

        return charsParsed;

    }

    /**
     * Appends data from the given buffer to the current instruction.
     * 
     * @param chunk The data to append.
     * @return The number of characters appended, or 0 if complete instructions
     *         have already been parsed and must be read via next() before
     *         more data can be appended.
     * @throws GuacamoleException If an error occurs while parsing the new data.
     */   
    public int append(char chunk[]) throws GuacamoleException {
        return append(chunk, 0, chunk.length);
    }

    @Override
    public boolean hasNext() {
        return state == State.COMPLETE;
    }

    @Override
    public GuacamoleInstruction next() {

        // No instruction to return if not yet complete
        if (state != State.COMPLETE)
            return null;
        
        // Reset for next instruction.
        state = State.PARSING_LENGTH;
        elementCount = 0;
        elementLength = 0;
        
        return parsedInstruction;

    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("GuacamoleParser does not support remove().");
    }

}
