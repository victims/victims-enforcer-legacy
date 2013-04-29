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
import com.redhat.victims.db.VictimsRecord;
import java.io.File;
import java.util.List;
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

        final String content = IOUtils.slurp(new File("testdata", "test.json"));
        json = new JSONArray(content);

        db = new Database(Settings.defaults.get(Settings.DATABASE_DRIVER),
                Settings.defaults.get(Settings.DATABASE_URL));
        //db = new Database("org.apache.derby.jdbc.ClientDriver",
        //        "jdbc:derby://localhost:1527/victims-test");

        db.dropTables();
        db.createTables();

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (db != null) {

            try {
                //db.dropTables();

            } catch (Exception e) {
            } finally {
                db.disconnect();
            };

        }
    }


    public void testInsert() throws Exception {

        try {

            //System.out.println(Arrays.toString(db.list().toArray()));
            for (int i = 0; i < json.length(); i++) {

                JSONObject obj = json.getJSONObject(i).getJSONObject("fields");
                VictimsRecord record = VictimsRecord.fromJSON(obj);
                db.insert(record);
            }

            // Can retreive the same number of items we inserted.
            List<VictimsRecord> records = db.list();
            assertTrue(json.length() == records.size());

            // Retrieve a single record
            VictimsRecord record = db.get(1);

            // Delete a record
            db.remove(record.id);

            // One less record.
            assertTrue(json.length() > db.list().size());

            // Retrieve a record that doesn't exist
            VictimsRecord notThere = db.get(1);
            assert(notThere == null);

        } catch (Exception e) {

            e.printStackTrace();
            fail(e.getMessage());
        }
    }




}


