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
 *
 * @author gm
 */
public class Query {
    
     public static final String CREATE_VICTIMS_TABLE = 
            "CREATE TABLE victims ("
                + "id          INTEGER         NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "cves        VARCHAR(255)    NOT NULL,"
                + "vendor      VARCHAR(255)    NOT NULL,"
                + "name        VARCHAR(255)    NOT NULL,"
                + "created     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "version     VARCHAR(255)    NOT NULL,"
                + "submitter   VARCHAR(255)    NOT NULL,"
                + "format      VARCHAR(255)    NOT NULL,"
                + "status      VARCHAR(255)    NOT NULL)";

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
                + "id           INTEGER         NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                + "source       VARCHAR(255)    NOT NULL, "
                + "victims_id   INTEGER         NOT NULL, "
                + "property     VARCHAR(255)    NOT NULL, "
                + "value        VARCHAR(255)    NOT NULL, "
                + "FOREIGN KEY(victims_id) REFERENCES VICTIMS(ID))";
    
    public static final String INSERT_VICTIMS = 
            "INSERT INTO victims(cves, vendor, name, "
                + "created, version, submitter, format, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
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
    
    public static final String FIND_BY_CLASSNAME = 
            "SELECT victims_id FROM fingerprint WHERE filename = ?";
    
    public static final String FIND_BY_CLASS_HASH = 
            "SELECT victims_id FROM fingerprint WHERE hash = ?";
    
    public static final String FIND_BY_JAR_HASH = 
            "SELECT DISTINCT victims_id FROM fingerprint WHERE combined = ?";
    
    public static final String GET_METADATA_SOURCES =
            "SELECT source FROM metadata WHERE victims_id = ?";
    
    public static final String GET_METADATA_PROPERTIES = 
            "SELECT * FROM metadata WHERE victims_id = ? and source = ?";
    
    public static final String GET_FINGERPRINT_FILES = 
            "SELECT * FROM fingerprints WHERE victims_id = ? and algorithm = ? ";
          
    public static final String GET_FINGERPRINT_ALGORITHMS = 
            "SELECT algorithm FROM fingerprints WHERE victims_id = ?";
    
    public static final String GET_ALL_VICTIMS_IDS = 
            "SELECT id FROM victims";

    public static final String GET_VICTIM_BY_ID = 
            "SELECT * FROM victims WHERE id = ?";
    
    public static final String GET_LATEST = 
            "SELECT MAX(created) FROM VICTIMS";
}
