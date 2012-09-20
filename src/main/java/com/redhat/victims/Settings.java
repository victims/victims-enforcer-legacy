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
 */
package com.redhat.victims;

import java.util.*;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONObject;

/**
 * Configuration settings are defined and stored by this class. All settings of
 * the VictimsRule are validated and initialized with defaults from this class.
 *
 * @author gmurphy
 */
public final class Settings {

    /*
     * Different modes of operation for the plugin
     */
    public static final String ModeWarning = "warning";
    public static final String ModeFatal = "fatal";
    public static final String ModeDisabled = "disabled";
    /*
     * Allow developers to disable automatic updates
     */
    public static final String UpdatesOff = "offline";
    public static final String UpdatesAutomatic = "auto";
    /*
     * Different settings that can be configured. These need to map back to the
     * names of each of the private members in the rule definition in order to
     * be configurable in the pom.xml @see VictimsRule
     */
    public static final String URL = "url";
    public static final String Metadata = "metadata";
    public static final String Fingerprint = "fingerprint";
    public static final String DatabasePath = "path";
    public static final String UpdateDatabase = "updates";
    /**
     * Reasonably sensible defaults
     */
    public static final Map<String, String> defaults;

    static {
        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put(URL, "https://victims-websec.rhcloud.com/service/v1");
        mappings.put(Metadata, ModeWarning);
        mappings.put(Fingerprint, ModeFatal);
        mappings.put(DatabasePath, ".victims");
        mappings.put(UpdateDatabase, UpdatesAutomatic);
        defaults = Collections.unmodifiableMap(mappings);
    }
    /**
     * Map containing configuration values
     */
    private Map<String, String> settings;

    /**
     * Generic interface used validate settings in the configuration
     */
    private interface Validator {

        public void validate() throws VictimsException;
    }
    /**
     * FIXME This is nasty
     */
    private Validator[] required = {
        (new Validator() {

    public void validate() throws VictimsException {

        try {

            String entry = settings.get(URL);
            if (entry == null || entry.length() <= 0) {
                throw new VictimsException(IOUtils.fmt(Resources.ERR_SETTING_MISSING, URL));
            }

            URI url;
            try {
                url = new HttpURL(entry);

            } catch (URIException e) {
                url = new HttpsURL(entry);
            }

            if (!url.getPath().endsWith("service/v1")) {
                System.err.println("Doesn't end in service/v1");
                throw new VictimsException(IOUtils.fmt(Resources.ERR_INVALID_URL, url.toString()));
            }

        } catch (URIException e) {
            throw new VictimsException(IOUtils.fmt(Resources.ERR_INVALID_URL, settings.get(URL)));
        }
    }
}),
        (new Validator() {

    public void validate() throws VictimsException {

        List<String> modes = new ArrayList<String>();
        modes.add(ModeFatal);
        modes.add(ModeWarning);
        modes.add(ModeDisabled);

        List<String> modeSettings = new ArrayList<String>();
        modeSettings.add(Metadata);
        modeSettings.add(Fingerprint);

        for (String item : modeSettings) {
            String value = settings.get(item);
            if (value == null) {
                throw new VictimsException(IOUtils.fmt(Resources.ERR_SETTING_MISSING, item));
            }

            if (!modes.contains(value)) {
                String err = IOUtils.fmt(Resources.ERR_INVALID_MODE, value, item, modes.toString());
                throw new VictimsException(err);
            }
        }
    }
})
    };

    public Settings() {
        settings = new HashMap<String, String>();
    }

    public void set(String k, String v) {
        settings.put(k, v);
    }

    public String get(String k) {
        return settings.get(k);
    }

    public void show(Log log) {
        JSONObject obj = new JSONObject(settings);
        log.info(IOUtils.prettyPrint(IOUtils.fmt(Resources.INFO_SETTINGS_HEADING), obj));
    }

    public void validate() throws VictimsException {
        for (Validator v : required) {
            v.validate();
        }
    }

    public boolean inFatalMode(String setting) {
        String val = settings.get(setting);
        return val != null && val.equalsIgnoreCase(ModeFatal);

    }

    public boolean isEnabled(String setting) {
        String val = settings.get(setting);
        return val != null && !val.equalsIgnoreCase(ModeDisabled);
    }

    public boolean updatesEnabled() {
        String val = settings.get(UpdateDatabase);
        return val != null && val.equalsIgnoreCase(UpdatesAutomatic);
    }
}
