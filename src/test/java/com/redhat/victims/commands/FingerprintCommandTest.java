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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.redhat.victims.TextUI;
import com.redhat.victims.Settings;
import com.redhat.victims.Synchronizer;
import com.redhat.victims.archive.java.FingerprintClassfile;
import com.redhat.victims.archive.java.Jar;
import com.redhat.victims.archive.java.JarMetadata;
import com.redhat.victims.db.Database;
import com.redhat.victims.db.HashRecord;
import com.redhat.victims.db.Status;
import com.redhat.victims.db.VictimsRecord;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipFile;
import junit.framework.TestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.json.JSONObject;

/**
 *
 * @author gmurphy
 */
public class FingerprintCommandTest extends TestCase {

    private Database db;
    private HttpServer httpd;

    public FingerprintCommandTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

//        db = new Database("org.apache.derby.jdbc.ClientDriver",
//                "jdbc:derby://localhost:1527/victims-test");
        db = new Database(Settings.defaults.get(Settings.DATABASE_DRIVER),
                Settings.defaults.get(Settings.DATABASE_URL));

        db.dropTables();
        db.createTables();

        httpd = HttpServer.create(new InetSocketAddress(1337), 0);

        HttpHandler dummy = new HttpHandler() {
            public void handle(HttpExchange exchange) {

                try {
                    final byte[] json = TextUI.slurp(new File("testdata", "test.json")).getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, json.length);
                    exchange.getResponseBody().write(json);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        httpd.createContext("/", dummy);
        httpd.start();

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
        httpd.stop(0);
    }

    public void testExecute() throws Exception {


        boolean found = false;
        try {

//            Jar j = new Jar(new ZipFile(new File("testdata/junit-3.8.1.jar")));
//            FingerprintClassfile sha512 = new FingerprintClassfile("SHA-512");
//            JarMetadata meta = new JarMetadata();
//            j.accept(sha512);
//            j.accept(meta);
//
//            VictimsRecord r = new VictimsRecord();
//            r.cves = new String[]{ "CVE-9999-999" };
//            r.date = new Date();
//            r.format = "jar";
//            r.hash = "8e6f9fa5eb3ba93a8b1b5a39e01a81c142b33078264dbd0a2030d60dd26735407249a12e66f5cdcab8056e93a5687124fe66e741c233b4c7a06cc8e49f82e98b";
//            r.name = "junit";
//            r.submitter = "Grant Murphy";
//            r.version = "3.8.1";
//            r.status = Status.RELEASED;
//            r.vendor = "junit";
//
//            Map<String, HashRecord> hashes = new HashMap<String, HashRecord>();
//            HashRecord hashRecord = new HashRecord();
//            hashRecord.combined = r.hash;
//            JSONObject fileHashes = sha512.results().getJSONObject(0);
//
//            Iterator iter = fileHashes.keys();
//            while (iter.hasNext()){
//                String h = (String) iter.next();
//                String file = fileHashes.getString(h);
//                hashRecord.files.put(h, file);
//            }
//
//            hashes.put("sha512", hashRecord);
//            r.hashes = hashes;
//
//            System.out.println(r.toJSON().toString());
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            //System.out.println(sha512.results().toString());
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println(meta.results().toString());
//
//
//
//

            db.createTables();
            Synchronizer dbsync = new Synchronizer("http://localhost:1337/service/v2");
            dbsync.synchronizeDatabase(db);;

            ArtifactHandler handler = new DefaultArtifactHandler();
            Artifact testArtifact = new DefaultArtifact("junit", "junit", "3.8.1", "test", "jar", null, handler);
            testArtifact.setFile(new File("testdata", "junit-3.8.1.jar"));

            Log log = new SystemStreamLog();
            Settings config = new Settings();
            config.set(Settings.FINGERPRINT, Settings.MODE_FATAL);
            config.set(Settings.METADATA, Settings.MODE_WARNING);
            config.set(Settings.URL, "http://www.dummy.com/service/v2");
            config.set(Settings.UPDATE_DATABASE, "auto");
            //config.set(Settings.TOLERANCE, "0.75");
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
            found = true;

        } finally {
            if (!found)
               fail("Failed to detect vulnerability from hashes");
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
