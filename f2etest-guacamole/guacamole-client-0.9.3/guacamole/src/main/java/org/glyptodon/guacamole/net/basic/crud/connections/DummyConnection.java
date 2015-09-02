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

package org.glyptodon.guacamole.net.basic.crud.connections;

import java.util.List;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.GuacamoleSocket;
import org.glyptodon.guacamole.net.auth.AbstractConnection;
import org.glyptodon.guacamole.net.auth.ConnectionRecord;
import org.glyptodon.guacamole.protocol.GuacamoleClientInformation;

/**
 * Basic Connection skeleton, providing a means of storing Connection data
 * prior to CRUD operations. This Connection has no functionality for actually
 * performing a connection operation, and does not promote any of the
 * semantics that would otherwise be present because of the authentication
 * provider. It is up to the authentication provider to create a new
 * Connection based on the information contained herein.
 *
 * @author Michael Jumper
 */
public class DummyConnection extends AbstractConnection {

    @Override
    public GuacamoleSocket connect(GuacamoleClientInformation info) throws GuacamoleException {
        throw new UnsupportedOperationException("Connection unsupported in DummyConnection.");
    }

    @Override
    public List<ConnectionRecord> getHistory() throws GuacamoleException {
        throw new UnsupportedOperationException("History unsupported in DummyConnection.");
    }
	
    public GuacamoleSocket connect(GuacamoleClientInformation info, String username, String password, String program)
            throws GuacamoleException {
			throw new UnsupportedOperationException("Connection unsupported in DummyConnection.");
	}

}
