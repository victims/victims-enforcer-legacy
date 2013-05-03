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
 * @author gmurphy
 */
public class Query {

    /*
     * TODO: Move database table creation logic to external source. The
     * autoincrement element of these tables is not likely to be portable.
     */
     public static final String CREATE_VICTIMS_TABLE_DERBY =
            "CREATE TABLE victims ("
                + "id          INTEGER         NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "cves        VARCHAR(255)    NOT NULL,"
                + "vendor      VARCHAR(255)    NOT NULL,"
                + "name        VARCHAR(255)    NOT NULL,"
                + "created     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "version     VARCHAR(255)    NOT NULL,"
                + "submitter   VARCHAR(255)    NOT NULL,"
                + "format      VARCHAR(255)    NOT NULL,"
                + "status      VARCHAR(255)    NOT NULL,"
                + "file_hash   VARCHAR(255))";


    public static final String CREATE_VICTIMS_TABLE_H2 =
            "CREATE TABLE victims ("
                + "id          INTEGER         NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                + "cves        VARCHAR(255)    NOT NULL,"
                + "vendor      VARCHAR(255)    NOT NULL,"
                + "name        VARCHAR(255)    NOT NULL,"
                + "created     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "version     VARCHAR(255)    NOT NULL,"
                + "submitter   VARCHAR(255)    NOT NULL,"
                + "format      VARCHAR(255)    NOT NULL,"
                + "status      VARCHAR(255)    NOT NULL,"
                + "file_hash   VARCHAR(255))";

    public static final String CREATE_FINGERPRINT_TABLE_DERBY =
            "CREATE TABLE fingerprints ("
                + "id          INTEGER        NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                + "victims_id  INTEGER         NOT NULL,"
                + "algorithm   VARCHAR(255)    NOT NULL,"
                + "combined    VARCHAR(255)    NOT NULL,"
                + "filename    VARCHAR(255)    NOT NULL,"
                + "hash        VARCHAR(255)    NOT NULL,"
                + "FOREIGN KEY(victims_id) REFERENCES victims(id))";

   public static final String CREATE_FINGERPRINT_TABLE_H2 =
            "CREATE TABLE fingerprints ("
                + "id          INTEGER        NOT NULL PRIMARY KEY AUTO_INCREMENT, "
                + "victims_id  INTEGER         NOT NULL,"
                + "algorithm   VARCHAR(255)    NOT NULL,"
                + "combined    VARCHAR(255)    NOT NULL,"
                + "filename    VARCHAR(255)    NOT NULL,"
                + "hash        VARCHAR(255)    NOT NULL,"
                + "FOREIGN KEY(victims_id) REFERENCES victims(id))";

    public static final String CREATE_METADATA_TABLE_DERBY =
            "CREATE TABLE metadata ("
                + "id           INTEGER         NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                + "source       VARCHAR(255)    NOT NULL, "
                + "victims_id   INTEGER         NOT NULL, "
                + "property     VARCHAR(255)    NOT NULL, "
                + "value        VARCHAR(255)    NOT NULL, "
                + "FOREIGN KEY(victims_id) REFERENCES VICTIMS(ID))";

    public static final String CREATE_METADATA_TABLE_H2 =
            "CREATE TABLE metadata ("
                + "id           INTEGER         NOT NULL PRIMARY KEY AUTO_INCREMENT, "
                + "source       VARCHAR(255)    NOT NULL, "
                + "victims_id   INTEGER         NOT NULL, "
                + "property     VARCHAR(255)    NOT NULL, "
                + "value        VARCHAR(255)    NOT NULL, "
                + "FOREIGN KEY(victims_id) REFERENCES VICTIMS(ID))";


    public static final String INSERT_VICTIMS =
            "INSERT INTO victims(cves, vendor, name, "
                + "created, version, submitter, format, status, file_hash) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String INSERT_FINGERPRINT =
            "INSERT INTO fingerprints (victims_id, algorithm,"
                + " combined, filename, hash) VALUES(?, ?, ?, ?, ?)";

    public static final String INSERT_METADATA =
            "INSERT INTO metadata (source, victims_id, "
                + "property, value) VALUES(?, ?, ?, ?)";

    public static final String DELETE_VICTIMS =
            "DELETE FROM victims WHERE id = ?";

    public static final String DELETE_FINGERPRINT =
            "DELETE FROM fingerprints WHERE victims_id = ?";

    public static final String DELETE_METADATA =
            "DELETE FROM metadata WHERE victims_id = ?";


    public static final String FIND_BY_POM_PROPERTIES =
            "SELECT victims_id FROM metadata "
            + "where source like '%pom.properties' and ("
            + " property = 'groupId'    and value = ?  or "
            + " property = 'artifactId' and value = ?  or "
            + " property = 'version'    and value like ?"
            + ")"
            + "group by victims_id having count(victims_id) = 3";

    public static final String FIND_BY_IMPLEMENTATION_INFO =
            "SELECT victims_id from METADATA "
            + "where source like '%MANIFEST.MF' and ("
            + " property = 'Implementation-Title'   and value = ? or "
            + " property = 'Implementation-Vendor'  and value = ? or "
            + " property = 'Implementation-Version' and value like ?"
            + ")"
            + "group by victims_id having count(victims_id) = 3";

    public static final String FIND_BY_PROPERTY =
            "SELECT vicitms_id FROM metadata WHERE property = ? AND value LIKE ?";

    public static final String FIND_BY_NAME_AND_VERSION =
            "SELECT id FROM victims WHERE name like ? and version = ?";

    public static final String FIND_BY_CLASSNAME =
            "SELECT victims_id FROM fingerprints WHERE filename = ?";

    public static final String FIND_BY_CLASS_HASH =
            "SELECT victims_id FROM fingerprints WHERE hash = ?";

    public static final String FIND_BY_COMBINED_HASH =
            "SELECT DISTINCT victims_id FROM fingerprints WHERE combined = ?";

    public static final String FIND_BY_JAR_HASH =
            "SELECT id from victims where file_hash = ?";

    public static final String GET_METADATA_SOURCES =
            "SELECT DISTINCT source FROM metadata WHERE victims_id = ?";

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

    public static final String GET_LATEST = "SELECT MAX(created) FROM VICTIMS";

    public static final String FIND_VULNERABLE_BY_HASHES =
            "select matched.id from"
         +   "  ( select victims_id as id,"
         +   "        count(*) as total"
         +   "    from fingerprints"
         +   "    where algorithm = ?"
         +   "    and hash in ?"
         +   "    group by victims_id"
         +   "  ) as matched,"
         +   "  ("
         +   "    select"
         +   "        victims_id as id,"
         +   "        count(*) as total"
         +   "    from fingerprints"
         +   "    where algorithm = ?"
         +   "    group by victims_id"
         +   "  ) as vulnerable where vulnerable.total = matched.total"
         +   "  and vulnerable.id = matched.id";

}