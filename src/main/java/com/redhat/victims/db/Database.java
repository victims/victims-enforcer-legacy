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
import com.redhat.victims.Settings;
import com.redhat.victims.VictimsException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final String url; 
    private Log log;

    /**
     * Create a new database instance. The database content will be stored at
     * the specified location.
     *
     * @param db The location to store the database files.
     */
    public Database(String db, String driver, String conn) {
        this(db, driver, conn, new SystemStreamLog());
    }

    /**
     * Create a new database instance. The database content will be stored at
     * the specified location. All errors will be reported on the supplied
     * logging mechanism.
     *
     * @param db
     * @param l
     */
    public Database(String db, String driver, String conn, Log l) {

        database = db;
        log = l;
        connection = null;
        url = conn;
        try {
            //final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
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
     */
    public void insert(VictimsRecord r) throws SQLException {
        
        int changed = 0;
        PreparedStatement stmt = null;
        try {
           
            /* victims table entries -----------------------------------------*/
            stmt = connection.prepareStatement(Query.INSERT_VICTIMS);
            stmt.setInt   (1, r.id);
            stmt.setString(2, Arrays.toString(r.cves));
            stmt.setString(3, r.vendor);
            stmt.setString(4, r.name);
            stmt.setDate  (5, new java.sql.Date(r.date.getTime()));
            stmt.setString(6, r.version);
            stmt.setString(7, r.submitter);
            stmt.setString(8, r.status.name());
            
            changed = stmt.executeUpdate();
            // TODO Remove
            System.out.println(String.format("Updated %d records in the VICTIMS table.", changed));
            stmt.close();
            
            /* fingerprint table entries -------------------------------------*/
            for (Map.Entry<String, HashRecord> hashes : r.hashes.entrySet()){
                
                String algorithm = hashes.getKey();
                HashRecord record = hashes.getValue();
                 
                changed = 0;
                for (Map.Entry<String, String> hash : record.files.entrySet()){
                    
                    stmt = connection.prepareStatement(Query.INSERT_FINGERPRINT); 
                    stmt.setInt(1, r.id);
                    stmt.setString(2,algorithm);
                    stmt.setString(3, record.combined);
                    stmt.setString(4, hash.getKey());   // filename
                    stmt.setString(5, hash.getValue()); //  hash  
                    changed+= stmt.executeUpdate();
                    stmt.close();
                }      
                // TODO Remove
                System.out.println(String.format("Updated %d records in the FINGERPRINT table.", changed));
            }
            
            /* metadata table entries ---------------------------------------*/
            for (Map.Entry<String, Map<String, String> > meta : r.meta.entrySet()){
                
                String source = meta.getKey();
                Map<String, String> data = meta.getValue(); 
                
                changed = 0;
                for (String property : data.keySet()){
                    
                    stmt = connection.prepareStatement(Query.INSERT_METADATA);
                    stmt.setString(1, source);
                    stmt.setInt(2, r.id);
                    stmt.setString(3, property); 
                    stmt.setString(4, data.get(property));
                    
                    changed += stmt.executeUpdate();
                    stmt.close();            
                }
                
                System.out.println(String.format("Updated %d record in the METADATA table", changed));
                
            }          
            
        } finally { 
            if (stmt != null){
                stmt.close();
            }
        }
    }

    /**
     */
    public List<VictimsRecord> list() throws SQLException {
        
        List<VictimsRecord> records = new ArrayList<VictimsRecord>();
        PreparedStatement all = null;
        try {
            
            all = connection.prepareStatement("SELECT ID FROM VICTIMS");
            ResultSet rs = all.executeQuery();

            rs.first();
            do {
                records.add(getVictimsRecord(rs.getInt("id")));        
            } 
            while (rs.next());
             
        } finally {
            
            if (all != null){
                all.close();
            }
        }
        
        return records;
    }
        
    /**
     * 
     */
    public VictimsRecord getVictimsRecord(int victimsId) throws SQLException {
        
        VictimsRecord record = new VictimsRecord();
        PreparedStatement stmt = null;
        try {
            
            stmt = connection.prepareStatement("SELECT * FROM victims WHERE id = ?");
            stmt.setInt(1, victimsId);
            
            /* victims data -------------------------------------------------*/
            ResultSet rs = stmt.executeQuery();
            record.id           = rs.getInt("id");
            record.cves         = rs.getString("cves").split(",");
            record.date         = rs.getDate("created");
            record.format       = rs.getString("format");
            record.name         = rs.getString("name");
            record.status       = Status.valueOf(rs.getString("status"));
            record.submitter    = rs.getString("submitter");
            record.vendor       = rs.getString("vendor");
            record.version      = rs.getString("version");
            
            stmt.close();
            
            /* fingerprint data ---------------------------------------------*/
            stmt = connection.prepareStatement("SELECT algorithm FROM fingerprints WHERE victims_id = ?");
            stmt.setInt(1, victimsId);
            rs = stmt.executeQuery();
            rs.first();
            
            do {
                
                String algorithm = rs.getString("algorithm");
                HashRecord hashRecord = null;
                PreparedStatement hashes = null;
                try {
                    hashes = connection.prepareStatement("SELECT * FROM "
                            + "fingerprints WHERE victims_id = ? "
                            + "and algorithm = ? ");
                    hashes.setInt(1, victimsId);
                    hashes.setString(2, algorithm);
                    
                    ResultSet files = hashes.executeQuery();
                    
                    files.first();
                    hashRecord = new HashRecord();
                    hashRecord.combined = files.getString("combined");
                    
                    do {
                        
                        String filename = files.getString("filename");
                        String hash = files.getString("hash");
                        hashRecord.files.put(filename, hash);
                                           
                    } 
                    while(files.next());
                    
                } finally {
                    
                    if (hashes != null){
                        hashes.close();
                    }
                }
                
                if (hashRecord != null){
                    record.hashes.put(algorithm, hashRecord);
                }
                
            } 
            while (rs.next());
            
            stmt.close();
            
            /* metadata -----------------------------------------------------*/
            stmt = connection.prepareStatement("SELECT source FROM fingerprints WHERE victims_id = ?");
            stmt.setInt(1, victimsId);
            rs = stmt.executeQuery();
            rs.first();
                     
            do {
                
                String source = rs.getString("source");
                
                PreparedStatement metadata = connection.prepareStatement("SELECT * FROM metadata WHERE victims_id = ? and source = ?");
                metadata.setInt(1, victimsId);
                metadata.setString(2, source);
                ResultSet properties = metadata.executeQuery();
                rs.first();
                
                Map<String, String> data = new HashMap<String, String>();
                do {
                    
                    String property = properties.getString("property");
                    String value = properties.getString("value");
                    data.put(property, value);
                    
                    
                }
                while (properties.next());
                
                record.meta.put(source, data);
               
                
            } 
            while(rs.next());
            
                
        } finally {
            
            if (stmt != null)
                stmt.close();
        }

        return record;
    }
    
    /**
     * 
     * @param victimsId 
     */
    public void remove(int victimsId) {
        
        

    
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
     * @return A JSONObject populated with the default result values.
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
     * @throws SQLException Occurs when unable to retrieve expected value from the database results.
     * @throws JSONException Occurs when attempting to insert an invalid value into the JSON.
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
     * @throws SQLException When unable to connect to database.
     */
    private void connect() throws SQLException {

        //String protocol = String.format("jdbc:derby:%s;create=true", database);
        connection = DriverManager.getConnection(url);

        PreparedStatement stmt = null;
        try {

            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getTables(null, null, "victims", null);
            if (!rs.next()) {

                stmt = connection.prepareStatement(Query.CREATE_VICTIMS_TABLE);
                stmt.execute();
                
                stmt = connection.prepareStatement(Query.CREATE_FINGERPRINT_TABLE);
                stmt.execute();
                
                stmt = connection.prepareStatement(Query.CREATE_METADATA_TABLE);
                stmt.execute();
           
            }

        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
     * Closes the connection to the database
     *
     * @throws SQLException If unable to disconneect cleanly.
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
     * @throws VictimsException Occurs if an invalid statement is supplied.
     */
    private Statement resolveStatement(Statements id) throws VictimsException {

        switch (id) {

//            case INSERT:
//                return insert();
//            case REMOVE:
//                return remove();
//            case LIST:
//                return list();
//            case CHECK_HASH:
//                return containsHash();
//            case CHECK_JAR:
//                return containsJAR();
//            case VERSION:
//                return version();
            default:
                throw new VictimsException(IOUtils.fmt(Resources.ERR_INVALID_SQL_STATEMENT));
        }
    }
}
