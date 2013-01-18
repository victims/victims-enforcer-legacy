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
package com.redhat.victims.archive;

import java.io.File;
import java.io.FileWriter;
import java.util.zip.ZipFile;
import junit.framework.TestCase;

/**
 *
 * @author gm
 */
public class MetadataVisitorTest extends TestCase {
    
    public MetadataVisitorTest(String testName) {
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
     * Test of visit method, of class MetadataVisitor.
     */
    public void testVisit() {
        
        try { 
            
            ZipFile jar = new ZipFile(new File("testdata", "junit-3.8.1.jar"));
            Jar j = new Jar(jar);
            
            MetadataVisitor instance = new MetadataVisitor();
            j.accept(instance);
          
            FileWriter fout = new FileWriter(new File("tmp", "junit-3.8.1.jar.meta"));
            fout.write(instance.getInformation().toString());
            fout.close();
            
            // FIXME: Actual testing for expected values
            fail("this test is not finished yet");
            
        } catch(Exception e){
            fail(e.toString());
        }
    }

    
}
