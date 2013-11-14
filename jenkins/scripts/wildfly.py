#!/usr/bin/env python
import os
import sys
import urllib2
import shutil
import zipfile
import xml.etree.ElementTree as ET
import subprocess

def ns(tag):
    return "{{http://maven.apache.org/POM/4.0.0}}{}".format(tag)

def download_source(url, output):

    print("Downloading latest source from {}".format(url))
    chunk_size = 1024 
    rsp = urllib2.urlopen(url)
    total = int(rsp.info().getheader("Content-Length").strip())

    with open(output, "wb") as f:
        saved = 0
        while True:
            chunk = rsp.read(chunk_size)
            if not chunk: 
                print("\nok")
                break

            f.write(chunk)
            saved += len(chunk)
            sys.stdout.write("{}/{} KiB \r".format(saved//1024, total//1024))


def inject_config(elem, versionString):

    # <plugin>
    plugin = ET.SubElement(elem, ns("plugin"))
    #   <groupId>org.apache.maven.plugins</groupId>
    group = ET.SubElement(plugin, ns("groupId"))
    group.text = "org.apache.maven.plugins"

    #   <artifactId>maven-enforcer-plugin</artifactId>
    artifact = ET.SubElement(plugin, ns("artifactId"))
    artifact.text = "maven-enforcer-plugin"

    #   <version>1.3.1</version>
    version = ET.SubElement(plugin, ns("version"))
    version.text = "1.3.1"

    #   <dependencies>
    dependencies = ET.SubElement(plugin, ns("dependencies"))

    #       <dependency>
    dependency = ET.SubElement(dependencies, ns("dependency"))

    #           <groupId>com.redhat.victims</groupId>
    victimsGroup = ET.SubElement(dependency, ns("groupId"))
    victimsGroup.text = "com.redhat.victims"

    #           <artifactId>enforce-victims-rule</artifactId>
    victimsArtifact = ET.SubElement(dependency, ns("artifactId"))
    victimsArtifact.text = "enforce-victims-rule"

    #           <version>?</version>
    victimsVersion = ET.SubElement(dependency, ns("version"))
    victimsVersion.text = versionString
    #       </dependency>
    #   </dependencies>

    #   <executions>
    executions = ET.SubElement(plugin, ns("executions"))

    #       <execution>
    execution = ET.SubElement(executions, ns("execution"))

    #           <id>enforce-victims-rule</id>
    ident = ET.SubElement(execution, ns("id"))
    ident.text = "enforce-victims-rule"

    #           <goals>
    goals = ET.SubElement(execution, ns("goals"))

    #                <goal>enforce</goal>
    goal = ET.SubElement(goals, ns("goal"))
    goal.text = "enforce"
    #           </goals>

    #           <configuration>
    config = ET.SubElement(execution, ns("configuration"))

    #               <rules>
    rules = ET.SubElement(config, ns("execution"))

    #                   <rule implementation="com.redhat.victims.VictimsRule">
    rule = ET.SubElement(rules, ns("rule"))
    rule.attrib["implementation"] = "com.redhat.victims.VictimsRule"

    #                       <metadata>warning</metadata>
    meta = ET.SubElement(rule, ns("metadata"))
    meta.text = "warning"

    #                       <fingerprint>fatal</fingerprint>
    fingerprint = ET.SubElement(rule, ns("fingerprint"))
    fingerprint.text = "fatal"

    #                       <updates>daily</updates>
    updates = ET.SubElement(rule, ns("updates"))
    updates.text = "daily"

    #                   </rule>
    #               </rules>
    #           </configuration>
    #       </execution>
    #   </executions>



def patch_pom(filename, target, version):

    # Warning - assumes victims configuration doesn't exist
    # in pom file already.
    ET.register_namespace("", "http://maven.apache.org/POM/4.0.0")
    doc = ET.parse(filename)
    build = doc.getroot().find(ns("build"))
    plugins = build.find(ns("plugins"))
    inject_config(plugins, version)
    doc.write(target)


def main():

    # le Configuration
    url         = "https://github.com/wildfly/wildfly/archive/master.zip"
    output      = "source.zip"
    pomfile     = "wildfly-master/pom.xml"
    srcdir      = "wildfly-master"
    buildcmd    = "mvn package -X -Dmaven.test.skip=True"
    version     = sys.argv[1:]
    if not version:
        version = "1.3.2-SNAPSHOT"

    if not os.path.exists(output):
        download_source(url, output)
    
    # Extract source
    if os.path.exists(output) and not os.path.exists(srcdir):
        src = zipfile.ZipFile(output)
        src.extractall()

    # Enter the latest victims  (overwrite pom.xml)
    patch_pom(pomfile, pomfile, version)

    # Kickoff the build
    os.chdir(srcdir)
    rc = subprocess.call(buildcmd.split(" "))
    os.chdir("..")

    # Cleanup (for clean exit)
    if rc == 0: 
        shutil.rmtree(srcdir)
        os.remove(output)
    
    # Exit with maven return code
    print("exit({})".format(rc))
    sys.exit(rc)

if __name__ == "__main__":
    main()


    
    




