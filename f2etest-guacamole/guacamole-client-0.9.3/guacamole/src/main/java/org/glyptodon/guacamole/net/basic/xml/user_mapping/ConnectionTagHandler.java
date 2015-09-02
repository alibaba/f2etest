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

package org.glyptodon.guacamole.net.basic.xml.user_mapping;

import org.glyptodon.guacamole.net.basic.auth.Authorization;
import org.glyptodon.guacamole.net.basic.xml.TagHandler;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * TagHandler for the "connection" element.
 *
 * @author Mike Jumper
 */
public class ConnectionTagHandler implements TagHandler {

    /**
     * The GuacamoleConfiguration backing this tag handler.
     */
    private GuacamoleConfiguration config = new GuacamoleConfiguration();

    /**
     * The name associated with the connection being parsed.
     */
    private String name;

    /**
     * The Authorization this connection belongs to.
     */
    private Authorization parent;

    /**
     * Creates a new ConnectionTagHandler that parses a Connection owned by
     * the given Authorization.
     *
     * @param parent The Authorization that will own this Connection once
     *               parsed.
     */
    public ConnectionTagHandler(Authorization parent) {
        this.parent = parent;
    }

    @Override
    public void init(Attributes attributes) throws SAXException {
        name = attributes.getValue("name");
        parent.addConfiguration(name, this.asGuacamoleConfiguration());
    }

    @Override
    public TagHandler childElement(String localName) throws SAXException {

        if (localName.equals("param"))
            return new ParamTagHandler(config);

        if (localName.equals("protocol"))
            return new ProtocolTagHandler(config);

        return null;

    }

    @Override
    public void complete(String textContent) throws SAXException {
        // Do nothing
    }

    /**
     * Returns a GuacamoleConfiguration whose contents are populated from data
     * within this connection element and child elements. This
     * GuacamoleConfiguration will continue to be modified as the user mapping
     * is parsed.
     *
     * @return A GuacamoleConfiguration whose contents are populated from data
     *         within this connection element.
     */
    public GuacamoleConfiguration asGuacamoleConfiguration() {
        return config;
    }

    /**
     * Returns the name associated with this connection.
     *
     * @return The name associated with this connection.
     */
    public String getName() {
        return name;
    }

}
