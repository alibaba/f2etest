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
import org.glyptodon.guacamole.io.GuacamoleReader;

/**
 * GuacamoleReader which applies a given GuacamoleFilter to observe or alter all
 * read instructions. Instructions may also be dropped or denied by the the
 * filter.
 *
 * @author Michael Jumper
 */
public class FilteredGuacamoleReader implements GuacamoleReader {

    /**
     * The wrapped GuacamoleReader.
     */
    private final GuacamoleReader reader;

    /**
     * The filter to apply when reading instructions.
     */
    private final GuacamoleFilter filter;

    /**
     * Wraps the given GuacamoleReader, applying the given filter to all read
     * instructions. Future reads will return only instructions which pass
     * the filter.
     *
     * @param reader The GuacamoleReader to wrap.
     * @param filter The filter which dictates which instructions are read, and
     *               how.
     */
    public FilteredGuacamoleReader(GuacamoleReader reader, GuacamoleFilter filter) {
        this.reader = reader;
        this.filter = filter;
    }
    
    @Override
    public boolean available() throws GuacamoleException {
        return reader.available();
    }

    @Override
    public char[] read() throws GuacamoleException {

        GuacamoleInstruction filteredInstruction = readInstruction();
        if (filteredInstruction == null)
            return null;

        return filteredInstruction.toString().toCharArray();
        
    }

    @Override
    public GuacamoleInstruction readInstruction() throws GuacamoleException {

        GuacamoleInstruction filteredInstruction;

        // Read and filter instructions until no instructions are dropped
        do {

            // Read next instruction
            GuacamoleInstruction unfilteredInstruction = reader.readInstruction();
            if (unfilteredInstruction == null)
                return null;

            // Apply filter
            filteredInstruction = filter.filter(unfilteredInstruction);

        } while (filteredInstruction == null);

        return filteredInstruction;
        
    }

}
