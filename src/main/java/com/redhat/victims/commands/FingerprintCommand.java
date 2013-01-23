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
package com.redhat.victims.commands;

import com.redhat.victims.*;
import com.redhat.victims.archive.java.FingerprintClassfile;
import com.redhat.victims.archive.java.Jar;
import com.redhat.victims.db.VictimsRecord;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.apache.commons.codec.binary.Hex;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The fingerprint command resolves the supplied artifact and compares the
 * fingerprint of that item to entries within a database of vulnerable artifacts.
 *
 * @author gmurphy
 */
public final class FingerprintCommand implements Command {

    public void execute(ExecutionContext ctx) throws EnforcerRuleException {
        
        try {
            
            // FIXME - Retreive algorithms from the database or allow user 
            // to set these values;
            String algorithms[] = { "SHA-1", "SHA-512" };
            
            VictimsRecord record;
            String combined;
            Iterator iter;
            JSONObject result;
            MessageDigest md;
            FingerprintClassfile visitor;
            Jar jarfile;
            
            jarfile = new Jar(new ZipFile(ctx.getArtifact().getFile()));
            for (String algorithm : algorithms) {
                
                // Visit each class file in the JAR. 
                visitor = new FingerprintClassfile(algorithm);
                jarfile.accept(visitor);
                result = visitor.result();
                
                // Create a master hash 
                iter = result.keys();
                md = MessageDigest.getInstance(algorithm);
                
                while (iter.hasNext()){
                    
                    // Update the master hash
                    String hash = result.getString(iter.next().toString());
                    md.update(hash.getBytes());
                
                    // Search for the class fingerprint whilst at it. 
                    record = ctx.getDatabase().findByClassHash(hash);
                    
                    // TODO
                    if (record != null)
                        System.out.println("Partial match: " + record.toJSON());
                }
                
                combined = new String(Hex.encodeHex(md.digest()));
                record = ctx.getDatabase().findByJarHash(combined);
                
                // TODO
                if (record != null)
                    System.out.println("Full match: " + record.toJSON());
            }
        
        } catch (NoSuchAlgorithmException e) {
            ctx.getLog().error(e);

        } catch (IOException e) {
            ctx.getLog().error(e);

        } catch (JSONException e) {
            ctx.getLog().error(e);
        } catch (SQLException e){
            ctx.getLog().error(e);
        }         
    }

    public String getDefaultExecutionMode() {
        return Settings.MODE_FATAL;
    }
}
