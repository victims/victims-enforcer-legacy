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
