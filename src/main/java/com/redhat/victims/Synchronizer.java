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

import com.redhat.victims.db.Database;
import com.redhat.victims.db.Statements;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The VictimClient class provides the mechanism for synchronizing data within a
 * local Victims database with the remote canonical server.
 *
 * @author gmurphy
 */
public class Synchronizer {

    private String baseURL;
    private Log log;

    /**
     * Creates a new synchronizer instance that will attempt to
     * retrieve updates from the supplied URL.
     * @param url The url to synchronize to.
     */
    public Synchronizer(String url) {
        this(url, new SystemStreamLog());
    }

    /**
     * Creates a new synchronizer instance that will attempt to
     * retreive updates from the supplied URL.
     * @param url The url to synchronize to.
     * @param log The log to send messages to.
     */
    public Synchronizer(String url, Log l) {
        baseURL = url;
        log = l;
    }

    private String getUpdateURL(final int version) {
        return String.format("%s/update/%d", baseURL, version);
    }

    private String getObseleteURL(final int version) {
        return String.format("%s/remove/%d", baseURL, version);
    }

    /**
     * Actual synchronization mechanism abstraction as essentially does the same
     * thing for update / delete.
     *
     * @param db The database to update.
     * @param q  The statement (update / insert) to perform on the database.
     * @param url The url to make the request to.
     * @return The number of entries that were updated.
     * @throws Exception Thrown if a bad response is receieved from the server.
     */
    private int sync(Database db, Statements q, final String url) throws Exception {

        int modified = 0;
        HttpMethod get = new GetMethod(url);
        HttpClient client = new HttpClient();
        client.executeMethod(get);
        String response = get.getResponseBodyAsString();

        if (response.length() > "[]".length()) {
            JSONArray entries = new JSONArray(get.getResponseBodyAsString());
            modified = entries.length();
            for (int i = 0; i < entries.length(); i++) {

                JSONObject entry = entries.getJSONObject(i).getJSONObject("fields");
                db.executeStatement(q, entry);
            }
        }

        return modified;
    }

    /**
     * The supplied database is updated and synchronized to contain the latest
     * content available from the Red Hat Security Response Team.
     *
     * @param db The database to be updated
     * @throws VictimsException If the synchronization operation failed.
     */
    public void synchronizeDatabase(Database db) throws VictimsException {

        try {

            int changes;
            JSONObject result = db.executeStatement(Statements.VERSION, null);
            int version = result.getInt("db_version");

            log.info(IOUtils.fmt(Resources.INFO_DATABASE_VERSION, version));
            log.info(IOUtils.fmt(Resources.INFO_PERFORMING_SYNC));

            changes = sync(db, Statements.INSERT, getUpdateURL(version));
            log.info(String.format(IOUtils.fmt(Resources.INFO_ITEMS_ADDED, changes)));

            changes = sync(db, Statements.REMOVE, getObseleteURL(version));
            log.info(String.format(IOUtils.fmt(Resources.INFO_ITEMS_REMOVED, changes)));

            result = db.executeStatement(Statements.VERSION, null);
            version = result.getInt("db_version");
            log.info(IOUtils.fmt(Resources.INFO_NEW_DATABASE_VERSION, version));

        } catch (Exception e) {
            throw new VictimsException(IOUtils.fmt(Resources.ERR_SYNCHRONIZATION_FAILURE), e);

        }
    }
}
