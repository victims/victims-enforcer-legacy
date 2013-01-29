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

import com.redhat.victims.Settings;
import com.redhat.victims.archive.java.Jar;
import com.redhat.victims.archive.java.JarMetadata;
import com.redhat.victims.db.VictimsRecord;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.zip.ZipFile;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Checks the supplied artifact to see if it has a name and version similar to
 * any entries within the victims database.
 *
 * @author gmurphy
 */
public final class MetadataCommand implements Command {

    
    public void execute(ExecutionContext ctx) throws EnforcerRuleException {

         Jar jarfile;
         JSONObject result;
         JarMetadata visitor;
         String source; 
         Iterator iter;
         VictimsRecord record; 
         
         try {
             
             jarfile = new Jar(new ZipFile(ctx.getArtifact().getFile()));
             visitor = new JarMetadata();
             jarfile.accept(visitor);
             result = visitor.result();
          
             System.out.println("JSON Result ::=" + result);

             iter = result.keys();
             while (iter.hasNext()){
                 
                 source = iter.next().toString();
                 System.out.println("source: " + source);
                 
                 if (source.endsWith("pom.properties")){
                     
                     JSONObject properties = result.getJSONObject(source);
                     System.out.println("processing pom.xml: " + properties.toString() );

                     
                     
                     record = ctx.getDatabase().findByProperties(
                             properties.getString("groupId"), 
                             properties.getString("artifactId"),
                             properties.getString("version"));
                     
                     // TODO Fix this
                     if (record != null)
                         System.out.println("Parital match: " + record.toJSON());
                     else
                         System.out.println("No match for (pom): " + properties.toString());
                     
                 }
                 
                 if (source.endsWith("MANIFEST.MF")){
                     
                     JSONObject manifest = result.getJSONObject(source);
                     System.out.println("processing MANIFEST.MF: " + manifest.toString());
                     
                     record = ctx.getDatabase().findByImplementation(
                             manifest.getString(Attributes.Name.IMPLEMENTATION_VENDOR.toString()),
                             manifest.getString(Attributes.Name.IMPLEMENTATION_TITLE.toString()),
                             manifest.getString(Attributes.Name.IMPLEMENTATION_VERSION.toString()));
                     
                     if (record != null)
                         System.out.println("Partial match: " + record.toJSON());
                     else
                         System.out.println("No match for (manifest): " + manifest.toString());
                 }
                 
             }
             
         } catch (JSONException e){
             ctx.getLog().error(e);
         } catch (IOException e){
             ctx.getLog().error(e);
         } catch (SQLException e){
             ctx.getLog().error(e);
         }
    }

    public String getDefaultExecutionMode() {
        return Settings.MODE_WARNING;
    }
}
