
package com.redhat.victims;

import com.redhat.victims.db.Statements;
import com.redhat.victims.db.Database;
import java.io.File;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseTest extends TestCase {

    public DatabaseTest(String testName) {
        super(testName);
    }

    Database db;
    JSONArray json;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String content = IOUtils.slurp(new File("testdata", "testdata.json"));
        json = new JSONArray(content);
        db = new Database("test.db", "org.apache.derby.jdbc.EmbeddedDriver");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (db != null) {
            try {
                for (int i = 0; i < json.length(); i++) {
                    JSONObject next = (json.getJSONObject(i)).getJSONObject("fields");
                    db.executeStatement(Statements.REMOVE, next);
                }
            } catch (Exception e) {
            };
        }
    }


    public void testInsert() {


        try {

            int inserted = 0;
            JSONObject result;
            for (int i = 0; i < json.length(); i++) {
                JSONObject next = (json.getJSONObject(i)).getJSONObject("fields");
                result = db.executeStatement(Statements.INSERT, next);
                assertTrue(result.getBoolean("result"));
                inserted ++;
            }

            JSONObject listing = db.executeStatement(Statements.LIST, null);
            JSONArray entries = listing.getJSONArray("collection");
            int listed = 0;
            for (int i = 0 ; i < entries.length(); i++) {
                //System.out.println(entries.get(i).toString());
                listed ++;
            }
            assertTrue(inserted == listed);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testContains() {

        String vuln = "0a54d7c34e792162c7cad21d6e9ba8038efa98e6863c15c70857b110291fc22727f00d6042ded140ff295cfadaf83c5e75585fd6da5e0d0ccb6a83642353fbd1";
        String notvuln = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

        try {

            JSONObject result;
            for (int i = 0; i < json.length(); i++) {
                JSONObject next = (json.getJSONObject(i)).getJSONObject("fields");
                db.executeStatement(Statements.INSERT, next);
            }

            // This hash exists in the database (after insert test)
            JSONObject expectTrue = new JSONObject();
            expectTrue.put("hash", vuln);
            result = db.executeStatement(Statements.CHECK_HASH, expectTrue);
            assertTrue(result.getBoolean("result"));

            // This is not a valid hash
            JSONObject expectFail = new JSONObject();
            expectFail.put("hash", notvuln);
            result = db.executeStatement(Statements.CHECK_HASH, expectFail);
            assertFalse(result.getBoolean("result"));


        } catch(Exception e) {
            fail(e.getMessage());
        }

    }

    public void testVersion() {

        try {

            for (int i = 0; i < json.length(); i++) {
                JSONObject next = (json.getJSONObject(i)).getJSONObject("fields");
                db.executeStatement(Statements.INSERT, next);
            }

            JSONObject result = db.executeStatement(Statements.VERSION, null);
            System.out.println("Latest version of database is: " + result.getInt("db_version"));
            assert(result.getInt("db_version") == 6);

        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public void testVersionEmpty() {
        try {
            JSONObject result = db.executeStatement(Statements.VERSION, null);
            System.out.println("Latest version of the database is: " + result.getInt("db_version"));
            assert(result.getInt("db_version") == 0);

        } catch(Exception e) {
            fail("Test failed: "  + e.getMessage());
        }
    }


    public void testContainsJAR() {

        //String name = "junit";
        //String version = "3.8.1";

        String name = "axis2";
        String version = "1.4.1";
        try {

            for (int i = 0; i < json.length(); i++) {
                JSONObject next = (json.getJSONObject(i)).getJSONObject("fields");
                db.executeStatement(Statements.INSERT, next);
            }

            JSONObject query = new JSONObject();
            query.put("version", version);
            query.put("name", name);
            JSONObject rs = db.executeStatement(Statements.CHECK_JAR, query);
            System.out.println("JSON: " + rs.toString());
            if (rs.has("version"))
                System.out.println("RESULT: " + rs.getBoolean("version"));

            assertTrue(rs.getBoolean("result"));

        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }
}


