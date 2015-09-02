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


import java.util.ArrayList;
import java.util.List;

/**
 * An abstract representation of Guacamole client information, including all
 * information required by the Guacamole protocol during the preamble.
 *
 * @author Michael Jumper
 */
public class GuacamoleClientInformation {

    /**
     * The optimal screen width requested by the client, in pixels.
     */
    private int optimalScreenWidth  = 1024;

    /**
     * The optimal screen height requested by the client, in pixels.
     */
    private int optimalScreenHeight = 768;

    /**
     * The resolution of the optimal dimensions given, in DPI.
     */
    private int optimalResolution = 96;

    /**
     * The list of audio mimetypes reported by the client to be supported.
     */
    private final List<String> audioMimetypes = new ArrayList<String>();

    /**
     * The list of audio mimetypes reported by the client to be supported.
     */
    private final List<String> videoMimetypes = new ArrayList<String>();

    /**
     * Returns the optimal screen width requested by the client, in pixels.
     * @return The optimal screen width requested by the client, in pixels.
     */
    public int getOptimalScreenWidth() {
        return optimalScreenWidth;
    }

    /**
     * Sets the client's optimal screen width.
     * @param optimalScreenWidth The optimal screen width of the client.
     */
    public void setOptimalScreenWidth(int optimalScreenWidth) {
        this.optimalScreenWidth = optimalScreenWidth;
    }

    /**
     * Returns the optimal screen height requested by the client, in pixels.
     * @return The optimal screen height requested by the client, in pixels.
     */
    public int getOptimalScreenHeight() {
        return optimalScreenHeight;
    }

    /**
     * Sets the client's optimal screen height.
     * @param optimalScreenHeight The optimal screen height of the client.
     */
    public void setOptimalScreenHeight(int optimalScreenHeight) {
        this.optimalScreenHeight = optimalScreenHeight;
    }

    /**
     * Returns the resolution of the screen if the optimal width and height are
     * used, in DPI.
     * 
     * @return The optimal screen resolution.
     */
    public int getOptimalResolution() {
        return optimalResolution;
    }

    /**
     * Sets the resolution of the screen if the optimal width and height are
     * used, in DPI.
     * 
     * @param optimalResolution The optimal screen resolution in DPI.
     */
    public void setOptimalResolution(int optimalResolution) {
        this.optimalResolution = optimalResolution;
    }

    /**
     * Returns the list of audio mimetypes supported by the client. To add or
     * removed supported mimetypes, the list returned by this function can be
     * modified.
     *
     * @return The set of audio mimetypes supported by the client.
     */
    public List<String> getAudioMimetypes() {
        return audioMimetypes;
    }

    /**
     * Returns the list of video mimetypes supported by the client. To add or
     * removed supported mimetypes, the list returned by this function can be
     * modified.
     *
     * @return The set of video mimetypes supported by the client.
     */
    public List<String> getVideoMimetypes() {
        return videoMimetypes;
    }

}
