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
import java.io.Writer;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.protocol.GuacamoleInstruction;

/**
 * A GuacamoleWriter which wraps a standard Java Writer, using that Writer as
 * the Guacamole instruction stream.
 *
 * @author Michael Jumper
 */
public class WriterGuacamoleWriter implements GuacamoleWriter {

    /**
     * Wrapped Writer to be used for all output.
     */
    private Writer output;

    /**
     * Creates a new WriterGuacamoleWriter which will use the given Writer as
     * the Guacamole instruction stream.
     *
     * @param output The Writer to use as the Guacamole instruction stream.
     */
    public WriterGuacamoleWriter(Writer output) {
        this.output = output;
    }

    @Override
    public void write(char[] chunk, int off, int len) throws GuacamoleException {
        try {
            output.write(chunk, off, len);
            output.flush();
        }
        catch (IOException e) {
            throw new GuacamoleServerException(e);
        }
    }

    @Override
    public void write(char[] chunk) throws GuacamoleException {
        write(chunk, 0, chunk.length);
    }

    @Override
    public void writeInstruction(GuacamoleInstruction instruction) throws GuacamoleException {
        write(instruction.toString().toCharArray());
    }

}
