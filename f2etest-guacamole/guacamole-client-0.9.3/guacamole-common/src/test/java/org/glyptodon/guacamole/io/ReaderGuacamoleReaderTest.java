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

package org.glyptodon.guacamole.io;

import java.io.StringReader;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.protocol.GuacamoleInstruction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the ReaderGuacamoleReader implementation of GuacamoleReader, validating
 * that instructions are parsed correctly.
 *
 * @author Michael Jumper
 */
public class ReaderGuacamoleReaderTest {

    /**
     * Test of ReaderGuacamoleReader parsing.
     * 
     * @throws GuacamoleException If a parse error occurs while parsing the
     *                            known-good test string.
     */
    @Test
    public void testReader() throws GuacamoleException {

        // Test string
        final String test = "1.a,2.bc,3.def,10.helloworld;4.test,5.test2;0.;3.foo;";

        GuacamoleReader reader = new ReaderGuacamoleReader(new StringReader(test));

        GuacamoleInstruction instruction;

        // Validate first test instruction
        instruction = reader.readInstruction();
        assertNotNull(instruction);
        assertEquals(3, instruction.getArgs().size());
        assertEquals("a", instruction.getOpcode());
        assertEquals("bc", instruction.getArgs().get(0));
        assertEquals("def", instruction.getArgs().get(1));
        assertEquals("helloworld", instruction.getArgs().get(2));

        // Validate second test instruction
        instruction = reader.readInstruction();
        assertNotNull(instruction);
        assertEquals(1, instruction.getArgs().size());
        assertEquals("test", instruction.getOpcode());
        assertEquals("test2", instruction.getArgs().get(0));

        // Validate third test instruction
        instruction = reader.readInstruction();
        assertNotNull(instruction);
        assertEquals(0, instruction.getArgs().size());
        assertEquals("", instruction.getOpcode());

        // Validate fourth test instruction
        instruction = reader.readInstruction();
        assertNotNull(instruction);
        assertEquals(0, instruction.getArgs().size());
        assertEquals("foo", instruction.getOpcode());

        // There should be no more instructions
        instruction = reader.readInstruction();
        assertNull(instruction);

    }


   
}
