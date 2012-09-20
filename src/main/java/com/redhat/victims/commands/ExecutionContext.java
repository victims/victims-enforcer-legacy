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
 *
 * @author gmurphy
 */
public final class ExecutionContext {

    private Settings settings;
    private Artifact artifact;
    private Database database;
    private Log log;

    public Settings getSettings() {
        return settings;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public Database getDatabase() {
        return database;
    }

    public Log getLog() {
        return this.log;
    }

    public void setSettings(Settings setup) {
        this.settings = setup;
    }

    public void setArtifact(Artifact a) {
        this.artifact = a;
    }

    public void setDatabase(Database db) {
        this.database = db;
    }

    public void setLog(Log l) {
        this.log = l;
    }
}
