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

package org.glyptodon.guacamole.net.basic.crud.protocols;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.net.basic.AuthenticatingHttpServlet;
import org.glyptodon.guacamole.net.basic.ProtocolInfo;
import org.glyptodon.guacamole.net.basic.ProtocolParameter;
import org.glyptodon.guacamole.net.basic.ProtocolParameterOption;
import org.glyptodon.guacamole.net.basic.xml.DocumentHandler;
import org.glyptodon.guacamole.net.basic.xml.protocol.ProtocolTagHandler;
import org.glyptodon.guacamole.properties.GuacamoleHome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Simple HttpServlet which outputs XML containing a list of all visible
 * protocols.
 *
 * @author Michael Jumper
 */
public class List extends AuthenticatingHttpServlet {

    /**
     * Logger for this class.
     */
    private Logger logger = LoggerFactory.getLogger(List.class);

    /**
     * Array of all known protocol names.
     */
    private static final String[] KNOWN_PROTOCOLS = new String[]{
        "vnc", "rdp", "ssh", "telnet"};

    /**
     * Parses the given XML file, returning the parsed ProtocolInfo.
     *
     * @param input An input stream containing XML describing the parameters
     *              associated with a protocol supported by Guacamole.
     * @return A new ProtocolInfo object which contains the parameters described
     *         by the XML file parsed.
     * @throws GuacamoleException If an error occurs while parsing the XML file.
     */
    private ProtocolInfo getProtocol(InputStream input)
            throws GuacamoleException {

        // Parse document
        try {

            // Get handler for root element
            ProtocolTagHandler protocolTagHandler =
                    new ProtocolTagHandler();

            // Set up document handler
            DocumentHandler contentHandler = new DocumentHandler(
                    "protocol", protocolTagHandler);

            // Set up XML parser
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(contentHandler);

            // Read and parse file
            InputStream xml = new BufferedInputStream(input);
            parser.parse(new InputSource(xml));
            xml.close();

            // Return parsed protocol
            return protocolTagHandler.asProtocolInfo();

        }
        catch (IOException e) {
            throw new GuacamoleException("Error reading basic user mapping file.", e);
        }
        catch (SAXException e) {
            throw new GuacamoleException("Error parsing basic user mapping XML.", e);
        }

    }

    /**
     * Given an XML stream and a fully-populated ProtocolInfo object, writes
     * out the corresponding protocol XML describing all available parameters.
     *
     * @param xml The XMLStreamWriter to use to write the XML.
     * @param protocol The ProtocolInfo object to read parameters and protocol
     *                 information from.
     * @throws XMLStreamException If an error occurs while writing the XML.
     */
    private void writeProtocol(XMLStreamWriter xml, ProtocolInfo protocol)
            throws XMLStreamException {

        // Write protocol
        xml.writeStartElement("protocol");
        xml.writeAttribute("name", protocol.getName());
        xml.writeAttribute("title", protocol.getTitle());

        // Write parameters
        for (ProtocolParameter param : protocol.getParameters()) {

            // Write param tag
            xml.writeStartElement("param");
            xml.writeAttribute("name", param.getName());
            xml.writeAttribute("title", param.getTitle());

            // Write type
            switch (param.getType()) {

                // Text parameter
                case TEXT:
                    xml.writeAttribute("type", "text");
                    break;

                // Password parameter
                case PASSWORD:
                    xml.writeAttribute("type", "password");
                    break;

                // Numeric parameter
                case NUMERIC:
                    xml.writeAttribute("type", "numeric");
                    break;

                // Boolean parameter
                case BOOLEAN:
                    xml.writeAttribute("type", "boolean");
                    xml.writeAttribute("value", param.getValue());
                    break;

                // Enumerated parameter
                case ENUM:
                    xml.writeAttribute("type", "enum");
                    break;

                // Multiline parameter
                case MULTILINE:
                    xml.writeAttribute("type", "multiline");
                    break;

                // If unknown, fail explicitly
                default:
                    throw new UnsupportedOperationException(
                        "Parameter type not supported: " + param.getType());

            }

            // Write options
            for (ProtocolParameterOption option : param.getOptions()) {
                xml.writeStartElement("option");
                xml.writeAttribute("value", option.getValue());
                xml.writeCharacters(option.getTitle());
                xml.writeEndElement();
            }

            // End parameter
            xml.writeEndElement();

        }

        // End protocol
        xml.writeEndElement();

    }

    @Override
    protected void authenticatedService(
            UserContext context,
            HttpServletRequest request, HttpServletResponse response)
    throws GuacamoleException {

        // Do not cache
        response.setHeader("Cache-Control", "no-cache");
        
        // Set encoding
        response.setCharacterEncoding("UTF-8");

        // Map of all available protocols
        Map<String, ProtocolInfo> protocols = new HashMap<String, ProtocolInfo>();

        // Get protcols directory
        File protocol_directory = new File(GuacamoleHome.getDirectory(),
                "protocols");

        // Read protocols from directory if it exists
        if (protocol_directory.isDirectory()) {

            // Get all XML files
            File[] files = protocol_directory.listFiles(
                new FilenameFilter() {

                    @Override
                    public boolean accept(File file, String string) {
                        return string.endsWith(".xml");
                    }

                }
            );

            // Load each protocol from each file
            for (File file : files) {

                try {

                    // Parse protocol
                    FileInputStream stream = new FileInputStream(file);
                    ProtocolInfo protocol = getProtocol(stream);
                    stream.close();

                    // Store protocol
                    protocols.put(protocol.getName(), protocol);

                }
                catch (IOException e) {
                    logger.error("Unable to read protocol XML.", e);
                }

            }

        }

        // If known protocols are not already defined, read from classpath
        for (String protocol : KNOWN_PROTOCOLS) {

            // If protocol not defined yet, attempt to load from classpath
            if (!protocols.containsKey(protocol)) {

                InputStream stream = List.class.getResourceAsStream(
                        "/net/sourceforge/guacamole/net/protocols/"
                        + protocol + ".xml");

                // Parse XML if available
                if (stream != null)
                    protocols.put(protocol, getProtocol(stream));

            }

        }

        // Write actual XML
        try {
            // Write XML content type
            response.setHeader("Content-Type", "text/xml");

            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xml = outputFactory.createXMLStreamWriter(response.getWriter());

            // Begin document
            xml.writeStartDocument();
            xml.writeStartElement("protocols");

            // Write all protocols
            for (ProtocolInfo protocol : protocols.values())
                writeProtocol(xml, protocol);

            // End document
            xml.writeEndElement();
            xml.writeEndDocument();

        }
        catch (XMLStreamException e) {
            throw new GuacamoleServerException(
                    "Unable to write protocol list XML.", e);
        }
        catch (IOException e) {
            throw new GuacamoleServerException(
                    "I/O error writing protocol list XML.", e);
        }

    }

}
