
package com.redhat.victims;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import com.redhat.victims.VictimsConfig;
import com.redhat.victims.VictimsRecord;
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

  private String baseUrl = null;
  private String entryPoint = null;
  private String jdbcDriver = null;
  private String jdbcUrl = null; 
 
  /**
   * Action taken when vulnerability detected
   */
  
  private void vulnerabilityDetected(ExecutionContext ctx, String cve) throws VictimsException {
    
    StringBuilder logMsg = new StringBuilder(); 
    logMsg.append("The Maven artifact: ")
      .append(ctx.getArtifact().toString())
      .append(" matches ")
      .append(cve)
      .append(" , a vulnerability within the Victims Database. ");
    TextUI.report(ctx.getLog(), ctx.getAction(), logMsg.toString());
    
    
    StringBuilder errMsg = new StringBuilder();
    errMsg.append("Vulnerability detected: ")
      .append(cve)
      .append(" for more information visit -")
      .append("\nhttps://cve.mitre.org/cgi-bin/cvename.cgi?name=")
      .append(cve);
    
    if (ctx.getSettings().inFatalMode(ctx.getAction())){
      throw new VictimsException(TextUI.wrap(80, errMsg.toString()));
    }
    
  }
      
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
  
  public ExecutionContext setupContext(Log log) throws EnforcerRuleException {
    
    ExecutionContext ctx = new ExecutionContext(); 
    ctx.setLog(log);
    ctx.setSettings(new Settings());
    ctx.getSettings().set(Settings.METADATA, metadata);
    ctx.getSettings().set(Settings.FINGERPRINT, fingerprint);
    ctx.getSettings().set(Settings.UPDATE_DATABASE, updates);
          
    if (baseUrl != null){
      System.setProperty(VictimsConfig.Key.URI, baseUrl);
    }
    if (entryPoint != null){
      System.setProperty(VictimsConfig.Key.ENTRY, entryPoint);
    }
    if (jdbcDriver != null){
      System.setProperty(VictimsConfig.Key.DB_DRIVER, jdbcDriver);
    }
    if (jdbcUrl != null){
      System.setProperty(VictimsConfig.Key.DB_URL, jdbcUrl);
    }
       
    try {
      ctx.getSettings().validate();
      ctx.getSettings().show(ctx.getLog());
     
    } catch (VictimsException e) {
      throw new EnforcerRuleException(e.getMessage());
    }
    
    return ctx;
  }
  
  
  public void execute(ExecutionContext ctx, Set<Artifact> artifacts) throws EnforcerRuleException {
        
    try {

      VictimsDBInterface db = VictimsDB.db();
      if (ctx.getSettings().updatesEnabled()){
        db.synchronize();
      }
     
      for (Artifact a : artifacts){
        
        ctx.setArtifact(a);
        
        // fingerprint  
        if (ctx.getSettings().isEnabled(Settings.FINGERPRINT)){
          ctx.setAction(Settings.FINGERPRINT);
          String dependency = a.getFile().getAbsolutePath();
          for (VictimsRecord vr : VictimsScanner.getRecords(dependency)){
            
            for (String cve : db.getVulnerabilities(vr)){
              vulnerabilityDetected(ctx, cve);
              
            }
          }
        }
        
        // metadata 
        if (ctx.getSettings().isEnabled(Settings.METADATA)){
          ctx.setAction(Settings.METADATA);
          HashMap<String, String> gav = new HashMap<String, String>();
          gav.put("groupId", a.getGroupId());
          gav.put("artifactId", a.getArtifactId());
          gav.put("version", a.getVersion());
          
          for (String cve : db.getVulnerabilities(gav)){
            vulnerabilityDetected(ctx, cve);       
          }         
        }        
      }
      
    } catch (IOException e){
      throw new EnforcerRuleException(e.getMessage());
    
    } catch (VictimsException e) {
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
