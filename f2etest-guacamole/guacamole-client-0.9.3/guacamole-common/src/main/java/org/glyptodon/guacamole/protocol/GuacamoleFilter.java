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

/**
 * Interface which provides for the filtering of individual instructions. Each
 * filtered instruction may be allowed through untouched, modified, replaced,
 * dropped, or explicitly denied.
 *
 * @author Michael Jumper
 */
public interface GuacamoleFilter {

    /**
     * Applies the filter to the given instruction, returning the original
     * instruction, a modified version of the original, or null, depending
     * on the implementation.
     *
     * @param instruction The instruction to filter.
     * @return The original instruction, if the instruction is to be allowed,
     *         a modified version of the instruction, if the instruction is
     *         to be overridden, or null, if the instruction is to be dropped.
     * @throws GuacamoleException If an error occurs filtering the instruction,
     *                            or if the instruction must be explicitly
     *                            denied.
     */
    public GuacamoleInstruction filter(GuacamoleInstruction instruction) throws GuacamoleException;
    
}
