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

import java.io.StringReader;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.io.GuacamoleReader;
import org.glyptodon.guacamole.io.ReaderGuacamoleReader;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test which validates filtering of Guacamole instructions with
 * FilteredGuacamoleReader.
 *
 * @author Michael Jumper
 */
public class FilteredGuacamoleReaderTest {

    /**
     * Filter which allows through "yes" instructions but drops all others.
     */
    private class TestFilter implements GuacamoleFilter {

        @Override
        public GuacamoleInstruction filter(GuacamoleInstruction instruction) throws GuacamoleException {

            if (instruction.getOpcode().equals("yes"))
                return instruction;

            return null;
            
        }

    }
    
    @Test
    public void testFilter() throws Exception {

        // Test string
        final String test = "3.yes,1.A;2.no,1.B;3.yes,1.C;3.yes,1.D;4.nope,1.E;";

        GuacamoleReader reader = new FilteredGuacamoleReader(new ReaderGuacamoleReader(new StringReader(test)),
                                                             new TestFilter());

        GuacamoleInstruction instruction;

        // Validate first instruction
        instruction = reader.readInstruction();
        assertNotNull(instruction);
        assertEquals("yes", instruction.getOpcode());
        assertEquals(1, instruction.getArgs().size());
        assertEquals("A", instruction.getArgs().get(0));

        // Validate second instruction
        instruction = reader.readInstruction();
        assertNotNull(instruction);
        assertEquals("yes", instruction.getOpcode());
        assertEquals(1, instruction.getArgs().size());
        assertEquals("C", instruction.getArgs().get(0));

        // Validate third instruction
        instruction = reader.readInstruction();
        assertNotNull(instruction);
        assertEquals("yes", instruction.getOpcode());
        assertEquals(1, instruction.getArgs().size());
        assertEquals("D", instruction.getArgs().get(0));

        // Should be done now
        instruction = reader.readInstruction();
        assertNull(instruction);

    }
    
}
