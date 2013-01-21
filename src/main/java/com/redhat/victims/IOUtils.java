/*
 * Copyright (C) 2012 Red Hat Inc.
 *
 * This file is part of enforce-victims-rule for the Maven Enforcer Plugin.
 * enforce-victims-rule is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * enforce-victims-rule is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with enforce-victims-rule.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package com.redhat.victims;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A collection of miscellaneous utilities used throughout the plug-in to format
 * output.
 *
 * @author gmurphy
 */
public final class IOUtils {

   /**
    * Repeats a character n times.
    *
    * @param c charater to repeat n times
    * @param n the number of times to repeat the character
    * @return The string value of c * n
    */
    public static String repeat(final char c, final int n) {

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < n; i++) {
            buf.append(c);
        }
        return buf.toString();
    }

    /**
     * Creates an ASCII box for use in titles etc.
     *
     * @param word The word to put in the box
     * @return String formated to contain the word in an ASCII box.
     */
    public static String box(final String word) {

        StringBuilder buf = new StringBuilder();
        buf.append(String.format("%n"));
        buf.append("+");
        buf.append(repeat('=', word.length()));
        buf.append("+");
        buf.append(String.format("%n"));
        buf.append("|");
        buf.append(word.toUpperCase());
        buf.append("|");
        buf.append(String.format("%n"));
        buf.append("+");
        buf.append(repeat('=', word.length()));
        buf.append("+");
        buf.append(String.format("%n"));

        return buf.toString();
    }

    /**
     * Places n indentations after each new line character detected.
     *
     * @param amount The number of spaces to input text by.
     * @param str The string to indent.
     * @return An string indented by specified amount after each newline.
     *
     */
    public static String indent(final int amount, final String str) {

        StringBuilder buf = new StringBuilder();
        StringTokenizer toks = new StringTokenizer(str, "\n");
        while (toks.hasMoreTokens()) {
            buf.append(toks.nextToken());
            buf.append(String.format("%n"));
            buf.append(repeat(' ', amount));
        }
        return buf.toString();
    }

    /**
     * Wraps lines of text in accordance to specified arguments.
     *
     * @param limit The length to wrap the text at
     * @param str The string to wrap
     * @return A string that has been wrapped to fit the specified length
     */
    public static String wrap(final int limit, final String str) {

        StringBuilder buf = new StringBuilder();
        int pos = 0;
        int edge = Math.round(0.1f * limit);
        while (pos < str.length()) {

            int end = pos + limit;
            if (end > str.length()) {
                end = str.length();
            }

            String chunk = str.substring(pos, end);
            int newline = chunk.indexOf('\n');
            int space = chunk.lastIndexOf(' ');
            int len = chunk.length();

            if (newline > 0) {
                String addition = chunk.substring(0, newline);
                buf.append(addition.trim());
                //buf.append(String.format("%n"));
                pos += addition.length();

            } else if (len - edge > 0 && space > (len - edge)) {

                String addition = chunk.substring(0, space);
                buf.append(addition.trim());
                buf.append(String.format("%n"));
                pos += addition.length() + 1; // Skip the leading space

            } else {

                buf.append(chunk.trim());
                buf.append(String.format("%n"));
                pos += limit;
            }
        }

        return buf.toString();

    }

    /**
     * Truncate a line of text to 'n' characters.
     *
     * @param n Number of characters allowed before truncating the rest of
     * input.
     * @param str The string to be truncated if necessary
     * @return A truncated string ending in ...
     */
    public static String truncate(final int n, final String str) {

        if (str.length() <= n) {
            return str;
        }

        return str.substring(0, n).concat("...");
    }

    /**
     * Displays a JSON object in a k : v type format.
     *
     * @param title - The title to display above the JSON object
     * @param o - The JSON object to be printed
     * @return - Pretty formated string of JSON content
     */
    public static String prettyPrint(final String title, final JSONObject o) {

        int indenting = 15;
        int wrapping  = 48;
        String formatting = "%12s : %-16s%n";
        StringBuilder sb = new StringBuilder();
        Iterator i = o.keys();

        if (i.hasNext()) {
            sb.append(box(title));
        }
        while (i.hasNext()) {

            try {

                String key = (String) i.next();
                String val = indent(indenting, wrap(wrapping, String.valueOf(o.get(key))));
                sb.append(String.format(formatting, key, val));

            } catch (JSONException e) {
                sb.append("Invalid format of JSON object: ").append(o.toString()).append(String.format("%n"));
            }
        }
        return sb.toString();
    }

    /**
     * A gluttonous way of reading file content into memory. Used mostly in
     * testing.
     *
     * @param f - UTF8 encoded file to read content from.
     * @return The content of the specified file.
     * @throws IOException Is thrown if unable to read the file.
     * @return The entire file content as a string
     */
    public static String slurp(final File f) throws IOException {

        String line;
        StringBuilder buffer = new StringBuilder();
        BufferedReader in = null;

        try {

            FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            in = new BufferedReader(isr);

            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }

            return buffer.toString();

        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    

    /**
     * Formats a message appropriately based on the resource bundle key
     * supplied.
     *
     * @param key The key from the resource bundle.
     * @param args The arguments to pass to the message formatter.
     * @return Returns a formated string using a format specified in the Resource bundle.
     */
    public static String fmt(final String key, final Object... args) {

        ResourceBundle bundle = ResourceBundle.getBundle("com.redhat.victims.Resources");
        String formatting = bundle.getString(key);
        return String.format(formatting, args);
    }

    /**
     * A rather lousy way at altering the logging level based on the mode of
     * operation that the rule has been configured for.
     *
     * @param l The log instance to invoke warn, error, or info on.
     * @param mode The mode which determines which log method is invoked.
     * @param msg The message to send to the log.
     */
    public static void report(final Log l, final String mode, final String msg) {

        String level;
        if (mode.equals(Settings.MODE_FATAL)) {
            level = "error";
        } else if (mode.equals(Settings.MODE_WARNING)) {
            level = "warn";
        } else {
            level = "info";
        }
        try {
            Class[] args = {CharSequence.class};
            Method m = l.getClass().getDeclaredMethod(level, args);
            m.invoke(l, msg);

        } catch (NoSuchMethodException e) {
            l.error(e);
        } catch (IllegalAccessException e) {
            l.error(e);
        } catch (InvocationTargetException e) {
            l.error(e);
        }
    }
    
 }
