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
import com.redhat.victims.Synchronizer;
import com.redhat.victims.db.Database;
import java.io.File;
import junit.framework.TestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

/**
 *
 * @author gmurphy
 */
public class FingerprintCommandTest extends TestCase {

    private Database db;

    public FingerprintCommandTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        db = new Database("org.apache.derby.jdbc.ClientDriver", 
                "jdbc:derby://localhost:1527/victims-test");
        
        db.dropTables();
       
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (db != null) {
            try {

               db.dropTables();
              
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void testExecute() throws Exception {
        
        try {
            
            db.createTables();
            Synchronizer dbsync = new Synchronizer("http://localhost:5000/service/v2");
            dbsync.synchronizeDatabase(db);

            ArtifactHandler handler = new DefaultArtifactHandler();
            Artifact testArtifact = new DefaultArtifact("junit", "junit", "3.8.1", "test", "jar", null, handler);
            testArtifact.setFile(new File("testdata", "junit-3.8.1.jar"));

            Log log = new SystemStreamLog();
            Settings config = new Settings();
            config.set(Settings.FINGERPRINT, Settings.MODE_FATAL);
            config.set(Settings.DATABASE_PATH, ".victims");
            config.set(Settings.METADATA, Settings.MODE_WARNING);
            config.set(Settings.URL, "http://www.dummy.com/service/v1");
            config.set(Settings.UPDATE_DATABASE, "auto");
            config.set(Settings.TOLERANCE, "0.75");
            config.validate();


            ExecutionContext ctx = new ExecutionContext();
            ctx.setArtifact(testArtifact);
            ctx.setDatabase(db);
            ctx.setLog(log);
            ctx.setSettings(config);

            FingerprintCommand cmd = new FingerprintCommand();
            config.set(Settings.FINGERPRINT, Settings.MODE_DISABLED);
            ctx.setSettings(config);
            cmd.execute(ctx);

            config.set(Settings.FINGERPRINT, Settings.MODE_WARNING);
            ctx.setSettings(config);
            cmd.execute(ctx);

            config.set(Settings.FINGERPRINT, Settings.MODE_FATAL);
            ctx.setSettings(config);
            cmd.execute(ctx);



        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof EnforcerRuleException);
        }

    }

    public void testGetDefaultExecutionMode() {
        System.out.println("getDefaultExecutionMode");
        FingerprintCommand instance = new FingerprintCommand();
        String expResult = com.redhat.victims.Settings.MODE_FATAL;
        String result = instance.getDefaultExecutionMode();
        assertEquals(expResult, result);
    }
    
}
