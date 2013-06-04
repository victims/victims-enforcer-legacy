package com.redhat.victims;

import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import com.sun.net.httpserver.Headers;
import java.io.File;
import java.io.IOException;
//import java.util.Calendar;
//import java.util.Date;
import java.util.HashSet;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;

@SuppressWarnings("restriction")
public class VictimsRuleTest extends TestCase {

  private HttpServer httpd;
  private VictimsDBInterface database = null;


  @Override
  public void setUp() throws Exception {
    
  
    try {
     database = VictimsDB.db();
    } catch (VictimsException e) {
     
      e.printStackTrace();
      fail(e.getCause().getMessage());
    }
    httpd = HttpServer.create(new InetSocketAddress(1337), 0);

    HttpHandler dummy = new HttpHandler() {
      public void handle(HttpExchange exchange) {

        try {
          final byte[] json =
              FileUtils.readFileToByteArray(new File("testdata", "test.json"));
          Headers headers = exchange.getResponseHeaders();
          headers.add("Content-Type", "application/json");

          exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, json.length);
          exchange.getResponseBody().write(json);
          exchange.getResponseBody().close();

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };

    httpd.createContext("/service/update/", dummy);
    httpd.createContext("/service/remove/", dummy);
    httpd.start();
  }

  @Override
  public void tearDown() {
    httpd.stop(0);
  }

  private void contextRunner(ExecutionContext context, boolean exceptionExpected){
    
    // Create a dummy artifact
    ArtifactHandler handler = new DefaultArtifactHandler();
    Artifact testArtifact =
        new DefaultArtifact("junit", "junit", "3.8.1", "test", "jar", null,
            handler);

    testArtifact.setFile(new File("testdata", "junit-3.8.1.jar"));

    HashSet<Artifact> artifacts = new HashSet<Artifact>();
    artifacts.add(testArtifact);

    // Overwrite the victims url
    System.setProperty(VictimsConfig.Key.URI, "http://localhost:1337");
    
    VictimsRule enforcer = new VictimsRule();
    try {
      enforcer.execute(context, artifacts);
    
    } catch(EnforcerRuleException e){
      if (!exceptionExpected){
        e.printStackTrace();
        fail("Exception not expected");
      }
    } 
    
  }
  
  //@Test
  public void testFatalExection() {
    
    ExecutionContext context = new ExecutionContext();
    context.setLog(new SystemStreamLog());
    context.setSettings(new Settings());
    context.getSettings().set(Settings.FINGERPRINT, Settings.MODE_FATAL);
    context.getSettings().set(Settings.METADATA, Settings.MODE_FATAL);
    context.getSettings().set(Settings.UPDATE_DATABASE, Settings.UPDATES_AUTO);
    context.setDatabase(database);
    try{
      context.setCache(new VictimsResultCache());
    } catch(IOException e){
      fail("Can't create cache");
    }
    contextRunner(context, true);
    
  }
 
  //@Test
  public void testWarning()  {
    
    ExecutionContext context = new ExecutionContext();
    context.setLog(new SystemStreamLog());
    context.setSettings(new Settings());
    context.getSettings().set(Settings.FINGERPRINT, Settings.MODE_WARNING);
    context.getSettings().set(Settings.METADATA, Settings.MODE_WARNING);
    context.getSettings().set(Settings.UPDATE_DATABASE, Settings.UPDATES_AUTO);
    context.setDatabase(database);
    try { 
      context.setCache(new VictimsResultCache());
    } catch(IOException e){
      fail("Can't create cache");
    }
    contextRunner(context, false);
  }
  
  
  //@Test
  public void testDefaultSettings() {

      VictimsRule enforcer = new VictimsRule();
      try {
          enforcer.setupContext(new SystemStreamLog());
      } catch (EnforcerRuleException e){
          fail(e.getMessage());
      }

  }

}
