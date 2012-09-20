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
package com.redhat.victims.commands;

import com.redhat.victims.*;
import com.redhat.victims.db.Statements;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The fingerprint command resolves the supplied artifact and compares the 
 * fingerprint of that item to entries within a database of vulnerable
 * artifacts
 * .
 * @author gmurphy
 */
public class FingerprintCommand implements Command {

    public void execute(ExecutionContext ctx) throws EnforcerRuleException {
                
        try { 
            
            String hash = Checksum.sha512(ArtifactLocator.locate(ctx.getArtifact()));
            
            JSONObject q = new JSONObject();
            q.put("hash", hash);
            JSONObject rs = ctx.getDatabase().executeStatement(Statements.CHECK_HASH, q);
                  
            if (rs.getBoolean("result") && rs.has("item")){
                
                String heading = IOUtils.fmt(Resources.INFO_FINGERPRINT_HEADING);
                String info = IOUtils.prettyPrint(heading, rs.getJSONObject("item"));
                String mode = ctx.getSettings().get(Settings.Fingerprint);
                IOUtils.report(ctx.getLog(), mode, info);
              
                if (ctx.getSettings().inFatalMode(Settings.Fingerprint)){
                   
                    String id = ctx.getArtifact().getId();
                    String file = ctx.getArtifact().getFile().getAbsolutePath();           
                    StringBuilder err = new StringBuilder();
                    err.append(IOUtils.box(IOUtils.fmt(Resources.FATAL_FINGERPRINT_HEADING)));
                    err.append(IOUtils.wrap(80, IOUtils.fmt(Resources.FATAL_FINGERPRINT_BODY, file, id)));

                    throw new EnforcerRuleException(err.toString());
                }
            }
             
        } catch(NoSuchAlgorithmException e){
            ctx.getLog().error(e);
            
        } catch(IOException e){
            ctx.getLog().error(e);
            
        } catch(JSONException e ){
            ctx.getLog().error(e);
           
        } catch(VictimsException e){
            ctx.getLog().error(e);
        }
    }

    public String getDefaultExecutionMode() {
        return Settings.ModeFatal;
    }
    
}
