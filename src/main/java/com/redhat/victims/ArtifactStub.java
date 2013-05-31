package com.redhat.victims;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import org.apache.maven.artifact.Artifact;

public class ArtifactStub implements Serializable {

  private static final long serialVersionUID = 2272353689488498021L;
  
  private String artifactId;
  private String filename;
  private Date cached;
 
  public ArtifactStub(Artifact a){
    
    artifactId = a.getArtifactId();
   
    try {
      filename = a.getFile().getCanonicalPath();
    } catch (IOException e) {
      filename = null;
    }
    
    cached = new Date();

  }
  
  public String getArtifactId() {
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
