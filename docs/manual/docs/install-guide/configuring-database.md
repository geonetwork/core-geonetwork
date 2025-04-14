# Configuring the database {#configuring-database}

## Introduction

GeoNetwork uses a database to persist aspects such as metadata records, privileges and configurations. The database default structure is created by the application on initial startup. Subsequent releases of GeoNetwork will update the database structure automatically. For this reason the database user initially needs create privileges on the database. A number of database dialects are supported; ***H2***,***PostgreSQL***,***PostGIS***,***Oracle***,***SQL Server***. This section describes various options to configure the database connection.

## H2 database

By default, a [H2](https://www.h2database.com/html/main.html) database is configured and created when the application first starts. The H2 database named `gn.h2.db` is created:

-   In the **`jetty`** folder of the GeoNetwork application folder when using the [ZIP distribution](installing-from-zip.md).
-   In the **`bin`** folder of Tomcat when deploying the [WAR](installing-from-war-file.md) on Tomcat (started using `startup.sh`).

!!! note

    You don't **need** to configure the database if you are happy with the local H2 database. Only change the configuration if you wish to store the data on a remote database.


## Configuring a database via config files

The database dialect is configured in **`/WEB-INF/config-node/srv.xml`**. Uncomment the dialect to use.

A jdbc driver is included for PostgreSQL, Oracle and H2. Other dialects require a jdbc driver to be installed. Download the jdbc library for the dialect and place it in `/WEB-INF/lib` or in the tomcat or GeoNetwork lib folder.

To update the connection details, update the **`WEB-INF/config-db/jdbc.properties`** file with relevant connection information.

GeoNetwork assumes data is stored in the default schema for a user. If this is not the case, you need to activate a setting `hibernate.default_schema` in **`/WEB-INF/config-spring-geonetwork.xml`**. There are some scripts that run directly on the database at initialisation and can't use the `hibernate.default-schema` parameter. For these scripts you need to set the default-schema manually. In PostgreSQL this is possible by appending `?currentSchema=example` to the database connection.

## Configuring a database via JNDI

The Java Naming and Directory Interface (JNDI) is a technology which allows to configure the database in tomcat and reference the JNDI connection by name.

1.  To activate JNDI, you need to activate the JNDI database type in **`/WEB-INF/config-node/srv.xml`**.

2.  Configure the JNDI connection in Tomcat by adding a new resource to **`TOMCAT/conf/context.xml`**. For jetty in **`WEB-INF/jetty-env.xml`**.

    ``` xml
    <Resource name="geonetwork"
        type="javax.sql.DataSource"
        driverClassName="org.postgresql.Driver"
        url="jdbc:postgresql://localhost:5432/geonetwork"
        username="xxxxx" password="xxxxxx"
        maxActive="20"
        />
    ```

## Configuring a database via environment

Setting configuration properties via environment variables is common in container environments such as Docker. 2 options exist:

1.  Add the parameters directly to the Java environment by substituting JAVA_OPTS.

    ``` text
    docker run --rm --name gn -p 8080:8080 -e JAVA_OPTS=" 
        -Dgeonetwork.db.type=postgres 
        -Djdbc.database=example 
        -Djdbc.username=example
        -Djdbc.password=xxx
        -Djdbc.host=localhost
        -Djdbc.port=5432" geonetwork:latest
    ```

2.  Set an exact environment variable including '.'. Many of the GeoNetwork configuration parameters contain a dot, which is a challenge for substitution via environment variables. Docker is an exception here, it provides a mechanism to allow dots in environment variables.

    ``` text
    docker run --rm --name gn -p 8080:8080
        -e geonetwork.db.type=postgres 
        -e jdbc.database=example 
        -e jdbc.username=example
        -e jdbc.password=xxx
        -e jdbc.host=localhost
        -e jdbc.port=5432 geonetwork:latest
    ```

Within PostgreSQL it is possible to configure `postgres` or `postgis`. In the latter case GeoNetwork will use spatial capabilities of PostGIS to filter metadata. In the first case (and for other database dialects) a Shapefile is created for storage of metadata coverage.

## Logging

To see more details about the database connection and queries, the log can be switched to DEBUG level in `web/src/main/webapp/WEB-INF/classes/log4j.xml` (or see [Catalog Server](../administrator-guide/configuring-the-catalog/system-configuration.md#system-config-server) --> Log level).

``` xml
<logger name="org.hibernate.SQL" additivity="false">
    <level value="DEBUG" />
    <appender-ref ref="consoleAppender" />
    <appender-ref ref="fileAppender" />
</logger>
<logger name="org.hibernate.type" additivity="false">
    <level value="DEBUG" />
    <appender-ref ref="consoleAppender" />
    <appender-ref ref="fileAppender" />
</logger>
<logger name="org.hibernate.tool.hbm2ddl" additivity="false">
    <level value="DEBUG" />
    <appender-ref ref="consoleAppender" />
    <appender-ref ref="fileAppender" />
</logger>
```

## Summary

There are various ways to configure a database in GeoNetwork. JNDI and environment are favourable, because when updating to a new version, or changing a database, you don't need to touch any application files.
