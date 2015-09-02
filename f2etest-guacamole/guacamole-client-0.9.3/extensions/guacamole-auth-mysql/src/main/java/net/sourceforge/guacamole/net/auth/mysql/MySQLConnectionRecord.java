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

package net.sourceforge.guacamole.net.auth.mysql;


import java.util.Date;
import org.glyptodon.guacamole.net.auth.ConnectionRecord;

/**
 * A ConnectionRecord which is based on data stored in MySQL.
 *
 * @author James Muehlner
 */
public class MySQLConnectionRecord implements ConnectionRecord {

    /**
     * The start date of the ConnectionRecord.
     */
    private Date startDate;

    /**
     * The end date of the ConnectionRecord.
     */
    private Date endDate;

    /**
     * The name of the user that is associated with this ConnectionRecord.
     */
    private String username;

    /**
     * Whether this connection is currently active.
     */
    private boolean active;
    
    /**
     * Initialize this MySQLConnectionRecord with the start/end dates,
     * and the name of the user it represents.
     *
     * @param startDate The start date of the connection history.
     * @param endDate The end date of the connection history.
     * @param username The name of the user that used the connection.
     * @param active Whether the connection is currently active.
     */
    public MySQLConnectionRecord(Date startDate, Date endDate,
            String username, boolean active) {
        if (startDate != null) this.startDate = new Date(startDate.getTime());
        if (endDate != null) this.endDate = new Date(endDate.getTime());
        this.username = username;
        this.active = active;
    }

    @Override
    public Date getStartDate() {
        if (startDate == null) return null;
        return new Date(startDate.getTime());
    }

    @Override
    public Date getEndDate() {
        if (endDate == null) return null;
        return new Date(endDate.getTime());
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isActive() {
        return active;
    }

}
