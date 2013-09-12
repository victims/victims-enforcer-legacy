# enforce-victims-rule [![Build Status](https://travis-ci.org/victims/victims-web.png)](https://travis-ci.org/victims/victims-web)


## About

This rule provides the logic to scan a Maven's project's dependencies against a database of artifacts with publicly known Common Vulnerabilities and Exposures (CVE). The canonical version of the database is hosted at https://victi.ms and is maintained by Red Hat security teams. 

## Getting Started

A sample project is provided in sample/

To see the victims-enforcer in action run:
```sh
mvn clean package
```

On environments running JDK 1.7 or greater, you will have to disable _jsse.enableSNIExtension_ for synchronization to work.
```sh
mvn clean package -Djsse.enableSNIExtension=false
```

## Example pom.xml
```xml
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
              <version>1.3.1</version>
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

### baseUrl

   The URL of the victims web service to used to synchronize the local database.

   default: "https://victi.ms"

### entryPoint 

    The entrypoint of the victims webservice to synchronize against

    default: "/service"


### metadata

   The severity of exception to be thrown when a dependency is encountered that matches the known vulnerable database based on metadata. Fatal indicates the build should fail, warning indicates a warning should be issued but the build should proceed.

   allowed : warning, fatal, disabled  
   default : warning


### fingerprint

   The severity of exception to be thrown when a dependency is encountered that matches the known vulnerable database based on a fingerprint. Fatal indicates the build should fail, warning indicates a warning should be issued but the build should proceed.

   allowed : warning, fatal, disabled  
   default : fatal


### updates

   Allows the configuration of the synchronization mechanism. In automatic mode new entries in the victims database are pulled from the victims-web instance during each build. In daily mode new entries are pulled from the victims-web instance only once per day. The synchronization mechanism may be disabled and processed manually for closed build environments.

   allowed : auto, daily, offline  
   default : auto


### jdbcDriver

   The jdbc driver to use for the local victims database. By default victims uses an embedded H2 database.

   default :  org.h2.Driver

### jdbcUrl

   The jdbc connection URL to for the local victims database.

   default : .victims (embedded h2 instance).

### jdbcUser

   The username to use for the jdbc connection.

   default : ""

### jdbcPass

   The password to use for the jdbc connection.

   default : ""

