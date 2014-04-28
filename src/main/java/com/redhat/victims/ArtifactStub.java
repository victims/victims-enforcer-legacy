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

import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
 * An artifact stub is a serializable container for maven 
 * artifact information. This is necessary to allow for 
 * use with the concurrency utilities executors and futures. 
 * 
 * @author gm
 */
public class ArtifactStub implements Serializable {
  

  private static final long serialVersionUID = 2272353689488498021L;
  
  private String artifactId;
  private String filename;
  private Date cached;
 
  public ArtifactStub(Artifact a){
    
    artifactId = a.getId();
   
    try {
      File f = a.getFile();
      if (f != null) {
        filename = a.getFile().getCanonicalPath();
      }
    } catch (IOException e) {
      filename = null;
    }
    cached = new Date();

  }
  
  public String getId() {
    return artifactId;
  }
 
  public String getFilename() {
    return filename;
  }
  public Date getCachedDate() {
    return cached;
  }
  
  public String toString(){
    
    return String.format("artifact: %s, file: %s, created on %s",
      artifactId, filename, cached.toString());
  }
  
  
  

}
