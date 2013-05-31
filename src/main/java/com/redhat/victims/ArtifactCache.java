package com.redhat.victims;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
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
  private String region;
  private int validity;
  
  
  public ArtifactCache(String cacheRegion, int period){
    region = cacheRegion;
    validity = period;
    
    // stfu jcs
    Logger.getLogger("org.apache.jcs").setLevel(Level.OFF);
    
    Properties p = new Properties();
    p.put("jcs.default", "DC");
    p.put("jcs.default.cacheattributes",  "org.apache.jcs.engine.CompositeCacheAttributes");
    p.put("jcs.default.cacheattributes.MaxObjects", "100");
    p.put("jcs.default.cacheattributes.MemoryCacheName", "org.apache.jcs.engine.memory.lru.LRUMemoryCache");
    p.put("jcs.auxiliary.DC",  "org.apache.jcs.auxiliary.disk.indexed.IndexedDiskCacheFactory");
    p.put("jcs.auxiliary.DC.attributes", "org.apache.jcs.auxiliary.disk.indexed.IndexedDiskCacheAttributes");
    p.put("jcs.auxiliary.DC.attributes.DiskPath", ".victims.cache");
    //p.put("jcs.auxiliary.RC.cacheeventlogger", "import org.apache.maven.plugin.logging.Log");
        
    initCacheWithProperties(p);
 
  }
  
  public ArtifactCache(File propertiesFile, String cacheRegion, int period) throws FileNotFoundException, IOException{
    region = cacheRegion;
    validity = period;
    Properties p = new Properties();
    p.load(new FileInputStream(propertiesFile));
    initCacheWithProperties(p);
  }
  
  private void initCacheWithProperties(Properties p){
    
    try {
      CompositeCacheManager ccm = CompositeCacheManager.getUnconfiguredInstance(); 
      ccm.configure(p);
     
      cache = JCS.getInstance(region);
      
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
          return cached.getFilename() == a.getFile().getCanonicalPath() && 
              ! expired(cached.getCachedDate());
          
        }
      }
    } catch (Exception e){
    }
    return false;
  }
  
  public boolean expired(Date cachedDate) {

    if (validity < 0)
      return true;
    
    
    Calendar now = Calendar.getInstance();
    now.setTime(new Date());
    
    Calendar expiration = Calendar.getInstance();
    expiration.setTime(cachedDate);
    expiration.add(Calendar.SECOND, (int) validity);
    
    return expiration.before(now);
 
  }
}
