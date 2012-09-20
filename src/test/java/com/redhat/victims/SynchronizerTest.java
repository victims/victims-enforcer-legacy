
package com.redhat.victims;

import com.redhat.victims.db.Statements;
import com.redhat.victims.db.Database;
import junit.framework.TestCase;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;


public class SynchronizerTest extends TestCase {
    
    public SynchronizerTest(String testName) {
        super(testName);
    }
    Database db = null;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        db = new Database("test.db");
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
         if (db != null) {
            try {
                
                JSONObject results = db.executeStatement(Statements.LIST, null);
                if (results.has("collection")){
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

    public void testSynchronizeDatabase() {
        
        System.out.println("synchronizeDatabase");
        
        try { 
            Log log = new org.apache.maven.plugin.logging.SystemStreamLog();
            Synchronizer client = new Synchronizer("https://victims-websec.rhcloud.com/service/v1", log);
            client.synchronizeDatabase(db);
            JSONObject result = db.executeStatement(Statements.LIST, null);
     
            assertTrue(result.has("collection"));
            assertTrue(result.getJSONArray("collection").length() > 0);
            
            
        } catch (Exception e){
            fail("Test failed: " + e.getMessage());
        }
    }
}
