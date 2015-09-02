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

package net.sourceforge.guacamole.net.auth.noauth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML parser for the configuration file used by the NoAuth auth provider.
 *
 * @author Laurent Meunier
 */
public class NoAuthConfigContentHandler extends DefaultHandler {

    /**
     * Map of all configurations, indexed by name.
     */
    private Map<String, GuacamoleConfiguration> configs = new HashMap<String, GuacamoleConfiguration>();

    /**
     * The name of the current configuration, if any.
     */
    private String current = null;

    /**
     * The current configuration being parsed, if any.
     */
    private GuacamoleConfiguration currentConfig = null;

    /**
     * Returns the a map of all available configurations as parsed from the
     * XML file. This map is unmodifiable.
     *
     * @return A map of all available configurations.
     */
    public Map<String, GuacamoleConfiguration> getConfigs() {
        return Collections.unmodifiableMap(configs);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        // If end of config element, add to map
        if (localName.equals("config")) {

            // Add to map
            configs.put(current, currentConfig);

            // Reset state for next configuration
            currentConfig = null;
            current = null;

        }

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        // Begin configuration parsing if config element
        if (localName.equals("config")) {

            // Ensure this config is on the top level
            if (current != null)
                throw new SAXException("Configurations cannot be nested.");

            // Read name
            String name = attributes.getValue("name");
            if (name == null)
                throw new SAXException("Each configuration must have a name.");

            // Read protocol
            String protocol = attributes.getValue("protocol");
            if (protocol == null)
                throw new SAXException("Each configuration must have a protocol.");

            // Create config stub
            current = name;
            currentConfig = new GuacamoleConfiguration();
            currentConfig.setProtocol(protocol);

        }

        // Add parameters to existing configuration
        else if (localName.equals("param")) {

            // Ensure a corresponding config exists
            if (currentConfig == null)
                throw new SAXException("Parameter without corresponding configuration.");

            currentConfig.setParameter(attributes.getValue("name"), attributes.getValue("value"));

        }

    }

}
