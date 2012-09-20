/*
 * Copyright (C) 2012 Red Hat Inc.  
 * 
 * This file is part of enforce-victims-rule for the Maven Enforcer Plugin.
 * enforce-victims-rule is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * enforce-victims-rule is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with enforce-victims-rule.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.redhat.victims;

import java.io.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.maven.artifact.Artifact;

/**
 * Helper methods to locate local and remote JAR files. 
 * 
 * @author gmurphy
 */
public final class ArtifactLocator {
    
    /**
     * Attempts to resolve a remote Maven artifact and return an stream to 
     * its content. 
     * 
     * @param a Artifact to resolve. 
     * @return An input stream to that artifact. 
     * @throws VictimsException 
     */
    public static InputStream resolveRemoteArtifact(Artifact a) throws VictimsException {
       
        try { 
            HttpMethod get = new GetMethod(a.getDownloadUrl());
            HttpClient client = new HttpClient();
            client.executeMethod(get);
            return get.getResponseBodyAsStream(); 
       
        } catch(IOException e){
            throw new VictimsException(IOUtils.fmt(Resources.ERR_ARTIFACT_NOT_FOUND, a.getArtifactId()), e);
        }
        
    }
    
    /**
     * Attempts to resolve the specified artifact in order to fingerprint it. 
     * In most use cases any dependency that is required by Maven will exist
     * in a users .m2 local repository. If it doesn't by default Maven will 
     * pull those files down from the Central Repository. 
     * @param a The artifact to resolve.
     * @return InputStream that for the artifact file content.
     * @throws VictimsException 
     */
    public static InputStream resolveLocalArtifact(Artifact a) throws VictimsException {
        
        // Should exist locally if is in the pom.xml 
        try {
            File dep = a.getFile();
            return new FileInputStream(dep);
            
        } catch (FileNotFoundException e) {
            throw new VictimsException(IOUtils.fmt(Resources.ERR_ARTIFACT_NOT_FOUND, a.getArtifactId()), e);
        }   
    }
    
    /**
     * Try to resolve the specified artifact locally, and if that fails look 
     * up the remote instance. 
     * 
     * @param a Artifact to search for. 
     * @return A stream to the artifacts content
     * @throws VictimsException 
     */
    public static InputStream locate(Artifact a) throws VictimsException{
        
        try { 
            return resolveLocalArtifact(a);
        } catch (VictimsException e){
            return resolveRemoteArtifact(a);
        }
        
        
    }
    
}
