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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
            String[] hashes;
            VictimsRecord[] matches;
            double tolerance;
            
            jarfile = new Jar(new ZipFile(ctx.getArtifact().getFile()));
            for (String algorithm : algorithms) {
                
                // Visit each class file in the JAR. 
                visitor = new FingerprintClassfile(algorithm);
                jarfile.accept(visitor);
                result = visitor.result();
                
                // Create a master hash  
                // FIXME: This is pointless really. The master hash should 
                // just be replaced with a hash of the entired JAR content.
                iter = result.keys();
                md = MessageDigest.getInstance(algorithm);
                
                List<String> jarContent = new ArrayList<String>();
                while (iter.hasNext()){
                    
                    // Update the master hash
                    String hash = result.getString(iter.next().toString());
                    md.update(hash.getBytes());
                    jarContent.add(hash);
            
                }
                
                // Check the combined hash 
                combined = new String(Hex.encodeHex(md.digest()));
                record = ctx.getDatabase().findByJarHash(combined);
                if (record != null){
                    
                    System.out.println("Found JAR hash: ");
                    // Display purposes only
                    JSONObject obj = new JSONObject(record.toJSON());
                    obj.remove("hashes");
                    obj.put("hash", combined);
                    
                    // Notify of the error
                    String fmt = IOUtils.fmt(Resources.INFO_FINGERPRINT_HEADING);
                    String info = IOUtils.prettyPrint(fmt, obj);
                    String mode = ctx.getSettings().get(Settings.FINGERPRINT);
                    IOUtils.report(ctx.getLog(), mode, info);
                    
                    if (ctx.getSettings().inFatalMode(Settings.FINGERPRINT)){
                        System.out.println("This is a fatal error!");
                    }
                } else {
                    System.out.println("No matches found...");
                }
                
                tolerance = Double.parseDouble(ctx.getSettings().get("tolerance"));      
                hashes = jarContent.toArray(new String[jarContent.size()]);
                matches = ctx.getDatabase().findByClassSet(hashes, tolerance);
                
                if (matches.length > 0) {
                    
                    System.out.println("Found some matches");
                           
                    for (VictimsRecord r : matches){
                        
                        // Display purposes only
                        JSONObject rjson = new JSONObject(r.toJSON());
                        rjson.remove("hashes");
                        for (String h : r.hashes.keySet()){
                            if (jarContent.contains(h)){
                                rjson.put(h, r.hashes.get(h));
                            }
                        }
                        
                        System.out.println("RJSON: " + rjson.toString());
                        
                        // Notify of the error
                        String fmt = IOUtils.fmt(Resources.INFO_CLASSMATCH_HEADING, String.valueOf(tolerance * 100));
                        String info = IOUtils.prettyPrint(fmt, rjson);
                        String mode = ctx.getSettings().get(Settings.FINGERPRINT);
                        IOUtils.report(ctx.getLog(), mode, info);
                    }
                    if (ctx.getSettings().inFatalMode(Settings.FINGERPRINT)){
                        System.out.println("This is a fatal error!");
                    }
                } else {
                    System.out.println("No matches were found..");
                }
     
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
