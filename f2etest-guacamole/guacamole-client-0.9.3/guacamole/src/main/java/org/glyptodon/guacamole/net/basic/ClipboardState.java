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

/**
 * Provides central storage for a cross-connection clipboard state. This
 * clipboard state is shared only for a single HTTP session. Multiple HTTP
 * sessions will all have their own state.
 * 
 * @author Michael Jumper
 */
public class ClipboardState {

    /**
     * The maximum number of bytes to track.
     */
    private static final int MAXIMUM_LENGTH = 262144;

     /**
     * The mimetype of the current contents.
     */
    private String mimetype = "text/plain";

    /**
     * The mimetype of the pending contents.
     */
    private String pending_mimetype = "text/plain";
    
    /**
     * The current contents.
     */
    private byte[] contents = new byte[0];

    /**
     * The pending clipboard contents.
     */
    private final byte[] pending = new byte[MAXIMUM_LENGTH];

    /**
     * The length of the pending data, in bytes.
     */
    private int pending_length = 0;
    
    /**
     * The timestamp of the last contents update.
     */
    private long last_update = 0;
    
    /**
     * Returns the current clipboard contents.
     * @return The current clipboard contents
     */
    public synchronized byte[] getContents() {
        return contents;
    }

    /**
     * Returns the mimetype of the current clipboard contents.
     * @return The mimetype of the current clipboard contents.
     */
    public synchronized String getMimetype() {
        return mimetype;
    }

    /**
     * Begins a new update of the clipboard contents. The actual contents will
     * not be saved until commit() is called.
     * 
     * @param mimetype The mimetype of the contents being added.
     */
    public synchronized void begin(String mimetype) {
        pending_length = 0;
        this.pending_mimetype = mimetype;
    }

    /**
     * Appends the given data to the clipboard contents.
     * 
     * @param data The raw data to append.
     */
    public synchronized void append(byte[] data) {

        // Calculate size of copy
        int length = data.length;
        int remaining = pending.length - pending_length;
        if (remaining < length)
            length = remaining;
    
        // Append data
        System.arraycopy(data, 0, pending, pending_length, length);
        pending_length += length;

    }

    /**
     * Commits the pending contents to the clipboard, notifying any threads
     * waiting for clipboard updates.
     */
    public synchronized void commit() {

        // Commit contents
        mimetype = pending_mimetype;
        contents = new byte[pending_length];
        System.arraycopy(pending, 0, contents, 0, pending_length);

        // Notify of update
        last_update = System.currentTimeMillis();
        this.notifyAll();

    }
    
    /**
     * Wait up to the given timeout for new clipboard data.
     * 
     * @param timeout The amount of time to wait, in milliseconds.
     * @return true if the contents were updated within the timeframe given,
     *         false otherwise.
     */
    public synchronized boolean waitForContents(int timeout) {

        // Wait for new contents if it's been a while
        if (System.currentTimeMillis() - last_update > timeout) {
            try {
                this.wait(timeout);
                return true;
            }
            catch (InterruptedException e) { /* ignore */ }
        }

        return false;

    }
    
}
