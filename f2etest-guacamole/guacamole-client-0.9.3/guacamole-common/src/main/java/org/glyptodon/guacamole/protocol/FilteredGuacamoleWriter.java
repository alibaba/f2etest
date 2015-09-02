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
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.io.GuacamoleWriter;

/**
 * GuacamoleWriter which applies a given GuacamoleFilter to observe or alter
 * all written instructions. Instructions may also be dropped or denied by
 * the filter.
 *
 * @author Michael Jumper
 */
public class FilteredGuacamoleWriter implements GuacamoleWriter {

    /**
     * The wrapped GuacamoleWriter.
     */
    private final GuacamoleWriter writer;

    /**
     * The filter to apply when writing instructions.
     */
    private final GuacamoleFilter filter;

    /**
     * Parser for reading instructions prior to writing, such that they can be
     * passed on to the filter.
     */
    private final GuacamoleParser parser = new GuacamoleParser();
    
    /**
     * Wraps the given GuacamoleWriter, applying the given filter to all written 
     * instructions. Future writes will only write instructions which pass
     * the filter.
     *
     * @param writer The GuacamoleWriter to wrap.
     * @param filter The filter which dictates which instructions are written,
     *               and how.
     */
    public FilteredGuacamoleWriter(GuacamoleWriter writer, GuacamoleFilter filter) {
        this.writer = writer;
        this.filter = filter;
    }
 
    @Override
    public void write(char[] chunk, int offset, int length) throws GuacamoleException {

        // Write all data in chunk
        while (length > 0) {

            // Pass as much data through the parser as possible
            int parsed;
            while ((parsed = parser.append(chunk, offset, length)) != 0) {
                offset += parsed;
                length -= parsed;
            }

            // If no instruction is available, it must be incomplete
            if (!parser.hasNext())
                throw new GuacamoleServerException("Filtered write() contained an incomplete instruction.");

            // Write single instruction through filter
            writeInstruction(parser.next());

        }
        
    }

    @Override
    public void write(char[] chunk) throws GuacamoleException {
        write(chunk, 0, chunk.length);
    }

    @Override
    public void writeInstruction(GuacamoleInstruction instruction) throws GuacamoleException {

        // Write instruction only if not dropped
        GuacamoleInstruction filteredInstruction = filter.filter(instruction);
        if (filteredInstruction != null)
            writer.writeInstruction(filteredInstruction);

    }

}
