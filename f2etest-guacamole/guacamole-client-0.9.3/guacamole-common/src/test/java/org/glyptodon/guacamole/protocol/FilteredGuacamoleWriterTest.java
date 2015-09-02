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

import java.io.StringWriter;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.io.GuacamoleWriter;
import org.glyptodon.guacamole.io.WriterGuacamoleWriter;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test which validates filtering of Guacamole instructions with
 * FilteredGuacamoleWriter.
 *
 * @author Michael Jumper
 */
public class FilteredGuacamoleWriterTest {

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

        StringWriter stringWriter = new StringWriter();
        GuacamoleWriter writer = new FilteredGuacamoleWriter(new WriterGuacamoleWriter(stringWriter),
                                                             new TestFilter());

        // Write a few chunks of complete instructions
        writer.write("3.yes,1.A;2.no,1.B;3.yes,1.C;3.yes,1.D;4.nope,1.E;".toCharArray());
        writer.write("1.n,3.abc;3.yes,5.hello;2.no,4.test;3.yes,5.world;".toCharArray());

        // Validate filtered results
        assertEquals("3.yes,1.A;3.yes,1.C;3.yes,1.D;3.yes,5.hello;3.yes,5.world;", stringWriter.toString());

    }
    
}
