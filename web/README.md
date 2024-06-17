# GeoNetwork Web application

The web module gathers the static resources and configuration files for building the final web application WAR.

Before you start check out [software development building](../software_development/BUILDING.md) instructions on setting up Elasticsearch.

1. Run embedded Jetty server:

   ```
   mvn jetty:run
   ```

   Optional: Use profile `env-dev` to disable XSLT cache during XSLT development. Edit URL to include ``?debug=true`` to 
   test user-interface changes.
 
   ```
   mvn jetty:run -Penv-dev
   ```

   Optional: Use profile `ui-dev` to disable JavaScript cache for user-interface development.

   ```
   mvn jetty:run -Pui-dev
   ```

2. After a moment, GeoNetwork should be accessible at:
   
   * http://localhost:8080/geonetwork

4. Jetty is configured to use `src/main/webapp` and maven classpath and will reload changes:
   
   * For changes related to the user interface in the `web-ui` module:
     
     ```
     mvn process-resources
     ```
   
   * For changes to schema plugins:
   
     ```
     cd ../schemas
     mvn install
     cd ../web
     mvn process-resources
     ```
   
   * You may also try the following (which just copies between folders):
     
     ```
     mvn process-resources -DschemasCopy=true
     ```

## Clean

The build generates, processes and compile files into:

* ``src/main/webapp/WEB-INF/data/config/schema_plugins``
* ``target/``

Use ``mvn clean`` to remove these files.

In addition running jetty makes use of a database:

* ``~/gn.mv.db``
* ``~/gn.trace.db``
* ``*.db`` (based on geonetwork 3.x use)

Along with data directory and cache, notably: 

* ``images/`` ...
* ``jcs_caching/`` ...
* ``logs/`` ...
* ``src/main/webapp/WEB-INF/data/config/schema_plugins/`` ...
* ``src/main/webapp/WEB-INF/data/data/`` ...
* ``src/main/webapp/WEB-INF/data/wro4j*.db``
* ``src/main/webapp/WEB-INF/doc/en/`` ...
* ``src/main/webapp/WEB-INF/doc/fn/`` ...

Use `mvn clean:clean@reset` to remove these files.

## Managing Schema Plugins

The web application `src/main/webapp` contains `WEB-INF/data/config/schema_plugins` used
by the application.

If your plugin is in the `schemas` folder:

1. The `web/pom.xml` is setup to run jetty and automatically include any additional
   schema plugins in the `schemas` folder.

If your plugin is not in the `schemas` folder:

2. If you are building a metadata101 plugin separately, or working with your own plugin:

   ```
   cd iso19139.xyz
   mvn install
   ```

3. Use `jetty:run` with an additional profile to test your plugin:
   
   ```
   mvn install -Pschema-iso19139.xyz
   mvn jetty:run -Pschema-iso19139.xyz
   ```

2. In the example above the profile `-Pschema-iso19139.xyz`:
   
   * Includes `schema-iso19139.xyz` artifact as a dependency, making the schema plugin bean is available on the CLASSPATH.
   * Unpacks the `schema-iso19139.xyz` artifact `plugin` folder into `WEB-INF/data/config/schema_plugins`

4. The profile can also be used with `process-resources` while jetty is running:
   
   ```
   cd web
   mvn process-resources -Pschema-iso19139.xyz
   ```

5. Tip: If work with a set series of plugins you can manage via [settings.xml](https://maven.apache.org/settings.html).

# Managing Schema Plugins

The web application `src/main/webapp` contains `WEB-INF/data/config/schema_plugins` used
by the application.

If your plugin is in the `schemas` folder:

1. The `web/pom.xml` is setup to run jetty and automatically include any additional
   schema plugins in the `schemas` folder.
   

If your plugin is not in the `schemas` folder:

2. If you are building a metadata101 plugin separately, or working with your own plugin:

   ```
   cd iso19139.xyz
   mvn install
   ```

3. Use `jetty:run` with an additional profile to test your plugin:
   
   ```
   mvn install -Pschema-iso19139.xyz
   mvn jetty:run -Pschema-iso19139.xyz
   ```

2. In the example above the profile `-Pschema-iso19139.xyz`:
   
   * Includes `schema-iso19139.xyz` artifact as a dependency, making the schema plugin bean is available on the CLASSPATH.
   * Unpacks the `schema-iso19139.xyz` artifact `plugin` folder into `WEB-INF/data/config/schema_plugins`

4. The profile can also be used with `process-resources` while jetty is running:
   
   ```
   cd web
   mvn process-resources -Pschema-iso19139.xyz
   ```

5. Tip: If work with a set series of plugins you can manage via [settings.xml](https://maven.apache.org/settings.html).

## Including additional schema plugins

To include your schema plugin in `web/pom.xml`:

1.  Add a profile to `web/pom.xml` unpacking your schema plugin.

    Use the `iso19115-3.2018` as an example, at the time of writing:
   
   ```xml
    <profile>
      <id>schema-iso19115-xyz</id>
      <activation>
        <property><name>schemasCopy</name><value>!true</value></property>
        <file><exists>../schemas/iso19115-3.2018</exists></file>
      </activation>
      <dependencies>
        <dependency>
          <groupId>org.geonetwork-opensource.schemas</groupId>
          <artifactId>schema-iso19139.xyz</artifactId>
          <version>${project.version}</version>
        </dependency>
      </dependencies>
      <build>
        <plugins>
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <executions>
              <execution>
                <id>iso19115-xyz-resources</id>
                <phase>process-resources</phase>
                <goals><goal>unpack</goal></goals>
                <configuration>
                  <encoding>UTF-8</encoding>
                  <artifactItems>
                    <artifactItem>
                      <groupId>org.geonetwork-opensource.schemas</groupId>
                      <artifactId>schema-iso19115-xyz</artifactId>
                      <type>zip</type>
                      <overWrite>false</overWrite>
                      <outputDirectory>${schema-plugins.dir}</outputDirectory>
                    </artifactItem>
                  </artifactItems>
                  </artifactItems>
                </configuration>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
    </profile>
   ```

2. This profile example has several interesting features:
   
   * Activates automatically if the `schemas/iso19115-xyz` folder is present
   * Disabled if `-DschemasCopy=true` property is set
   * Adds a dependency so that the schema plugin jar is included on the classpath
   * Unpacks a `plugin` folder into the webapp `schema_plugins` folder

3. Over time we expect active metadata101 schema plugins be listed.

## Alternative: Add schema plugin to `copy-schemas` profile

An alternative approach of copying folders is available using the `-DschemasCopy` flag:

* `-DschemasCopy=true`: copies folders from `../schemas` location
* `-DschemasCopy=false`: default approach, using artifact and maven repository

To add your schema plugin to the `schemas-copy` profile:

1. Locate the `schemas-copy` profile in `web/pom.xml`:

2. Add your plugin as a dependency to the profile:

   ```xml
   <dependency>
     <groupId>org.geonetwork-opensource.schemas</groupId>
     <artifactId>schema-iso19139.xyz</artifactId>
     <version>${project.version}</version>
   </dependency>
   ```
   
3. By default each `src/main/plugin` folder in `schemas` is copied.

   If your plugin is in not in the `schemas` folder:
   
   ```xml
   <resource>
     <directory>${metadata101}/iso19139.xyz/src/main/plugin</directory>
     <targetPath>${schema-plugins.dir}</targetPath>
   </resource>
   ```

5. This approach modifies `web/pom.xml` so it can only be recommended in a fork or branch.

7. Implementation notes:
   
   * The `<dependency>` added here makes use of a specific verion number.
   
   * If your plugin is inheriting its version number from `schemas/pom.xml` you may use
     the property `gn.schemas.version`.

6. The `add-schema.sh` script automates these changes.
