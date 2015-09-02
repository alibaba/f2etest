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

import org.glyptodon.guacamole.net.basic.ProtocolParameter;
import org.glyptodon.guacamole.net.basic.xml.TagHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * TagHandler for the "param" element.
 *
 * @author Mike Jumper
 */
public class ParamTagHandler implements TagHandler {

    /**
     * The ProtocolParameter backing this tag handler.
     */
    private ProtocolParameter protocolParameter = new ProtocolParameter();

    @Override
    public void init(Attributes attributes) throws SAXException {

        protocolParameter.setName(attributes.getValue("name"));
        protocolParameter.setTitle(attributes.getValue("title"));
        protocolParameter.setValue(attributes.getValue("value"));

        // Parse type
        String type = attributes.getValue("type");

        // Text field
        if ("text".equals(type))
            protocolParameter.setType(ProtocolParameter.Type.TEXT);

        // Numeric field
        else if ("numeric".equals(type))
            protocolParameter.setType(ProtocolParameter.Type.NUMERIC);

        // Password field
        else if ("password".equals(type))
            protocolParameter.setType(ProtocolParameter.Type.PASSWORD);

        // Enumerated field
        else if ("enum".equals(type))
            protocolParameter.setType(ProtocolParameter.Type.ENUM);

        // Multiline field
        else if ("multiline".equals(type))
            protocolParameter.setType(ProtocolParameter.Type.MULTILINE);

        // Boolean field
        else if ("boolean".equals(type)) {
            protocolParameter.setType(ProtocolParameter.Type.BOOLEAN);

            if(protocolParameter.getValue() == null)
                throw new SAXException
                        ("A value is required for the boolean parameter type.");
        }

        // Otherwise, fail with unrecognized type
        else
            throw new SAXException("Invalid parameter type: " + type);

    }

    @Override
    public TagHandler childElement(String localName) throws SAXException {

        // Start parsing of option tags
        if (localName.equals("option")) {

            // Get tag handler for option tag
            OptionTagHandler tagHandler = new OptionTagHandler();

            // Store stub in options collection
            protocolParameter.getOptions().add(
                tagHandler.asProtocolParameterOption());
            return tagHandler;

        }

        return null;

    }

    @Override
    public void complete(String textContent) throws SAXException {
        // Do nothing
    }

    /**
     * Returns the ProtocolParameter backing this tag.
     * @return The ProtocolParameter backing this tag.
     */
    public ProtocolParameter asProtocolParameter() {
        return protocolParameter;
    }

}
