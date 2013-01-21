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
 * @author gm
 */
public class Tables {
    
    public static final String VICTIMS_TABLE = 
            "CREATE TABLE VICTIMS ("
                + "id          INTEGER         NOT NULL,"
                + "cves        VARCHAR(255)    NOT NULL,"
                + "db_version  INTEGER         NOT NULL,"
                + "vendor      VARCHAR(255)    NOT NULL,"
                + "name        VARCHAR(255)    NOT NULL,"
                + "created     timestamp       NOT NULL default current timestamp,"
                + "version     VARCHAR(255)    NOT NULL,"
                + "submitter   VARCHAR(255)    NOT NULL,"
                + "format      VARCHAR(255)    NOT NULL,"
                + "status      VARCHAR(255)    NOT NULL,"
                + "PRIMARY KEY(id))";

    public static final String FINGERPRINT_TABLE = 
            "CREATE TABLE FINGERPRINTS ("
                + "victims_id  INTEGER         NOT NULL," 
                + "algorithm   VARCHAR(255)    NOT NULL,"
                + "combined    VARCHAR(255)    NOT NULL,"
                + "filename    VARCHAR(255)    NOT NULL," 
                + "hash        VARCHAR(255)    NOT NULL," 
                + "PRIMARY KEY(hash), "
                + "FOREIGN KEY(victims_id) REFERENCES VICTIMS(ID))";
            
    public static final String METADATA_TABLE = 
            "CREATE TABLE METADATA ("
                + "id           INTEGER         NOT NULL AUTO_INCREMENT, "
                + "source       VARCHAR(255)    NOT NULL, "
                + "victims_id   INTEGER         NOT NULL, "
                + "property     VARCHAR(255)    NOT NULL, "
                + "value        VARCHAR(255)    NOT NULL, "
                + "PRIMARY KEY(id), "
                + "FOREIGN KEY(victims_id) REFERENCES VICTIMS(ID))";
    
}
