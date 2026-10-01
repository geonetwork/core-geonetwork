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

GeoNetwork currently has limitations when deployed in a load-balanced/failover configuration. The search index is stored in memory and will not reflect changes made to records on other nodes. One option to work around this is a master-minion model: modifications are made on the master, and minions harvest from the master at regular intervals. Each minion will have its own local database. Typical aspects stored in the database, like groups, settings, and user feedback, will not be synchronised between nodes. The data folder can be shared between nodes using a network share.

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

A best practice is to whitelist a series of servers which are known to contain data services. However the best guidance here is to recommend to any data provider to enable [CORS](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing) on their services, and then disable the web proxy. CORS fixes the cross browser communication limitation in the proper way.

If configured incorrectly, remote users may gain access to restricted resources, or impersonate the GeoNetwork server while browsing the web.

GeoNetwork has two modes to limit the access via the proxy. The configuration of this mode is defined in ``WEB-INF/web.xml``.

``` xml
<init-param>
  <param-name>securityMode</param-name>
  <param-value>NONE</param-value>
</init-param>
```

-   NONE: (dis)allow certain domains via security configuration (default before 3.10.3)
-   DB_LINK_CHECK (default since 3.10.3)

It is recommended to use the DB_LINK_CHECK mode. The following rules apply:

-   Authenticated users can use the proxy to all domains, apart from the hosts kept out of reach by the exclusion list described below.
-   For anonymous users, if the host of the URL requested is not used in any metadata record links, then a NotAllowedException is returned. If a WMS URL is registered, all GetCapabilities, GetFeatureInfo will be accepted. That's why only a host check is done.
-   Also if a request is made directly to the proxy, a SecurityException is returned because no session exist. This limit its usage to user with a catalog session.
-   Catalog reviewers have to use the metadata link analysis tool to register links allowed for the proxy. The tool is available at 'Record and link analysis' in the ``Admin --> Statistics & status`` menu. In the future we may trigger link analysis as a background task to have an up to date list of links. For now, if the table is empty, the exception highlights the fact that the link analysis tool should be used to populate the list.

One issue that anonymous users can encounter is if using the map viewer and the user adds a WMS/WFS service URL which is not registered in any metadata records and which has no CORS enabled. The user will not be able to add any layers from those services.

### Hosts and ports the proxy can reach {#proxy-hosts-and-ports}

Independently of the security mode, the proxy refuses a request whose host matches ``excludeHosts``. It also refuses a request naming a port other than 80, 443, or one of the ports listed in ``allowPorts``; a URL that names no port at all uses the default of its protocol and is accepted. Both settings are defined in ``WEB-INF/web.xml`` and both apply to every caller, authenticated or not.

``` xml
<init-param>
  <param-name>excludeHosts</param-name>
  <param-value>^(localhost|127\..*|...)$</param-value>
</init-param>
<init-param>
  <param-name>allowPorts</param-name>
  <param-value>8443|8080</param-value>
</init-param>
```

``excludeHosts`` is a regular expression matched against the whole host. Matching ignores case, since a host name is not case sensitive. Its default value keeps the proxy away from the server itself and from the ranges reserved for private networks:

-   the loopback addresses, ``localhost``, ``127.0.0.0/8`` and ``::1``, and the ``.local`` and ``.localhost`` domains
-   the unspecified addresses, ``0.0.0.0/8`` and ``::``, and the broadcast address ``255.255.255.255``
-   the private ranges of RFC 1918, ``10.0.0.0/8``, ``172.16.0.0/12`` and ``192.168.0.0/16``
-   the link-local range ``169.254.0.0/16``, the shared address space ``100.64.0.0/10``, and the benchmarking range ``198.18.0.0/15``
-   the multicast range ``224.0.0.0/4``
-   the IPv6 unique local, link-local and site-local ranges, ``fc00::/7``, ``fe80::/10`` and ``fec0::/10``
-   the IPv6 ranges that carry an IPv4 address, ``::/96`` and the NAT64 prefix ``64:ff9b::/96``

An address can be written in more than one way, so the host is compared in several forms: as it appears in the URL, without the brackets that surround an IPv6 literal, and in the canonical form of the address it denotes. An entry therefore applies whichever way a caller spells an address, and a single entry covers every spelling of it.

#### Adding an IPv6 address

The same address can be written in several ways, so an entry is best written in the canonical form: lower case, all eight groups spelled out, no ``::`` and no leading zeros. That form is the one the proxy derives from whatever the caller sent, so a single entry covers every spelling. To exclude the host ``2001:db8:1::10``, write its canonical form ``2001:db8:1:0:0:0:0:10``. To exclude a whole prefix, keep the groups the prefix fixes and finish with ``.*``: ``2001:db8:2::/48`` becomes ``2001:db8:2:.*``.

``` xml
<init-param>
  <param-name>excludeHosts</param-name>
  <param-value>^(localhost|127\..*|2001:db8:1:0:0:0:0:10|2001:db8:2:.*)$</param-value>
</init-param>
```

Those two entries exclude ``[2001:db8:1::10]``, ``[2001:db8:1:0:0:0:0:10]`` and ``[2001:0db8:0001::0010]``, which are the same address, along with every address under ``2001:db8:2::/48``.

Three details are worth keeping in mind:

-   Write the entry without the square brackets of a URL. They are part of the URL syntax, not of the address. An entry that keeps them is still matched, but only against that one way of writing the address, so it misses the other spellings of the same host.
-   Keep the colon that closes a prefix. ``2001:db8:2:.*`` stops at the addresses under ``2001:db8:2``, whereas ``2001:db8:2.*`` would also take in ``2001:db8:20`` and ``2001:db8:2a``, which are different networks.
-   A prefix is straightforward to write when its length is a multiple of 16, since it ends on a group boundary. For a length such as /56, which cuts a group in two, exclude the enclosing /48 rather than trying to express the exact boundary in a regular expression.

#### Setting them without editing web.xml {#proxy-settings-outside-web-xml}

Both settings can be given outside ``WEB-INF/web.xml``, which is what you want when the file sits inside a container image. Each is looked up in turn as an environment variable, a Java system property, and an entry of ``WEB-INF/config.properties``, and the value in ``web.xml`` is used only when none of the three is set.

The name of the property is the name of the servlet, ``HttpProxy``, between the ``geonetwork`` prefix and the name of the setting:

-   ``geonetwork.HttpProxy.excludeHosts``
-   ``geonetwork.HttpProxy.allowPorts``

As an environment variable the same name is written in upper case with underscores in place of the dots:

``` shell
docker run --name geonetwork \
  -e 'GEONETWORK_HTTPPROXY_EXCLUDEHOSTS=^(localhost|...|internal\.example\.org)$' \
  -e 'GEONETWORK_HTTPPROXY_ALLOWPORTS=8080|8443' \
  geonetwork
```

A value given this way replaces the default list rather than adding to it. Copy the value shipped in ``web.xml``, shown abbreviated above as ``...``, and put your own entries inside the same parentheses; an entry left out is an entry the proxy is once again free to fetch.

If the catalogue is deployed under a context path other than ``/geonetwork``, that path is also accepted in place of the ``geonetwork`` prefix, so a catalogue served from ``/catalogue`` reads ``catalogue.HttpProxy.excludeHosts`` first and falls back to the ``geonetwork`` name.

Keep those defaults and add your own entries for any other host the catalogue should not be asked to fetch. Entries are matched against the host as it appears in the request. Where the catalogue is published in front of a private network, restricting the outbound traffic of the server at the network level is worthwhile too, and use ``allowPorts`` sparingly, since every port added there is opened towards every host the proxy accepts.

## WEB

Since an important part of the catalogue behaves like a normal website. Adopting website best practices is recommended:

-   GeoNetwork supports login, so browsers expect the site to run securely over HTTPS. However, note that browsers on HTTPS sites will block any content loaded over HTTP (mixed content). Many links (thumbnails, WMS services, etc.) in archived metadata may still use HTTP. One approach is to run the site on both HTTP and HTTPS, switching to HTTPS when users log in.
-   Decide whether you want your GeoNetwork instance listed in search engine results. Register the GeoNetwork sitemap in the various search engine administration pages and monitor crawling and search behaviour. This can reveal useful insights, such as search trends and dead links in metadata. To identify yourself to search engines, place an identification file in the root of your website. Also place a robots.txt file there, linking to the sitemap. robots.txt can also be used to prevent search engines from crawling certain parts of the catalogue. If GeoNetwork is installed in the root folder, robots.txt is already in the correct location.
-   Verify that the catalogue URIs of records and APIs are persistent over time. Other sites may deep-link into the catalogue, and those links should remain valid after a migration. Fix broken links by setting up redirect rules to new URLs. Prevent future broken links by following [cool URIs](https://www.w3.org/TR/cooluris/). For example, do not use a product name (e.g. GeoNetwork) in a URL.
-   Provide a link to the authority managing the catalogue, a disclaimer, cookie warning and/or privacy policy on the header/footer of the site.
-   Monitor the availability of the application using a tool like [Zabbix](https://www.zabbix.com/), [Nagios](https://www.nagios.org/), or [GeoHealthCheck](https://geohealthcheck.org/).
