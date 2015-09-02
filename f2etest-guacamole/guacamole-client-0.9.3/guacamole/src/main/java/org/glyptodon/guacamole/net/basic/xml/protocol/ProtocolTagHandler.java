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

package org.glyptodon.guacamole.net.basic.xml.protocol;

import org.glyptodon.guacamole.net.basic.ProtocolInfo;
import org.glyptodon.guacamole.net.basic.xml.TagHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * TagHandler for the "protocol" element.
 *
 * @author Mike Jumper
 */
public class ProtocolTagHandler implements TagHandler {

    /**
     * The ProtocolInfo object which will contain all data parsed by this tag
     * handler.
     */
    private ProtocolInfo info = new ProtocolInfo();

    @Override
    public void init(Attributes attributes) throws SAXException {
        info.setName(attributes.getValue("name"));
        info.setTitle(attributes.getValue("title"));
    }

    @Override
    public TagHandler childElement(String localName) throws SAXException {

        // Start parsing of param tags, add to list of all parameters
        if (localName.equals("param")) {

            // Get tag handler for param tag
            ParamTagHandler tagHandler = new ParamTagHandler();

            // Store stub in parameters collection
            info.getParameters().add(tagHandler.asProtocolParameter());
            return tagHandler;

        }

        return null;

    }

    @Override
    public void complete(String textContent) throws SAXException {
        // Do nothing
    }

    /**
     * Returns the ProtocolInfo backing this tag.
     * @return The ProtocolInfo backing this tag.
     */
    public ProtocolInfo asProtocolInfo() {
        return info;
    }

}
