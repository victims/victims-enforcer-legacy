# enforce-victims-rule

## About

This rule provides the logic to scan a Maven's project's dependencies against 
a database of artifacts with publicly known Common Vulnerabilities and Exposures
(CVE). The canoical version of the database is hosted at https://victims-websec.rhcloud.com
and is maintained by Red Hat's Security Response Team (SRT). 

## Getting Started

A sample project and configuration instructions are avaiable [here](http://people.redhat.com/~gmurphy/projects/victims.html)

## Example pom.xml
```
  <project>
    ...
    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-enforcer-plugin</artifactId>
          <version>1.1.1</version>
          <dependencies>
            <dependency>
              <groupId>com.redhat.victims</groupId>
              <artifactId>enforce-victims-rule</artifactId>
              <version>1.0</version>
              <type>jar</type>
            </dependency>
          </dependencies>
          <executions>
            <execution>
              <id>enforce-victims-rule</id>
                <goals>
                  <goal>enforce</goal>
                </goals>
                <configuration>
                  <rules>
                    <rule implementation="com.redhat.victims.VictimsRule">     
                      <!-- The URL where the rule should synchronize the database with --> 
                      <url>https://victims-websec.rhcloud.com/service/v1</url>
                            
                      <!-- 
                        Check the project's dependencies against the database using 
                        name and version. The default mode for this is 'warning'.

                        Valid options are: 

                        disabled: Rule is still run but only INFO level messages aand no errors.
                        warning : Rule will spit out a warning message but doesn't result in a failure. 
                        fatal   : Rule will spit out an error message and fail the build. 
                      -->
                      <metadata>warning</metadata>

                      <!--
                        Check the project's dependencies against the database using 
                        the SHA-512 checksum of the artifact. The default is fatal. 

                        Valid options are: 

                        disabled: Rule is still run but only INFO level messages aand no errors.
                        warning : Rule will spit out a warning message but doesn't result in a failure. 
                        fatal   : Rule will spit out an error message and fail the build. 
                      -->
                      <fingerprint>fatal</fingerprint>
                            
                      <!-- The location to save the embedded Apache Derby instance --> 
                      <path>.victims</path>

                      <!-- 
                        Disables the synchronization mechansim. By default the rule will 
                        attempt to update the database for each build. 

                        Valid options are: 

                        auto  : Automatically update the database entries on each build. 
                        off   : Disable the synchronization mechanism. 
                              
                      -->  
                      <updates>auto</updates>
                            
                    </rule>
                  </rules>
                </configuration>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
    ...
  </project>
```
