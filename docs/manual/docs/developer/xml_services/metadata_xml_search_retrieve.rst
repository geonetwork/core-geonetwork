.. _metadata_xml_search_retrieve:

Search and Retrieve Metadata services
=====================================

.. index:: xml.search

Search metadata (xml.search)
----------------------------

The **xml.search** service can be used to retrieve metadata records from GeoNetwork.

Requires authentication: Optional

Request
```````

Search configuration parameters (all values are optional)

- **remote**: Search in local catalog or in a remote catalog. Values: off (default), on

- **extended**: Values: on, off (default)

- **timeout**: Timeout for request in seconds (default: 20)

- **hitsPerPage**: Results per page (default: 10)

- **similarity**: Lucene accuracy for searches (default 0.8)

- **sortBy**: Sorting criteria. Values: relevance (default), rating, popularity, changeDate, title

Search parameters (all values are optional):

- **eastBL, southBL, northBL, westBL**:
  Bounding box to restrict the search

- **relation**: Bounding box criteria.
  Values: equal, overlaps (default), encloses, fullyOutsideOf,
  intersection, crosses, touches, within

- **any**: Text to search in a free text search

- **title**: Metadata title

- **abstract**: Metadata abstract

- **themeKey**: Metadata keywords. To search for several use a value like "Global" or "watersheds"

- **template**: Indicates if search for templates or not. Values: n (default), y

- **dynamic**: Map type. Values: off (default), on

- **download**: Map type. Values: off (default), on

- **digital**: Map type. Values: off (default), on

- **paper**: Map type. Values: off (default), on

- **group**: Filter metadata by group, if missing search in all groups

- **attrset**:

- **dateFrom**: Filter metadata created after specified date

- **dateTo**: Filter metadata created before specified date

- **category**: Metadata category. If not specified, search all categories

Request to search for all metadata example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.search

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request />

Request with free text search example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.search

  Mime-type:
  application/xml

  Post request:s
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <any>africa</any>
  </request>

Request with a geographic search example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.search

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <any>africa</any>
    <eastBL>74.91574</eastBL>
    <southBL>29.40611</southBL>
    <northBL>38.47198</northBL>
    <westBL>60.50417</westBL>
    <relation>overlaps</relation>
    <sortBy>relevance</sortBy>
    <attrset>geo</attrset>
  </request>

Request to search using dates and keywords example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.search

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <title>africa</title>
    <themekey>"Global" or "World"</themekey>
    <dateFrom>2000-02-03T12:47:00</dateFrom>
    <dateTo>2010-02-03T12:49:00</dateTo>
  </request>

Response
````````

The response is the metadata record with additional
**geonet:info** section. The main fields are:

- **response**: Response container.

  - **summary**: Attribute
    **count** indicates the number of metadata records retrieved

    - **keywords**: List of keywords
      that are part of the metadata resultset. Each keyword
      contains the value and the number of occurences in the
      retrieved metadata records.

  - **metadata**: Container for 
    each metadata record found.  Each container has a
    **geonet:info** element with the
    following information:

      - **id**: Metadata internal
        identifier
      - **uuid** : Metadata
        Universally Unique Identifier (UUID)
      - **schema**: Metadata
        schema
      - **createDate**: Metadata
        creation date
      - **changeDate**: Metadata last
        modification date
      - **source**: Source catalogue
        the metadata
      - **category**: Metadata
        category (Can be multiple elements)
      - **score**: Value indicating
        the accuracy of search

Metadata search response example::
  
  <?xml version="1.0" encoding="UTF-8"?>
  <response from="1" to="7">
    <summary count="7" type="local">
      <keywords>
        <keyword count="2" name="Global"/>
        <keyword count="2" name="World"/>
        <keyword count="2" name="watersheds"/>
        <keyword count="1" name="Biology"/>
        <keyword count="1" name="water resources"/>
        <keyword count="1" name="endangered plant species"/>
        <keyword count="1" name="Africa"/>
        <keyword count="1" name="Eurasia"/>
        <keyword count="1" name="endangered animal species"/>
        <keyword count="1" name="Antarctic ecosystem"/>
      </keywords>
    </summary>
    <metadata xmlns:gmx="http://www.isotc211.org/2005/gmx">
      <geonet:info xmlns:geonet="http://www.fao.org/geonetwork">
        <id>12</id>
        <uuid>bc179f91-11c1-4878-b9b4-2270abde98eb</uuid>
        <schema>iso19139</schema>
        <createDate>2007-07-25T12:05:45</createDate>
        <changeDate>2007-11-06T12:10:47</changeDate>
        <source>881a1630-d4e7-4c9c-aa01-7a9bbbbc47b2</source>
        <category>maps</category>
        <category>interactiveResources</category>
        <score>1.0</score>
      </geonet:info>
    </metadata>
    <metadata xmlns:gmx="http://www.isotc211.org/2005/gmx">
      <geonet:info xmlns:geonet="http://www.fao.org/geonetwork">
        <id>11</id>
        <uuid>5df54bf0-3a7d-44bf-9abf-84d772da8df1</uuid>
        <schema>iso19139</schema>
        <createDate>2007-07-19T14:45:07</createDate>
        <changeDate>2007-11-06T12:13:00</changeDate>
        <source>881a1630-d4e7-4c9c-aa01-7a9bbbbc47b2</source>
        <category>maps</category>
        <category>datasets</category>
        <category>interactiveResources</category>
        <score>0.9178859</score>
      </geonet:info>
    </metadata>
  </response>

.. index:: xml.metadata.get

Get metadata (xml.metadata.get)
-------------------------------

The **xml.metadata.get** service can be used to retrieve a metadata record stored in GeoNetwork.

Requires authentication: Optional

Request
```````

*One* of the following parameters:

- **uuid** : Metadata Universal Unique Identifier (UUID)

- **id**: Metadata internal identifier

Get metadata request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.get

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <uuid>aa9bc613-8eef-4859-a9eb-4df35d8b21e4</uuid>
  </request>

Response
````````

Successful response (HTTP status code 200) is the XML metadata record with additional **geonet:info** section. The principal fields for **geonet:info** are:

- **schema**: Metadata schema

- **createDate**: Metadata creation date

- **changeDate**: Metadata last modification date

- **isTemplate**: Indicates if the metadata returned is a template

- **title**: Metadata title

- **source**: Source catalogue the metadata

- **uuid** : Metadata Universally Unique Identifier (UUID)

- **isHarvested**: Indicates if the metadata is harvested

- **popularity**: Indicates how often the record is retrieved

- **rating**: Average rating provided by users

- State of operation on metadata for the user: view, notify, download, dynamic, featured, edit

- **owner**: Indicates if the user that executed the service is the owner of metadata

- **ownername**: Metadata owner name

Get metadata response example::

  <?xml version="1.0" encoding="UTF-8"?>
  <Metadata xmlns:geonet="http://www.fao.org/geonetwork"
    xmlns:csw="http://www.opengis.net/cat/csw/2.0.2">
    <mdFileID>aa9bc613-8eef-4859-a9eb-4df35d8b21e4</mdFileID>
    ...
    <geonet:info>
      <id>10</id>
      <schema>iso19115</schema>
      <createDate>2005-08-23T17:58:18</createDate>
      <changeDate>2007-03-12T17:49:50</changeDate>
      <isTemplate>n</isTemplate>
      <title />
      <source>881a1630-d4e7-4c9c-aa01-7a9bbbbc47b2</source>
      <uuid>aa9bc613-8eef-4859-a9eb-4df35d8b21e4</uuid>
      <isHarvested>n</isHarvested>
      <popularity>0</popularity>
      <rating>0</rating>
      <view>true</view>
      <notify>true</notify>
      <download>true</download>
      <dynamic>true</dynamic>
      <featured>true</featured>
      <edit>true</edit>
      <owner>true</owner>
      <ownername>admin</ownername>
      <subtemplates />
    </geonet:info>
  </Metadata>

Error response (HTTP 500 status code) is an XML document with the details of what
went wrong. An example of such a response is as follows::
 
 <error id="operation-not-allowed">
   <message>Operation not allowed</message>
   <class>OperationNotAllowedEx</class> 
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **Request must contain a UUID or an ID**, if a uuid or id parameter was not provided. Returns 500 HTTP code

- **Operation not allowed (error id:
  operation-not-allowed)**, when the user is not allowed
  to view the metadata record. Returns 500 HTTP code

.. index:: xml.user.metadata

Get user metadata (xml.user.metadata)
-------------------------------------

The **xml.user.metadata** service can be used to retrieve a metadata records according to the user profile of the authenticated user running the service:

- *Administrator* profile: return all metadata records
- *Reviewer* or *User Administrator* profile: return all metadata records with 
  groupOwner in the set of groups the user belongs to
- *Editor* profile: return all metadata records owned by the user

Requires authentication: Yes

Request
```````

- **sortBySelect** : (optional) parameter specifying sort order of metadata records returned.

Get metadata request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.user.metadata

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request/>

Response
````````

Successful response is an XML document with a response container and the user metadata records as children of that container. Each child has a **geonet:info** element which gives GeoNetwork specific metadata about the metadata record. An example response (with some content removed for brevity) is as follows::
 

 <response>
   <!-- metadata record 1 -->
   <gmd:MD_Metadata ....> 
   </gmd:MD_Metadata>
   <!-- metadata record 2 -->
   <gmd:MD_Metadata ....>
   </gmd:MD_Metadata>
 </response>

Error response is an XML document with error container and the details of the error. Example::
 
 <error id="service-not-allowed">
 	 <message>Service not allowed</message>
   ....
   <object>xml.user.metadata</object>
   <request>
     <language>eng</language>
     <service>xml.user.metadata</service>
   </request>
 </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **Service not allowed (error id: service-not-allowed)**, user isn't allowed to 
  run this service. Returned 500 HTTP code.

- **Unauthorized user attempted to list editable metadata (error id:
  operation-not-allowed)**, when the user is not allowed
  to list metadata records. Returned 500 HTTP code

.. index:: rss.search

RSS Search: Search metadata and retrieve in RSS format (rss.search)
-------------------------------------------------------------------

The **rss.search** service can be used to
retrieve metadata records in RSS format, using regular search
parameters. This service can be configured in
**WEB-INF/config.xml** with the following parameters:

- **maxSummaryKeys**: Maximum number of RSS records to retrieve (default = 10)

Requires authentication: Optional. If not provided only public metadata records are retrieved

Request
```````

Parameters:

- **georss**: valid values are simple,
  simplepoint and default. See also http://georss.org

  - **simple**: Bounding box in georss
    simple format
  - **simplepoint**: Bounding box in
    georss simplepoint format
  - **default**: Bounding box in georss
    GML format

- **eastBL, southBL, northBL, westBL**:
  Bounding box to restrict the search****

- **relation**: Bounding box criteria.
  Values: equal, overlaps (default), encloses, fullyOutsideOf,
  intersection, crosses, touches, within

- **any**: Text to search in a free text search

- **title**: Metadata title

- **abstract**: Metadata abstract

- themeKey: Metadata keywords. To search for several use a value like "Global" or "watersheds"

- **dynamic**: Map type. Values: off (default), on

- **download**: Map type. Values: off (default), on

- **digital**: Map type. Values: off (default), on

- **paper**: Map type. Values: off (default), on

- **group**: Filter metadata by group, if missing search in all groups

- **attrset**:

- **dateFrom**: Filter metadata created after specified date

- **dateTo**: Filter metadata created before specified date

- **category**: Metadata category. If not specified, search all categories

RSS search request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/rss.search

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <georss>simplepoint</georss>
    <any>africa</any>
    <eastBL>74.91574</eastBL>
    <southBL>29.40611</southBL>
    <northBL>38.47198</northBL>
    <westBL>60.50417</westBL>
    <relation>overlaps</relation>
    <sortBy>relevance</sortBy>
    <attrset>geo</attrset>
  </request>

Response
````````

The principal fields of the response are:

- **channel**: This is the container for
  the RSS response

  - **title**: RSS channel title
  - **description**: RSS channel description
  - **item**: Metadata RSS item (one item for each metadata
    retrieved)

    - **title**: Metadata title
    - **link**: Link to show metadata page. Additional link
      elements (with rel="alternate") to OGC WXS services,
      shapefile/images files, Google KML, etc. can be returned
      depending on metadata
    - **description**: Metadata description
    - **pubDate**: Metadata publication date
    - **media**: Metadata thumbnails
    - **georss:point**: Bounding box in georss simplepoint
      format

RSS search response example::

  Mimetype:
  application/rss+xml

  Response:
  <?xml version="1.0" encoding="UTF-8"?>
  <rss xmlns:media="http://search.yahoo.com/mrss/" xmlns:georss="http://www.georss.org/georss" xmlns:gml="http://www.opengis.net/gml" version="2.0">
    <channel>
      <title>GeoNetwork opensource portal to spatial data and information</title>
      <link>http://localhost:8080/geonetwork</link>
      <description>GeoNetwork opensource provides Internet access to interactive maps, satellite imagery and related spatial databases ... </description>
      <language>en</language>
      <copyright>All rights reserved. Your generic copyright statement </copyright>
      <category>Geographic metadata catalog</category>
      <generator>GeoNetwork opensource</generator>
      <ttl>30</ttl>
      <item>
        <title>Hydrological Basins in Africa (Sample record, please remove!)</title>
        <link>http://localhost:8080/geonetwork?uuid=5df54bf0-3a7d-44bf-9abf-84d772da8df1</link>
        <link href="http://geonetwork3.fao.org/ows/296?SERVICE=wms$amp;VERSION=1.1.1&REQUEST=GetMap&BBOX=-17.3,-34.6,51.1,38.2&LAYERS=hydrological_basins&SRS=EPSG:4326&WIDTH=200&HEIGHT=213&FORMAT=image/png&TRANSPARENT=TRUE&STYLES=default" type="image/png" rel="alternate" title="Hydrological basins in Africa"/>
        <link href="http://localhost:8080/geonetwork/srv/eng/google.kml?uuid=5df54bf0-3a7d-44bf-9abf-84d772da8df1&layers=hydrological_basins" type="application/vnd.google-earth.kml+xml" rel="alternate" title="Hydrological basins in Africa"/>
        <category>Geographic metadata catalog</category>
        <description><![CDATA[ ... ]]></description>
        <pubDate>06 Nov 2007 12:13:00 EST</pubDate>
        <guid>http://localhost:8080/geonetwork?uuid=5df54bf0-3a7d-44bf-9abf-84d772da8df1</guid>
        <media:content url="/geonetwork/srv/eng/resources.get?id=11&fname=thumbnail_s.gif&access=public" type="image/gif" width="100"/>
        <media:text>Major hydrological basins and their sub-basins ...</media:text>
        <!--Bounding box in georss simplepoint format (default) (http://georss.org)-->
        <georss:point>16.9 1.8</georss:point>
        </item>
    </channel>
  </rss>

.. index:: rss.latest

RSS latest: Get latest updated metadata (rss.latest)
----------------------------------------------------

The **rss.latest** service can be used to retrieve the most recently modified metadata records in RSS format. This service can be configured in **WEB-INF/config.xml** file with the following parameters:

- **maxItems**: Maximum number of RSS records to retrieve (default = 20)

- **timeBetweenUpdates**: Minimum time (in seconds) between queries for latest updated metadata. If a request is received less than timeBetweenUpdates seconds after the last request, it will receive the same response.

Requires authentication: Optional. If not provided only public metadata records are retrieved

Request
```````

Parameters:

- **georss**: valid values are simple, simplepoint and default. See also http://georss.org

  - **simple**: Bounding box in georss simple format
  - **simplepoint**: Bounding box in georss simplepoint format
  - **default**: Bounding box in georss GML format

RSS latest request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/rss.latest

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <georss>default</georss>
    <maxItems>1</maxItems>
  </request>

Response
````````

The following are the principal fields of the response:

- **channel**: This is the container for the RSS response

  - **title**: RSS channel title
  - **description**: RSS channel description
  - **item**: Metadata RSS item (one item for each metadata
    retrieved)

    - **title**: Metadata title
    - **link**: Link to show metadata page. Additional link
      elements (with rel="alternate") to OGC WXS services,
      shapefile/images files, Google KML, etc. can be returned
      depending on metadata
    - **description**: Metadata description
    - **pubDate**: Metadata publication date
    - **media**: Metadata thumbnails
    - **georss:where**: Bounding box with the metadata
      extent

RSS latest response example::

  Mimetype:
  application/rss+xml

  Response:
  <?xml version="1.0" encoding="UTF-8"?>
  <rss xmlns:media="http://search.yahoo.com/mrss/" xmlns:georss="http://www.georss.org/georss"
    xmlns:gml="http://www.opengis.net/gml" version="2.0">
  <channel>
    <title>GeoNetwork opensource portal to spatial data and information</title>
    <link>http://localhost:8080/geonetwork</link>
    <description>GeoNetwork opensource provides Internet access to interactive maps,
    satellite imagery and related spatial databases ... </description>
    <language>en</language>
    <copyright>All rights reserved. Your generic copyright statement </copyright>
    <category>Geographic metadata catalog</category>
    <generator>GeoNetwork opensource</generator>
    <ttl>30</ttl>
    <item>
      <title>Hydrological Basins in Africa (Sample record, please remove!)</title>
      <link>http://localhost:8080/geonetwork?uuid=5df54bf0-3a7d-44bf-9abf-84d772da8df1</link>
      <link href="http://geonetwork3.fao.org/ows/296?SERVICE=wms$amp;VERSION=1.1.1&REQUEST=GetMap
        &BBOX=-17.3,-34.6,51.1,38.2&LAYERS=hydrological_basins&SRS=EPSG:4326&WIDTH=200
        &HEIGHT=213&FORMAT=image/png&TRANSPARENT=TRUE&STYLES=default" type="image/png"
        rel="alternate" title="Hydrological basins in Africa"/>
      <link href="http://localhost:8080/geonetwork/srv/eng/google.kml?
        uuid=5df54bf0-3a7d-44bf-9abf-84d772da8df1&layers=hydrological_basins"
        type="application/vnd.google-earth.kml+xml"
        rel="alternate" title="Hydrological basins in Africa"/>
      <category>Geographic metadata catalog</category>
      <description><![CDATA[ ... ]]></description>
      <pubDate>06 Nov 2007 12:13:00 EST</pubDate>
      <guid>http://localhost:8080/geonetwork?uuid=5df54bf0-3a7d-44bf-9abf-84d772da8df1</guid>
      <media:content url="/geonetwork/srv/eng/resources.get?id=11&fname=thumbnail_s.gif
        &access=public" type="image/gif" width="100"/>
        <media:text>Major hydrological basins and their sub-basins ...</media:text>
     <!--Bounding box in georss GML format (http://georss.org)-->
     <georss:where>
       <gml:Envelope>
         <gml:lowerCorner>-34.6 -17.3</gml:lowerCorner>
         <gml:upperCorner>38.2 51.1</gml:upperCorner>
       </gml:Envelope>
     </georss:where>
    </item>
  </channel>
  </rss>
