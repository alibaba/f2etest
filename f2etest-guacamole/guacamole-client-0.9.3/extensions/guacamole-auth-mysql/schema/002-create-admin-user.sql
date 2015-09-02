--
-- Copyright (C) 2013 Glyptodon LLC
--
-- Permission is hereby granted, free of charge, to any person obtaining a copy
-- of this software and associated documentation files (the "Software"), to deal
-- in the Software without restriction, including without limitation the rights
-- to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
-- copies of the Software, and to permit persons to whom the Software is
-- furnished to do so, subject to the following conditions:
--
-- The above copyright notice and this permission notice shall be included in
-- all copies or substantial portions of the Software.
--
-- THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
-- IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
-- FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
-- AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
-- LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
-- OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
-- THE SOFTWARE.
--

-- Create default user "guacadmin" with password "guacadmin"
insert into guacamole_user values(1, 'guacadmin',
    x'CA458A7D494E3BE824F5E1E175A1556C0F8EEF2C2D7DF3633BEC4A29C4411960',  -- 'guacadmin'
    x'FE24ADC5E11E2B25288D1704ABE67A79E342ECC26064CE69C5B3177795A82264');

-- Grant this user create permissions
insert into guacamole_system_permission values(1, 'CREATE_CONNECTION');
insert into guacamole_system_permission values(1, 'CREATE_CONNECTION_GROUP');
insert into guacamole_system_permission values(1, 'CREATE_USER');
insert into guacamole_system_permission values(1, 'ADMINISTER');

-- Grant admin permission to read/update/administer self
insert into guacamole_user_permission values(1, 1, 'READ');
insert into guacamole_user_permission values(1, 1, 'UPDATE');
insert into guacamole_user_permission values(1, 1, 'ADMINISTER');

