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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
  private String cacheConfig = null;
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
      project = ((MavenProject) helper.evaluate("${project}")).clone();
    } catch (ExpressionEvaluationException e) {
      helper.getLog().debug(e);
      throw new EnforcerRuleException(e.toString(), e.getCause());
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
    ctx.getSettings().set(Settings.CACHE_SETTINGS, cacheConfig);
    ctx.getSettings().set(Settings.NTHREADS, threads);
    
    // Only need to query using one hashing mechanism
    System.setProperty(VictimsConfig.Key.ALGORITHMS, "SHA512");
   
    // Setup database 
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
    
    // Setup database
    try {
      ctx.setDatabase(VictimsDB.db());
    } catch (VictimsException e) {
      ctx.debug(e);
      throw new EnforcerRuleException(e.getMessage());
    }
    
    // Check the last time the database was updated
    Date lastUpdated; 
    if (VictimsConfig.purgeCache()){
      lastUpdated = new Date();   // Will invalidate all cache data
    } else{ 
      try {
        lastUpdated = ctx.getDatabase().lastUpdated();
      } catch(VictimsException e){
        lastUpdated = new Date();
      }
    }
    
    // Set the cache file
    String cacheFile; 
    try {
      cacheFile = new File(VictimsConfig.home(), "victims-enforcer.cache").getCanonicalPath();
    } catch(IOException e){
      cacheFile = ".victims-enforcer.cache";
    }
    
    // Setup the cache
    try {
      
      if (cacheConfig != null){
        ctx.setCache(new ArtifactCache(new File(cacheConfig), lastUpdated));
      } else {
        ctx.setCache(new ArtifactCache(cacheFile, lastUpdated));
      }
      
    } catch (Exception e){
      ctx.setCache(new ArtifactCache(cacheFile, lastUpdated));
    }

    // Validate settings
    try {
      ctx.getSettings().validate();
      ctx.getSettings().show(ctx.getLog());
   
    } catch (VictimsException e) {
      ctx.debug(e); 
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

      if (ctx.updatesEnabled()){
        ctx.info(TextUI.fmt(Resources.INFO_UPDATES, VictimsConfig.serviceURI()));
        ctx.getDatabase().synchronize();
      }

      int nthreads = Integer.parseInt(ctx.getSettings().get(Settings.NTHREADS));
      List<Future<ArtifactStub>> jobs = new ArrayList<Future<ArtifactStub>>();
      ExecutorService executor = Executors.newFixedThreadPool(nthreads);
      
      for (Artifact a : artifacts){
           
        // Check if we've already examined this value
        if (ctx.isCached(a)){
          ctx.debug("Cached: " + a.getArtifactId());
          continue;
        }
                
        Callable<ArtifactStub> worker = new VictimsCommand(ctx, a);
        jobs.add(executor.submit(worker));    
      }
      
      for (Future<ArtifactStub> future : jobs){
        
        try {
          
          ArtifactStub checked = future.get();
          if (checked != null){
            ctx.debug("Done: " + checked.getArtifactId());
            ctx.cacheArtifact(checked);
          }
          
        } catch (InterruptedException e){
          ctx.info(e.getMessage());
        } catch (ExecutionException e){
          ctx.debug(e);
          throw new EnforcerRuleException(e.getCause().getMessage());
        }
      }
 
    } catch (IOException e){
      ctx.debug(e);
      throw new EnforcerRuleException(e.getMessage());

    } catch (VictimsException e) {
      ctx.debug(e);
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
