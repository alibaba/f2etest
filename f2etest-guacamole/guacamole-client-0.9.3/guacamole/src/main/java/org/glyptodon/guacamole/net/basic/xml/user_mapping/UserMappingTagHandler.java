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

import org.glyptodon.guacamole.net.basic.auth.UserMapping;
import org.glyptodon.guacamole.net.basic.xml.TagHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * TagHandler for the "user-mapping" element.
 *
 * @author Mike Jumper
 */
public class UserMappingTagHandler implements TagHandler {

    /**
     * The UserMapping which will contain all data parsed by this tag handler.
     */
    private UserMapping user_mapping = new UserMapping();

    @Override
    public void init(Attributes attributes) throws SAXException {
        // Do nothing
    }

    @Override
    public TagHandler childElement(String localName) throws SAXException {

        // Start parsing of authorize tags, add to list of all authorizations
        if (localName.equals("authorize"))
            return new AuthorizeTagHandler(user_mapping);

        return null;

    }

    @Override
    public void complete(String textContent) throws SAXException {
        // Do nothing
    }

    /**
     * Returns a user mapping containing all authorizations and configurations
     * parsed so far. This user mapping will be backed by the data being parsed,
     * thus any additional authorizations or configurations will be available
     * in the object returned by this function even after this function has
     * returned, once the data corresponding to those authorizations or
     * configurations has been parsed.
     *
     * @return A user mapping containing all authorizations and configurations
     *         parsed so far.
     */
    public UserMapping asUserMapping() {
        return user_mapping;
    }

}
