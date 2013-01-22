/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.victims.db;

import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author gm
 */
public class VictimsRecordTest extends TestCase {
    
    public VictimsRecordTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of toJSON method, of class VictimsRecord.
     */
    public void testToJSON() {
        
        
        System.out.println("toJSON");
        VictimsRecord rec = new VictimsRecord();
        rec.cves = new String[]{ "CVE-1001", "CVE-1002" };
        rec.date = new java.util.Date();
        rec.format = "jar";
        rec.id = 1234;
        rec.name = "test.jar";
        rec.status  = Status.RELEASED;
        rec.submitter = "gmurphy@redhat.com";
        rec.vendor = "apache";
        rec.version = "1.0.0";
        
        HashRecord sample = new HashRecord();
        sample.combined  = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
        sample.files.put("sample1.class", "AAAAAAAAAAAAAAAAAAAAAAAAA");
        sample.files.put("sample2.class", "bbbbbbbbbbbbbbbbbbbbbbbbb");
        sample.files.put("sample3.class", "ccccccccccccccccccccccccc");
      
        rec.hashes.put("SHA-1", sample);
        rec.hashes.put("SHA-512", sample);
        
        Map<String, String> metadata = new java.util.HashMap<String, String>();
        metadata.put("somekey1", "somevalue");
        metadata.put("somekey2", "somevalue");
        metadata.put("somekey3", "somevalue");
        metadata.put("somekey4", "somevalue");
        
        rec.meta.put("MANIFEST.MF", metadata);
        rec.meta.put("pom.properties", metadata);
      
 
        
        System.out.println(rec.toJSON());
        
    }
}
