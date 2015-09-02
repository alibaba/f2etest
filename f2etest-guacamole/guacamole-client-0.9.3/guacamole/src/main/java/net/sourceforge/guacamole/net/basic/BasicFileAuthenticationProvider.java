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

package net.sourceforge.guacamole.net.basic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.Credentials;
import org.glyptodon.guacamole.net.auth.simple.SimpleAuthenticationProvider;
import org.glyptodon.guacamole.net.basic.auth.Authorization;
import org.glyptodon.guacamole.net.basic.auth.UserMapping;
import org.glyptodon.guacamole.net.basic.xml.DocumentHandler;
import org.glyptodon.guacamole.net.basic.xml.user_mapping.UserMappingTagHandler;
import org.glyptodon.guacamole.properties.FileGuacamoleProperty;
import org.glyptodon.guacamole.properties.GuacamoleProperties;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Authenticates users against a static list of username/password pairs.
 * Each username/password may be associated with multiple configurations.
 * This list is stored in an XML file which is reread if modified.
 *
 * @author Michael Jumper, Michal Kotas
 */
public class BasicFileAuthenticationProvider extends SimpleAuthenticationProvider {

    /**
     * Logger for this class.
     */
    private Logger logger = LoggerFactory.getLogger(BasicFileAuthenticationProvider.class);

    /**
     * The time the user mapping file was last modified.
     */
    private long mod_time;

    /**
     * The parsed UserMapping read when the user mapping file was last parsed.
     */
    private UserMapping user_mapping;

    /**
     * The filename of the XML file to read the user user_mapping from.
     */
    public static final FileGuacamoleProperty BASIC_USER_MAPPING = new FileGuacamoleProperty() {

        @Override
        public String getName() { return "basic-user-mapping"; }

    };

    /**
     * Returns a UserMapping containing all authorization data given within
     * the XML file specified by the "basic-user-mapping" property in
     * guacamole.properties. If the XML file has been modified or has not yet
     * been read, this function may reread the file.
     *
     * @return A UserMapping containing all authorization data within the
     *         user mapping XML file.
     * @throws GuacamoleException If the user mapping property is missing or
     *                            an error occurs while parsing the XML file.
     */
    private UserMapping getUserMapping() throws GuacamoleException {

        // Get user user_mapping file
        File user_mapping_file =
                GuacamoleProperties.getRequiredProperty(BASIC_USER_MAPPING);

        // If user_mapping not yet read, or user_mapping has been modified, reread
        if (user_mapping == null ||
                (user_mapping_file.exists()
                 && mod_time < user_mapping_file.lastModified())) {

            logger.info("Reading user mapping file: {}", user_mapping_file);

            // Parse document
            try {

                // Get handler for root element
                UserMappingTagHandler userMappingHandler =
                        new UserMappingTagHandler();

                // Set up document handler
                DocumentHandler contentHandler = new DocumentHandler(
                        "user-mapping", userMappingHandler);

                // Set up XML parser
                XMLReader parser = XMLReaderFactory.createXMLReader();
                parser.setContentHandler(contentHandler);

                // Read and parse file
                InputStream input = new BufferedInputStream(new FileInputStream(user_mapping_file));
                parser.parse(new InputSource(input));
                input.close();

                // Store mod time and user mapping
                mod_time = user_mapping_file.lastModified();
                user_mapping = userMappingHandler.asUserMapping();

            }
            catch (IOException e) {
                throw new GuacamoleException("Error reading basic user mapping file.", e);
            }
            catch (SAXException e) {
                throw new GuacamoleException("Error parsing basic user mapping XML.", e);
            }

        }

        // Return (possibly cached) user mapping
        return user_mapping;

    }

    @Override
    public Map<String, GuacamoleConfiguration>
            getAuthorizedConfigurations(Credentials credentials)
            throws GuacamoleException {

        // Validate and return info for given user and pass
        Authorization auth = getUserMapping().getAuthorization(credentials.getUsername());
        if (auth != null && auth.validate(credentials.getUsername(), credentials.getPassword()))
            return auth.getConfigurations();

        // Unauthorized
        return null;

    }

}
