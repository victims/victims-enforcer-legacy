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

import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCacheManager;


/**
 * This class is the main entry point for working with the Maven Enforcer
 * plug-in. It provides the logic to synchronize a local database with a remote
 * database of vulnerable Java artifacts. This database is used to check for any
 * dependencies used within the project that may have known vulnerabilities.
 *
 * @author gmurphy
 */
public class VictimsRule implements EnforcerRule {

  /*
   * Configuration options available in pom.xml
   */
  private String metadata = Settings.defaults.get(Settings.METADATA);
  private String fingerprint = Settings.defaults.get(Settings.FINGERPRINT);
  private String updates = Settings.defaults.get(Settings.UPDATE_DATABASE);
  private String cacheRegion = Settings.defaults.get(Settings.CACHE_REGION);
  private String cacheSettings = null; // manual configuration only
  private String threads = Settings.defaults.get(Settings.NTHREADS);
  private String baseUrl = null;
  private String entryPoint = null;
  private String jdbcDriver = null;
  private String jdbcUrl = null;
  private String jdbcUser = null;
  private String jdbcPass = null;

  

  /**
   * Main entry point for the enforcer rule.
   *
   * @param helper
   * @throws EnforcerRuleException
   */
  public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {

    MavenProject project;
    try {
      project = (MavenProject) helper.evaluate("${project}");
    } catch (ExpressionEvaluationException e) {
      throw new EnforcerRuleException(e.getMessage());
    }

    execute(setupContext(helper.getLog()), project.getArtifacts());

  }

  /**
   * Configure execution context based on sensible defaults and
   * overrides in the pom.xml configuration.
   * @param log
   * @return Configured execution context
   * @throws EnforcerRuleException
   */
  public ExecutionContext setupContext(Log log) throws EnforcerRuleException {

    ExecutionContext ctx = new ExecutionContext();
    ctx.setLog(log);
    ctx.setSettings(new Settings());
    ctx.getSettings().set(Settings.METADATA, metadata);
    ctx.getSettings().set(Settings.FINGERPRINT, fingerprint);
    ctx.getSettings().set(Settings.UPDATE_DATABASE, updates);
    ctx.getSettings().set(Settings.CACHE_SETTINGS, cacheSettings);
    ctx.getSettings().set(Settings.CACHE_REGION,  cacheRegion);
    ctx.getSettings().set(Settings.NTHREADS, threads);
    
    // Only need to query using one hashing mechanism
    System.setProperty(VictimsConfig.Key.ALGORITHMS, "SHA512");
   
    // Setup cache
    try {
      CompositeCacheManager ccm = CompositeCacheManager.getUnconfiguredInstance(); 
      Properties p = new Properties(); 
      FileInputStream fin = null; 
      try {
        if (cacheSettings != null) {
          fin = new FileInputStream(cacheSettings);   
          p.load(fin);
        }
      } catch (FileNotFoundException e){
        fin = null;
      } catch (IOException e){
        fin = null;
      }
      if (fin == null){
        // default cache configuration
        p.put("jcs.default", "DC");
        p.put("jcs.default.cacheattributes",  "org.apache.jcs.engin.CompositeCacheAttributes");
        p.put("jcs.default.cacheattributes.MaxObjects", "100");
        p.put("jcs.default.cacheattributes.MemoryCacheName", "org.apache.jcs.engine.memory.lru.LRUMemoryCache");
        p.put("jcs.auxiliary.DC",  "org.apache.jcs.auxiliary.disk.indexed.IndexedDiskCacheFactory");
        p.put("jcs.auxiliary.DC.attributes", "org.apache.jcs.auxiliary.disk.indexed.IndexedDiskCacheAttributes");
        p.put("jcs.auxiliary.DC.attributes.DiskPath", ".victims.cache");
      }    
           
      ccm.configure(p);
      
      ctx.setCache(JCS.getInstance(ctx.getSettings().get(Settings.CACHE_REGION))); 
    } catch (CacheException e){
      ctx.setCache(null);
    }
        
    if (baseUrl != null){
      System.setProperty(VictimsConfig.Key.URI, baseUrl);
      ctx.getSettings().set(VictimsConfig.Key.URI, baseUrl);
    }
    if (entryPoint != null){
      System.setProperty(VictimsConfig.Key.ENTRY, entryPoint);
      ctx.getSettings().set(VictimsConfig.Key.URI, baseUrl);
    }
    if (jdbcDriver != null){
      System.setProperty(VictimsConfig.Key.DB_DRIVER, jdbcDriver);
      ctx.getSettings().set(VictimsConfig.Key.DB_DRIVER, jdbcDriver);
    }
    if (jdbcUrl != null){
      System.setProperty(VictimsConfig.Key.DB_URL, jdbcUrl);
      ctx.getSettings().set(VictimsConfig.Key.DB_URL, jdbcUrl);
    }
    if (jdbcUser != null){
      System.setProperty(VictimsConfig.Key.DB_USER, jdbcUser);
      ctx.getSettings().set(VictimsConfig.Key.DB_USER, jdbcUser);
    }
    if (jdbcPass != null){
      System.setProperty(VictimsConfig.Key.DB_PASS, jdbcPass);
      ctx.getSettings().set(VictimsConfig.Key.DB_PASS, "(not shown)");
    }

    try {
      ctx.getSettings().validate();
      ctx.getSettings().show(ctx.getLog());
   

    } catch (VictimsException e) {
      
      ctx.getLog().debug(e);
      
      throw new EnforcerRuleException(e.getMessage());
    }

    return ctx;
  }


  /**
   * Scan the supplied artifacts given the provided execution context. An
   * exception will be raised if a vulnerable artifact has been detected.
   * @param ctx
   * @param artifacts
   * @throws EnforcerRuleException
   */
  public void execute(ExecutionContext ctx, Set<Artifact> artifacts) throws EnforcerRuleException {

    try {

      VictimsDBInterface db = VictimsDB.db();
      ctx.getLog().debug("Connecting to database using: " + VictimsConfig.dbDriver());
      
      if (ctx.getSettings().updatesEnabled()){
        ctx.getLog().info(TextUI.fmt(Resources.INFO_UPDATES, VictimsConfig.serviceURI()));
        db.synchronize();
      }

      int nthreads = Integer.parseInt(ctx.getSettings().get(Settings.NTHREADS));
      List<Future<String>> jobs = new ArrayList<Future<String>>();
      ExecutorService executor = Executors.newFixedThreadPool(nthreads);
      
      for (Artifact a : artifacts){
      
        // Check if we've already examined this value
        if (ctx.getCache() != null){
          String cachedFile = (String) ctx.getCache().get(a.getArtifactId());
          if (cachedFile != null && cachedFile.equals(a.getFile().getAbsolutePath())){
            ctx.getLog().debug("Skipping cached artifact: " + a.toString());
            continue;
          }
        }
                
        ctx.getLog().debug("Checking: " + a.toString());
        Callable<String> worker = new VictimsCommand(ctx, a, db);
        jobs.add(executor.submit(worker));    
      }
      
      for (Future<String> future : jobs){
        
        try {
          
          String checked = future.get();
          if (checked != null){
            ctx.getLog().debug("Finished checking: " + checked.toString());
            // update cache if needed
            if (ctx.getCache() != null){
              try {
                
                // so this is dumb
                for (Artifact artifact : artifacts){
                  if (artifact.getArtifactId().equals(checked))
                    ctx.getCache().put(checked, artifact.getFile().getAbsolutePath());
                }
                
              } catch(CacheException e){
                ctx.getLog().debug("Unable to cache object: " + e.toString());
              }
            }
          }
          
        } catch (InterruptedException e){
          ctx.getLog().info(e.getMessage());
        } catch (ExecutionException e){
          throw new EnforcerRuleException(e.getCause().getMessage());
        }
      }
 
    } catch (IOException e){
      ctx.getLog().debug(e);
      throw new EnforcerRuleException(e.getMessage());

    } catch (VictimsException e) {
      ctx.getLog().debug(e);
      throw new EnforcerRuleException(e.getMessage());
    }
  }

  /**
   * The database is always synchronized with the Victims Server. The initial
   * import of entries will take some time but subsequent requests should be
   * relatively fast.
   *
   * @return Always will return false.
   */
  public boolean isCacheable() {
    return false;
  }

  /**
   * Feature not used by this rule.
   *
   * @param er
   * @return Always returns false
   */
  public boolean isResultValid(EnforcerRule er) {
    return false;
  }

  /**
   * Feature not used by this plug-in. Return a bogus value.
   *
   * @return Not used
   */
  public String getCacheId() {
    return " " + new java.util.Date().getTime();
  }
}
