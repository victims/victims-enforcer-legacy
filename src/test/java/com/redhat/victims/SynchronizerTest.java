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

import com.redhat.victims.db.Database;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import junit.framework.TestCase;


public class SynchronizerTest extends TestCase {

    public SynchronizerTest(String testName) {
        super(testName);
    }







    Database db = null;
    HttpServer httpd = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();


        db = new Database(Settings.defaults.get(Settings.DATABASE_DRIVER),
                Settings.defaults.get(Settings.DATABASE_URL));
        //db = new Database("org.apache.derby.jdbc.ClientDriver",
        //        "jdbc:derby://localhost:1527/victims-test");

        db.dropTables();
        db.createTables();

        httpd = HttpServer.create(new InetSocketAddress(1337), 0);
        httpd.start();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
         if (db != null) {

            try {
                db.dropTables();

            } catch (Exception e) {
            } finally {
                db.disconnect();
                httpd.stop(0);
            };
        }
    }

    public void testSynchronizeDatabase() {

        try {

            // TODO: Actual data
            HttpHandler handler = new HttpHandler(){
                public void handle(HttpExchange ex) throws IOException {

                    byte[] rsp = "[]".getBytes();
                    ex.sendResponseHeaders(HttpURLConnection.HTTP_OK, rsp.length);
                    ex.getResponseBody().write("[]".getBytes());
                }
            };


            HttpContext ctx = httpd.createContext("/", handler);

            Synchronizer client = new Synchronizer("http://localhost:1337/service/v2");
            client.synchronizeDatabase(db);

            httpd.removeContext(ctx);
            
            fail("Test not implemented");
            assert(db.list().size() > 0);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        } finally {
        }
    }

}
