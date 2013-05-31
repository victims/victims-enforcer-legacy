package com.redhat.victims;

/*
 * #%L
 * This file is part of victims-enforcer.
 * %%
 * Copyright (C) 2013 The Victims Project
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
//import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.maven.artifact.Artifact;


public class ArtifactCache {
  
  public static final int CACHE_VALIDITY_PERIOD = 2;
  
  private JCS cache; 
  private Date lastUpdated;

  public ArtifactCache(String path, Date lastUpdated){
    this.lastUpdated = lastUpdated;

    // stfu 
    Logger.getLogger("org.apache.jcs").setLevel(Level.OFF);
    
    Properties p = new Properties();
    p.put("jcs.default", "DC");
    p.put("jcs.default.cacheattributes",  "org.apache.jcs.engine.CompositeCacheAttributes");
    p.put("jcs.default.cacheattributes.MaxObjects", "100");
    p.put("jcs.default.cacheattributes.MemoryCacheName", "org.apache.jcs.engine.memory.lru.LRUMemoryCache");
    p.put("jcs.auxiliary.DC",  "org.apache.jcs.auxiliary.disk.indexed.IndexedDiskCacheFactory");
    p.put("jcs.auxiliary.DC.attributes", "org.apache.jcs.auxiliary.disk.indexed.IndexedDiskCacheAttributes");
    p.put("jcs.auxiliary.DC.attributes.DiskPath", path);
        
    initCacheWithProperties(p);
 
  }
  
  public ArtifactCache(File propertiesFile, Date lastUpdated) throws FileNotFoundException, IOException{
    this.lastUpdated = lastUpdated;
    Properties p = new Properties();
    p.load(new FileInputStream(propertiesFile));
    initCacheWithProperties(p);
  }
  
  private void initCacheWithProperties(Properties p){
    
    try {
      CompositeCacheManager ccm = CompositeCacheManager.getUnconfiguredInstance(); 
      ccm.configure(p);
      cache = JCS.getInstance("default");

    } catch (CacheException e){
      cache = null;
  
    }
  }
  
  public ArtifactStub get(String artifactId){
    if (cache != null)
      return (ArtifactStub) cache.get(artifactId);
  
    return null;
  }
  
  public boolean put(Artifact a){
    try { 
      if (cache != null){
        cache.put(a.getArtifactId(), new ArtifactStub(a));
        return true;
      }
    } catch (CacheException e){
    }
    return false;
  }
  
  public boolean put(ArtifactStub stub){
    try {
      if (cache != null){
        cache.put(stub.getArtifactId(), stub);
        return true;
      }
    } catch(CacheException e){
    }
    return false;
  }
 
  public boolean isCached(Artifact a){
    try { 
      if (cache != null){
        ArtifactStub cached = (ArtifactStub) cache.get(a.getArtifactId());
        if (cached != null){
          
          if (expired(cached.getCachedDate())){
            cache.remove(cached.getArtifactId());
            return false;
          }
          return cached.getFilename().equals(a.getFile().getCanonicalPath()); 
        }
      }
    } catch (Exception e){
    }
    return false;
  }
  
  public boolean expired(Date cachedDate){
    return cachedDate.before(lastUpdated);
  }
  
//  public boolean expired(Date cachedDate) {
//
//    if (validity < 0)
//      return true;
//    
//    
//    Calendar now = Calendar.getInstance();
//    now.setTime(new Date());
//    
//    Calendar expiration = Calendar.getInstance();
//    expiration.setTime(cachedDate);
//    expiration.add(Calendar.SECOND, validity);
//    
//    return expiration.before(now);
// 
//  }
  
}
