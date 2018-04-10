# How to setup

## geonetwork.xml 

Example file to add to **tomcat/conf/Catalina/localhost** to define a JNDI property with the absolute path (the **path** attribute) of the **external GeoNetwork node configuration folder**. Generally, when the JNDI property **configNodeFolderLocation** is defined, the path associated is considered in place of **/WEB-INF/config-node** inside GeoNetwork war.

## External GeoNetwork node configuration folder

The external GeoNetwork node configuration folder must contain **ONLY** the node configuration xml files, like srv.xml, the default in single node instances and the one placed in this folder as example.

## srv.xml

Inside the node file example must be configured the relative path (relative in relation to this file) to the dataSource.xml

## dataSource.xml

For common parameters check [/web/src/main/webResources/WEB-INF/config-db/README.md](https://github.com/geonetwork/core-geonetwork/blob/3.4.x/web/src/main/webResources/WEB-INF/config-db/README.md).

For database specific configuration uncomment the appropriate section and edit the contents.
