# Maven module containing schema plugins

## Schema Plugin Definition

1. `src/main/plugin/<folder>` contents:
   
   * `schema-ident.xml` required configuration file
   * schema definition `xsd` files
   * transformations `xsl` files
  
   See [GeoNetwork Manual](https://geonetwork-opensource.org/manuals/trunk/en/customizing-application/implementing-a-schema-plugin.html)

2. `src/main/java` providing:
  
  * Optional: SchemaPlugin bean
  * Optional: ApplicationListener<ServerStartup> to auto install plugin

3. Schema plugin `pom.xml` are asked to bundle their `src/main/plugin` into a `zip`:
   
   ```xml
      <!-- package up plugin folder as a zip -->
      <plugin>
        <artifactId>maven-assembly-plugin</artifactId>
        <executions>
          <execution>
            <id>plugin-assembly</id>
            <phase>package</phase>
            <goals><goal>single</goal></goals>
            <inherited>false</inherited>
            <configuration>
             <appendAssemblyId>false</appendAssemblyId>
             <descriptors>
              <descriptor>src/assembly/schema-plugin.xml</descriptor>
             </descriptors>
            </configuration>
          </execution>
        </executions>
      </plugin>
   ```
   
4. Using a `src/assembly/schema-plugin.xml` assembly:
   
   ```xml
   <assembly xmlns="http://maven.apache.org/ASSEMBLY/2.1.0"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.1.0 http://maven.apache.org/xsd/assembly-2.1.0.xsd">
     <id>plugin</id>
     <includeBaseDirectory>false</includeBaseDirectory>
     <formats>
       <format>zip</format>
     </formats>
     <fileSets>
       <fileSet>
         <directory>src/main/plugin/</directory>
         <outputDirectory></outputDirectory>
         <useDefaultExcludes>true</useDefaultExcludes>
       </fileSet>
     </fileSets>
   </assembly>
   ```

4. If you need to depend on any geonetwork modules please make use of `project.version` property:
   
   ```xml
   <dependency>
     <groupId>org.geonetwork-opensource</groupId>
     <artifactId>common</artifactId>
     <version>${project.version}</version>
   </dependency>
   ```

5. The use of `project.version` is also required for schemas modules:

     ```xml
     <dependencies>
       <dependency>
         <groupId>org.geonetwork-opensource.schemas</groupId>
         <artifactId>schema-core</artifactId>
         <version>${project.version}</version>
       </dependency>
       <dependency>
         <groupId>org.geonetwork-opensource.schemas</groupId>
         <artifactId>schema-iso19139</artifactId>
         <version>${project.version}</version>
       </dependency>
     </dependencies>
     ```
   
5. Use `mvn install` to install your schema plugin in your local repository.

## Optional: Add a plugin to the build

While schema plugins can be built independently, they can be conditionally included in the build:

1. Add schema plugin:

   * As a folder, using `.gitignore` to avoid accidentally committing:
     
     ```
     iso19139.xyz
     ```
     
   * As a git submodule if you are making a fork
`
2. Use a profile (activated by your schema plugin folder being present) to include your schema plugin to the list of modules in `pom.xml`.
   
   ```xml
   <profiles>
     <profile>
       <id>schema-iso19139.xyz</id>
       <activation>
       <file>
         <exists>iso19139.xyz</exists>
       </file>
       </activation>
       <modules>
         <module>iso19139.xyz</module>
       </modules>
     </profile>
   </profiles>
   ```
   
3. The `add-schema.sh` script automates these changes:

   ```
   ./add-schema.sh iso19139.ca.HNAP https://github.com/metadata101/iso19139.ca.HNAP 3.11.x"
   ```

