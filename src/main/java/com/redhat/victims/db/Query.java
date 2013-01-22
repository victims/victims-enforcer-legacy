/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.victims.db;

/**
 *
 * @author gm
 */
public class Query {
    
     public static final String CREATE_VICTIMS_TABLE = 
            "CREATE TABLE victims ("
                + "id          INTEGER         NOT NULL,"
                + "cves        VARCHAR(255)    NOT NULL,"
                + "vendor      VARCHAR(255)    NOT NULL,"
                + "name        VARCHAR(255)    NOT NULL,"
                + "created     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "version     VARCHAR(255)    NOT NULL,"
                + "submitter   VARCHAR(255)    NOT NULL,"
                + "format      VARCHAR(255)    NOT NULL,"
                + "status      VARCHAR(255)    NOT NULL,"
                + "PRIMARY KEY(id))";

    public static final String CREATE_FINGERPRINT_TABLE = 
            "CREATE TABLE fingerprints ("
                + "victims_id  INTEGER         NOT NULL," 
                + "algorithm   VARCHAR(255)    NOT NULL,"
                + "combined    VARCHAR(255)    NOT NULL,"
                + "filename    VARCHAR(255)    NOT NULL," 
                + "hash        VARCHAR(255)    NOT NULL," 
                + "PRIMARY KEY(hash), "
                + "FOREIGN KEY(victims_id) REFERENCES victims(id))";
            
    public static final String CREATE_METADATA_TABLE = 
            "CREATE TABLE metadata ("
                + "id           INTEGER         NOT NULL AUTO_INCREMENT, "
                + "source       VARCHAR(255)    NOT NULL, "
                + "victims_id   INTEGER         NOT NULL, "
                + "property     VARCHAR(255)    NOT NULL, "
                + "value        VARCHAR(255)    NOT NULL, "
                + "PRIMARY KEY(id), "
                + "FOREIGN KEY(victims_id) REFERENCES VICTIMS(ID))";
    
    public static final String INSERT_VICTIMS = 
            "INSERT INTO victims(id, cves, vendor, name, "
                + "created, version, submitter, format, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    public static final String INSERT_FINGERPRINT = 
            "INSERT INTO fingerprints (victims_id, algorithm,"
                + " combined, filename, hash) VALUES(?, ?, ?, ?, ?)";
    
    public static final String INSERT_METADATA = 
            "INSERT INTO metadata (source, victims_id, "
                + "property, value) VALUES(?, ?, ?, ?, ?)"; 
    
    public static final String DELETE_VICTIMS = 
            "DELETE FROM victims WHERE id = ?";
    
    public static final String DELETE_FINGERPRINT = 
            "DELETE FROM fingerprints WHERE victims_id = ?";
    
    public static final String DELETE_METADATA = 
            "DELETE FROM metadata WHERE victims_id = ?";
            

    public static final String FIND_BY_PROPERTY = 
            "SELECT vicitms_id FROM metadata WHERE property = ? AND value LIKE ?";
    

    public static final String FIND_BY_HASH = 
            "SELECT victims_id FROM fingerprints WHERE hash = ?";
          
}
