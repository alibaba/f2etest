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
import org.glyptodon.guacamole.net.basic.auth.UserMapping;
import org.glyptodon.guacamole.net.basic.xml.TagHandler;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * TagHandler for the "authorize" element.
 *
 * @author Mike Jumper
 */
public class AuthorizeTagHandler implements TagHandler {

    /**
     * The Authorization corresponding to the "authorize" tag being handled
     * by this tag handler. The data of this Authorization will be populated
     * as the tag is parsed.
     */
    private Authorization authorization = new Authorization();

    /**
     * The default GuacamoleConfiguration to use if "param" or "protocol"
     * tags occur outside a "connection" tag.
     */
    private GuacamoleConfiguration default_config = null;

    /**
     * The UserMapping this authorization belongs to.
     */
    private UserMapping parent;

    /**
     * Creates a new AuthorizeTagHandler that parses an Authorization owned
     * by the given UserMapping.
     *
     * @param parent The UserMapping that owns the Authorization this handler
     *               will parse.
     */
    public AuthorizeTagHandler(UserMapping parent) {
        this.parent = parent;
    }

    @Override
    public void init(Attributes attributes) throws SAXException {

        // Init username and password
        authorization.setUsername(attributes.getValue("username"));
        authorization.setPassword(attributes.getValue("password"));

        // Get encoding
        String encoding = attributes.getValue("encoding");
        if (encoding != null) {

            // If "md5", use MD5 encoding
            if (encoding.equals("md5"))
                authorization.setEncoding(Authorization.Encoding.MD5);

            // If "plain", use plain text
            else if (encoding.equals("plain"))
                authorization.setEncoding(Authorization.Encoding.PLAIN_TEXT);

            // Otherwise, bad encoding
            else
                throw new SAXException(
                        "Invalid encoding: '" + encoding + "'");

        }

        parent.addAuthorization(this.asAuthorization());

    }

    @Override
    public TagHandler childElement(String localName) throws SAXException {

        // "connection" tag
        if (localName.equals("connection"))
            return new ConnectionTagHandler(authorization);

        // "param" tag
        if (localName.equals("param")) {

            // Create default config if it doesn't exist
            if (default_config == null) {
                default_config = new GuacamoleConfiguration();
                authorization.addConfiguration("DEFAULT", default_config);
            }

            return new ParamTagHandler(default_config);
        }

        // "protocol" tag
        if (localName.equals("protocol")) {

            // Create default config if it doesn't exist
            if (default_config == null) {
                default_config = new GuacamoleConfiguration();
                authorization.addConfiguration("DEFAULT", default_config);
            }

            return new ProtocolTagHandler(default_config);
        }

        return null;

    }

    @Override
    public void complete(String textContent) throws SAXException {
        // Do nothing
    }

    /**
     * Returns an Authorization backed by the data of this authorize tag
     * handler. This Authorization is guaranteed to at least have the username,
     * password, and encoding available. Any associated configurations will be
     * added dynamically as the authorize tag is parsed.
     *
     * @return An Authorization backed by the data of this authorize tag
     *         handler.
     */
    public Authorization asAuthorization() {
        return authorization;
    }

}
