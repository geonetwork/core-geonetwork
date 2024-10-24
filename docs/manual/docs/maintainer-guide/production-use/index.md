# Production use

This paragraph shares some guidance around setting up GeoNetwork for production use.

## Database

GeoNetwork arrives with a file based H2 database. In production make sure to switch to an external database system, such as PostGres, Oracle or SQL server. Read more about setting up a database at [Configuring the database](../../install-guide/configuring-database.md)

JNDI is a technology that allows GeoNetwork to delegate the configuration of the database to Tomcat. By using JNDI the database can be easily configured without the need to change config files inside the application folder.

GeoNetwork may run out of database connections, especially if a catalogue is set up with many harvesters. You can increase the number of allowed connections (if the database allows it). But also consider to set up some periodical monitoring to evaluate if GeoNetwork is running out of connections. The catalogue will throw random errors if running low on connections.

## Java container

GeoNetwork requires Java 8. The Oracle JRE version 8 is reaching end-of-live, we suggest to use the [openJDK](https://adoptopenjdk.net).

GeoNetwork arrives with a default container called Jetty. Jetty is a powerful minimal container implementation. If you need more configuration options consider to use Tomcat. Other containers can be used, but there are not many user experiences. Read more at [Installing from WAR file](../../install-guide/installing-from-war-file.md)

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

GeoNetwork is a memory intensive application. Consider to provide at least 2GB, but 4GB is probably better. But don't go higher than 6GB. Read more about memory in Java applications at the [geoserver documentation](https://docs.geoserver.org/stable/en/user/production/container.html). If you're setting up an instance of Elastic Search, consider to provide at least 8GB to Elastic.

## Scaling

GeoNetwork currently has challenges to be set up in a load balanced/fail over configuration. The search index is stored in memory and will not be aware of changes on records done in other nodes. An option to optimise this is introducing a master-minion model; modifications are done in master, minion harvest master at intervals. Each minion will have a local database. Typical aspects stored in the database, like groups, settings, user feedback and search statistics will not be synchronised between nodes. The data folder can be shared between nodes by using a network share.

## GeoNetwork and Docker

Docker is a popular virtualisation technology for hosting services. Conventions from Docker can also be used in other cloud environments. As GeoNetwork community we maintain a [docker image on docker hub](https://hub.docker.com/_/geonetwork). Note that for each version there is also a postgres tag which uses a remote postgres database. A best practices for Docker is to parameterise GeoNetwork using environment variables which are injected from docker machine or an orchestration.

## Web Proxy

GeoNetwork contains a web proxy to bypass cross browser communication limitations of browsers. This proxy is used for example:

-   Map viewer / GetCapabilities document retrieval
-   Map viewer / Load a WFS layer
-   Map viewer / WMS GetFeatureInfo
-   Record view / List atom feed resources
-   Editor / Warning if a link return http errors
-   Admin / Harvesting / GetCapabilities for CSW to retrieve queryable fields
-   Admin / Thesaurus / Add from INSPIRE registry

A best practice is to whitelist a series of servers which are known to contain data services. However the best guidance here is to recommend to any data provider to enable [CORS](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing) on their services, and then disable the web proxy. CORS fixes the cross browser communication limitation in the proper way.

If set up in an incorrect way, remote users may get access to resources that should not be accessible to them, or impersonate themselves as the geonetwork server while browsing the web.

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

-   GeoNetwork has a capability to login, for that reason browsers expect the site to run secure over https. However you have to consider that browsers on https sites will block any content included as http (mixed content). Many links (thumbnails, wms services, \...) in (archived) metadata may still be based on http. A consideration could be to run the website on both http and https and switch to https in case users login.
-   Engage with the popular search engines to either or not have your GeoNetwork listed in search results. Register the GeoNetwork Sitemap in the various search engine administration pages, and monitor the crawling and search behaviour. It will lead to interesting insights, such as search behaviour and dead links in metadata. In order to identify yourself to search engines, you need to place an identification file in the root of your website. At the same site also place the robots.txt file, which links to the sitemap. Robots.txt can also be used to guide the search engine to not crawl certain parts of the catalogue. If GeoNetwork is installed in the root folder, robots.txt is already in the correct location.
-   Verify that the catalogue uri's of records and api's are persistent over time. Other sites may deep link into the catalogue, those links should not be broken after a migration. Fix broken links by setting up forward rules that forward traffic to new url's. Prevent broken links in future by using [cool uri's](https://www.w3.org/TR/cooluris/). For example do not use a product name (eg GeoNetwork) in a url.
-   Provide a link to the authority managing the catalogue, a disclaimer, cookie warning and/or privacy policy on the header/footer of the site.
-   Monitor the availability of the application using a tool like zabbix, nagios or [geohealthcheck](https://geohealthcheck.org/).
