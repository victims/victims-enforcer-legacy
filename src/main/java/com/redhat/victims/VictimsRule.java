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

import com.redhat.victims.commands.Command;
import com.redhat.victims.commands.ExecutionContext;
import com.redhat.victims.commands.FingerprintCommand;
import com.redhat.victims.commands.MetadataCommand;
import com.redhat.victims.db.Database;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

/**
 * This class is the main entry point for working with the Maven Enforcer
 * plug-in. It provides the logic to synchronize a local database with a remote
 * database of vulnerable Java artifacts. This database is used to check for any
 * dependencies used within the project that may have known vulnerabilities.
 *
 * @author gmurphy
 */
public class VictimsRule implements EnforcerRule {

    /*
     * Configuration options available in pom.xml
     */
    private String url = Settings.defaults.get(Settings.URL);
    private String metadata = Settings.defaults.get(Settings.METADATA);
    private String fingerprint = Settings.defaults.get(Settings.FINGERPRINT);
    private String path = Settings.defaults.get(Settings.DATABASE_PATH);
    private String updates = Settings.defaults.get(Settings.UPDATE_DATABASE);

    /*
     * Checks performed as a part of this rule
     */
    private Command[] commands = {
        new MetadataCommand(),
        new FingerprintCommand()
    };

    /**
     * Main entry point for the enforcer rule.
     *
     * @param helper
     * @throws EnforcerRuleException
     */
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {

        Log log = helper.getLog();

        try {

            // Create and validate settings
            Settings setup = new Settings();
            setup.set(Settings.URL, url);
            setup.set(Settings.METADATA, metadata);
            setup.set(Settings.FINGERPRINT, fingerprint);
            setup.set(Settings.DATABASE_PATH, path);
            setup.set(Settings.UPDATE_DATABASE, updates);
            setup.validate();
            setup.show(log);

            // Create database instance
            Database db = new Database(setup.get(Settings.DATABASE_PATH));

            // Synchronize it with the server
            if (setup.updatesEnabled()) {
                Synchronizer sync = new Synchronizer(setup.get(Settings.URL));
                sync.synchronizeDatabase(db);
            }

            ExecutionContext ctx = new ExecutionContext();
            ctx.setDatabase(db);
            ctx.setLog(log);
            ctx.setSettings(setup);

            MavenProject project = (MavenProject) helper.evaluate("${project}");
            for (Artifact a : project.getArtifacts()) {

                for (Command c : commands) {
                    ctx.setArtifact(a);
                    c.execute(ctx);
                }
            }
            log.info(IOUtils.fmt(Resources.INFO_NO_VULNERABILTIES_FOUND));

        } catch (ExpressionEvaluationException e) {
            log.error(e.toString());

        } catch (VictimsException e) {
            throw new EnforcerRuleException(e.getMessage());
        }

    }

    /**
     * The database is always synchronized with the Victims Server. The initial
     * import of entries will take some time but subsequent requests should be
     * relatively fast.
     *
     * @return Always will return false.
     */
    public boolean isCacheable() {
        return false;
    }

    /**
     * Feature not used by this rule.
     *
     * @param er
     * @return Always returns false
     */
    public boolean isResultValid(EnforcerRule er) {
        return false;
    }

    /**
     * Feature not used by this plug-in. Return a bogus value.
     *
     * @return Not used
     */
    public String getCacheId() {
        return " " + new java.util.Date().getTime();
    }
}
