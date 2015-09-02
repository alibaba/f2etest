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

package org.glyptodon.guacamole.net.basic.crud.connectiongroups;

import java.io.IOException;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleSecurityException;
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.net.auth.ConnectionGroup;
import org.glyptodon.guacamole.net.auth.Directory;
import org.glyptodon.guacamole.net.auth.User;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.net.auth.permission.ConnectionPermission;
import org.glyptodon.guacamole.net.auth.permission.ObjectPermission;
import org.glyptodon.guacamole.net.auth.permission.Permission;
import org.glyptodon.guacamole.net.auth.permission.SystemPermission;
import org.glyptodon.guacamole.net.basic.AuthenticatingHttpServlet;

/**
 * Simple HttpServlet which outputs XML containing a list of all authorized
 * connection groups for the current user.
 *
 * @author Michael Jumper
 */
public class List extends AuthenticatingHttpServlet {

    /**
     * System administration permission.
     */
    private static final Permission SYSTEM_PERMISSION = 
                new SystemPermission(SystemPermission.Type.ADMINISTER);

    
    /**
     * Checks whether the given user has permission to perform the given
     * object operation. Security exceptions are handled appropriately - only
     * non-security exceptions pass through.
     *
     * @param user The user whose permissions should be verified.
     * @param type The type of operation to check for permission for.
     * @param identifier The identifier of the connection the operation
     *                   would be performed upon.
     * @return true if permission is granted, false otherwise.
     *
     * @throws GuacamoleException If an error occurs while checking permissions.
     */
    private boolean hasConfigPermission(User user, ObjectPermission.Type type,
            String identifier)
    throws GuacamoleException {

        // Build permission
        Permission permission = new ConnectionPermission(
            type,
            identifier
        );

        try {
            // Return result of permission check, if possible
            return user.hasPermission(permission);
        }
        catch (GuacamoleSecurityException e) {
            // If cannot check due to security restrictions, no permission
            return false;
        }

    }

    /**
     * Writes the XML for the given connection group.
     * 
     * @param self The user whose permissions dictate the availability of the
     *             data written.
     * @param xml The XMLStremWriter to use when writing the data.
     * @param group The connection group whose XML representation will be
     *              written.
     * @throws GuacamoleException If an error occurs while reading the
     *                            requested data.
     * @throws XMLStreamException If an error occurs while writing the XML.
     */
    private void writeConnectionGroup(User self, XMLStreamWriter xml,
            ConnectionGroup group) throws GuacamoleException, XMLStreamException {

        // Write group 
        xml.writeStartElement("group");
        xml.writeAttribute("id", group.getIdentifier());
        xml.writeAttribute("name", group.getName());

        // Write group type
        switch (group.getType()) {

            case ORGANIZATIONAL:
                xml.writeAttribute("type", "organizational");
                break;

            case BALANCING:
                xml.writeAttribute("type", "balancing");
                break;

        }

        // Write contained connection groups
        writeConnectionGroups(self, xml, group.getConnectionGroupDirectory());

        // End of group
        xml.writeEndElement();

    }

    /**
     * Writes the XML for the given directory of connection groups.
     * 
     * @param self The user whose permissions dictate the availability of the
     *             data written.
     * @param xml The XMLStremWriter to use when writing the data.
     * @param directory The directory whose XML representation will be
     *                  written.
     * @throws GuacamoleException If an error occurs while reading the
     *                            requested data.
     * @throws XMLStreamException If an error occurs while writing the XML.
     */
    private void writeConnectionGroups(User self, XMLStreamWriter xml,
            Directory<String, ConnectionGroup> directory)
            throws GuacamoleException, XMLStreamException {

        // If no connections, write nothing
        Set<String> identifiers = directory.getIdentifiers();
        if (identifiers.isEmpty())
            return;
        
        // Begin connections
        xml.writeStartElement("groups");

        // For each entry, write corresponding connection element
        for (String identifier : identifiers) {

            // Write each group
            ConnectionGroup group = directory.get(identifier);
            writeConnectionGroup(self, xml, group);

        }

        // End connections
        xml.writeEndElement();

    }

    @Override
    protected void authenticatedService(
            UserContext context,
            HttpServletRequest request, HttpServletResponse response)
    throws GuacamoleException {

        // Do not cache
        response.setHeader("Cache-Control", "no-cache");

        // Write XML content type
        response.setHeader("Content-Type", "text/xml");
        
        // Set encoding
        response.setCharacterEncoding("UTF-8");

        // Get root group
        ConnectionGroup root = context.getRootConnectionGroup();

        // Write actual XML
        try {

            // Get self
            User self = context.self();

            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xml = outputFactory.createXMLStreamWriter(response.getWriter());

            // Write content of root group
            xml.writeStartDocument();
            writeConnectionGroup(self, xml, root);
            xml.writeEndDocument();

        }
        catch (XMLStreamException e) {
            throw new GuacamoleServerException(
                    "Unable to write connection group list XML.", e);
        }
        catch (IOException e) {
            throw new GuacamoleServerException(
                    "I/O error writing connection group list XML.", e);
        }

    }

}

