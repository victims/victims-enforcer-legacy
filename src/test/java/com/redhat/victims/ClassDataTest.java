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

import junit.framework.TestCase;
import java.io.File;
import java.io.DataInputStream;
import java.io.FileInputStream;
import com.redhat.victims.hash.ClassData;

public class ClassDataTest extends TestCase {

    public ClassDataTest(String testName) {
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

    public void testRead() {

        try {
            
            DataInputStream stream = 
                new DataInputStream(
                    new FileInputStream(new File("testdata", "Hello.class")));

            ClassData data = new ClassData(stream);
            data.readAll();
            assertEquals(0, data.getInput().available());

        } catch(Exception e) {
            fail("Test failed: "  + e);
        }
    }

}


