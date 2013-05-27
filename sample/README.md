Run:

$ mvn clean compile jar:jar

You will see a fatal error triggered due to the vulnerable spring dependency included in the project.

Edit pom.xml so that instead of:

<version>2.5.6</version>

You have:

<version>2.5.6.SEC03</version>

Run "mvn clean compile jar:jar" again. The build will succeed because no vulnerable dependency is found.
