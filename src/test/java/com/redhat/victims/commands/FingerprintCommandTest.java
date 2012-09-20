/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.victims.commands;

import com.redhat.victims.Settings;
import com.redhat.victims.db.Database;
import com.redhat.victims.db.Statements;
import java.io.File;
import junit.framework.TestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.json.JSONArray;
import org.json.JSONObject;

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
        db = new Database("test.db");
        JSONObject json = new JSONObject();
        json.put("cves", "CVE-TEST-ONLY");
        json.put("hash", "8e6f9fa5eb3ba93a8b1b5a39e01a81c142b33078264dbd0a2030d60dd26735407249a12e66f5cdcab8056e93a5687124fe66e741c233b4c7a06cc8e49f82e98b");
        json.put("db_version", 0);
        json.put("version", "3.8.1");
        json.put("vendor", "Test Vendor");
        json.put("name", "junit");
        db.executeStatement(Statements.INSERT, json);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (db != null) {
            try {

                JSONObject results = db.executeStatement(Statements.LIST, null);
                if (results.has("collection")) {
                    JSONArray json = results.getJSONArray("collection");
                    for (int i = 0; i < json.length(); i++) {
                        JSONObject next = json.getJSONObject(i);
                        db.executeStatement(Statements.REMOVE, next);
                    }
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void testExecute() throws Exception {
        try {

        ArtifactHandler handler = new DefaultArtifactHandler();
        Artifact testArtifact = new DefaultArtifact("junit", "junit", "3.8.1", "test", "jar", null, handler);
        testArtifact.setFile(new File("testdata", "junit-3.8.1.jar"));

        Log log = new SystemStreamLog();
        Settings config = new Settings();
        config.set(Settings.Fingerprint, Settings.ModeFatal);
        config.set(Settings.DatabasePath, ".victims");
        config.set(Settings.Metadata, Settings.ModeWarning);
        config.set(Settings.URL, "http://www.dummy.com/service/v1");
        config.set(Settings.UpdateDatabase, "auto");
        config.validate();


        ExecutionContext ctx = new ExecutionContext();
        ctx.setArtifact(testArtifact);
        ctx.setDatabase(db);
        ctx.setLog(log);
        ctx.setSettings(config);

        FingerprintCommand cmd = new FingerprintCommand();
        config.set(Settings.Fingerprint, Settings.ModeDisabled);
        ctx.setSettings(config);
        cmd.execute(ctx);

        config.set(Settings.Fingerprint, Settings.ModeWarning);
        ctx.setSettings(config);
        cmd.execute(ctx);

        config.set(Settings.Fingerprint, Settings.ModeFatal);
        ctx.setSettings(config);
        cmd.execute(ctx);

        cmd.execute(ctx);


        } catch (Exception e) {
            System.err.println(e.getMessage());
            assertTrue(e instanceof EnforcerRuleException);
        }

    }

    public void testGetDefaultExecutionMode() {
        System.out.println("getDefaultExecutionMode");
        FingerprintCommand instance = new FingerprintCommand();
        String expResult = com.redhat.victims.Settings.ModeFatal;
        String result = instance.getDefaultExecutionMode();
        assertEquals(expResult, result);
    }
}
