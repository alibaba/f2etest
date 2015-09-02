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

package org.glyptodon.guacamole;

import org.glyptodon.guacamole.protocol.GuacamoleStatus;


/**
 * A security-related exception thrown when parts of the Guacamole API is
 * denying access to a resource.
 *
 * @author Michael Jumper
 */
public class GuacamoleSecurityException extends GuacamoleClientException {

    /**
     * Creates a new GuacamoleSecurityException with the given message and cause.
     *
     * @param message A human readable description of the exception that
     *                occurred.
     * @param cause The cause of this exception.
     */
    public GuacamoleSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new GuacamoleSecurityException with the given message.
     *
     * @param message A human readable description of the exception that
     *                occurred.
     */
    public GuacamoleSecurityException(String message) {
        super(message);
    }

    /**
     * Creates a new GuacamoleSecurityException with the given cause.
     *
     * @param cause The cause of this exception.
     */
    public GuacamoleSecurityException(Throwable cause) {
        super(cause);
    }

    @Override
    public GuacamoleStatus getStatus() {
        return GuacamoleStatus.CLIENT_FORBIDDEN;
    }

}
