
package com.redhat.victims;

import com.redhat.victims.db.Database;
import com.redhat.victims.db.VictimsRecord;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    File scriptfile;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.gc();

        String content = IOUtils.slurp(new File("testdata", "test.json"));
        json = new JSONArray(content);
        
        scriptfile = new File("testdata", "test.sql");
        
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
                //db.dropTables();
           
            } catch (Exception e) {
            } finally {
                db.disconnect();
            };
            
        }
    }
    

    public void testInsert() throws Exception {
        
        try {
    
            for (int i = 0; i < json.length(); i++) {
                
                JSONObject obj = json.getJSONObject(i).getJSONObject("fields");
                String dateString = obj.getString("date").split("\\.")[0];
                obj.put("date", dateString);
                
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


