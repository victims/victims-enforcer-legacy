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

import java.util.HashSet;

import org.apache.maven.artifact.Artifact;

public class VulnerableArtifactException extends VictimsException {

   
  private static final long serialVersionUID = 3584021685052005272L;
  private String infoMessage;
  private String errorMessage;
  private String artifact;
  private String action;
  private HashSet<String> cves;
    
  public VulnerableArtifactException(Artifact a, String action, HashSet<String> cves) {

    super(String.format("CVE: %s, Artifact: %s", cves, a.getId()));
    
    this.action = action;
      
    this.infoMessage =
        TextUI.fmt(Resources.INFO_VULNERABLE_DEPENDENCY, a.getArtifactId(), a.getVersion(),
            cves.toString());

    StringBuilder errMsg = new StringBuilder();
    errMsg.append(TextUI.box(TextUI.fmt(Resources.ERR_VULNERABLE_HEADING))).append(
        TextUI.fmt(Resources.ERR_VULNERABLE_DEPENDENCY, cves));

    this.errorMessage = errMsg.toString();
    this.cves = cves;
    this.artifact = a.getId();

  }
  
  public boolean isFatal(ExecutionContext ctx){
    return (ctx.inFatalMode(action));
  }
  
  public String getId(){
    return this.artifact;
  }
  
  public String getErrorMessage(){
    return this.errorMessage;
  }
  
  public String getLogMessage(){
    return this.infoMessage;
  }
  
  public HashSet<String> getVulnerabilites(){
    return this.cves;
  }

}
