package com.redhat.victims;

/*
 * #%L
 * This file is part of victims-enforcer.
 * %%
 * Copyright (C) 2013 The Victims Project
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.ListResourceBundle;

/**
 * Basic resource bundle for messages and formating.
 * @author gmurphy
 */
public class Resources extends ListResourceBundle  {

    /**
     * Heading shown when vulnerability detected
     */
    public static final String ERR_VULNERABLE_HEADING       = "ERR_VULNERABLE_HEADING";

    /**
     * Informational message shown if vulnerability is matched to the database
     */
    public static final String INFO_VULNERABLE_DEPENDENCY   = "INFO_VULNERABLE_DEPENDENCY";

    /**
     * Message shown if in fatal mode
     */
    public static final String ERR_VULNERABLE_DEPENDENCY    = "ERR_VULNERABLE_DEPENDENCY";

    /**
     * Heading shown when displaying settings.
     */
    public static final String INFO_SETTINGS_HEADING        = "INFO_SETTINGS_HEADING";

    /**
     * Configuration error.
     */
    public static final String ERR_SETTING_MISSING          = "ERR_SETTING_MISSING";


    /**
     * Configuration error - Invalid mode of operation.
     */
    public static final String ERR_INVALID_MODE             = "ERR_INVALID_MODE";

    /**
     * Show details of where updates are being retrieved from.
     */
    public static final String INFO_UPDATES                 = "INFO_UPDATES";

    @Override
    protected Object[][] getContents() {
        return new Object[][]{
            { ERR_VULNERABLE_HEADING,       "Vulnerability detected!" },
            { INFO_VULNERABLE_DEPENDENCY,   "The dependency %s-%s matches a vulnerability recorded in the victims database. [%s]"},
            { ERR_VULNERABLE_DEPENDENCY,    "For more information visit https://access.redhat.com/security/cve/%s"},
            { INFO_SETTINGS_HEADING,        "victims-enforcer settings"                                         },
            { ERR_INVALID_MODE,             "Invalid mode '%s' for the '%s' setting. Valid options are %s. "    },
            { ERR_SETTING_MISSING,          "Required setting '%s' is missing. "                                },
            { INFO_UPDATES,                 "Retrieving updates from %s..."                                     }


        };
    }
}
