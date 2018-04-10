# How to setup an external database configuration

To setup an external folder check the **geonetwork.xml** file in this folder.

## geonetwork.xml 

Example file to add to **tomcat/conf/Catalina/localhost** to define a JNDI property with the absolute path (the **path** attribute) of the **external GeoNetwork node configuration folder**. Generally, when the JNDI property **configNodeFolderLocation** is defined, the path associated is considered in place of **/WEB-INF/config-node** inside GeoNetwork war.

## External GeoNetwork node configuration folder

The external GeoNetwork node configuration folder must contain **ONLY** the node configuration xml files, like srv.xml, the default in single node instances and the one placed in this folder as example.

## srv.xml (or another node file)

In this file must be configured the relative path (relative to this file location) to **dataSource.xml**

```

  <!-- The relative path to db configuration file -->
  <import resource="../db-confs/dataSource.xml"/>
  
```

## dataSource.xml

The configuration parameters to setup and configure a database connection.

For database specific configuration uncomment the appropriate section and edit the contents.

Parameters informations is available here [/web/src/main/webResources/WEB-INF/config-db/README.md](https://github.com/geonetwork/core-geonetwork/blob/3.4.x/web/src/main/webResources/WEB-INF/config-db/README.md).



