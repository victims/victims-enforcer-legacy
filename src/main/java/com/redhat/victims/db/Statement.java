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

import java.sql.SQLException;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Abstraction used within the database to query the database.
 */
interface Statement {
    /**
     * The executes the query on the database.
     * @param o The data to use within the database query.
     * @return A converted result set that has been marshaled to JSON.
     * @throws SQLException If a database error occurs.
     * @throws JSONException If the supplied JSON object is missing a required key.
     */
    public JSONObject execute(JSONObject o) throws SQLException, JSONException;
}
