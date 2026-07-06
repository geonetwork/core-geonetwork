# Production use

This paragraph shares some guidance around setting up GeoNetwork for production use.

## Database

GeoNetwork ships with a file-based H2 database. In production, make sure to switch to an external database system, such as PostgreSQL, Oracle, or SQL Server. Read more about setting up a database at [Configuring the database](../../install-guide/configuring-database.md).

JNDI is a technology that allows GeoNetwork to delegate the configuration of the database to Tomcat. By using JNDI the database can be easily configured without the need to change config files inside the application folder.

GeoNetwork may run out of database connections, especially if a catalogue is set up with many harvesters. You can increase the number of allowed connections (if the database allows it), but also consider setting up periodic monitoring to evaluate whether GeoNetwork is running low on connections. The catalogue will throw random errors if connections are exhausted.

## Java container

GeoNetwork 4.4 requires Java 11. Oracle JRE 8 has reached end of life; we recommend using [OpenJDK](https://adoptopenjdk.net).

GeoNetwork ships with a default container called Jetty. Jetty is a powerful minimal container. If you need more configuration options, consider using Tomcat. Other containers can be used, but there is limited community experience with them. Read more at [Installing from WAR file](../../install-guide/installing-from-war-file.md).

If you run Apache in front of Tomcat, make sure to enable [AJP](https://tomcat.apache.org/tomcat-4.0-doc/config/ajp.html), else you may run into page not found errors around login. On Apache 2, enable `mod_proxy_ajp` and set the `ProxyPass` and `ProxyPassReverse` on apache2.conf to use the AJP protocol on Tomcat URL and port 8009:

``` shell
ProxyPass /geonetwork ajp://gn_tomcat_host:8009/geonetwork
ProxyPassReverse /geonetwork ajp://gn_tomcat_host:8009/geonetwork
```

On Tomcat 9, define an AJP Connector on port 8009 in server.xml.

A common challenge in production use is the fact that Java only has a limited set of root certificates that it trusts natively. This causes problems if GeoNetwork tries to access a secure server which has a certificate not trusted by Java. An administrator has to explicitly [load the certificate in to the Java keystore](https://stackoverflow.com/questions/4325263/how-to-import-a-cer-certificate-into-a-java-keystore).

## Data folder

GeoNetwork requires a data folder to store objects uploaded by administrators and managers and some configuration options. By default this folder is located in **`/geonetwork/WEB-INF/data`**. In production situation configure the location of this folder outside the application and make sure the folder is backed up. You can use an environment variable to configure the location of the data folder. Read more at [Customizing the data directory](../../install-guide/customizing-data-directory.md)

## Memory

GeoNetwork is a memory-intensive application. Consider providing at least 2 GB, though 4 GB is recommended. Do not exceed 6 GB. Read more about memory in Java applications in the [GeoServer documentation](https://docs.geoserver.org/stable/en/user/production/container.html). If you are setting up Elasticsearch, consider providing at least 8 GB.

## Scaling

GeoNetwork currently has limitations when deployed in a load-balanced/failover configuration. The search index is stored in memory and will not reflect changes made to records on other nodes. One option to work around this is a master-minion model: modifications are made on the master, and minions harvest from the master at regular intervals. Each minion will have its own local database. Typical aspects stored in the database, like groups, settings, user feedback, and search statistics, will not be synchronised between nodes. The data folder can be shared between nodes using a network share.

## GeoNetwork and Docker

Docker is a popular virtualisation technology for hosting services. Conventions from Docker can also be used in other cloud environments. As a GeoNetwork community, we maintain a [Docker image on Docker Hub](https://hub.docker.com/_/geonetwork). Note that for each version there is also a postgres tag that uses a remote PostgreSQL database. A best practice for Docker is to configure GeoNetwork using environment variables injected from Docker Machine or an orchestration tool.

## Web Proxy

GeoNetwork contains a web proxy to bypass cross browser communication limitations of browsers. This proxy is used for example:

-   Map viewer / GetCapabilities document retrieval
-   Map viewer / Load a WFS layer
-   Map viewer / WMS GetFeatureInfo
-   Record view / List atom feed resources
-   Editor / Warning if a link return http errors
-   Admin / Harvesting / GetCapabilities for CSW to retrieve queryable fields
-   Admin / Thesaurus / Add from INSPIRE registry

A best practice is to allow a series of servers which are known to contain data services. However the best guidance here is to recommend to any data provider to enable [CORS](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing) on their services, and then disable the web proxy. CORS fixes the cross browser communication limitation in the proper way.

If configured incorrectly, remote users may gain access to restricted resources, or impersonate the GeoNetwork server while browsing the web.

GeoNetwork has 2 modes to limit the access via the proxy. The configuration of this mode is defined in ``WEB-INF/web.xml``.

``` xml
<init-param>
  <param-name>securityMode</param-name>
  <param-value>NONE</param-value>
</init-param>
```

-   NONE: (dis)allow certain domains via security configuration (default before 3.10.3)
-   DB_LINK_CHECK (default since 3.10.3)

It is recommended to use the DB_LINK_CHECK mode. The following rules apply:

-   Authenticated users can use the proxy to all domains.
-   For anonymous users, if the host of the URL requested is not used in any metadata record links, then a NotAllowedException is returned. If a WMS URL is registered, all GetCapabilities, GetFeatureInfo will be accepted. That's why only a host check is done.
-   Also if a request is made directly to the proxy, a SecurityException is returned because no session exist. This limit its usage to user with a catalog session.
-   Catalog reviewers have to use the metadata link analysis tool to register links allowed for the proxy. The tool is available at 'Record and link analysis' in the ``Admin --> Statistics & status`` menu. In the future we may trigger link analysis as a background task to have an up to date list of links. For now, if the table is empty, the exception highlights the fact that the link analysis tool should be used to populate the list.

One issue that anonymous users can encounter is if using the map viewer and the user adds a WMS/WFS service URL which is not registered in any metadata records and which has no CORS enabled. The user will not be able to add any layers from those services.

## WEB

Since an important part of the catalogue behaves like a normal website. Adopting website best practices is recommended:

-   GeoNetwork supports login, so browsers expect the site to run securely over HTTPS. However, note that browsers on HTTPS sites will block any content loaded over HTTP (mixed content). Many links (thumbnails, WMS services, etc.) in archived metadata may still use HTTP. One approach is to run the site on both HTTP and HTTPS, switching to HTTPS when users log in.
-   Decide whether you want your GeoNetwork instance listed in search engine results. Register the GeoNetwork sitemap in the various search engine administration pages and monitor crawling and search behaviour. This can reveal useful insights, such as search trends and dead links in metadata. To identify yourself to search engines, place an identification file in the root of your website. Also place a robots.txt file there, linking to the sitemap. robots.txt can also be used to prevent search engines from crawling certain parts of the catalogue. If GeoNetwork is installed in the root folder, robots.txt is already in the correct location.
-   Verify that the catalogue URIs of records and APIs are persistent over time. Other sites may deep-link into the catalogue, and those links should remain valid after a migration. Fix broken links by setting up redirect rules to new URLs. Prevent future broken links by following [cool URIs](https://www.w3.org/TR/cooluris/). For example, do not use a product name (e.g. GeoNetwork) in a URL.
-   Provide a link to the authority managing the catalogue, a disclaimer, cookie warning and/or privacy policy on the header/footer of the site.
-   Monitor the availability of the application using a tool like [Zabbix](https://www.zabbix.com/), [Nagios](https://www.nagios.org/), or [GeoHealthCheck](https://geohealthcheck.org/).
