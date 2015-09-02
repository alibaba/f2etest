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

var Guacamole = Guacamole || {};

/**
 * Dynamic on-screen keyboard. Given the URL to an XML keyboard layout file,
 * this object will download and use the XML to construct a clickable on-screen
 * keyboard with its own key events.
 * 
 * @constructor
 * @param {String} url The URL of an XML keyboard layout file.
 */
Guacamole.OnScreenKeyboard = function(url) {

    var on_screen_keyboard = this;

    /**
     * State of all modifiers. This is the bitwise OR of all active modifier
     * values.
     * 
     * @private
     */
    var modifiers = 0;

    /**
     * Map of currently-set modifiers to the keysym associated with their
     * original press. When the modifier is cleared, this keysym must be
     * released.
     *
     * @type Object.<String, Number>
     */
    var modifier_keysyms = {};

    var scaledElements = [];
    
    var modifier_masks = {};
    var next_mask = 1;

    /**
     * Adds a class to an element.
     * 
     * @private
     * @function
     * @param {Element} element The element to add a class to.
     * @param {String} classname The name of the class to add.
     */
    var addClass;

    /**
     * Removes a class from an element.
     * 
     * @private
     * @function
     * @param {Element} element The element to remove a class from.
     * @param {String} classname The name of the class to remove.
     */
    var removeClass;

    /**
     * The number of mousemove events to require before re-enabling mouse
     * event handling after receiving a touch event.
     */
    this.touchMouseThreshold = 3;

    /**
     * Counter of mouse events to ignore. This decremented by mousemove, and
     * while non-zero, mouse events will have no effect.
     * @private
     */
    var ignore_mouse = 0;

    // Ignore all pending mouse events when touch events are the apparent source
    function ignorePendingMouseEvents() { ignore_mouse = on_screen_keyboard.touchMouseThreshold; }

    // If Node.classList is supported, implement addClass/removeClass using that
    if (Node.classList) {

        /** @ignore */
        addClass = function(element, classname) {
            element.classList.add(classname);
        };
        
        /** @ignore */
        removeClass = function(element, classname) {
            element.classList.remove(classname);
        };
        
    }

    // Otherwise, implement own
    else {

        /** @ignore */
        addClass = function(element, classname) {

            // Simply add new class
            element.className += " " + classname;

        };
        
        /** @ignore */
        removeClass = function(element, classname) {

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

        };
        
    }

    // Returns a unique power-of-two value for the modifier with the
    // given name. The same value will be returned for the same modifier.
    function getModifierMask(name) {
        
        var value = modifier_masks[name];
        if (!value) {

            // Get current modifier, advance to next
            value = next_mask;
            next_mask <<= 1;

            // Store value of this modifier
            modifier_masks[name] = value;

        }

        return value;
            
    }

    function ScaledElement(element, width, height, scaleFont) {

        this.width = width;
        this.height = height;

        this.scale = function(pixels) {
            element.style.width      = (width  * pixels) + "px";
            element.style.height     = (height * pixels) + "px";

            if (scaleFont) {
                element.style.lineHeight = (height * pixels) + "px";
                element.style.fontSize   = pixels + "px";
            }
        }

    }

    // For each child of element, call handler defined in next
    function parseChildren(element, next) {

        var children = element.childNodes;
        for (var i=0; i<children.length; i++) {

            // Get child node
            var child = children[i];

            // Do not parse text nodes
            if (!child.tagName)
                continue;

            // Get handler for node
            var handler = next[child.tagName];

            // Call handler if defined
            if (handler)
                handler(child);

            // Throw exception if no handler
            else
                throw new Error(
                      "Unexpected " + child.tagName
                    + " within " + element.tagName
                );

        }

    }

    // Create keyboard
    var keyboard = document.createElement("div");
    keyboard.className = "guac-keyboard";

    // Retrieve keyboard XML
    var xmlhttprequest = new XMLHttpRequest();
    xmlhttprequest.open("GET", url, false);
    xmlhttprequest.send(null);

    var xml = xmlhttprequest.responseXML;

    if (xml) {

        function parse_row(e) {
            
            var row = document.createElement("div");
            row.className = "guac-keyboard-row";

            parseChildren(e, {
                
                "column": function(e) {
                    row.appendChild(parse_column(e));
                },
                
                "gap": function parse_gap(e) {

                    // Create element
                    var gap = document.createElement("div");
                    gap.className = "guac-keyboard-gap";

                    // Set gap size
                    var gap_units = 1;
                    if (e.getAttribute("size"))
                        gap_units = parseFloat(e.getAttribute("size"));

                    scaledElements.push(new ScaledElement(gap, gap_units, gap_units));
                    row.appendChild(gap);

                },
                
                "key": function parse_key(e) {
                    
                    // Create element
                    var key_element = document.createElement("div");
                    key_element.className = "guac-keyboard-key";

                    // Append class if specified
                    if (e.getAttribute("class"))
                        key_element.className += " " + e.getAttribute("class");

                    // Position keys using container div
                    var key_container_element = document.createElement("div");
                    key_container_element.className = "guac-keyboard-key-container";
                    key_container_element.appendChild(key_element);

                    // Create key
                    var key = new Guacamole.OnScreenKeyboard.Key();

                    // Set key size
                    var key_units = 1;
                    if (e.getAttribute("size"))
                        key_units = parseFloat(e.getAttribute("size"));

                    key.size = key_units;

                    parseChildren(e, {
                        "cap": function parse_cap(e) {

                            // TODO: Handle "sticky" attribute
                            
                            // Get content of key cap
                            var content = e.textContent || e.text;

                            // If read as blank, assume cap is a single space.
                            if (content.length == 0)
                                content = " ";
                            
                            // Get keysym
                            var real_keysym = null;
                            if (e.getAttribute("keysym"))
                                real_keysym = parseInt(e.getAttribute("keysym"));

                            // If no keysym specified, try to get from key content
                            else if (content.length == 1) {

                                var charCode = content.charCodeAt(0);
                                if (charCode >= 0x0000 && charCode <= 0x00FF)
                                    real_keysym = charCode;
                                else if (charCode >= 0x0100 && charCode <= 0x10FFFF)
                                    real_keysym = 0x01000000 | charCode;

                            }
                            
                            // Create cap
                            var cap = new Guacamole.OnScreenKeyboard.Cap(content, real_keysym);

                            if (e.getAttribute("modifier"))
                                cap.modifier = e.getAttribute("modifier");
                            
                            // Create cap element
                            var cap_element = document.createElement("div");
                            cap_element.className = "guac-keyboard-cap";
                            cap_element.textContent = content;
                            key_element.appendChild(cap_element);

                            // Append class if specified
                            if (e.getAttribute("class"))
                                cap_element.className += " " + e.getAttribute("class");

                            // Get modifier value
                            var modifierValue = 0;
                            if (e.getAttribute("if")) {

                                // Get modifier value for specified comma-delimited
                                // list of required modifiers.
                                var requirements = e.getAttribute("if").split(",");
                                for (var i=0; i<requirements.length; i++) {
                                    modifierValue |= getModifierMask(requirements[i]);
                                    addClass(cap_element, "guac-keyboard-requires-" + requirements[i]);
                                    addClass(key_element, "guac-keyboard-uses-" + requirements[i]);
                                }

                            }

                            // Store cap
                            key.modifierMask |= modifierValue;
                            key.caps[modifierValue] = cap;

                        }
                    });

                    scaledElements.push(new ScaledElement(key_container_element, key_units, 1, true));
                    row.appendChild(key_container_element);

                    // Set up click handler for key
                    function press() {

                        // Press key if not yet pressed
                        if (!key.pressed) {

                            addClass(key_element, "guac-keyboard-pressed");

                            // Get current cap based on modifier state
                            var cap = key.getCap(modifiers);

                            // Update modifier state
                            if (cap.modifier) {

                                // Construct classname for modifier
                                var modifierClass = "guac-keyboard-modifier-" + cap.modifier;
                                var modifierMask = getModifierMask(cap.modifier);

                                // Toggle modifier state
                                modifiers ^= modifierMask;

                                // Activate modifier if pressed
                                if (modifiers & modifierMask) {
                                    
                                    addClass(keyboard, modifierClass);
                                    modifier_keysyms[cap.modifier] = cap.keysym;
                                    
                                    // Send key event
                                    if (on_screen_keyboard.onkeydown && cap.keysym)
                                        on_screen_keyboard.onkeydown(cap.keysym);

                                }

                                // Deactivate if not pressed
                                else {

                                    var original_keysym = modifier_keysyms[cap.modifier];

                                    removeClass(keyboard, modifierClass);
                                    delete modifier_keysyms[cap.modifier];
                                    
                                    // Send key event
                                    if (on_screen_keyboard.onkeyup && original_keysym)
                                        on_screen_keyboard.onkeyup(original_keysym);

                                }

                            }

                            // If not modifier, send key event now
                            else if (on_screen_keyboard.onkeydown && cap.keysym)
                                on_screen_keyboard.onkeydown(cap.keysym);

                            // Mark key as pressed
                            key.pressed = true;

                        }

                    }

                    function release() {

                        // Release key if currently pressed
                        if (key.pressed) {

                            // Get current cap based on modifier state
                            var cap = key.getCap(modifiers);

                            removeClass(key_element, "guac-keyboard-pressed");

                            // Send key event if not a modifier key
                            if (!cap.modifier && on_screen_keyboard.onkeyup && cap.keysym)
                                on_screen_keyboard.onkeyup(cap.keysym);

                            // Mark key as released
                            key.pressed = false;

                        }

                    }

                    function touchPress(e) {
                        e.preventDefault();
                        ignore_mouse = on_screen_keyboard.touchMouseThreshold;
                        press();
                    }

                    function touchRelease(e) {
                        e.preventDefault();
                        ignore_mouse = on_screen_keyboard.touchMouseThreshold;
                        release();
                    }

                    function mousePress(e) {
                        e.preventDefault();
                        if (ignore_mouse == 0)
                            press();
                    }

                    function mouseRelease(e) {
                        e.preventDefault();
                        if (ignore_mouse == 0)
                            release();
                    }

                    key_element.addEventListener("touchstart", touchPress, true);
                    key_element.addEventListener("touchend",   touchRelease, true);

                    key_element.addEventListener("mousedown", mousePress,   true);
                    key_element.addEventListener("mouseup",   mouseRelease, true);
                    key_element.addEventListener("mouseout",  mouseRelease, true);

                }
                
            });

            return row;

        }

        function parse_column(e) {
            
            var col = document.createElement("div");
            col.className = "guac-keyboard-column";

            if (col.getAttribute("align"))
                col.style.textAlign = col.getAttribute("align");

            // Columns can only contain rows
            parseChildren(e, {
                "row": function(e) {
                    col.appendChild(parse_row(e));
                }
            });

            return col;

        }

        // Parse document
        var keyboard_element = xml.documentElement;
        if (keyboard_element.tagName != "keyboard")
            throw new Error("Root element must be keyboard");

        // Get attributes
        if (!keyboard_element.getAttribute("size"))
            throw new Error("size attribute is required for keyboard");
        
        var keyboard_size = parseFloat(keyboard_element.getAttribute("size"));
        
        parseChildren(keyboard_element, {
            
            "row": function(e) {
                keyboard.appendChild(parse_row(e));
            },
            
            "column": function(e) {
                keyboard.appendChild(parse_column(e));
            }
            
        });

    }

    // Do not allow selection or mouse movement to propagate/register.
    keyboard.onselectstart =
    keyboard.onmousemove   =
    keyboard.onmouseup     =
    keyboard.onmousedown   =
    function(e) {

        // If ignoring events, decrement counter
        if (ignore_mouse)
            ignore_mouse--;

        e.stopPropagation();
        return false;

    };

    /**
     * Fired whenever the user presses a key on this Guacamole.OnScreenKeyboard.
     * 
     * @event
     * @param {Number} keysym The keysym of the key being pressed.
     */
    this.onkeydown = null;

    /**
     * Fired whenever the user releases a key on this Guacamole.OnScreenKeyboard.
     * 
     * @event
     * @param {Number} keysym The keysym of the key being released.
     */
    this.onkeyup   = null;

    /**
     * Returns the element containing the entire on-screen keyboard.
     * @returns {Element} The element containing the entire on-screen keyboard.
     */
    this.getElement = function() {
        return keyboard;
    };

    /**
     * Resizes all elements within this Guacamole.OnScreenKeyboard such that
     * the width is close to but does not exceed the specified width. The
     * height of the keyboard is determined based on the width.
     * 
     * @param {Number} width The width to resize this Guacamole.OnScreenKeyboard
     *                       to, in pixels.
     */
    this.resize = function(width) {

        // Get pixel size of a unit
        var unit = Math.floor(width * 10 / keyboard_size) / 10;

        // Resize all scaled elements
        for (var i=0; i<scaledElements.length; i++) {
            var scaledElement = scaledElements[i];
            scaledElement.scale(unit)
        }

    };

};

/**
 * Basic representation of a single key of a keyboard. Each key has a set of
 * caps associated with tuples of modifiers. The cap determins what happens
 * when a key is pressed, while it is the state of modifier keys that determines
 * what cap is in effect on any particular key.
 * 
 * @constructor
 */
Guacamole.OnScreenKeyboard.Key = function() {

    var key = this;

    /**
     * Whether this key is currently pressed.
     */
    this.pressed = false;

    /**
     * Width of the key, relative to the size of the keyboard.
     */
    this.size = 1;

    /**
     * An associative map of all caps by modifier.
     */
    this.caps = {};

    /**
     * Bit mask with all modifiers that affect this key set.
     */
    this.modifierMask = 0;

    /**
     * Given the bitwise OR of all active modifiers, returns the key cap
     * which applies.
     */
    this.getCap = function(modifier) {
        return key.caps[modifier & key.modifierMask];
    };

};

/**
 * Basic representation of a cap of a key. The cap is the visible part of a key
 * and determines the active behavior of a key when pressed. The state of all
 * modifiers on the keyboard determines the active cap for all keys, thus
 * each cap is associated with a set of modifiers.
 * 
 * @constructor
 * @param {String} text The text to be displayed within this cap.
 * @param {Number} keysym The keysym this cap sends when its associated key is
 *                        pressed or released.
 * @param {String} modifier The modifier represented by this cap.
 */
Guacamole.OnScreenKeyboard.Cap = function(text, keysym, modifier) {
    
    /**
     * Modifier represented by this keycap
     */
    this.modifier = null;
    
    /**
     * The text to be displayed within this keycap
     */
    this.text = text;

    /**
     * The keysym this cap sends when its associated key is pressed/released
     */
    this.keysym = keysym;

    // Set modifier if provided
    if (modifier) this.modifier = modifier;
    
};
