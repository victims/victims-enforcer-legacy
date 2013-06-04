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

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;

import org.apache.maven.artifact.Artifact;

import com.redhat.victims.database.VictimsDBInterface;

public class VictimsCommand implements Callable<ArtifactStub> {
  
  private Artifact artifact;
  private ExecutionContext context;
  
  public VictimsCommand(ExecutionContext ctx, Artifact a){
    this.context = ctx;
    this.artifact = a;
  }

  public ArtifactStub call() throws Exception {
     
    boolean alreadyReported = false;
    context.getLog().debug("Scanning: " + artifact.toString());
    
    VictimsDBInterface db = context.getDatabase();

    // fingerprint
    if (context.isEnabled(Settings.FINGERPRINT)){
      
      String dependency = artifact.getFile().getAbsolutePath();
      for (VictimsRecord vr : VictimsScanner.getRecords(dependency)){
        HashSet<String> cves = db.getVulnerabilities(vr);
        if (! cves.isEmpty()){
          throw new VulnerableArtifactException(artifact, Settings.FINGERPRINT, cves);
        }
      }
    }
    
    // metadata
    if (! alreadyReported && context.isEnabled(Settings.METADATA)){
     
      HashMap<String, String> gav = new HashMap<String, String>();
      gav.put("groupId", artifact.getGroupId());
      gav.put("artifactId", artifact.getArtifactId());
      gav.put("version", artifact.getVersion());

      HashSet<String> cves = db.getVulnerabilities(gav);
      if (! cves.isEmpty()){
        throw new VulnerableArtifactException(artifact, Settings.METADATA, cves);
      }
    } 
    
    return new ArtifactStub(artifact);
    
  }

}
