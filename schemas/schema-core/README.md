GeoNetwork schema plugins core
==============================

Module provides java classes to schema plugin development, including XSL processing. This module provides a transitive dependency on `common` and with it access to geonetwork application and data model.

In addition a shared `schema-plugin` assembly is defined, responsible for packaging plugin contents as a `zip` (see [sharing assembly descriptors](http://maven.apache.org/plugins/maven-assembly-plugin/examples/sharing-descriptors.html) ):

```xml
      <plugin>
        <artifactId>maven-assembly-plugin</artifactId>
        <dependencies>
          <dependency>
            <groupId>org.geonetwork-opensource.schemas</groupId>
            <artifactId>schema-core</artifactId>
            <version>${project.version}</version>
          </dependency>
        </dependencies>
        <executions>
          <execution>
            <id>make-assembly</id>
            <phase>package</phase>
            <goals><goal>single</goal></goals>
            <configuration>
              <descriptorRefs><descriptorRef>schema-plugin</descriptorRef></descriptorRefs>
            </configuration>
          </execution>
        </executions>
      </plugin>
```
