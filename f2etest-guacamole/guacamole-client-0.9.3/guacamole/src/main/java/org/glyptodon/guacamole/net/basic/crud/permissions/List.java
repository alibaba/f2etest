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

package org.glyptodon.guacamole.net.basic.crud.permissions;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.glyptodon.guacamole.GuacamoleClientException;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleSecurityException;
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.net.auth.Directory;
import org.glyptodon.guacamole.net.auth.User;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.net.auth.permission.ConnectionGroupPermission;
import org.glyptodon.guacamole.net.auth.permission.ConnectionPermission;
import org.glyptodon.guacamole.net.auth.permission.ObjectPermission;
import org.glyptodon.guacamole.net.auth.permission.Permission;
import org.glyptodon.guacamole.net.auth.permission.SystemPermission;
import org.glyptodon.guacamole.net.auth.permission.UserPermission;
import org.glyptodon.guacamole.net.basic.AuthenticatingHttpServlet;

/**
 * Simple HttpServlet which outputs XML containing a list of all visible
 * permissions of a given user.
 *
 * @author Michael Jumper
 */
public class List extends AuthenticatingHttpServlet {

    /**
     * Returns the XML attribute value representation of the given
     * SystemPermission.Type.
     *
     * @param type The SystemPermission.Type to translate into a String.
     * @return The XML attribute value representation of the given
     *         SystemPermission.Type.
     *
     * @throws GuacamoleException If the type given is not implemented.
     */
    private String toString(SystemPermission.Type type)
        throws GuacamoleException {

        switch (type) {
            case CREATE_USER:             return "create-user";
            case CREATE_CONNECTION:       return "create-connection";
            case CREATE_CONNECTION_GROUP: return "create-connection-group";
            case ADMINISTER:              return "admin";
        }

        throw new GuacamoleException("Unknown permission type: " + type);

    }

    /**
     * Returns the XML attribute value representation of the given
     * ObjectPermission.Type.
     *
     * @param type The ObjectPermission.Type to translate into a String.
     * @return The XML attribute value representation of the given
     *         ObjectPermission.Type.
     *
     * @throws GuacamoleException If the type given is not implemented.
     */
    private String toString(ObjectPermission.Type type)
        throws GuacamoleException {

        switch (type) {
            case READ:       return "read";
            case UPDATE:     return "update";
            case DELETE:     return "delete";
            case ADMINISTER: return "admin";
        }

        throw new GuacamoleException("Unknown permission type: " + type);

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

        // Write actual XML
        try {

            User user;

            // Get username
            String username = request.getParameter("user");
            if (username != null) {

                // Get user directory
                Directory<String, User> users = context.getUserDirectory();

                // Get specific user
                user = users.get(username);
            }
            else
                user = context.self();
            
            if (user == null)
                throw new GuacamoleSecurityException("No such user.");

            // Write XML content type
            response.setHeader("Content-Type", "text/xml");

            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xml = outputFactory.createXMLStreamWriter(response.getWriter());

            // Begin document
            xml.writeStartDocument();
            xml.writeStartElement("permissions");
            xml.writeAttribute("user", user.getUsername());

            // For each entry, write corresponding user element
            for (Permission permission : user.getPermissions()) {

                // System permission
                if (permission instanceof SystemPermission) {

                    // Get permission
                    SystemPermission sp = (SystemPermission) permission;

                    // Write permission
                    xml.writeEmptyElement("system");
                    xml.writeAttribute("type", toString(sp.getType()));

                }

                // Config permission
                else if (permission instanceof ConnectionPermission) {

                    // Get permission
                    ConnectionPermission cp =
                            (ConnectionPermission) permission;

                    // Write permission
                    xml.writeEmptyElement("connection");
                    xml.writeAttribute("type", toString(cp.getType()));
                    xml.writeAttribute("name", cp.getObjectIdentifier());

                }

                // Connection group permission
                else if (permission instanceof ConnectionGroupPermission) {

                    // Get permission
                    ConnectionGroupPermission cgp =
                            (ConnectionGroupPermission) permission;

                    // Write permission
                    xml.writeEmptyElement("connection-group");
                    xml.writeAttribute("type", toString(cgp.getType()));
                    xml.writeAttribute("name", cgp.getObjectIdentifier());

                }

                // User permission
                else if (permission instanceof UserPermission) {

                    // Get permission
                    UserPermission up = (UserPermission) permission;

                    // Write permission
                    xml.writeEmptyElement("user");
                    xml.writeAttribute("type", toString(up.getType()));
                    xml.writeAttribute("name", up.getObjectIdentifier());

                }

                else
                    throw new GuacamoleClientException(
                            "Unsupported permission type.");

            }

            // End document
            xml.writeEndElement();
            xml.writeEndDocument();

        }
        catch (XMLStreamException e) {
            throw new GuacamoleServerException(
                    "Unable to write permission list XML.", e);
        }
        catch (IOException e) {
            throw new GuacamoleServerException(
                    "I/O error writing permission list XML.", e);
        }

    }

}
