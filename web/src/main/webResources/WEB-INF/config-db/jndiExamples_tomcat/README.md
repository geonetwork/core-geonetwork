# JNDI example files for Tomcat

This folder contains JNDI example files for Tomcat for H2, Oracle and Postgres.

* Pick the jndi configuration for the database you want to use and fill the relevant parameters for your database
* Rename the file to geonetwork.xml (or to appName.xml if you use another name)
* Put the file inside `TOMCAT_DIR/conf/Catalina/localhost` 
* Be sure that the jar for the driver you want to use is in `TOMCAT_DIR/lib`

**Note:** replace `TOMCAT_DIR` with Tomcat path.

Check the following links for additional documentation about JNDI configuration in Tomcat:

- https://tomcat.apache.org/tomcat-8.5-doc/jndi-resources-howto.html#JDBC_Data_Sources
- https://tomcat.apache.org/tomcat-8.5-doc/jndi-datasource-examples-howto.html

## Postgres database example

For a Postgres database use the file `postgres_JNDI_Example.xml`, configuring the following parameters:

- `url`: JDBC connection string for Postgresql database, typically fill the hostname/port/database name. See additional parameters in https://jdbc.postgresql.org/documentation/head/connect.html
- `username`: database username with read/write permissions in the database
- `password`: database user password.
- For other parameters check additional documentation in https://tomcat.apache.org/tomcat-8.5-doc/jndi-resources-howto.html#JDBC_Data_Sources

```
<Context>
  <Environment name="gnDatabaseDialect" value="POSTGRESQL" type="java.lang.String" override="false"/>
  <Resource name="jdbc/geonetwork" auth="Container"
            factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
            type="javax.sql.DataSource" driverClassName="org.postgresql.Driver"
            url="jdbc:postgresql://localhost:5432/postgres"
            username="postgres" password="postgres" 
            maxActive="20" maxIdle="10" defaultAutoCommit="false" maxWait="-1"/>
</Context>
```

Once configured, assuming GeoNetwork is deployed as `geonetwork` (otherwise use the proper application name), 
rename the file to `geonetwork.xml` and copy it to `TOMCAT_DIR/conf/Catalina/localhost`.

Copy the Postgresql JDBC driver to `TOMCAT_DIR/lib`.

**Note:** replace `TOMCAT_DIR` with Tomcat path.

### Postgis support

Additionally if you want to enable the spatial index to be stored in Postgis, you should add the Postgis extension to your database:

```
CREATE EXTENSION postgis;
```

See additional details in https://postgis.net/install/

And uncomment the following Spring beans in the file `TOMCAT_DIR/webapps/geonetwork/WEB-INF/config-db/jndi.xml`:

```
  <!-- Enable for PostGis configuration with JNDI -->
  <bean id="datastoreFactory" class="org.geotools.data.postgis.PostgisNGJNDIDataStoreFactory"/>
  <bean id="datastore" factory-bean="datastoreFactory" factory-method="createDataStore">
    <constructor-arg>
      <map>
        <description>The datastore factory parameters see Geotools documentation for details.
          http://docs.geotools.org/latest/userguide/library/data/datastore.html
        </description>
        <entry key="dbtype" value="postgis"/>
        <entry key="Data Source" value-ref="jdbcDataSource"/>
        <entry key="Loose bbox" value="true"/>
        <entry key="Estimated extends" value="true"/>
        <entry key="encode functions" value="true"/>
        <entry key="validate connections" value="true"/>
        <entry key="fetch size" value="1000"/>
        <entry key="Expose primary keys" value="true"/>
      </map>
    </constructor-arg>
  </bean>
```
