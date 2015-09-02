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
 * Global storage for Guacamole pages. 
 */
var openerGuacamoleSessionStorage;
try{
    openerGuacamoleSessionStorage = opener && opener.GuacamoleSessionStorage;
}
catch(e){}

GuacamoleSessionStorage = openerGuacamoleSessionStorage || new (function() {

    /**
     * The contents of storage, as a JSON string containing name/value pairs as
     * properties.
     * 
     * @private
     * @type String
     */
    var stored_json = "{}";

    /**
     * Called whenever an item value changes.
     *
     * @callback onchange
     * @param {String} name The name of the item changed.
     * @param value The new item value.
     */

    /**
     * All attached listeners.
     * 
     * @type onchange[]
     */
    var listeners = [];

    /**
     * Notifies all listeners that an item has changed.
     * 
     * @param {String} name The name of the item that changed.
     * @param value The new item value.
     */
    function __notify_changed(name, value) {
        for (var i=0; i<listeners.length; i++)
            listeners[i](name, value);
    }

    /**
     * Returns the value stored within the item having the given name.
     * 
     * @param {String} name The name of the item to read.
     * @param [value] The default value, if any.
     * @return The value of the given item.
     */
    this.getItem = function(name, value) {

        // Attempt to read JSON from localStorage, default to local variable
        var json = stored_json;
        if (localStorage) {
            try {
                json = localStorage.getItem("GUACAMOLE_STATE") || "{}";
            }
            catch (ignore) {}
        }

        var obj = JSON.parse(json);
        if (obj[name] !== undefined)
            return obj[name];

        return value;

    };

    /**
     * Sets the item having the given name to the given value.
     * 
     * @param {String} name The name of the item to change.
     * @param [value] An arbitrary value.
     */
    this.setItem = function(name, value) {

        // Attempt to read JSON from localStorage, default to local variable
        var json = stored_json;
        if (localStorage) {
            try {
                json = localStorage.getItem("GUACAMOLE_STATE") || "{}";
            }
            catch (ignore) {}
        }

        // Modify object property        
        var obj = JSON.parse(json);
        var old = obj[name];
        obj[name] = value;

        // Notify of change
        if (old !== value)
            __notify_changed(name, value);

        // Attempt to set JSON within localStorage, default to local variable
        stored_json = JSON.stringify(obj);
        if (localStorage) {
            try {
                localStorage.setItem("GUACAMOLE_STATE", stored_json);
            }
            catch (ignore) {}
        }

    };

    // Reload when modified
    window.addEventListener("storage", function reload() {

        // Pull current state
        var new_json = localStorage.getItem("GUACAMOLE_STATE") || "{}";
        
        var new_state = JSON.parse(new_json);
        var old_state = JSON.parse(stored_json);

        // Check if any values are different
        for (var name in new_state) {

            // If value changed, notify
            var old = old_state[name];
            if (old !== new_state[name])
                __notify_changed(name, new_state[name]);

        }

        stored_json = new_json;

    }, false);

    /**
     * Ensures that the given function will be called for each change in
     * item value. The function must accept a single argument which will be
     * the name of the item changed.
     * 
     * @param {onchange} onchange The function to call when an item changes.
     */
    this.addChangeListener = function(onchange) {
        listeners.push(onchange);
    };

})();
