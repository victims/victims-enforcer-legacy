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
