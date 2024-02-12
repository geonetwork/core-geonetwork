# Managing metadata standards

Metadata records in GeoNetwork are described by a schema. The schema sets out the structuring of the metadata record and provides all the ancillary data and functions to use the schema in GeoNetwork.

A metadata schema plugin capability was introduced in GeoNetwork 2.8.0. In 3.0.0 the plugin capability was expanded to facilitate the fact that plugins can contain java-code.

!!! note

    Adding a metadata schema to GeoNetwork that is incorrect or invalid can thoroughly break your GeoNetwork instance. This section is for catalogue administrators who are confident about metadata schemas and understand the different technologies involved with a GeoNetwork metadata schema.


A detailed description of what constitutes a metadata schema for GeoNetwork can be found in the GeoNetwork Developers Manual. This section will describe how to access the schema add, update and delete functions and how those functions should be used.

!!! note

    Metadata schemas should be thoroughly tested in a development instance of GeoNetwork **before** they are deployed in a production instance. Errors in a schema plugin (particularly in the presentation XSLTs) may make your GeoNetwork instance unusable.


## Adding a schema

To add a metadata schema to the catalog, a rebuild of the sources is required. In a source-code tree there are 4 actions to be done in the settings (database).

-   Place the schema-folder in /schemas

-   Verify and if compatible update the version of GeoNetwork (parent) referenced from the pom.xml file in the schema plugin.

-   Add a reference in /schemas/pom.xml to the newly added schema

    ``` xml
    <module>{myschema}</module>
    ```

-   Add references to the newly added schema in /web/pom.xml

    ``` xml
    <dependency>
      <groupId>${project.groupId}</groupId>
      <artifactId>{myschema}</artifactId>
      <version>${project.version}</version>
    </dependency>
    ```

-   and

    ``` xml
    <resource>
      <directory>${project.basedir}/../schemas/{myschema}/src/main/plugin</directory>
      <targetPath>${basedir}/src/main/webapp/WEB-INF/data/config/schema_plugins</targetPath>
    </resource>
    ```

Then Build and deploy your instance of GeoNetwork.
