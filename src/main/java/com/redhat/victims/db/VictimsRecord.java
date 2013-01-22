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

import com.google.gson.Gson;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author gm
 */
public class VictimsRecord  {
    
    public Status       status;
    public int          id;
    public Date         date;
    public String       vendor;
    public String       name;
    public String       format;
    public String       version;
    public String       submitter;
    public Map<String, HashRecord> hashes;
    public String[]     cves;
    public Map<String, Map<String, String> > meta;
    
    public VictimsRecord(){
        
        hashes = new HashMap<String, HashRecord>();
        meta = new HashMap<String, Map<String, String> >();
    }
    
    public String toJSON(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    
    public static VictimsRecord fromJSON(String str){
        Gson gson = new Gson(); 
        return gson.fromJson(str, VictimsRecord.class);
    }
    
    public static VictimsRecord fromJSON(JSONObject o){
        return fromJSON(o.toString());
    }
}
