package com.redhat.victims;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

/**
 * Context to pass to each command.
 * 
 * @author gmurphy
 */
public final class ExecutionContext {

  private Settings settings;
  private Artifact artifact;
  private String action;
  private Log log;

  /**
   * @return Configuration to apply to this execution context.
   */
  public Settings getSettings() {
    return settings;
  }

  /**
   * @return The artifact associated with this execution context.
   */
  public Artifact getArtifact() {
    return artifact;
  }

  /**
   * @return The log to use within this execution context.
   */
  public Log getLog() {
    return this.log;
  }

  /**
   * @return The current action being performed.
   */
  public String getAction() {
    return this.action;
  }

  /**
   * Applies the given settings to the execution context.
   */
  public void setSettings(final Settings setup) {
    this.settings = setup;
  }

  /**
   * Associates the given artifact with this execution context.
   * 
   * @param a The artifact to associate with this context.
   */
  public void setArtifact(final Artifact a) {
    this.artifact = a;
  }

  /**
   * Send all messages to this log.
   * 
   * @param l The log to associate with this execution context.
   */
  public void setLog(Log l) {
    this.log = l;
  }

  /**
   * Set the current action being executed
   */
  public void setAction(String action) {
    this.action = action;
  }

}
