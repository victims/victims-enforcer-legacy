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
package com.redhat.victims.hash;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

/**
 * Mechanism for creating fingerprints compatible with those in the victims
 * database.
 *
 * @author gmurphy
 */
public final class Hash {

    /**
     * Disable constructor on utility class
     */
    private Hash() {
        // Not used
    }

    public static String hash(String algorithm, final InputStream is)
            throws NoSuchAlgorithmException, IOException {

        int nbytes;
        byte[] buf = new byte[1024];
        byte[] digest;

        MessageDigest md = MessageDigest.getInstance("SHA-1");

        while ((nbytes = is.read(buf)) > 0) {
            md.update(buf, 0, nbytes);
        }

        digest = md.digest().clone();

        return new String(Hex.encodeHex(digest));
    }

  
}
