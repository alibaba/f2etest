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

package org.glyptodon.guacamole.protocol;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An abstract representation of a Guacamole instruction, as defined by the
 * Guacamole protocol.
 *
 * @author Michael Jumper
 */
public class GuacamoleInstruction {

    /**
     * The opcode of this instruction.
     */
    private String opcode;

    /**
     * All arguments of this instruction, in order.
     */
    private List<String> args;

    /**
     * Creates a new GuacamoleInstruction having the given Operation and
     * list of arguments values.
     *
     * @param opcode The opcode of the instruction to create.
     * @param args The list of argument values to provide in the new
     *             instruction if any.
     */
    public GuacamoleInstruction(String opcode, String... args) {
        this.opcode = opcode;
        this.args = Collections.unmodifiableList(Arrays.asList(args));
    }

    /**
     * Creates a new GuacamoleInstruction having the given Operation and
     * list of arguments values. The list given will be used to back the
     * internal list of arguments and the list returned by getArgs().
     *
     * @param opcode The opcode of the instruction to create.
     * @param args The list of argument values to provide in the new
     *             instruction if any.
     */
    public GuacamoleInstruction(String opcode, List<String> args) {
        this.opcode = opcode;
        this.args = Collections.unmodifiableList(args);
    }

    /**
     * Returns the opcode associated with this GuacamoleInstruction.
     * @return The opcode associated with this GuacamoleInstruction.
     */
    public String getOpcode() {
        return opcode;
    }

    /**
     * Returns a List of all argument values specified for this
     * GuacamoleInstruction. Note that the List returned is immutable.
     * Attempts to modify the list will result in exceptions.
     *
     * @return A List of all argument values specified for this
     *         GuacamoleInstruction.
     */
    public List<String> getArgs() {
        return args;
    }

    /**
     * Returns this GuacamoleInstruction in the form it would be sent over the
     * Guacamole protocol.
     *
     * @return This GuacamoleInstruction in the form it would be sent over the
     *         Guacamole protocol.
     */
    @Override
    public String toString() {

        StringBuilder buff = new StringBuilder();

        // Write opcode
        buff.append(opcode.length());
        buff.append('.');
        buff.append(opcode);

        // Write argument values
        for (String value : args) {
            buff.append(',');
            buff.append(value.length());
            buff.append('.');
            buff.append(value);
        }

        // Write terminator
        buff.append(';');

        return buff.toString();

    }

}
