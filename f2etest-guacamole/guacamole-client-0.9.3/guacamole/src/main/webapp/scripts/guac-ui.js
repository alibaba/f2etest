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

/**
 * Main Guacamole UI namespace.
 * @namespace
 */
var GuacUI = GuacUI || {};

/**
 * Creates a new element having the given tagname and CSS class.
 */
GuacUI.createElement = function(tagname, classname) {
    var new_element = document.createElement(tagname);
    if (classname) new_element.className = classname;
    return new_element;
};

/**
 * Creates a new element having the given tagname, CSS class, and specified
 * parent element.
 */
GuacUI.createChildElement = function(parent, tagname, classname) {
    var element = GuacUI.createElement(tagname, classname);
    parent.appendChild(element);
    return element;
};

/**
 * Creates a new row within the given table having a single header cell
 * with the given title, and a single value cell. The value cell is returned.
 */
GuacUI.createTabulatedContainer = function(table, title) {

    // Create elements
    var row    = GuacUI.createChildElement(table, "tr");
    var header = GuacUI.createChildElement(row, "th");
    var cell   = GuacUI.createChildElement(row, "td");

    // Set title, return cell
    header.textContent = title;
    return cell;

};

/**
 * Adds the given CSS class to the given element.
 */
GuacUI.addClass = function(element, classname) {

    // If supported, use native classlist for addClass()
    if (Node.classlist)
        element.classList.add(classname);

    // Otherwise, simply add new class via string manipulation
    else
        element.className += " " + classname;

};

/**
 * Removes the given CSS class from the given element.
 */
GuacUI.removeClass = function(element, classname) {

    // If supported, use native classlist for removeClass()
    if (Node.classlist)
        element.classList.remove(classname);

    // Otherwise, remove class via string manipulation
    else {

        // Filter out classes with given name
        element.className = element.className.replace(/([^ ]+)[ ]*/g,
            function(match, testClassname, spaces, offset, string) {

                // If same class, remove
                if (testClassname == classname)
                    return "";

                // Otherwise, allow
                return match;
                
            }
        );

    } // end if no classlist support

};

/**
 * Opens the connection group having the given ID in a new tab/window.
 * 
 * @param {String} id The ID of the connection group to open.
 * @param {String} parameters Any parameters that should be added to the URL,
 *                            for sake of authentication.
 */
GuacUI.openConnectionGroup = function(id, parameters) {
    GuacUI.openObject("g/" + id, parameters);
};

/**
 * Opens the connection having the given ID in a new tab/window.
 * 
 * @param {String} id The ID of the connection to open.
 * @param {String} parameters Any parameters that should be added to the URL,
 *                            for sake of authentication.
 */
GuacUI.openConnection = function(id, parameters) {
    GuacUI.openObject("c/" + id, parameters);
};

/**
 * Opens the object having the given ID in a new tab/window. The ID must
 * include the relevant prefix.
 * 
 * @param {String} id The ID of the object to open, including prefix.
 * @param {String} parameters Any parameters that should be added to the URL,
 *                            for sake of authentication.
 */
GuacUI.openObject = function(id, parameters) {

    // Get URL
    var url = "client.xhtml?id=" + encodeURIComponent(id);

    // Add parameters, if given
    if (parameters)
        url += "&" + parameters;

    // Attempt to focus existing window
    var current = window.open(null, id);

    // If window did not already exist, set up as
    // Guacamole client
    if (!current.GuacUI)
        window.open(url, id);

};

/**
 * Object describing the UI's level of audio support. If the user has request
 * that audio be disabled, this object will pretend that audio is not
 * supported.
 */
GuacUI.Audio = new (function() {

    var codecs = [
        'audio/ogg; codecs="vorbis"',
        'audio/mp4; codecs="mp4a.40.5"',
        'audio/mpeg; codecs="mp3"',
        'audio/webm; codecs="vorbis"',
        'audio/wav; codecs=1'
    ];

    var probably_supported = [];
    var maybe_supported = [];

    /**
     * Array of all supported audio mimetypes, ordered by liklihood of
     * working.
     */
    this.supported = [];

    // If sound disabled, we're done now.
    if (GuacamoleSessionStorage.getItem("disable-sound", false))
        return;
    
    // Build array of supported audio formats
    codecs.forEach(function(mimetype) {

        var audio = new Audio();
        var support_level = audio.canPlayType(mimetype);

        // Trim semicolon and trailer
        var semicolon = mimetype.indexOf(";");
        if (semicolon != -1)
            mimetype = mimetype.substring(0, semicolon);

        // Partition by probably/maybe
        if (support_level == "probably")
            probably_supported.push(mimetype);
        else if (support_level == "maybe")
            maybe_supported.push(mimetype);

    });

    // Add probably supported types first
    Array.prototype.push.apply(
        this.supported, probably_supported);

    // Prioritize "maybe" supported types second
    Array.prototype.push.apply(
        this.supported, maybe_supported);

})();

/**
 * Object describing the UI's level of video support.
 */
GuacUI.Video = new (function() {

    var codecs = [
        'video/ogg; codecs="theora, vorbis"',
        'video/mp4; codecs="avc1.4D401E, mp4a.40.5"',
        'video/webm; codecs="vp8.0, vorbis"'
    ];

    var probably_supported = [];
    var maybe_supported = [];

    /**
     * Array of all supported video mimetypes, ordered by liklihood of
     * working.
     */
    this.supported = [];
    
    // Build array of supported audio formats
    codecs.forEach(function(mimetype) {

        var video = document.createElement("video");
        var support_level = video.canPlayType(mimetype);

        // Trim semicolon and trailer
        var semicolon = mimetype.indexOf(";");
        if (semicolon != -1)
            mimetype = mimetype.substring(0, semicolon);

        // Partition by probably/maybe
        if (support_level == "probably")
            probably_supported.push(mimetype);
        else if (support_level == "maybe")
            maybe_supported.push(mimetype);

    });

    // Add probably supported types first
    Array.prototype.push.apply(
        this.supported, probably_supported);

    // Prioritize "maybe" supported types second
    Array.prototype.push.apply(
        this.supported, maybe_supported);

})();

/**
 * A connection UI object which can be easily added to a list of connections
 * for sake of display.
 */
GuacUI.ListConnection = function(connection) {

    /**
     * Reference to this connection.
     * @private
     */
    var guac_connection = this;

    /**
     * The actual connection associated with this connection UI element.
     */
    this.connection = connection;

    /**
     * Fired when this connection is clicked.
     * @event
     */
    this.onclick = null;

    // Create connection display elements
    var element   = GuacUI.createElement("div",  "connection");
    var caption   = GuacUI.createChildElement(element, "div",  "caption");
    var protocol  = GuacUI.createChildElement(caption, "div",  "protocol");
    var name      = GuacUI.createChildElement(caption, "span", "name");
    GuacUI.createChildElement(protocol, "div",  "icon " + connection.protocol);

    element.addEventListener("click", function(e) {

        // Prevent click from affecting parent
        e.stopPropagation();
        e.preventDefault();

        // Fire event if defined
        if (guac_connection.onclick)
            guac_connection.onclick();

    }, false);

    // Set name
    name.textContent = connection.name;

    // Add active usages (if any)
    var active_users = connection.currentUsage();
    if (active_users > 0) {
        var usage = GuacUI.createChildElement(caption, "span", "usage");
        usage.textContent = "Currently in use by " + active_users + " user(s)";
        GuacUI.addClass(element, "in-use");
    }

    /**
     * Returns the DOM element representing this connection.
     */
    this.getElement = function() {
        return element;
    };

};

/**
 * A paging component. Elements can be added via the addElement() function,
 * and will only be shown if they are on the current page, set via setPage().
 * 
 * Beware that all elements will be added to the given container element, and
 * all children of the container element will be removed when the page is
 * changed.
 */
GuacUI.Pager = function(container) {

    var guac_pager = this;

    /**
     * A container for all pager control buttons.
     */
    var element = GuacUI.createElement("div", "pager");

    /**
     * All displayable elements.
     */
    var elements = [];

    /**
     * The number of elements to display per page.
     */
    this.page_capacity = 10;

    /**
     * The number of pages to generate a window for.
     */
    this.window_size = 11;

    /**
     * The current page, where 0 is the first page.
     */
    this.current_page = 0;

    /**
     * The last existing page.
     */
    this.last_page = 0;

    function update_display() {

        var i;

        // Calculate first and last elements of page (where the last element
        // is actually the first element of the next page)
        var first_element = guac_pager.current_page * guac_pager.page_capacity;
        var last_element  = Math.min(elements.length,
                first_element + guac_pager.page_capacity);

        // Clear contents, add elements
        container.innerHTML = "";
        for (i=first_element; i < last_element; i++)
            container.appendChild(elements[i]);

        // Update buttons
        element.innerHTML = "";

        // Create first and prev buttons
        var first = GuacUI.createChildElement(element, "div", "first-page icon");
        var prev = GuacUI.createChildElement(element, "div", "prev-page icon");

        // Handle prev/first
        if (guac_pager.current_page > 0) {
            first.onclick = function() {
                guac_pager.setPage(0);
            };

            prev.onclick = function() {
                guac_pager.setPage(guac_pager.current_page - 1);
            };
        }
        else {
            GuacUI.addClass(first, "disabled");
            GuacUI.addClass(prev, "disabled");
        }

        // Calculate page jump window start/end
        var window_start = guac_pager.current_page - (guac_pager.window_size - 1) / 2;
        var window_end = window_start + guac_pager.window_size - 1;

        // Shift window as necessary
        if (window_start < 0) {
            window_end = Math.min(guac_pager.last_page, window_end - window_start);
            window_start = 0;
        }
        else if (window_end > guac_pager.last_page) {
            window_start = Math.max(0, window_start - window_end + guac_pager.last_page);
            window_end = guac_pager.last_page;
        }
        
        // Add ellipsis if window after beginning
        if (window_start != 0)
            GuacUI.createChildElement(element, "div", "more-pages").textContent = "...";
        
        // Add page jumps
        for (i=window_start; i<=window_end; i++) {

            // Create clickable element containing page number
            var jump = GuacUI.createChildElement(element, "div", "set-page");
            jump.textContent = i+1;
            
            // Mark current page
            if (i == guac_pager.current_page)
                GuacUI.addClass(jump, "current");

            // If not current, add click event
            else
                (function(page_number) {
                    jump.onclick = function() {
                        guac_pager.setPage(page_number);
                    };
                })(i);

        }

        // Add ellipsis if window before end
        if (window_end != guac_pager.last_page)
            GuacUI.createChildElement(element, "div", "more-pages").textContent = "...";
        
        // Create next and last buttons
        var next = GuacUI.createChildElement(element, "div", "next-page icon");
        var last = GuacUI.createChildElement(element, "div", "last-page icon");

        // Handle next/last
        if (guac_pager.current_page < guac_pager.last_page) {
            next.onclick = function() {
                guac_pager.setPage(guac_pager.current_page + 1);
            };
            
            last.onclick = function() {
                guac_pager.setPage(guac_pager.last_page);
            };
        }
        else {
            GuacUI.addClass(next, "disabled");
            GuacUI.addClass(last, "disabled");
        }

    }

    /**
     * Adds the given element to the set of displayable elements.
     */
    this.addElement = function(element) {
        elements.push(element);
        guac_pager.last_page = Math.max(0,
            Math.floor((elements.length - 1) / guac_pager.page_capacity));
    };

    /**
     * Sets the current page, where 0 is the first page.
     */
    this.setPage = function(number) {
        guac_pager.current_page = number;
        update_display();
    };

    /**
     * Returns the element representing the buttons of this pager.
     */
    this.getElement = function() {
        return element;
    };

};

/**
 * Interface object which displays the progress of a download, ultimately
 * becoming a download link once complete.
 * 
 * @constructor
 * @param {String} filename The name the file will have once complete.
 */
GuacUI.Download = function(filename) {

    /**
     * Reference to this GuacUI.Download.
     * @private
     */
    var guac_download = this;

    /**
     * The outer div representing the notification.
     * @private
     */
    var element = GuacUI.createElement("div", "download notification");

    /**
     * Title bar describing the notification.
     * @private
     */
    var title = GuacUI.createChildElement(element, "div", "title-bar");

    /**
     * Close button for removing the notification.
     * @private
     */
    var close_button = GuacUI.createChildElement(title, "div", "close");
    close_button.onclick = function() {
        if (guac_download.onclose)
            guac_download.onclose();
    };

    GuacUI.createChildElement(title, "div", "title").textContent =
        "File Transfer";

    GuacUI.createChildElement(element, "div", "caption").textContent =
        filename + " ";

    /**
     * Progress bar and status.
     * @private
     */
    var progress = GuacUI.createChildElement(element, "div", "progress");

    /**
     * Updates the content of the progress indicator with the given text.
     * 
     * @param {String} text The text to assign to the progress indicator.
     */
    this.updateProgress = function(text) {
        progress.textContent = text;
    };

    /**
     * Updates the content of the dialog to reflect an error condition
     * represented by the given text.
     * 
     * @param {String} text A human-readable description of the error.
     */
    this.showError = function(text) {

        element.removeChild(progress);
        GuacUI.addClass(element, "error");

        var status = GuacUI.createChildElement(element, "div", "status");
        status.textContent = text;

    };

    /**
     * Removes the progress indicator and replaces it with a download button.
     */
    this.complete = function() {

        element.removeChild(progress);
        GuacUI.addClass(element, "complete");

        var download = GuacUI.createChildElement(element, "button");
        download.textContent = "Download";
        download.onclick = function() {
            if (guac_download.ondownload)
                guac_download.ondownload();
        };

    };

    /**
     * Returns the element representing this notification.
     */
    this.getElement = function() {
        return element;
    };

    /**
     * Called when the close button of this notification is clicked.
     * @event
     */
    this.onclose = null;

    /**
     * Called when the download button of this notification is clicked.
     * @event
     */
    this.ondownload = null;

};

/**
 * Interface object which displays the progress of a upload.
 * 
 * @constructor
 * @param {String} filename The name the file will have once complete.
 */
GuacUI.Upload = function(filename) {

    /**
     * Reference to this GuacUI.Upload.
     * @private
     */
    var guac_upload = this;

    /**
     * The outer div representing the notification.
     * @private
     */
    var element = GuacUI.createElement("div", "upload notification");

    /**
     * Title bar describing the notification.
     * @private
     */
    var title = GuacUI.createChildElement(element, "div", "title-bar");

    /**
     * Close button for removing the notification.
     * @private
     */
    var close_button = GuacUI.createChildElement(title, "div", "close");
    close_button.onclick = function() {
        if (guac_upload.onclose)
            guac_upload.onclose();
    };

    GuacUI.createChildElement(title, "div", "title").textContent =
        "File Transfer";

    GuacUI.createChildElement(element, "div", "caption").textContent =
        filename + " ";

    /**
     * Progress bar and status.
     * @private
     */
    var progress = GuacUI.createChildElement(element, "div", "progress");

    /**
     * The actual moving bar within the progress bar.
     * @private
     */
    var bar = GuacUI.createChildElement(progress, "div", "bar");

    /**
     * The textual readout of progress.
     * @private
     */
    var progress_status = GuacUI.createChildElement(progress, "div");

    /**
     * Updates the content of the progress indicator with the given text.
     * 
     * @param {String} text The text to assign to the progress indicator.
     * @param {Number} percent The overall percent complete.
     */
    this.updateProgress = function(text, percent) {
        progress_status.textContent = text;
        bar.style.width = percent + "%";
    };

    /**
     * Updates the content of the dialog to reflect an error condition
     * represented by the given text.
     * 
     * @param {String} text A human-readable description of the error.
     */
    this.showError = function(text) {

        element.removeChild(progress);
        GuacUI.addClass(element, "error");

        var status = GuacUI.createChildElement(element, "div", "status");
        status.textContent = text;

    };

    /**
     * Returns the element representing this notification.
     */
    this.getElement = function() {
        return element;
    };

    /**
     * Called when the close button of this notification is clicked.
     * @event
     */
    this.onclose = null;

};

/**
 * A grouping component. Child elements can be added via the addElement()
 * function. By default, groups display as collapsed.
 */
GuacUI.ListGroup = function(caption) {

    /**
     * Reference to this group.
     * @private
     */
    var guac_group = this;

    /**
     * Whether this group is empty.
     * @private
     */
    var empty = true;

    /**
     * A container for for the list group itself.
     */
    var element = GuacUI.createElement("div", "group empty");

    // Create connection display elements
    var caption_element = GuacUI.createChildElement(element, "div",  "caption");
    var caption_icon    = GuacUI.createChildElement(caption_element, "div",  "icon group");
    GuacUI.createChildElement(caption_element, "div",  "icon type");
    GuacUI.createChildElement(caption_element, "span", "name").textContent = caption;

    /**
     * A container for all children of this list group.
     */
    var elements = GuacUI.createChildElement(element, "div", "children");

    /**
     * Whether this group is expanded.
     * 
     * @type Boolean
     */
    this.expanded = false;

    /**
     * Fired when this group is clicked.
     * @event
     */
    this.onclick = null;

    /**
     * Returns the element representing this notification.
     */
    this.getElement = function() {
        return element;
    };

    /**
     * Adds an element as a child of this group.
     */
    this.addElement = function(child) {

        // Mark as non-empty
        if (empty) {
            GuacUI.removeClass(element, "empty");
            empty = false;
        }

        elements.appendChild(child);

    };

    /**
     * Expands the list group, revealing all children of the group. This
     * functionality requires supporting CSS.
     */
    this.expand = function() {
        GuacUI.addClass(element, "expanded");
        guac_group.expanded = true;
    };

    /**
     * Collapses the list group, hiding all children of the group. This
     * functionality requires supporting CSS.
     */
    this.collapse = function() {
        GuacUI.removeClass(element, "expanded");
        guac_group.expanded = false;
    };

    // Toggle when icon is clicked
    caption_icon.addEventListener("click", function(e) {

        // Prevent click from affecting parent
        e.stopPropagation();
        e.preventDefault();

        if (guac_group.expanded)
            guac_group.collapse();
        else
            guac_group.expand();

    }, false);

    // Fire event when any other part is clicked
    element.addEventListener("click", function(e) {

        // Prevent click from affecting parent
        e.stopPropagation();
        e.preventDefault();

        // Fire event if defined
        if (guac_group.onclick)
            guac_group.onclick();

    }, false);

}

/**
 * Component which displays a paginated tree view of all groups and their
 * connections.
 * 
 * @constructor
 * @param {GuacamoleService.ConnectionGroup} root_group The group to display
 *                                                      within the view.
 * @param {Number} flags Any flags (such as MULTISELECT or SHOW_CONNECTIONS)
 *                       for modifying the behavior of this group view.
 * @param {Function} group_filter Function which returns true if the given
 *                                group should be displayed and false otherwise.
 * @param {Function} connection_filter Function which returns true if the given
 *                                     connection should be displayed and false
 *                                     otherwise.
 */
GuacUI.GroupView = function(root_group, flags,
    group_filter, connection_filter) {

    /**
     * Reference to this GroupView.
     * @private
     */
    var group_view = this;

    // Group view components
    var element = GuacUI.createElement("div", "group-view");
    var list = GuacUI.createChildElement(element, "div", "list");

    /**
     * Whether multiselect is enabled.
     */
    var multiselect = flags & GuacUI.GroupView.MULTISELECT;

    /**
     * Whether connections should be included in the view.
     */
    var show_connections = flags & GuacUI.GroupView.SHOW_CONNECTIONS;

    /**
     * Whether the root group should be included in the view.
     */
    var show_root = flags & GuacUI.GroupView.SHOW_ROOT_GROUP;

    /**
     * Set of all group checkboxes, indexed by ID. Only applicable when
     * multiselect is enabled.
     * @private
     */
    var group_checkboxes = {};

    /**
     * Set of all connection checkboxes, indexed by ID. Only applicable when
     * multiselect is enabled.
     * @private
     */
    var connection_checkboxes = {};

    /**
     * Set of all list groups, indexed by associated group ID.
     * @private
     */
    var list_groups = {};

    /**
     * Set of all connection groups, indexed by ID.
     */
    this.groups = {};

    /**
     * Set of all connections, indexed by ID.
     */
    this.connections = {};

    /**
     * Fired when a connection is clicked.
     *
     * @event
     * @param {GuacamolService.Connection} connection The connection which was
     *                                                clicked.
     */
    this.onconnectionclick = null;

    /**
     * Fired when a connection group is clicked.
     *
     * @event
     * @param {GuacamolService.ConnectionGroup} group The connection group which 
     *                                                was clicked.
     */
    this.ongroupclick = null;

    /**
     * Fired when a connection's selected status changes.
     *
     * @event
     * @param {GuacamolService.Connection} connection The connection whose
     *                                                status changed.
     * @param {Boolean} selected The new status of the connection.
     */
    this.onconnectionchange = null;

    /**
     * Fired when a connection group's selected status changes.
     *
     * @event
     * @param {GuacamolService.ConnectionGroup} group The connection group whose
     *                                                status changed.
     * @param {Boolean} selected The new status of the connection group.
     */
    this.ongroupchange = null;

    /**
     * Returns the element representing this group view.
     */
    this.getElement = function() {
        return element;
    };

    /**
     * Sets whether the group with the given ID can be selected. This function
     * only has an effect when multiselect is enabled.
     * 
     * @param {String} id The ID of the group to alter.
     * @param {Boolean} value Whether the group should be selected.
     */
    this.setGroupEnabled = function(id, value) {

        var checkbox = group_checkboxes[id];
        if (!checkbox)
            return;

        // If enabled, show checkbox, allow select
        if (value) {
            checkbox.style.visibility = "";
            checkbox.disabled = false;
        }

        // Otherwise, hide checkbox
        else {
            checkbox.style.visibility = "hidden";
            checkbox.disabled = true;
        }

    };

    /**
     * Sets whether the connection with the given ID can be selected. This
     * function only has an effect when multiselect is enabled.
     * 
     * @param {String} id The ID of the connection to alter.
     * @param {Boolean} value Whether the connection can be selected.
     */
    this.setConnectionEnabled = function(id, value) {

        var checkbox = connection_checkboxes[id];
        if (!checkbox)
            return;

        // If enabled, show checkbox, allow select
        if (value) {
            checkbox.style.visibility = "";
            checkbox.disabled = false;
        }

        // Otherwise, hide checkbox
        else {
            checkbox.style.visibility = "hidden";
            checkbox.disabled = true;
        }

    };

    /**
     * Sets the current value of the group with the given ID. This function
     * only has an effect when multiselect is enabled.
     * 
     * @param {String} id The ID of the group to change.
     * @param {Boolean} value Whether the group should be selected.
     */
    this.setGroupValue = function(id, value) {

        var checkbox = group_checkboxes[id];
        if (!checkbox)
            return;

        checkbox.checked = value;

    };

    /**
     * Sets the current value of the connection with the given ID. This function
     * only has an effect when multiselect is enabled.
     * 
     * @param {String} id The ID of the connection to change.
     * @param {Boolean} value Whether the connection should be selected.
     */
    this.setConnectionValue = function(id, value) {

        var checkbox = connection_checkboxes[id];
        if (!checkbox)
            return;

        checkbox.checked = value;

    };

    /**
     * Expands the given group and all parent groups all the way up to root.
     * 
     * @param {GuacamoleService.ConnectionGroup} group The group that should
     *                                                 be expanded.
     */
    this.expand = function(group) {

        // Skip current group - only need to expand parents
        group = group.parent;

        // For each group all the way to root
        while (group !== null) {

            // If list group exists, expand it
            var list_group = list_groups[group.id];
            if (list_group)
                list_group.expand();

            group = group.parent;
        }

    }

    // Create pager for contents 
    var pager = new GuacUI.Pager(list);
    pager.page_capacity = 20;

    /**
     * Adds the contents of the given group via the given appendChild()
     * function, but not the given group itself.
     * 
     * @param {GuacamoleService.ConnectionGroup} group The group whose contents
     *                                                 should be added.
     * @param {Function} appendChild A function which, given an element, will
     *                               add that element the the display as
     *                               desired.
     */
    function addGroupContents(group, appendChild) {

        var i;

        // Add all contained connections
        if (show_connections) {
            for (i=0; i<group.connections.length; i++)
                addConnection(group.connections[i], appendChild);
        }

        // Add all contained groups 
        for (i=0; i<group.groups.length; i++)
            addGroup(group.groups[i], appendChild);

    }

    /**
     * Adds the given connection via the given appendChild() function.
     * 
     * @param {GuacamoleService.Connection} connection The connection to add.
     * @param {Function} appendChild A function which, given an element, will
     *                               add that element the the display as
     *                               desired.
     */
    function addConnection(connection, appendChild) {

        // Do not add connection if filter says "no"
        if (connection_filter && !connection_filter(connection))
            return;

        group_view.connections[connection.id] = connection;

        // Add connection to connection list or parent group
        var guacui_connection = new GuacUI.ListConnection(connection);
        GuacUI.addClass(guacui_connection.getElement(), "list-item");

        // If multiselect, add checkbox for each connection
        if (multiselect) {

            var connection_choice = GuacUI.createElement("div", "choice");
            var connection_checkbox = GuacUI.createChildElement(connection_choice, "input");
            connection_checkbox.setAttribute("type", "checkbox");
            
            connection_choice.appendChild(guacui_connection.getElement());
            appendChild(connection_choice);

            function fire_connection_change(e) {

                // Prevent click from affecting parent
                e.stopPropagation();

                // Fire event if handler defined
                if (group_view.onconnectionchange)
                    group_view.onconnectionchange(connection, this.checked);

            }

            // Fire change events when checkbox modified
            connection_checkbox.addEventListener("click",  fire_connection_change, false);
            connection_checkbox.addEventListener("change", fire_connection_change, false);

            // Add checbox to set of connection checkboxes
            connection_checkboxes[connection.id] = connection_checkbox;

        }
        else
            appendChild(guacui_connection.getElement());

        // Fire click events when connection clicked
        guacui_connection.onclick = function() {
            if (group_view.onconnectionclick)
                group_view.onconnectionclick(connection);
        };

    }

    /**
     * Adds the given group via the given appendChild() function.
     * 
     * @param {GuacamoleService.ConnectionGroup} group The group to add.
     * @param {Function} appendChild A function which, given an element, will
     *                               add that element the the display as
     *                               desired.
     */
    function addGroup(group, appendChild) {

        // Do not add group if filter says "no"
        if (group_filter && !group_filter(group))
            return;

        // Add group to groups collection
        group_view.groups[group.id] = group;

        // Create element for group
        var list_group = new GuacUI.ListGroup(group.name);
        list_groups[group.id] = list_group;
        GuacUI.addClass(list_group.getElement(), "list-item");

        // Mark group as balancer if appropriate
        if (group.type === GuacamoleService.ConnectionGroup.Type.BALANCING)
            GuacUI.addClass(list_group.getElement(), "balancer");

        // Recursively add all children to the new element
        addGroupContents(group, list_group.addElement);

        // If multiselect, add checkbox for each group
        if (multiselect) {

            var group_choice = GuacUI.createElement("div", "choice");
            var group_checkbox = GuacUI.createChildElement(group_choice, "input");
            group_checkbox.setAttribute("type", "checkbox");
            
            group_choice.appendChild(list_group.getElement());
            appendChild(group_choice);

            function fire_group_change(e) {

                // Prevent click from affecting parent
                e.stopPropagation();

                // Fire event if handler defined
                if (group_view.ongroupchange)
                    group_view.ongroupchange(group, this.checked);

            }

            // Fire change events when checkbox modified
            group_checkbox.addEventListener("click",  fire_group_change, false);
            group_checkbox.addEventListener("change", fire_group_change, false);

            // Add checbox to set of group checkboxes
            group_checkboxes[group.id] = group_checkbox;

        }
        else
            appendChild(list_group.getElement());

        // Fire click events when group clicked
        list_group.onclick = function() {
            if (group_view.ongroupclick)
                group_view.ongroupclick(group);
        };

    }

    // If requested, include the root group as an item
    if (show_root) {
        addGroup(root_group, pager.addElement);
        list_groups[root_group.id].expand();
    }

    // Otherwise, only add contents of root group
    else
        addGroupContents(root_group, pager.addElement);

    // Add buttons if more than one page
    if (pager.last_page !== 0) {
        var list_buttons = GuacUI.createChildElement(element, "div", "buttons");
        list_buttons.appendChild(pager.getElement());
    }

    // Start at page 0
    pager.setPage(0);

};

/**
 * When set, allows multiple groups (or connections to be selected).
 */
GuacUI.GroupView.MULTISELECT = 0x1;

/**
 * When set, also displays connections within the visible groups.
 */
GuacUI.GroupView.SHOW_CONNECTIONS = 0x2;

/**
 * When set, also displays the root group. By default the root group is hidden.
 */
GuacUI.GroupView.SHOW_ROOT_GROUP = 0x4;

/**
 * Simple modal dialog providing a header, body, and footer. No other
 * functionality is provided other than a reasonable hierarchy of divs and
 * easy access to their corresponding elements.
 */
GuacUI.Dialog = function() {

    /**
     * The container of the entire dialog. Adding this element to the DOM
     * displays the dialog, while removing this element hides the dialog.
     * 
     * @private
     * @type Element
     */
    var element = GuacUI.createElement("div", "dialog-container");

    /**
     * The dialog itself. This element is not exposed outside this object,
     * but rather contains the header, body, and footer sections which are
     * exposed.
     * 
     * @private
     * @type Element
     */
    var dialog = GuacUI.createChildElement(element, "div", "dialog");

    /**
     * The header section of the dialog. This section would normally contain
     * the title.
     * 
     * @private
     * @type Element
     */
    var header = GuacUI.createChildElement(dialog, "div", "header");

    /**
     * The body section of the dialog. This section would normally contain any
     * form fields and content.
     * 
     * @private
     * @type Element
     */
    var body = GuacUI.createChildElement(dialog, "div", "body");

    /**
     * The footer section of the dialog. This section would normally contain
     * the buttons.
     * 
     * @private
     * @type Element
     */
    var footer = GuacUI.createChildElement(dialog, "div", "footer");

    /**
     * Returns the header section of this dialog. This section normally
     * contains the title of the dialog.
     * 
     * @return {Element} The header section of this dialog.
     */
    this.getHeader = function() {
        return header;
    };

    /**
     * Returns the body section of this dialog. This section normally contains
     * the form fields, etc. of a dialog.
     * 
     * @return {Element} The body section of this dialog.
     */
    this.getBody = function() {
        return body;
    };

    /**
     * Returns the footer section of this dialog. This section is normally
     * used to contain the buttons of the dialog.
     * 
     * @return {Element} The footer section of this dialog.
     */
    this.getFooter = function() {
        return footer;
    };

    /**
     * Returns the element representing this dialog. Adding this element to
     * the DOM shows the dialog, while removing this element hides the dialog.
     * 
     * @return {Element} The element representing this dialog.
     */
    this.getElement = function() {
        return element;
    };

};
