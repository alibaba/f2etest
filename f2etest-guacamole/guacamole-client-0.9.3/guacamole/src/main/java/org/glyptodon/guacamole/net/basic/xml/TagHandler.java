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

package org.glyptodon.guacamole.net.basic.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A simple element-level event handler for events triggered by the
 * SAX-driven DocumentHandler parser.
 *
 * @author Mike Jumper
 */
public interface TagHandler {

    /**
     * Called when a child element of the current element is parsed.
     *
     * @param localName The local name of the child element seen.
     * @return The TagHandler which should handle all element-level events
     *         related to the child element.
     * @throws SAXException If the child element being parsed was not expected,
     *                      or some other error prevents a proper TagHandler
     *                      from being constructed for the child element.
     */
    public TagHandler childElement(String localName)
            throws SAXException;

    /**
     * Called when the element corresponding to this TagHandler is first seen,
     * just after an instance is created.
     *
     * @param attributes The attributes of the element seen.
     * @throws SAXException If an error prevents a the TagHandler from being
     *                      from being initialized.
     */
    public void init(Attributes attributes) throws SAXException;

    /**
     * Called when this element, and all child elements, have been fully parsed,
     * and the entire text content of this element (if any) is available.
     *
     * @param textContent The full text content of this element, if any.
     * @throws SAXException If the text content received is not valid for any
     *                      reason, or the child elements parsed are not
     *                      correct.
     */
    public void complete(String textContent) throws SAXException;

}
