.. _csw_services:
 
CSW service
===========

GeoNetwork opensource catalog publishes metadata using the OGC CSW (Catalog Services for the Web) protocol supporting HTTP binding to invoke the operations.

The protocol operations are described in the document **OpenGIS® Catalogue Services Specification**:

**http://portal.opengeospatial.org/files/?artifact_id=20555**

GeoNetwork is compliant with the 2.0.2 version of the specification, supporting the following CSW operations:

- :ref:`GetCapabilities`

- :ref:`DescribeRecord`

- :ref:`GetRecordById`

- :ref:`GetRecords`

- :ref:`Harvest`

- :ref:`Transaction`

This chapter briefly describes the different operations
supported in GeoNetwork and gives some usage examples. To get a complete
reference of the operations and parameters of each CSW operation refer
to the document **OpenGIS® Catalogue Services Specification**.

The invocation of the operations from a Java client is analogous
as described in the chapter for XML services.

CSW operations
--------------

The CSW operations are divided in 2 types: Discovery and Publication. The Discovery
operations are used to query the server about its capacities and to search and retrieve metadata from it. The Publication
opertions (Harvest and Transaction) are used to insert metadata into the catalog.

The CSW operations can be accesed using POST, GET methods and SOAP encoding.

The GeoNetwork opensource catalog CSW Discovery service operations are accessible through the url:

**http://localhost:8080/geonetwork/srv/eng/csw**

.. _GetCapabilities:

GetCapabilities
```````````````

**GetCapabilities** operation allows CSW clients to retrieve service metadata from a server. The response to a **GetCapabilities** request is an XML document containing service metadata about the server.

Request examples
^^^^^^^^^^^^^^^^

GET request::

  http://localhost:8080/geonetwork/srv/eng/csw?request=GetCapabilities&service=CSW&acceptVersions=2.0.2&acceptFormats=application%2Fxml

POST request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw

  Content-type:
  application/xml

  Post data:
  <?xml version="1.0" encoding="UTF-8"?>
  <csw:GetCapabilities xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW">
  <ows:AcceptVersions xmlns:ows="http://www.opengis.net/ows">
  <ows:Version>2.0.2</ows:Version>
  </ows:AcceptVersions>
  <ows:AcceptFormats xmlns:ows="http://www.opengis.net/ows">
  <ows:OutputFormat>application/xml</ows:OutputFormat>
  </ows:AcceptFormats>
  </csw:GetCapabilities>

SOAP request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw

  Content-type:
  application/soap+xml

  Post data:
  <?xml version="1.0" encoding="UTF-8"?>
  <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">
  <env:Body>
  <csw:GetCapabilities xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
  service="CSW">
  <ows:AcceptVersions xmlns:ows="http://www.opengis.net/ows">
  <ows:Version>2.0.2</ows:Version>
  </ows:AcceptVersions>
  <ows:AcceptFormats xmlns:ows="http://www.opengis.net/ows">
  <ows:OutputFormat>application/xml</ows:OutputFormat>
  </ows:AcceptFormats>
  </csw:GetCapabilities>
  </env:Body>
  </env:Envelope>

.. _DescribeRecord:

DescribeRecord
``````````````

**DescribeRecord** operation allows a client to
discover elements of the information model supported by the target
catalogue service. The operation allows some or all of the information
model to be described.

Request examples
^^^^^^^^^^^^^^^^

GET request::

  http://localhost:8080/geonetwork/srv/eng/csw?request=DescribeRecord&service=CSW&version=2.0.2&outputFormat=application%2Fxml&schemaLanguage=http%3A%2F%2Fwww.w3.org%2FXML%2FSchema&namespace=csw%3Ahttp%3A%2F%2Fwww.opengis.net%2Fcat%2Fcsw%2F2.0.2

POST request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw

  Content-type:
  application/xml

  Post data:
  <?xml version="1.0" encoding="UTF-8"?>
  <csw:DescribeRecord xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW" version="2.0.2" outputFormat="application/xml" schemaLanguage="http://www.w3.org/XML/Schema" />

SOAP request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw

  Content-type:
  application/soap+xml

  Post data:
  <?xml version="1.0" encoding="UTF-8"?>
  <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">
    <env:Body>
      <csw:DescribeRecord xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW" version="2.0.2" outputFormat="application/xml" schemaLanguage="http://www.w3.org/XML/Schema" />
    </env:Body>
  </env:Envelope>

.. _GetRecordById:

GetRecordById
`````````````

**GetRecordById** request retrieves the default representation of catalogue metadata records using their identifier.

To retrieve non public metadata a previous **xml.user.login** service invocation is required. See :ref:`login service <xml.user.login>`.

Request examples
^^^^^^^^^^^^^^^^

GET request::

  http://localhost:8080/geonetwork/srv/eng/csw?request=GetRecordById&service=CSW&version=2.0.2&elementSetName=full&id=5df54bf0-3a7d-44bf-9abf-84d772da8df1

POST request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw

  Content-type:
  application/xml

  Post data:
  <?xml version="1.0" encoding="UTF-8"?>
    <csw:GetRecordById xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW" version="2.0.2">
    <csw:Id>5df54bf0-3a7d-44bf-9abf-84d772da8df1</csw:Id>
    <csw:ElementSetName>full</csw:ElementSetName>
  </csw:GetRecordById>

SOAP request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw

  Content-type:
  application/soap+xml

  Post data:
  <?xml version="1.0" encoding="UTF-8"?>
  <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">
    <env:Body>
      <csw:GetRecordById xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW" version="2.0.2">
        <csw:Id>5df54bf0-3a7d-44bf-9abf-84d772da8df1</csw:Id>
        <csw:ElementSetName>full</csw:ElementSetName>
      </csw:GetRecordById>
    </env:Body>
  </env:Envelope>

.. _GetRecords:

GetRecords
``````````

GetRecords request allows to query the catalogue metadata records specifying a query in OCG Filter or CQL languages.

To retrieve non public metadata a previous **xml.user.login** service invocation is required. See :ref:`login service <xml.user.login>`.

Request examples
^^^^^^^^^^^^^^^^

GET request (using CQL language)::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw?request=GetRecords&service=CSW&version=2.0.2&namespace=xmlns%28csw%3Dhttp%3A%2F%2Fwww.opengis.net%2Fcat%2Fcsw%2F2.0.2%29%2Cxmlns%28gmd%3Dhttp%3A%2F%2Fwww.isotc211.org%2F2005%2Fgmd%29&constraint=AnyText+like+%25africa%25&constraintLanguage=CQL_TEXT&constraint_language_version=1.1.0&typeNames=csw%3ARecord

POST request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw

  Content-type:
  application/xml

  Post data:
  <?xml version="1.0" encoding="UTF-8"?>
  <csw:GetRecords xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW" version="2.0.2">
    <csw:Query typeNames="csw:Record">
      <csw:Constraint version="1.1.0">
        <Filter xmlns="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsLike wildCard="%" singleChar="_" escape="\\">
            <PropertyName>AnyText</PropertyName>
            <Literal>%africa%</Literal>
          </PropertyIsLike>
        </Filter>
      </csw:Constraint>
    </csw:Query>
  </csw:GetRecords>

SOAP request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw

  Content-type:
  application/soap+xml

  Post data:
  <?xml version="1.0" encoding="UTF-8"?>
  <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">
    <env:Body>
      <csw:GetRecords xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW" version="2.0.2">
        <csw:Query typeNames="csw:Record">
          <csw:Constraint version="1.1.0">
            <Filter xmlns="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
              <PropertyIsLike wildCard="%" singleChar="_" escape="\\">
                <PropertyName>AnyText</PropertyName>
                <Literal>%africa%</Literal>
              </PropertyIsLike>
            </Filter>
          </csw:Constraint>
        </csw:Query>
      </csw:GetRecords>
    </env:Body>
  </env:Envelope>


The GeoNetwork opensource catalog CSW Publication service operations are accessible through the url:

**http://localhost:8080/geonetwork/srv/eng/csw-publication**

.. _Harvest:

Harvest
```````

The **Harvest** operation defines an interface for indirectly creating, modifying and deleting catalogue records by invoking
a CSW client harvesting run from the server to a specified target. It can be run in either synchronous or asynchronous mode
and the harvesting run can be executed just once or periodically. This operation requires user authentification to be invoked.

Synchronous one-run Harvest example
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

POST request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw-publication

  Content-type:
  application/xml

  Post data:
    <?xml version="1.0" encoding="UTF-8"?>
    <csw:Harvest xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:gmd="http://www.isotc211.org/2005/gmd" service="CSW" version="2.0.2">
        <csw:Source>http://[ URL to the target CSW server ]?request=GetCapabilities&amp;service=CSW&amp;version=2.0.2</csw:Source>
        <csw:ResourceType>http://www.isotc211.org/schemas/2005/gmd/</csw:ResourceType>
    </csw:Harvest>

GET request::

  Url:
    http://localhost:8080/geonetwork/srv/eng/csw-publication?request=Harvest&service=CSW&version=2.0.2&Source=http://[ URL to the target CSW server ]&ResourceType=http://www.isotc211.org/schemas/2005/gmd/

Response::

    <?xml version="1.0" encoding="UTF-8"?>
    <csw:HarvestResponse xmlns:csw="http://www.opengis.net/cat/csw/2.0.2">
        <csw:TransactionResponse>
            <csw:TransactionSummary>
                <csw:totalInserted>22</csw:totalInserted>
                <csw:totalUpdated>0</csw:totalUpdated>
                <csw:totalDeleted>0</csw:totalDeleted>
            </csw:TransactionSummary>
        </csw:TransactionResponse>
    </csw:HarvestResponse>


Aynchronous one-run Harvest example
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

POST request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw-publication

  Content-type:
  application/xml

  Post data:
    <?xml version="1.0" encoding="UTF-8"?>
    <csw:Harvest xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:gmd="http://www.isotc211.org/2005/gmd" service="CSW" version="2.0.2">
        <csw:Source>http://[ URL to the target CSW server ]?request=GetCapabilities&amp;service=CSW&amp;version=2.0.2</csw:Source>
        <csw:ResourceType>http://www.isotc211.org/schemas/2005/gmd/</csw:ResourceType>
        <csw:ResponseHandler>[ URI or email address of response handler ]</csw:ResponseHandler>
    </csw:Harvest>

GET request::

  Url:
    http://localhost:8080/geonetwork/srv/eng/csw-publication?request=Harvest&service=CSW&version=2.0.2&Source=http://[ URL to the target CSW server ]&ResourceType=http://www.isotc211.org/schemas/2005/gmd/&ResponseHandler=[ URI or email address of response handler ]

Response::

    <?xml version="1.0" encoding="UTF-8"?>
    <csw:HarvestResponse xmlns:csw="http://www.opengis.net/cat/csw/2.0.2">
      <csw:Acknowledgement timeStamp="2011-12-05T15:13:59">
        <csw:EchoedRequest>
            <csw:Harvest xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:gmd="http://www.isotc211.org/2005/gmd" service="CSW" version="2.0.2">
                <csw:Source>http://[ URL to the target CSW server ]?request=GetCapabilities&amp;service=CSW&amp;version=2.0.2</csw:Source>
                <csw:ResourceType>http://www.isotc211.org/schemas/2005/gmd/</csw:ResourceType>
                <csw:ResponseHandler>[ URI or email address of response handler ]</csw:ResponseHandler>
            </csw:Harvest>
        </csw:EchoedRequest>
        <csw:RequestId>e7684bec-1fa9-4053-814f-7ae970d7a4a1</csw:RequestId>
      </csw:Acknowledgement>
    </csw:HarvestResponse>


.. _Transaction:

Transaction
```````````

The **Transaction** operation defines an interface
for creating, modifying and deleting catalogue records. This operation
requires user authentification to be invoqued.

Insert operation example
^^^^^^^^^^^^^^^^^^^^^^^^

POST request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw-publication

  Content-type:
  application/xml

  Post data:
  <?xml version="1.0" encoding="UTF-8"?>
  <csw:Transaction xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" version="2.0.2" service="CSW">
    <csw:Insert>
      <gmd:MD_Metadata xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml" ....>
      ...
      </gmd:MD_Metadata>
    </csw:Insert>
  </csw:Transaction>

Response::

  <?xml version="1.0" encoding="UTF-8"?>
  <csw:TransactionResponse xmlns:csw="http://www.opengis.net/cat/csw/2.0.2">
    <csw:TransactionSummary>
      <csw:totalInserted>1</csw:totalInserted>
      <csw:totalUpdated>0</csw:totalUpdated>
      <csw:totalDeleted>0</csw:totalDeleted>
    </csw:TransactionSummary>
  </csw:TransactionResponse>

Update operation example
^^^^^^^^^^^^^^^^^^^^^^^^

POST request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw-publication

  Content-type:
  application/xml

  Post data:
  <?xml version="1.0" encoding="UTF-8"?>
  <csw:Transaction xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" version="2.0.2" service="CSW">
    <csw:Update>
      <gmd:MD_Metadata xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml" ....>
      ...
      </gmd:MD_Metadata>
      <csw:Constraint version="1.1.0">
        <ogc:Filter>
          <ogc:PropertyIsEqualTo>
            <ogc:PropertyName>title</ogc:PropertyName>
            <ogc:Literal>Eurasia</ogc:Literal>
          </ogc:PropertyIsEqualTo>
        </ogc:Filter>
      </csw:Constraint>
    </csw:Update>
  </csw:Transaction>

Response::

  <?xml version="1.0" encoding="UTF-8"?>
  <csw:TransactionResponse xmlns:csw="http://www.opengis.net/cat/csw/2.0.2">
    <csw:TransactionSummary>
      <csw:totalInserted>0</csw:totalInserted>
      <csw:totalUpdated>1</csw:totalUpdated>
      <csw:totalDeleted>0</csw:totalDeleted>
    </csw:TransactionSummary>
  </csw:TransactionResponse>

Delete operation example
^^^^^^^^^^^^^^^^^^^^^^^^

POST request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/csw-publication

  Content-type:
  application/xml

  Post data:
  <?xml version="1.0" encoding="UTF-8"?>
  <csw:Transaction xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" version="2.0.2" service="CSW">
    <csw:Delete>
      <csw:Constraint version="1.1.0">
        <ogc:Filter>
          <ogc:PropertyIsEqualTo>
            <ogc:PropertyName>title</ogc:PropertyName>
            <ogc:Literal>africa</ogc:Literal>
          </ogc:PropertyIsEqualTo>
        </ogc:Filter>
      </csw:Constraint>
    </csw:Delete>
  </csw:Transaction>

Response::

  <?xml version="1.0" encoding="UTF-8"?>
  <csw:TransactionResponse xmlns:csw="http://www.opengis.net/cat/csw/2.0.2">
    <csw:TransactionSummary>
      <csw:totalInserted>0</csw:totalInserted>
      <csw:totalUpdated>0</csw:totalUpdated>
      <csw:totalDeleted>1</csw:totalDeleted>
    </csw:TransactionSummary>
  </csw:TransactionResponse>

Errors
^^^^^^

- User is not authenticated::

    <?xml version="1.0" encoding="UTF-8"?>
    <ows:ExceptionReport xmlns:ows="http://www.opengis.net/ows" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0.0" xsi:schemaLocation=  "http://www.opengis.net/ows http://schemas.opengis.net/ows/1.0.0/owsExceptionReport.xsd">
      <ows:Exception exceptionCode="NoApplicableCode">
        <ows:ExceptionText>Cannot process transaction: User not authenticated.</ows:ExceptionText>
      </ows:Exception>
    </ows:ExceptionReport>


