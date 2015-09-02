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

package org.glyptodon.guacamole.net.basic;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.GuacamoleUnsupportedException;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.properties.BooleanGuacamoleProperty;
import org.glyptodon.guacamole.properties.GuacamoleProperties;

/**
 * Servlet which dumps the current contents of the clipboard.
 *
 * @author Michael Jumper
 */
public class CaptureClipboard extends AuthenticatingHttpServlet {

    /**
     * The amount of time to wait for clipboard changes, in milliseconds.
     */
    private static final int CLIPBOARD_TIMEOUT = 250;

    /**
     * Whether clipboard integration is enabled.
     */
    public static final BooleanGuacamoleProperty INTEGRATION_ENABLED = new BooleanGuacamoleProperty() {

        @Override
        public String getName() { return "enable-clipboard-integration"; }

    };


    @Override
    protected void authenticatedService(
            UserContext context,
            HttpServletRequest request, HttpServletResponse response)
    throws GuacamoleException {

        // Only bother if actually enabled
        if (GuacamoleProperties.getProperty(INTEGRATION_ENABLED, false)) {
        
            // Get clipboard
            final HttpSession session = request.getSession(true);
            final ClipboardState clipboard = getClipboardState(session);

            // Send clipboard contents
            try {
                synchronized (clipboard) {
                    clipboard.waitForContents(CLIPBOARD_TIMEOUT);
                    response.setContentType(clipboard.getMimetype());
                    response.getOutputStream().write(clipboard.getContents());
                }
            }
            catch (IOException e) {
                throw new GuacamoleServerException("Unable to send clipboard contents", e);
            }

        }

        // Otherwise, inform not supported
        else
            throw new GuacamoleUnsupportedException("Clipboard integration not supported");

    }

}
