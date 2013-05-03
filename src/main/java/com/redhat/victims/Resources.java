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
package com.redhat.victims;

import java.util.ListResourceBundle;

/**
 * Basic resource bundle for messages and formating.
 * @author gmurphy
 */
public class Resources extends ListResourceBundle  {

    /** The format for the message shown if a vulnerable
     * artifact is detected by fingerprint.
     */
    public static final String INFO_FINGERPRINT_HEADING     = "INFO_FINGERPRINT_HEADING";

    /** The format for the message shown if a percentage of a
     * JAR file makes up a known vulnerable artifact
     */
    public static final String INFO_CLASSMATCH_HEADING      = "INFO_CLASSMATCH_HEADING";
    /**
     * The message shown if a vulnerable artifact is
     * detected by fingerprint and rule is operating in FATAL mode.
     */
    public static final String FATAL_FINGERPRINT_BODY       = "FATAL_FINGERPRINT_BODY";

    /**
     * The heading shown above the fatal body message.
     */
    public static final String FATAL_FINGERPRINT_HEADING    = "FATAL_FINGERPRINT_HEADING";

    /**
     * The message shown if a vulnerable artifact is detected by metadata.
     */
    public static final String INFO_METADATA_HEADING        = "INFO_METADATA_HEADING";

    /**
     * Information shown that links meta data in an artifact
     * to the entry within the database.
     */
    public static final String INFO_METADATA_BODY           = "INFO_METADATA_BODY";

    /**
     * Information shown that links meta data in an pom.properties file
     * to an entry in the database
     */
    public static final String INFO_METADATA_BODY_POM       = "INFO_METADATA_BODY_POM";

     /**
     * Information shown that links meta data in an pom.properties file
     * to an entry in the database
     */
    public static final String INFO_METADATA_BODY_MANIFEST       = "INFO_METADATA_BODY_MANIFEST";

    /**
     * Heading shown when a metadata match is found and rule
     * considers this to be a fatal error.
     */
    public static final String FATAL_METADATA_HEADING       = "FATAL_METADATA_HEADING";

    /**
     * The message shown when a metadata match is found and rule
     * considers this to be a fatal error.
     */
    public static final String FATAL_METADATA_BODY          = "FATAL_METADATA_BODY";


    /**
     * Format of message for displaying the current database version.
     */
    public static final String INFO_DATABASE_LAST_UPDATE     = "INFO_DATABASE_LAST_UPDATE";

    /**
     * Format of message shown when database version has been updated.
     */
    public static final String INFO_NEW_DATABASE_VERSION    = "INFO_NEW_DATABASE_VERSION";

    /**
     * Details how many new items were added to the database.
     */
    public static final String INFO_ITEMS_ADDED             = "INFO_ITEMS_ADDED";

    /**
     * Details how many items are removed from the database.
     */
    public static final String INFO_ITEMS_REMOVED           = "INFO_ITEMS_REMOVED";

    /**
     * Message to indicate synchronization to the database.
     */
    public static final String INFO_PERFORMING_SYNC         = "INFO_PERFORMING_SYNC";


    /**
     * Heading shown when displaying settings.
     */
    public static final String INFO_SETTINGS_HEADING        = "INFO_SETTINGS_HEADING";

    /**
     * Shown when rule ran without finding an error.
     */
    public static final String INFO_NO_VULNERABILTIES_FOUND = "INFO_NO_VULNERABILTIES_FOUND";

    /**
     * Error shown if supplied JSON object does not contain required keys.
     */
    public static final String ERR_MALFORMED_JSON           = "ERR_MALFORMED_JSON";

    /**
     * Error shown if there is an error querying the database.
     */
    public static final String ERR_INVALID_SQL_STATEMENT    = "ERR_INVALID_SQL_STATEMENT";

    /**
     * Error shown if there is an issue synchronizing with the database.
     */
    public static final String ERR_SYNCHRONIZATION_FAILURE  = "ERR_SYNCHRONIZATION_FAILURE";

    /**
     * Error shown if cannot resolve the physical artifact in the project.
     */
    public static final String ERR_ARTIFACT_NOT_FOUND       = "ERR_ARTIFACT_NOT_FOUND";

    /**
     * Configuration error.
     */
    public static final String ERR_SETTING_MISSING          = "ERR_SETTING_MISSING";

    /**
     * Configuration error - Invalid URL.
     */
    public static final String ERR_INVALID_URL              = "ERR_INVALID_URL";

    /**
     * Configuration error - Invalid mode of operation.
     */
    public static final String ERR_INVALID_MODE             = "ERR_INVALID_MODE";


    @Override
    protected Object[][] getContents() {
        return new Object[][]{
            { ERR_ARTIFACT_NOT_FOUND,       "Artifact %s cannot be located. "                                   },
            { ERR_INVALID_MODE,             "Invalid mode '%s' for the '%s' setting. Valid options are %s. "    },
            { ERR_INVALID_SQL_STATEMENT,    "Invalid SQL statement. "                                           },
            { ERR_INVALID_URL,              "The URL '%s' does not appear to be valid. "                        },
            { ERR_MALFORMED_JSON,           "Malformed JSON object provided. "                                  },
            { ERR_SETTING_MISSING,          "Required setting '%s' is missing. "                                },
            { ERR_SYNCHRONIZATION_FAILURE,  "Database synchronization failed. "                                 },


            { FATAL_FINGERPRINT_HEADING,    "Known vulnerable artifact detected!"                               },
            { FATAL_FINGERPRINT_BODY,       "The file '%s' has a checksum that matches a fingerprint in the "
                                            + "Victims database. You should upgrade to a new version to patches "
                                            + "the reported vulnerabilities. The affected artifact ID is '%s'." },

            { INFO_FINGERPRINT_HEADING,     "Artifact matches a record in the vulnerability database"                     },
//            { INFO_CLASSMATCH_HEADING,      "Artifact contains %s%% of the classes in a record in the vulnerability database" },


            { FATAL_METADATA_HEADING,       "Detected potentially vulnerable artifact"                          },
            { FATAL_METADATA_BODY,          "The artifact '%s' has a metadata that is similar to other "
                                            + "entries in the Victims database. Run the enforcer plugin in "
                                            + "fingerprint mode for confirmation."                              },

            { INFO_METADATA_HEADING,        "Some dependencies in this project have been identified as"
                                            + " potentially being vulnerable."                                  },
            { INFO_METADATA_BODY_POM,       " * %s has a pom.properties file containing: artifactId %s, groupId %s,"
                    + " and version %s. These match a record in the victims database." },

            { INFO_METADATA_BODY_MANIFEST,  " * %s contains a MANIFEST.MF file with entries Implementation-Vendor %s, "
                    + "Implementation-Title, and Implementation-Version %s. These match a record in the victims database.  " },



            { INFO_DATABASE_LAST_UPDATE,    "Victims database last entry was created on %s."                    },
            { INFO_ITEMS_ADDED,             "Added %d new records to the database."                             },
            { INFO_ITEMS_REMOVED,           "Removed %d obsolete records from the database."                    },
            { INFO_NEW_DATABASE_VERSION,    "Victims database version is now %d.0"                              },
            { INFO_PERFORMING_SYNC,         "Synchronizing CVE definitions with local database.."               },
            { INFO_SETTINGS_HEADING,        "enforce-victims-rule settings"                                     },
            { INFO_NO_VULNERABILTIES_FOUND, "No vulnerable artifacts were detected in this project."            }
        };
    }
}
