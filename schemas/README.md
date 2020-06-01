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

3. Schema plugin `pom.xml` must include `src/main/plugin` in resources section:
   
   ```xml
    <resources>
      <resource>
        <directory>src/main/resources</directory>
      </resource>
      <resource>
        <directory>src/main/plugin</directory>
        <targetPath>plugin</targetPath>
      </resource>
    </resources>
   ```

4. If you need to depend on any modules please make use of:
  
   * `gn.project.version` property for geonetwork modules:
   
      ```xml
     <dependency>
       <groupId>${project.groupId}</groupId>
       <artifactId>common</artifactId>
       <version>${gn.project.version}</version>
     </dependency>
      ```
   
   * `project.version` property for other schemas (or schema-core):
     
     ```
     <dependencies>
       <dependency>
         <groupId>${project.groupId}</groupId>
         <artifactId>schema-core</artifactId>
         <version>${project.version}</version>
       </dependency>
       <dependency>
         <groupId>${project.groupId}</groupId>
         <artifactId>schema-iso19139</artifactId>
         <version>${project.version}</version>
       </dependency>
     </dependencies>
     ```
   
5. Use `mvn install` to install your schema plugin in your local repository.

## Add a plugin to the build

While schema plugins can be built independently, they can be conditionally included in the build:

1. Add schema plugin:

   * As a folder, using `.gitignore` to avoid accidentally commiting.
   
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
   
3. The `add-schema.sh` script automates these changes.

