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
package com.redhat.victims.db;

/**
 * Restriction of types of requests that can be made to the database.
 */
public enum Statements {

    /** Insert a JSON object into the database */
    INSERT,

    /** Remove a JSON object from the database */
    REMOVE,

    /** List all entries within the database */
    LIST,

    /** Check the database for an occurrence of the supplied hash */
    CHECK_HASH,

    /**
     * Check the database for an occurrence of an artifact
     * matching the supplied pattern.
     */
    CHECK_JAR,

    /**
     * Determine the newestt version of the database that
     * is stored within this database
     */
    VERSION
};
