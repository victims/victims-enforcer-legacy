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

import com.redhat.victims.IOUtils;
import com.redhat.victims.Resources;
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
import org.json.JSONArray;
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
         JSONArray results;
         JarMetadata visitor;
         String source;
         String mode;
         Iterator iter;
         VictimsRecord record;

         try {

             // Search pom.properties information that matches our artifact
             mode = ctx.getSettings().get(Settings.METADATA);
             String filename = ctx.getArtifact().getFile().getCanonicalPath();
             String artifactId = ctx.getArtifact().getArtifactId();
             String groupId = ctx.getArtifact().getGroupId();
             String version = ctx.getArtifact().getVersion();
             record = ctx.getDatabase().findByProperties(groupId, artifactId, version);

             if (record != null) {

                 IOUtils.report(ctx.getLog(), mode,
                         IOUtils.fmt(Resources.INFO_METADATA_BODY_POM,
                         filename, artifactId, groupId, version));

                 if (ctx.getSettings().inFatalMode(Settings.METADATA)) {
                     fatalError(artifactId);
                 }
             }

             if (ctx.getSettings().extendedMetadataEnabled()){
                 extendedExecution(ctx);
             }

         } catch (IOException e){
             ctx.getLog().error(e);
         } catch (SQLException e){
             ctx.getLog().error(e);
         }
    }

    private void extendedExecution(ExecutionContext ctx) throws EnforcerRuleException {

        Jar jarfile;
        JSONArray results;
        JarMetadata visitor;
        String source;
        String mode;
        Iterator iter;
        VictimsRecord record;

        try {
            mode = ctx.getSettings().get(Settings.METADATA);
            jarfile = new Jar(new ZipFile(ctx.getArtifact().getFile()));
            visitor = new JarMetadata();
            jarfile.accept(visitor);
            results = visitor.results();
            for (int elem = 0; elem < results.length(); elem++) {

                JSONObject metadata = results.getJSONObject(elem);
                String filename = metadata.getString("filename");

                if (filename.endsWith("pom.properties")) {

                    String artifactId = metadata.getString("artifactId");
                    String groupId = metadata.getString("groupId");
                    String version = metadata.getString("version");
                    record = ctx.getDatabase().findByProperties(groupId, artifactId, version);

                    if (record != null) {

                        IOUtils.report(ctx.getLog(), mode,
                                IOUtils.fmt(Resources.INFO_METADATA_BODY_POM,
                                filename, artifactId, groupId, version));

                        if (ctx.getSettings().inFatalMode(Settings.METADATA)) {
                            fatalError(artifactId);
                        }
                    }

                } else if (filename.endsWith("MANIFEST.MF")) {

                    String vendor = metadata.getString(Attributes.Name.IMPLEMENTATION_VENDOR.toString());
                    String title = metadata.getString(Attributes.Name.IMPLEMENTATION_TITLE.toString());
                    String version = metadata.getString(Attributes.Name.IMPLEMENTATION_VERSION.toString());

                    record = ctx.getDatabase().findByImplementation(vendor, title, version);
                    if (record != null) {

                        IOUtils.report(ctx.getLog(), mode,
                                IOUtils.fmt(Resources.INFO_METADATA_BODY_MANIFEST,
                                filename, vendor, title, version));

                        if (ctx.getSettings().inFatalMode(Settings.METADATA)) {
                            fatalError(ctx.getArtifact().getArtifactId().toString());
                        }
                    }

                } else {
                    if (!filename.endsWith(".class")) {
                        ctx.getLog().debug("No inspector available for: " + filename);
                    }
                }
            }

        } catch (JSONException e) {
            ctx.getLog().error(e);
        } catch (IOException e) {
            ctx.getLog().error(e);
        } catch (SQLException e) {
            ctx.getLog().error(e);
        }

    }

    private void fatalError(String artifactId) throws EnforcerRuleException {

        StringBuilder err = new StringBuilder();
        err.append(IOUtils.box(IOUtils.fmt(Resources.FATAL_METADATA_HEADING)));
        err.append(IOUtils.wrap(80, IOUtils.fmt(Resources.FATAL_METADATA_BODY, artifactId)));
        throw new EnforcerRuleException(err.toString());

    }


    public String getDefaultExecutionMode() {
        return Settings.MODE_WARNING;
    }
}
