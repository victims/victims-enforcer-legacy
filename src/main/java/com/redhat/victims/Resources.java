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
    
    /** Fingerprint command messages */ 
    public static final String INFO_FINGERPRINT_HEADING     = "INFO_FINGERPRINT_HEADING";
    public static final String FATAL_FINGERPRINT_BODY       = "FATAL_FINGERPRINT_BODY";
    public static final String FATAL_FINGERPRINT_HEADING    = "FATAL_FINGERPRINT_HEADING";
    
    /** Metadata command messages */ 
    public static final String INFO_METADATA_HEADING        = "INFO_METADATA_HEADING";
    public static final String INFO_METADATA_BODY           = "INFO_METADATA_BODY";
    public static final String FATAL_METADATA_HEADING       = "FATAL_METADATA_HEADING";
    public static final String FATAL_METADATA_BODY          = "FATAL_METADATA_BODY";
    
    /* Synchronization messages */ 
    public static final String INFO_DATABASE_VERSION        = "INFO_DATABASE_VERSION"; 
    public static final String INFO_NEW_DATABASE_VERSION    = "INFO_NEW_DATABASE_VERSION";
    public static final String INFO_ITEMS_ADDED             = "INFO_ITEMS_ADDED"; 
    public static final String INFO_ITEMS_REMOVED           = "INFO_ITEMS_REMOVED";
    public static final String INFO_PERFORMING_SYNC         = "INFO_PERFORMING_SYNC";
    
    /* Setting information messages */ 
    public static final String INFO_SETTINGS_HEADING        = "INFO_SETTINGS_HEADING";

    /* Successful run message */ 
    public static final String INFO_NO_VULNERABILTIES_FOUND = "INFO_NO_VULNERABILTIES_FOUND";
    
    /* Error messages */ 
    public static final String ERR_MALFORMED_JSON           = "ERR_MALFORMED_JSON"; 
    public static final String ERR_INVALID_SQL_STATEMENT    = "ERR_INVALID_SQL_STATEMENT"; 
    public static final String ERR_SYNCHRONIZATION_FAILURE  = "ERR_SYNCHRONIZATION_FAILURE";
    public static final String ERR_ARTIFACT_NOT_FOUND       = "ERR_ARTIFACT_NOT_FOUND";
    public static final String ERR_SETTING_MISSING          = "ERR_SETTING_MISSING";     
    public static final String ERR_INVALID_URL              = "ERR_INVALID_URL";
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
             
            { INFO_FINGERPRINT_HEADING,     "Vulnerable artifact matched a database record"                     },
        
            
            { FATAL_METADATA_HEADING,       "Detected potentially vulnerable artifact"                          }, 
            { FATAL_METADATA_BODY,          "The artifact '%s' has a name and version similar to one or more "
                                            + "entries in the Victims database. Run the enforcer plugin in "
                                            + "fingerprint mode for confirmation."                              }, 
            
            { INFO_METADATA_HEADING,        "Some dependencies in this project have been identified as"
                                            + " potentially being vulnerable."                                  },
            { INFO_METADATA_BODY,           " * Artifact %s matches the database entry %s (%s) "                },
           
            
            { INFO_DATABASE_VERSION,        "Victims database version is %d.0"                                  }, 
            { INFO_ITEMS_ADDED,             "Added %d new records to the database."                             },
            { INFO_ITEMS_REMOVED,           "Removed %d obsolete records from the database."                    },
            { INFO_NEW_DATABASE_VERSION,    "Victims database version is now %d.0"                              },
            { INFO_PERFORMING_SYNC,         "Synchronizing CVE definitions with local database.."               },
            { INFO_SETTINGS_HEADING,        "enforce-victims-rule settings"                                     },
            { INFO_NO_VULNERABILTIES_FOUND, "No vulnerable artifacts were detected in this project."            }
        };
    }
    
}
