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

import com.redhat.victims.IOUtils;
import com.redhat.victims.Resources;
import com.redhat.victims.VictimsException;
import java.sql.*;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The Database class provides a layer between the JSON content retrieved from
 * the master database and the the local SQL database in which fingerprints are
 * cached.
 *
 * @author gmurphy
 */
public class Database {

    private Connection connection;
    private final String database;
    private Log log;

    /**
     * Create a new database instance. The database content will be stored at
     * the specified location.
     *
     * @param db The location to store the database files.
     */
    public Database(String db) {
        this(db, new SystemStreamLog());
    }

    /**
     * Create a new database instance. The database content will be stored at
     * the specified location. All errors will be reported on the supplied
     * logging mechanism.
     *
     * @param db
     * @param l
     */
    public Database(String db, Log l) {

        database = db;
        log = l;
        connection = null;
        try {
            final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
            Class.forName(driver).newInstance();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Main execution of all database queries. The database only allows a few
     * specific queries to be made. These are defined in the Statements
     * enumeration. Values from the supplied JSON object are then mapped to an
     * SQL query as necessary.
     *
     * @param id - The operation to perform.
     * @param json - The object to use when performing the operation.
     * @return - A JSONObject that contains a true/false "result" indicator and
     * also any items that it has found in either a 'collection' entry where
     * multiple entries exist or a 'item' entry.
     * @throws VictimsException
     */
    public JSONObject executeStatement(Statements id, JSONObject json) throws VictimsException {

        JSONObject result = null;
        try {

            if (connection == null) {
                connect();
            }

            Statement stmt = resolveStatement(id);
            result = stmt.execute(json);

        } catch (SQLException e) {
            throw new VictimsException(IOUtils.fmt(Resources.ERR_INVALID_SQL_STATEMENT), e);

        } catch (JSONException e) {
            throw new VictimsException(IOUtils.fmt(Resources.ERR_MALFORMED_JSON), e);

        } finally {

            try {
                disconnect();
            } catch (Exception e) {
            }
        }
        return result;
    }

    /**
     * Returns a operation that will insert a JSON object into the database when
     * executed.
     *
     * @return An insert statement.
     */
    private Statement insert() {

        return (new Statement() {

            public JSONObject execute(JSONObject json) throws JSONException, SQLException {

                JSONObject result = createResultJSON(false, null);

                final String insertSQL =
                        "INSERT INTO VICTIMS"
                        + "(cves, db_version, hash, vendor, name, version) "
                        + "VALUES(?, ?, ?, ?, ?, ?)";

                PreparedStatement insert = null;
                try {

                    insert = connection.prepareStatement(insertSQL);
                    insert.setString(1, json.getString("cves"));
                    insert.setInt(2, json.getInt("db_version"));
                    insert.setString(3, json.getString("hash"));
                    insert.setString(4, json.getString("vendor"));
                    insert.setString(5, json.getString("name"));
                    insert.setString(6, json.getString("version"));

                    result = createResultJSON(insert.executeUpdate() > 0, result);
                    return result;

                } catch (SQLIntegrityConstraintViolationException e) {

                    // Don't barf on duplicate key just return false
                    if (e.getSQLState().equals("23505")) {
                        log.warn(e.getLocalizedMessage());
                    } else {
                        throw e;
                    }

                    return result;

                } finally {

                    if (insert != null) {
                        insert.close();
                    }
                }

            }
        });
    }

    /**
     * Returns an operation that will list all entries within the database when
     * executed.
     *
     * @return A list statement.
     */
    private Statement list() {

        return (new Statement() {

            public JSONObject execute(JSONObject o) throws JSONException, SQLException {

                JSONObject result = createResultJSON(false, null);
                PreparedStatement listing = null;
                try {

                    listing = connection.prepareStatement("SELECT * FROM VICTIMS ORDER BY created");
                    ResultSet rs = listing.executeQuery();


                    while (rs.next()) {
                        result.append("collection", marshal(getSchema(), rs));
                    }
                    if (result.has("collection")) {
                        result = createResultJSON(true, result);
                    }

                    return result;

                } finally {
                    if (listing != null) {
                        listing.close();
                    }
                }


            }
        });
    }

    /**
     * Returns an operation that will remove any objects from the database that
     * match the supplied primary key (SHA-512).
     *
     * @return The remove statement.
     */
    private Statement remove() {

        return (new Statement() {

            public JSONObject execute(JSONObject json) throws JSONException, SQLException {

                JSONObject result;
                final String deleteSQL = "DELETE FROM VICTIMS WHERE hash = ?";
                PreparedStatement delete = null;

                try {
                    delete = connection.prepareStatement(deleteSQL);
                    delete.setString(1, json.getString("hash"));
                    result = createResultJSON(delete.executeUpdate() > 0, null);
                    return result;

                } finally {
                    if (delete != null) {
                        delete.close();
                    }
                }


            }
        });
    }

    /**
     * Returns an operation that checks if a JAR file with a similar name, and
     * version to the supplied JSONObject exists within the database.
     *
     * @return A find statement that queries by name and version.
     */
    private Statement containsJAR() {

        return (new Statement() {

            public JSONObject execute(JSONObject json) throws JSONException, SQLException {

                JSONObject result = createResultJSON(false, null);
                final String findSQL = "SELECT * FROM VICTIMS WHERE version = ? AND name LIKE ?";
                PreparedStatement find = null;
                try {

                    find = connection.prepareStatement(findSQL);
                    String v = json.getString("version");
                    String n = json.getString("name");
                    find.setString(1, v);
                    find.setString(2, "%" + n + "%"); // Wildcards hmmm..

                    ResultSet rs = find.executeQuery();

                    JSONObject list = new JSONObject();
                    while (rs.next()) {
                        list.append("collection", marshal(getSchema(), rs));
                    }

                    if (list.has("collection")) {
                        result = createResultJSON(true, list);
                    }

                    return result;

                } finally {
                    if (find != null) {
                        find.close();
                    }
                }


            }
        });

    }

    /**
     * Returns a find operation that searches the database for an entry that
     * matches the supplied hash.
     *
     * @return A find statement that queries based on a SHA-512 hash.
     */
    private Statement containsHash() {

        return (new Statement() {

            public JSONObject execute(JSONObject json) throws JSONException, SQLException {

                JSONObject result = createResultJSON(false, null);
                final String findSQL = "SELECT * FROM VICTIMS WHERE hash = ?";
                PreparedStatement find = null;

                try {

                    find = connection.prepareStatement(findSQL);
                    find.setString(1, json.getString("hash"));
                    ResultSet rs = find.executeQuery();

                    boolean found = rs.next();
                    result = createResultJSON(found, result);
                    if (found) {
                        JSONObject item = marshal(getSchema(), rs);
                        result.put("item", item);
                    }

                    return result;

                } finally {
                    if (find != null) {
                        find.close();
                    }
                }
            }
        });
    }

    /**
     * Returns a statement that when executed returns the latest version of the
     * Victims database that we have locally. This is used when synchronizing to
     * the master database.
     *
     * @return A version statement;
     */
    private Statement version() {

        return (new Statement() {

            public JSONObject execute(JSONObject json) throws JSONException, SQLException {

                JSONObject result = createResultJSON(false, null);
                final String vSQL = "SELECT MAX (db_version) FROM VICTIMS";

                PreparedStatement q = null;
                try {

                    q = connection.prepareStatement(vSQL);
                    ResultSet rs = q.executeQuery();
                    if (rs.next()) {

                        result = createResultJSON(true, result);
                        result.put("db_version", rs.getInt(1));
                    }

                    return result;

                } finally {
                    if (q != null) {
                        q.close();
                    }
                }



            }
        });
    }

    /**
     * Populates a JSONObject with the supplied 'result' : (true|false) value.
     * If an existing JSONObject is also supplied it has all of the keys /
     * values associated with it copied to the returned value.
     *
     * @param result Create a flag in the JSON to indicate whether the database
     * query was successful.
     * @param obj An existing JSON object to copy attributes from. Can be null.
     * @return
     * @throws JSONException
     */
    private JSONObject createResultJSON(boolean result, JSONObject obj) throws JSONException {

        JSONObject json;
        if (obj == null) {
            json = new JSONObject();
        } else {
            json = new JSONObject(obj, JSONObject.getNames(obj));
        }

        json.put("result", result);
        return json;

    }

    /**
     * Converts a SQL result set into a JSON object. Only the specified keys
     * will be populated within the JSON object.
     *
     * @param keys Whitelist of keys to include in the JSON object
     * @param rs The SQL results to convert to JSON format
     * @return The JSONObject with the converted content
     * @throws SQLException
     * @throws JSONException
     */
    private static JSONObject marshal(String[] keys, ResultSet rs) throws SQLException, JSONException {

        JSONObject obj = new JSONObject();
        for (String k : keys) {
            obj.put(k, rs.getString(k));
        }

        return obj;
    }

    /**
     * Creates a connection to an embedded Apache Derby instance. If the
     * database does not exist it is created.
     *
     * @throws SQLException
     */
    private void connect() throws SQLException {

        String protocol = String.format("jdbc:derby:%s;create=true", database);
        connection = DriverManager.getConnection(protocol);

        PreparedStatement createTable = null;
        try {

            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getTables(null, null, "VICTIMS", null);
            if (!rs.next()) {

                createTable = connection.prepareStatement(
                        "CREATE TABLE VICTIMS ( "
                        + "cves VARCHAR(255) NOT NULL,"
                        + "db_version INTEGER NOT NULL, "
                        + "hash VARCHAR(1024) NOT NULL, "
                        + "vendor VARCHAR(255) NOT NULL, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "created timestamp not null default current timestamp,"
                        + "version VARCHAR(255) NOT NULL, PRIMARY KEY(hash))");

                createTable.execute();
            }

        } finally {
            if (createTable != null) {
                createTable.close();
            }
        }
    }

    /**
     * Closes the connection to the database
     *
     * @throws SQLException
     */
    private void disconnect() throws SQLException {
        connection.close();
        connection = null;
    }

    /**
     * Used when marshaling data to and from JSON / SQL format. These are the
     * white-list of keys that will be added to any JSON objects.
     *
     * @return The white list of keys that are allowed to be added to a JSON
     * object.
     */
    private String[] getSchema() {
        final String[] keys = {
            "cves", "name", "version", "vendor", "hash"
        };
        return keys;
    }

    /**
     * Simple lookup that converts a query ID to a statement.
     *
     * @param id A type of query operation to perform.
     * @return The corresponding statement that can be executed in a
     * transaction.
     * @throws VictimsException
     */
    private Statement resolveStatement(Statements id) throws VictimsException {

        switch (id) {

            case INSERT:
                return insert();
            case REMOVE:
                return remove();
            case LIST:
                return list();
            case CHECK_HASH:
                return containsHash();
            case CHECK_JAR:
                return containsJAR();
            case VERSION:
                return version();
        }
        throw new VictimsException("Unsupported statement requested", null);
    }
}
