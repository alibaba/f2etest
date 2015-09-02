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

package org.glyptodon.guacamole.protocol;

/**
 * All possible statuses returned by various Guacamole instructions, each having
 * a corresponding code.
 * 
 * @author Michael Jumper
 */
public enum GuacamoleStatus {

    /**
     * The operation succeeded.
     */
    SUCCESS(200, 1000, 0x0000),

    /**
     * The requested operation is unsupported.
     */
    UNSUPPORTED(501, 1011, 0x0100),

    /**
     * The operation could not be performed due to an internal failure.
     */
    SERVER_ERROR(500, 1011, 0x0200),

    /**
     * The operation could not be performed as the server is busy.
     */
    SERVER_BUSY(503, 1008, 0x0201),

    /**
     * The operation could not be performed because the upstream server is not
     * responding.
     */
    UPSTREAM_TIMEOUT(504, 1011, 0x0202),

    /**
     * The operation was unsuccessful due to an error or otherwise unexpected
     * condition of the upstream server.
     */
    UPSTREAM_ERROR(502, 1011, 0x0203),

    /**
     * The operation could not be performed as the requested resource does not
     * exist.
     */
    RESOURCE_NOT_FOUND(404, 1002, 0x0204),

    /**
     * The operation could not be performed as the requested resource is already
     * in use.
     */
    RESOURCE_CONFLICT(409, 1008, 0x0205),

    /**
     * The operation could not be performed because bad parameters were given.
     */
    CLIENT_BAD_REQUEST(400, 1002, 0x0300),

    /**
     * Permission was denied to perform the operation, as the user is not yet
     * authorized (not yet logged in, for example). As HTTP 401 has implications
     * for HTTP-specific authorization schemes, this status continues to map to
     * HTTP 403 ("Forbidden"). To do otherwise would risk unintended effects.
     */
    CLIENT_UNAUTHORIZED(403, 1008, 0x0301),

    /**
     * Permission was denied to perform the operation, and this operation will
     * not be granted even if the user is authorized.
     */
    CLIENT_FORBIDDEN(403, 1008, 0x0303),

    /**
     * The client took too long to respond.
     */
    CLIENT_TIMEOUT(408, 1002, 0x0308),

    /**
     * The client sent too much data.
     */
    CLIENT_OVERRUN(413, 1009, 0x030D),

    /**
     * The client sent data of an unsupported or unexpected type.
     */
    CLIENT_BAD_TYPE(415, 1003, 0x030F),

    /**
     * The operation failed because the current client is already using too
     * many resources.
     */
    CLIENT_TOO_MANY(429, 1008, 0x031D);

    /**
     * The most applicable HTTP error code.
     */
    private final int http_code;

    /**
     * The most applicable WebSocket error code.
     */
    private final int websocket_code;
    
    /**
     * The Guacamole protocol status code.
     */
    private final int guac_code;

    /**
     * Initializes a GuacamoleStatusCode with the given HTTP and Guacamole
     * status/error code values.
     * 
     * @param http_code The most applicable HTTP error code.
     * @param websocket_code The most applicable WebSocket error code.
     * @param guac_code The Guacamole protocol status code.
     */
    private GuacamoleStatus(int http_code, int websocket_code, int guac_code) {
        this.http_code = http_code;
        this.websocket_code = websocket_code;
        this.guac_code = guac_code;
    }

    /**
     * Returns the most applicable HTTP error code.
     * 
     * @return The most applicable HTTP error code.
     */
    public int getHttpStatusCode() {
        return http_code;
    }

    /**
     * Returns the most applicable HTTP error code.
     * 
     * @return The most applicable HTTP error code.
     */
    public int getWebSocketCode() {
        return websocket_code;
    }

    /**
     * Returns the corresponding Guacamole protocol status code.
     * 
     * @return The corresponding Guacamole protocol status code.
     */
    public int getGuacamoleStatusCode() {
        return guac_code;
    }
    
}
