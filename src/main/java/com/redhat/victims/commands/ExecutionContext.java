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
package com.redhat.victims.commands;

import com.redhat.victims.Settings;
import com.redhat.victims.db.Database;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

/**
 * Context to pass to each command.
 *
 * @author gmurphy
 */
public final class ExecutionContext {

    private Settings settings;
    private Artifact artifact;
    private Database database;
    private Log log;

    /**
     * @return Configuration to apply to this execution context.
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * @return The artifact associated with this execution context.
     */
    public Artifact getArtifact() {
        return artifact;
    }

    /**
     * @return The database associated with this execution context.
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * @return The log to use within this execution context.
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * Applies the given settings to the execution context.
     */
    public void setSettings(final Settings setup) {
        this.settings = setup;
    }

    /**
     * Associates the given artifact with this execution context.
     * @param a The artifact to associate with this context.
     */
    public void setArtifact(final Artifact a) {
        this.artifact = a;
    }

    /**
     * Associates the given database with this execution context.
     * @param db The database to associate with this context.
     */
    public void setDatabase(Database db) {
        this.database = db;
    }

    /**
     * Send all messages to this log.
     * @param l The log to associate with this execution context.
     */
    public void setLog(Log l) {
        this.log = l;
    }
}
