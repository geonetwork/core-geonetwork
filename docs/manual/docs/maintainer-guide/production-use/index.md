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

GeoNetwork currently has challenges to be set up in a load balanced/fail over configuration. The search index is stored in memory and will not be aware of changes on records done in other nodes. An option to optimise this is introducing a master-minion model; modifications are done in master, minion harvest master at intervals. Each minion will have a local database. Typical aspects stored in the database, like groups, settings and user feedback will not be synchronised between nodes. The data folder can be shared between nodes by using a network share.

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

A best practice is to allow a series of servers which are known to contain data services. However the best guidance here is to recommend to any data provider to enable [CORS](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing) on their services, and then disable the web proxy. CORS fixes the cross browser communication limitation in the proper way.

If set up in an incorrect way, remote users may get access to resources that should not be accessible to them, or impersonate themselves as the geonetwork server while browsing the web.

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
-   the IPv6 ranges that carry an IPv4 address, ``::/96`` and ``::ffff:0:0:0/96``

An address can be written in more than one way, so the host is compared in several forms: as it appears in the URL, without the brackets that surround an IPv6 literal, in the canonical form of the address it denotes, and without the dot that can close a fully qualified name, so ``localhost.`` is matched like ``localhost``. An entry therefore applies whichever way a caller spells an address, and a single entry covers every spelling of it.

An address under the NAT64 prefix ``64:ff9b::/96`` is compared by the IPv4 address it carries in its last 32 bits, so ``64:ff9b::a00:1`` is excluded as ``10.0.0.1`` while an address that carries a public IPv4 address is not. On a network that uses DNS64, every server reachable only over IPv4 resolves to an address under that prefix, so excluding the prefix itself would keep the proxy away from most external services there. Only the well-known prefix is read this way. A network that uses a NAT64 prefix of its own is not protected by this check for the IPv4 addresses behind it, so restrict the outbound traffic of the server there.

An address under the 6to4 prefix ``2002::/16`` is compared in the same way by the IPv4 address it carries in its bits 16 to 47, so ``2002:a00:1::1`` is excluded as ``10.0.0.1`` while ``2002:5db8:d70e::1`` is not.

The address a host name leads to is checked as well. When the proxy connects, every address the name resolves to is matched against ``excludeHosts``, and the request is refused if any of them is excluded, so a public name that points to an internal address is refused like the address itself. The name is still matched as written too, which is what entries such as ``.*\.local`` rely on.

When the catalogue reaches other servers through an outbound proxy, set with the ``http.proxyHost`` or ``https.proxyHost`` system property, the outbound proxy resolves the requested host instead, and the catalogue cannot check the address. Block the internal addresses on the outbound proxy itself in that case.

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

#### Allowing internal hosts {#proxy-allow-hosts}

The default list keeps the proxy away from private networks, which also keeps it away from a map server the catalogue reaches on its own network, such as a GeoServer in the same Docker or Kubernetes deployment. A host named in ``allowHosts`` is reachable even if ``excludeHosts`` matches it, or matches the addresses it resolves to. It is empty by default.

``` xml
<init-param>
  <param-name>allowHosts</param-name>
  <param-value>^(geoserver|maps\.intranet)$</param-value>
</init-param>
```

``allowHosts`` is a regular expression matched against the host as it is written in the URL, in the same forms as ``excludeHosts``. Prefer names to addresses: a name allowed this way is reachable whatever it resolves to, whereas an address such as ``10.0.0.5`` is reachable only when the URL names it directly, not through another name that points to it. ``allowPorts`` still applies, so a server listening on another port also needs that port listed there, which opens it towards every host the proxy accepts.

#### Redirects {#proxy-redirects}

A service may answer a request with a redirect to another URL. By default the proxy follows one kind of redirect only: one that moves the same URL from ``http`` to ``https``, keeping the host, the path and the query, on the default ports. Many services answer that way when they only accept ``https``, and following the redirect keeps them working through the proxy.

Any other redirect is passed back to the caller as it was received. The browser then requests the new location itself, without the proxy, so a service that does not enable CORS cannot be read from there. In the map viewer, for example, the layers of such a service fail to load.

To have the proxy follow every redirect, set ``followRedirects`` to ``true``:

``` xml
<init-param>
  <param-name>followRedirects</param-name>
  <param-value>true</param-value>
</init-param>
```

The destination of each redirect is then put through the same host and port checks as the requested URL before it is followed, so a redirect cannot send the request on to a host that ``excludeHosts`` keeps out of reach, or to a port that ``allowPorts`` does not open. Those are the only checks applied to it: in ``DB_LINK_CHECK`` mode a redirect can lead anonymous users to a host that no record links to. Enable it only when the services the catalogue relies on need it.

#### Setting them without editing web.xml {#proxy-settings-outside-web-xml}

These settings can be given outside ``WEB-INF/web.xml``, which is what you want when the file sits inside a container image. Each is looked up in turn as an environment variable, a Java system property, and an entry of ``WEB-INF/config.properties``, and the value in ``web.xml`` is used only when none of the three is set.

The name of the property is the name of the servlet, ``HttpProxy``, between the ``geonetwork`` prefix and the name of the setting:

-   ``geonetwork.HttpProxy.excludeHosts``
-   ``geonetwork.HttpProxy.allowPorts``
-   ``geonetwork.HttpProxy.allowHosts``
-   ``geonetwork.HttpProxy.followRedirects``

As an environment variable the same name is written in upper case with underscores in place of the dots:

``` shell
docker run --name geonetwork \
  -e 'GEONETWORK_HTTPPROXY_EXCLUDEHOSTS=^(localhost|...|internal\.example\.org)$' \
  -e 'GEONETWORK_HTTPPROXY_ALLOWPORTS=8080|8443' \
  -e 'GEONETWORK_HTTPPROXY_FOLLOWREDIRECTS=true' \
  geonetwork
```

A value given this way replaces the default list rather than adding to it. Copy the value shipped in ``web.xml``, shown abbreviated above as ``...``, and put your own entries inside the same parentheses; an entry left out is an entry the proxy is once again free to fetch.

If the catalogue is deployed under a context path other than ``/geonetwork``, that path is also accepted in place of the ``geonetwork`` prefix, so a catalogue served from ``/catalogue`` reads ``catalogue.HttpProxy.excludeHosts`` first and falls back to the ``geonetwork`` name.

Keep those defaults and add your own entries for any other host the catalogue should not be asked to fetch. Where the catalogue is published in front of a private network, restricting the outbound traffic of the server at the network level is worthwhile too, and use ``allowPorts`` sparingly, since every port added there is opened towards every host the proxy accepts.

## WEB

Since an important part of the catalogue behaves like a normal website. Adopting website best practices is recommended:

-   GeoNetwork has a capability to login, for that reason browsers expect the site to run secure over https. However you have to consider that browsers on https sites will block any content included as http (mixed content). Many links (thumbnails, wms services, \...) in (archived) metadata may still be based on http. A consideration could be to run the website on both http and https and switch to https in case users login.
-   Engage with the popular search engines to either or not have your GeoNetwork listed in search results. Register the GeoNetwork Sitemap in the various search engine administration pages, and monitor the crawling and search behaviour. It will lead to interesting insights, such as search behaviour and dead links in metadata. In order to identify yourself to search engines, you need to place an identification file in the root of your website. At the same site also place the robots.txt file, which links to the sitemap. Robots.txt can also be used to guide the search engine to not crawl certain parts of the catalogue. If GeoNetwork is installed in the root folder, robots.txt is already in the correct location.
-   Verify that the catalogue uri's of records and api's are persistent over time. Other sites may deep link into the catalogue, those links should not be broken after a migration. Fix broken links by setting up forward rules that forward traffic to new url's. Prevent broken links in future by using [cool uri's](https://www.w3.org/TR/cooluris/). For example do not use a product name (eg GeoNetwork) in a url.
-   Provide a link to the authority managing the catalogue, a disclaimer, cookie warning and/or privacy policy on the header/footer of the site.
-   Monitor the availability of the application using a tool like zabbix, nagios or [geohealthcheck](https://geohealthcheck.org/).
