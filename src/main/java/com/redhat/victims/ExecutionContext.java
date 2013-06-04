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



import org.apache.maven.plugin.logging.Log;

import com.redhat.victims.database.VictimsDBInterface;

/**
 * Context to pass to each command.
 *
 * @author gmurphy
 */
public final class ExecutionContext {

  private Settings settings;
  private Log log;
  private VictimsResultCache cache;
  private VictimsDBInterface database;

  /**
   * @return Configuration to apply to this execution context.
   */
  public Settings getSettings() {
    return settings;
  }

  /**
   * @return The log to use within this execution context.
   */
  public Log getLog() {
    return this.log;
  }
  
  /**
   * @return The cache to store artifacts in
   */
  public VictimsResultCache getCache(){
    return this.cache;
  }

  /**
   * Applies the given settings to the execution context.
   */
  public void setSettings(final Settings setup) {
    this.settings = setup;
  }

  /**
   * Send all messages to this log.
   *
   * @param l The log to associate with this execution context.
   */
  public void setLog(Log l) {
    this.log = l;
  }

  public VictimsDBInterface getDatabase() {
    return database;
  }

  public void setDatabase(VictimsDBInterface database) {
    this.database = database;
  }
  
  public void setCache(VictimsResultCache victimsResultCache) {
    this.cache = victimsResultCache;
    
  }
 
  /**
   * Returns true if the setting is in fatal mode. Used when
   * determining if the rule should fail a build.
   * @param setting The configuration item to check if in fatal mode.
   * @return True when the setting is in fatal mode.
   */
  public boolean inFatalMode(String setting) {
      String val = settings.get(setting);
      return val != null && val.equalsIgnoreCase(Settings.MODE_FATAL);
  }

  /**
   * Returns true if the value associated with the supplied
   * key isn't set to disabled.
   * @param setting The setting to check if is disabled.
   * @return  True if the setting is enabled.
   */
  public boolean isEnabled(String setting) {
      String val = settings.get(setting);
      return val != null && !val.equalsIgnoreCase(Settings.MODE_DISABLED);
  }

  /**
   * Returns true if automatic updates are enabled.
   * @return True if automatic updates of database are enabled.
   */
  public boolean updatesEnabled() {
      String val = settings.get(Settings.UPDATE_DATABASE);
      return val != null && 
          (val.equalsIgnoreCase(Settings.UPDATES_AUTO) ||
           val.equalsIgnoreCase(Settings.UPDATES_DAILY));
  }

  /**
   * @return True if daily updates are enabled
   */
  public boolean updateDaily() {
    String val = settings.get(Settings.UPDATE_DATABASE);
    return val != null && val.equalsIgnoreCase(Settings.UPDATES_DAILY);
  }
  
  /**
   * @return True if automatic updates are enabled and run for each enforcer build
   *
   */
  public boolean updateAlways() {
    String val = settings.get(Settings.UPDATE_DATABASE);
    return val != null && val.equalsIgnoreCase(Settings.UPDATES_AUTO);
    
  }

}
