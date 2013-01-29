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
import java.util.List;
import junit.framework.TestCase;
import org.apache.maven.plugin.logging.Log;


public class SynchronizerTest extends TestCase {

    public SynchronizerTest(String testName) {
        super(testName);
    }
    
    Database db = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        db = new Database("org.apache.derby.jdbc.ClientDriver", 
                "jdbc:derby://localhost:1527/victims-test");

        db.dropTables();
        db.createTables();
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
            };
        }
    }

    public void testSynchronizeDatabase() {
        
        try {
            
            System.out.println("synchronizeDatabase");
            System.out.println("VICTIMS: " + db.tableExists("VICTIMS"));
            System.out.println("FINGERPRINTS: " + db.tableExists("FINGERPRINTS"));
            System.out.println("METADATA: " + db.tableExists("METADATA"));
        
            Log log = new org.apache.maven.plugin.logging.SystemStreamLog();
            Synchronizer client = new Synchronizer("http://localhost:5000/service/v2", log);
            client.synchronizeDatabase(db);
           
            assert(db.list().size() > 0);
          
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }

}
