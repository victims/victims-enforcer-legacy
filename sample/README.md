# enforce-victims-rule sample project

## Usage

Run ```mvn clean compile jar:jar```. You will see a fatal error triggered due to the vulnerable spring dependency included in the project. Edit _pom.xml_ so that instead of:
```xml
<version>2.5.6</version>
```
You have:
```xml
<version>2.5.6.SEC03</version>
```
Run ```mvn clean compile jar:jar``` again. The build will succeed because no vulnerable dependency is found.

## Usage on an offline network

The enforce-victims-rule maven plugin requires internet connectivity to sync the local known-vulnerable JAR database with the canonical online version. In environments that do not have internet access, this sample project can be used to generate a local copy of the database, which can be copied to the offline network.

On a system with internet access, checkout this sample project and run ```mvn clean compile jar:jar```. After the database is synchronized, you will have a local file named _~/.victims/victims.h2.db_ . On a system on the offline network, login as the user who runs builds and copy this file to _~/.victims/victims.h2.db_ . In that project's _pom.xml_ set:
```xml
<updates>off</updates>
```
To ensure that it does not attempt to sync the local database with the canonical online version.
