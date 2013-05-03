# enforce-victims-rule

## About

This rule provides the logic to scan a Maven's project's dependencies against a database of artifacts with publicly known Common Vulnerabilities and Exposures (CVE). The canonical version of the database is hosted at https://victims-websec.rhcloud.com and is maintained by Red Hat security teams. 

## Getting Started

A sample project is provided in sample/

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
              <version>1.1</version>
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
                      <url>https://victims-websec.rhcloud.com/service/v2</url>
                            
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

## Configuration options reference

The following options can be specified as child elements of ```<rule implementation="com.redhat.victims.VictimsRule">```

### url

   The URL of the victims web service to used to synchronize the local database.

   default: "https://victims-websec.rhcloud.com/service/v2"

### metadata

   The severity of exception to be thrown when a dependency is encountered that matches the known vulnerable database based on metadata. Fatal indicates the build should fail, warning indicates a warning should be issued but the build should proceed.

   allowed : warning, fatal, disabled  
   default : warning


### fingerprint

   The severity of exception to be thrown when a dependency is encountered that matches the known vulnerable database based on a fingerprint. Fatal indicates the build should fail, warning indicates a warning should be issued but the build should proceed.

   allowed : warning, fatal, disabled  
   default : fatal


### updates

   Allows the configuration of the synchronization mechanism. In automatic mode new entries in the victims database are pulled from the victims-web instance during each build. The synchronization mechanism may be disabled and processed manually for closed build environments.

   allowed : auto, offline  
   default : auto


### dbdriver

   The jdbc driver to use for the local victims database. By default victims uses an embedded H2 database.

   default :  org.h2.Driver

### dburl

   The jdbc connection URL to for the local victims database.

   default : jdbc:h2:.victims

### metadataplus

   By default, victims-enforcer will compare the GAV information extracted from each dependency against that
 within the victims database. If the metadataplus option is enabled, this feature will be extended so metadata within the MANIFEST.MF and pom.properties files is also examined for matches.

   allowed : true, false  
   default : false
