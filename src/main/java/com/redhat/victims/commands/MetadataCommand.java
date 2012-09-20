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
import com.redhat.victims.VictimsException;
import com.redhat.victims.db.Database;
import com.redhat.victims.db.Statements;
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
public class MetadataCommand implements Command {

    public void execute(ExecutionContext ctx) throws EnforcerRuleException {

        try {

            JSONObject q = new JSONObject();
            q.put("version", ctx.getArtifact().getVersion());
            q.put("name", ctx.getArtifact().getArtifactId());

            Database db = ctx.getDatabase();
            JSONObject rs = db.executeStatement(Statements.CHECK_JAR, q);

            if (rs.getBoolean("result") && rs.has("collection")) {

                String mode = ctx.getSettings().get(Settings.Metadata);
                String info = IOUtils.fmt(Resources.INFO_METADATA_HEADING);
                IOUtils.report(ctx.getLog(), mode, info);

                JSONArray jars = rs.getJSONArray("collection");
                for (int i = 0; i < jars.length(); i++) {

                    JSONObject o = jars.getJSONObject(i);
                    String artifact = ctx.getArtifact().getArtifactId();
                    String name = o.getString("name");
                    String ver = o.getString("version");
                    String match = IOUtils.fmt(Resources.INFO_METADATA_BODY, artifact, name, ver);
                    IOUtils.report(ctx.getLog(), mode, match);

                }

                if (ctx.getSettings().inFatalMode(Settings.Metadata)) {

                    StringBuilder err = new StringBuilder();
                    err.append(IOUtils.box(IOUtils.fmt(Resources.FATAL_METADATA_HEADING)));
                    err.append(IOUtils.wrap(80, IOUtils.fmt(Resources.FATAL_METADATA_BODY, ctx.getArtifact().getId())));
                    throw new EnforcerRuleException(err.toString());

                }
            }

        } catch (JSONException e) {
            ctx.getLog().error(e.getMessage());
        } catch (VictimsException e) {
            ctx.getLog().error(e.getMessage());
        }
    }

    public String getDefaultExecutionMode() {
        return Settings.ModeWarning;
    }
}
