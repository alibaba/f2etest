/*
 * Copyright (C) 2014 Glyptodon LLC.
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

package org.glyptodon.guacamole.net.basic;

import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.io.GuacamoleReader;
import org.glyptodon.guacamole.protocol.GuacamoleInstruction;

/**
 * GuacamoleReader implementation which watches for specific instructions,
 * maintaining state based on the observed instructions.
 * 
 * @author Michael Jumper
 */
public class MonitoringGuacamoleReader implements GuacamoleReader {

    /**
     * The underlying GuacamoleReader.
     */
    private final GuacamoleReader reader;

    /**
     * Collection of all listeners which will receive events.
     */
    private final ClipboardState clipboard;

    /**
     * The index of the clipboard stream, if any.
     */
    private String clipboard_stream_index = null;
    
    /**
     * Creates a new MonitoringGuacamoleReader which watches the instructions
     * read by the given GuacamoleReader, firing events when specific
     * instructions are seen.
     * 
     * @param clipboard The clipboard state to maintain.
     * @param reader The reader to observe.
     */
    public MonitoringGuacamoleReader(ClipboardState clipboard,
            GuacamoleReader reader) {
        this.clipboard = clipboard;
        this.reader = reader;
    }

    @Override
    public boolean available() throws GuacamoleException {
        return reader.available();
    }

    @Override
    public char[] read() throws GuacamoleException {

        // Read single instruction, handle end-of-stream
        GuacamoleInstruction instruction = readInstruction();
        if (instruction == null)
            return null;

        return instruction.toString().toCharArray();

    }

    @Override
    public GuacamoleInstruction readInstruction() throws GuacamoleException {

        // Read single instruction, handle end-of-stream
        GuacamoleInstruction instruction = reader.readInstruction();
        if (instruction == null)
            return null;

        // If clipboard changing, reset clipboard state
        if (instruction.getOpcode().equals("clipboard")) {
            List<String> args = instruction.getArgs();
            if (args.size() >= 2) {
                clipboard_stream_index = args.get(0);
                clipboard.begin(args.get(1));
            }
        }

        // Add clipboard blobs to existing streams
        else if (instruction.getOpcode().equals("blob")) {
            List<String> args = instruction.getArgs();
            if (args.size() >= 2 && args.get(0).equals(clipboard_stream_index)) {
                String base64 = args.get(1);
                clipboard.append(DatatypeConverter.parseBase64Binary(base64));
            }
        }
        
        // Terminate and update clipboard at end of stream
        else if (instruction.getOpcode().equals("end")) {
            List<String> args = instruction.getArgs();
            if (args.size() >= 1 && args.get(0).equals(clipboard_stream_index)) {
                clipboard.commit();
                clipboard_stream_index = null;
            }
        }
        
        return instruction;
        
    }
    
}
