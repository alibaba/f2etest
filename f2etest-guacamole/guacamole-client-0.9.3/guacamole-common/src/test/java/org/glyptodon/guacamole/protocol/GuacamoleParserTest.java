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
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit test for GuacamoleParser. Verifies that parsing of the Guacamole
 * protocol works as required.
 *
 * @author Michael Jumper
 */
public class GuacamoleParserTest {

    /**
     * The GuacamoleParser instance being tested.
     */
    private final GuacamoleParser parser = new GuacamoleParser();

    /**
     * Test of append method, of class GuacamoleParser.
     * 
     * @throws GuacamoleException If a parse error occurs while parsing the
     *                            known-good test string.
     */
    @Test
    public void testParser() throws GuacamoleException {

        // Test string
        char buffer[] = "1.a,2.bc,3.def,10.helloworld;4.test,5.test2;0.;3.foo;".toCharArray();
        int offset = 0;
        int length = buffer.length;

        GuacamoleInstruction instruction;
        int parsed;

        // Parse more data
        while (length > 0 && (parsed = parser.append(buffer, offset, length)) != 0) {
            offset += parsed;
            length -= parsed;
        }

        // Validate first test instruction
        assertTrue(parser.hasNext());
        instruction = parser.next();
        assertNotNull(instruction);
        assertEquals(3, instruction.getArgs().size());
        assertEquals("a", instruction.getOpcode());
        assertEquals("bc", instruction.getArgs().get(0));
        assertEquals("def", instruction.getArgs().get(1));
        assertEquals("helloworld", instruction.getArgs().get(2));

        // Parse more data
        while (length > 0 && (parsed = parser.append(buffer, offset, length)) != 0) {
            offset += parsed;
            length -= parsed;
        }

        // Validate second test instruction
        assertTrue(parser.hasNext());
        instruction = parser.next();
        assertNotNull(instruction);
        assertEquals(1, instruction.getArgs().size());
        assertEquals("test", instruction.getOpcode());
        assertEquals("test2", instruction.getArgs().get(0));

        // Parse more data
        while (length > 0 && (parsed = parser.append(buffer, offset, length)) != 0) {
            offset += parsed;
            length -= parsed;
        }

        // Validate third test instruction
        assertTrue(parser.hasNext());
        instruction = parser.next();
        assertNotNull(instruction);
        assertEquals(0, instruction.getArgs().size());
        assertEquals("", instruction.getOpcode());

        // Parse more data
        while (length > 0 && (parsed = parser.append(buffer, offset, length)) != 0) {
            offset += parsed;
            length -= parsed;
        }

        // Validate fourth test instruction
        assertTrue(parser.hasNext());
        instruction = parser.next();
        assertNotNull(instruction);
        assertEquals(0, instruction.getArgs().size());
        assertEquals("foo", instruction.getOpcode());

        // Parse more data
        while (length > 0 && (parsed = parser.append(buffer, offset, length)) != 0) {
            offset += parsed;
            length -= parsed;
        }

        // There should be no more instructions
        assertFalse(parser.hasNext());

    }

}
