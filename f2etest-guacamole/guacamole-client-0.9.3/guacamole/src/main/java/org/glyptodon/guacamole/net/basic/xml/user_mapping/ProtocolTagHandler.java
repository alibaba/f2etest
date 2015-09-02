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

import org.glyptodon.guacamole.net.basic.xml.TagHandler;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * TagHandler for the "protocol" element.
 *
 * @author Mike Jumper
 */
public class ProtocolTagHandler implements TagHandler {

    /**
     * The GuacamoleConfiguration which will be populated with data from
     * the tag handled by this tag handler.
     */
    private GuacamoleConfiguration config;

    /**
     * Creates a new handler for a "protocol" tag having the given
     * attributes.
     *
     * @param config The GuacamoleConfiguration to update with the data parsed
     *               from the "protocol" tag.
     * @throws SAXException If the attributes given are not valid.
     */
    public ProtocolTagHandler(GuacamoleConfiguration config) throws SAXException {
        this.config = config;
    }

    @Override
    public void init(Attributes attributes) throws SAXException {
        // Do nothing
    }

    @Override
    public TagHandler childElement(String localName) throws SAXException {
        throw new SAXException("The 'protocol' tag can contain no elements.");
    }

    @Override
    public void complete(String textContent) throws SAXException {
        config.setProtocol(textContent);
    }

}
