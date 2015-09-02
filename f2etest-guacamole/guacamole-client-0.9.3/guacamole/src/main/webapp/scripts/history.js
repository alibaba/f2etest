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
 * Set of thumbnails for each connection, indexed by ID.
 */
GuacamoleHistory = new (function() {

    /**
     * Reference to this GuacamoleHistory.
     */
    var guac_history = this;

    /**
     * The number of entries to allow before removing old entries based on the
     * cutoff.
     */
    var IDEAL_LENGTH = 6;

    /**
     * The maximum age of a history entry before it is removed, in
     * milliseconds.
     */
    var CUTOFF_AGE = 900000;

    var history = {};

    function truncate() {

        // Build list of entries
        var entries = [];
        for (var old_id in history)
            entries.push(history[old_id]);

        // Avoid history growth beyond defined number of entries
        if (entries.length > IDEAL_LENGTH) {

            // Sort list
            entries.sort(GuacamoleHistory.Entry.compare);

            // Remove entries until length is ideal or all are recent
            var now = new Date().getTime();
            while (entries.length > IDEAL_LENGTH 
                    && now - entries[0].accessed > CUTOFF_AGE) {

                // Remove entry
                var removed = entries.shift();
                delete history[removed.id];

            }

        }

    }

    /**
     * Returns the URL for the thumbnail of the connection with the given ID,
     * or undefined if no thumbnail is associated with that connection.
     */
    this.get = function(id) {
        return history[id] || new GuacamoleHistory.Entry();
    };

    /**
     * Updates the thumbnail and access time of the history entry for the
     * connection with the given ID.
     */
    this.update = function(id, thumbnail) {

        /* Do nothing if localStorage not present */
        if (!localStorage)
            return;

        // Create updated entry
        var entry = new GuacamoleHistory.Entry(
            id,
            thumbnail,
            new Date().getTime()
        );

        // Store entry in history
        history[id] = entry;
        truncate();

        // Save updated history, ignore inability to use localStorage
        try {
            localStorage.setItem("GUAC_HISTORY", JSON.stringify(history));
        }
        catch (ignore) {}

    };

    /**
     * Reloads all history data.
     */
    this.reload = function() {

        /* Do nothing if localStorage not present */
        if (!localStorage)
            return;

        // Get old and new for comparison, ignore inability to use localStorage
        var old_history = history;
        try {
            var new_history = JSON.parse(localStorage.getItem("GUAC_HISTORY") || "{}");
        }
        catch (ignore) {
            return;
        }

        // Update history
        history = new_history;

        // Call onchange handler as necessary
        if (guac_history.onchange) {

            // Produce union of all known IDs
            var known_ids = {};
            for (var new_id in new_history) known_ids[new_id] = true;
            for (var old_id in old_history) known_ids[old_id] = true;

            // For each known ID
            for (var id in known_ids) {

                // Get entries
                var old_entry = old_history[id];    
                var new_entry = new_history[id];    

                // Call handler for all changed 
                if (!old_entry || !new_entry
                        || old_entry.accessed != new_entry.accessed)
                    guac_history.onchange(id, old_entry, new_entry);

            }

        } // end onchange

    };

    /**
     * Event handler called whenever a history entry is changed.
     * 
     * @event
     * @param {String} id The ID of the connection whose history entry is
     *                    changing.
     * @param {GuacamoleHistory.Entry} old_entry The old value of the entry, if
     *                                           any.
     * @param {GuacamoleHistory.Entry} new_entry The new value of the entry, if
     *                                           any.
     */
    this.onchange = null;

    // Reload when modified
    window.addEventListener("storage", guac_history.reload, false);

    // Initial load
    guac_history.reload();

})();

/**
 * A single entry in the indexed connection usage history.
 * 
 * @constructor
 * @param {String} id The ID of this connection.
 * @param {String} thumbnail The URL of the thumbnail to use to represent this
 *                           connection.
 * @param {Number} last_access The time this connection was last accessed, in
 *                             seconds.
 */
GuacamoleHistory.Entry = function(id, thumbnail, last_access) {

    /**
     * The ID of the connection associated with this history entry.
     */
    this.id = id;

    /**
     * The thumbnail associated with the connection associated with this history
     * entry.
     */
    this.thumbnail = thumbnail;

    /**
     * The time the connection associated with this entry was last accessed.
     */
    this.accessed = last_access;

};
 
GuacamoleHistory.Entry.compare = function(a, b) {
    return a.accessed - b.accessed;
};
