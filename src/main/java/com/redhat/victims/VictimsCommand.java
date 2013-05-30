package com.redhat.victims;

import java.util.HashMap;
import java.util.concurrent.Callable;

import org.apache.maven.artifact.Artifact;

import com.redhat.victims.database.VictimsDBInterface;

public class VictimsCommand implements Callable<String> {
  
  private Artifact artifact;
  private ExecutionContext context;
  private VictimsDBInterface database;
  
  
  public VictimsCommand(ExecutionContext ctx, Artifact a, VictimsDBInterface db){
    this.context = ctx;
    this.artifact = a;
    this.database = db;
  }

  public String call() throws Exception {
     
    boolean alreadyReported = false;
    context.getLog().debug("Inspecting: " + artifact.toString());

    // fingerprint
    if (context.getSettings().isEnabled(Settings.FINGERPRINT)){
      
      String dependency = artifact.getFile().getAbsolutePath();
      for (VictimsRecord vr : VictimsScanner.getRecords(dependency)){
        for (String cve : database.getVulnerabilities(vr)){
          vulnerabilityDetected(Settings.FINGERPRINT, cve);
          alreadyReported = true;
        }
      }
    }
    
    // metadata
    if (! alreadyReported && context.getSettings().isEnabled(Settings.METADATA)){
     
      HashMap<String, String> gav = new HashMap<String, String>();
      gav.put("groupId", artifact.getGroupId());
      gav.put("artifactId", artifact.getArtifactId());
      gav.put("version", artifact.getVersion());

      for (String cve : database.getVulnerabilities(gav)){
        vulnerabilityDetected(Settings.METADATA, cve);
      }
    }
    
    
    return artifact.getArtifactId();
    
  }
  
  /**
   * Action taken when vulnerability detected
   */
  private void vulnerabilityDetected(String action, String cve) throws VictimsException {

    // Report finding
    String logMsg = TextUI.fmt(Resources.INFO_VULNERABLE_DEPENDENCY,
                        artifact.getArtifactId(),
                        artifact.getVersion(),
                        cve.trim());

    TextUI.report(context.getLog(), action, logMsg);

    // Fail if in fatal mode
    StringBuilder errMsg = new StringBuilder();
    errMsg.append(TextUI.box(TextUI.fmt(Resources.ERR_VULNERABLE_HEADING)))
          .append(TextUI.fmt(Resources.ERR_VULNERABLE_DEPENDENCY, cve));

    if (context.getSettings().inFatalMode(action)){
      throw new VictimsException(errMsg.toString());
    }

  }

}
