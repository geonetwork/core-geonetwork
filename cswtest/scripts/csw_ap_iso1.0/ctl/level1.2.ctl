<?xml version="1.0" encoding="UTF-8"?>
<package
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns="http://www.occamlab.com/ctl"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:parsers="http://www.occamlab.com/te/parsers"
   xmlns:p="http://teamengine.sourceforge.net/parsers"
   xmlns:saxon="http://saxon.sf.net/"
   xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:csw2="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:iso19115="http://schemas.opengis.net/iso19115full"
   xmlns:ows="http://www.opengis.net/ows"
   xmlns:ogc="http://www.opengis.net/ogc"
   xmlns:gmd="http://www.isotc211.org/2005/gmd"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:dct="http://purl.org/dc/terms/"
   xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
   xmlns:xlink="http://www.w3.org/1999/xlink"
   xmlns:xi="http://www.w3.org/2001/XInclude">

  <test name="csw:level1.2">
    <param name="csw.GetCapabilities.document"/>
    <assertion>Run tests for level 1.2 compliance.</assertion>
    <code>

      <xsl:variable name="csw.GetCapabilities.get.url">
        <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
      </xsl:variable>

      <xsl:variable name="csw.GetRecordById.get.url">
        <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
      </xsl:variable>

      <xsl:variable name="csw.GetRecords.post.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.GetRecords.soap.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.GetRecordById.post.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.GetRecordById.soap.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.DescribeRecord.post.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.DescribeRecord.soap.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <call-test name="csw:CorrectRequestResponse.GetCapabilities-CapabilitiesDocument">
        <with-param name="csw.GetCapabilities.get.url" select="$csw.GetCapabilities.get.url"/>
      </call-test>

      <call-test name="csw:CorrectRequestResponse.GetRecords-ValidResponseStructures">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectRequestResponse.GetRecords-ValidFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
        <with-param name="csw.GetRecordById.soap.url" select="$csw.GetRecordById.soap.url"/>
      </call-test>

      <call-test name="csw:InterfaceBindings.DescribeRecord-ValidResponseStructure">
        <with-param name="csw.DescribeRecord.soap.url" select="$csw.DescribeRecord.soap.url"/>
      </call-test>

    </code>
  </test>

  <test name="csw:CorrectRequestResponse.GetCapabilities-CapabilitiesDocument">
    <param name="csw.GetCapabilities.get.url"/>
    <assertion>
      The response to a GetCapabilities request (HTTP/GET request where KVPs  must be
      defined as follows: service = &quot;CSW&quot;, request = &quot;GetCapabilities&quot;) must satisfy
      the applicable assertion:
      1. it contains the XML representation of a capabilities document, which can be
      validated against the XML schema defined for CSW 2.0.2 (see
      http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd)
      2. must include a Filter_Capabilities section in the service metadata to describe
      which elements of the predicate language are supported
      3. The value &quot;http://www.isotc211.org/2005/gmd&quot; must be listed for the global
      &quot;IsoProfiles&quot; constraint in the capabilities document
      (ows:OperationsMetadata/ows:Constraint/@name=&quot;IsoProfiles&quot;).
      &lt;ows:OperationsMetadata>
      .   .   .
      &lt;ows:Constraint name=&quot;IsoProfiles&quot;&gt;
      &lt;ows:Value&gt;http://www.isotc211.org/2005/gmd&lt;/ows:Value&gt;
      &lt;/ows:Constraint&gt;
      &lt;/ows:OperationsMetadata&gt;
    </assertion>
    <comment>Pass if the assertions hold.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetCapabilities.get.url"/>
          </url>
          <method>GET</method>
          <param name="service">CSW</param>
          <param name="request">GetCapabilities</param>
        </request>
      </xsl:variable>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:copy-of select="$response"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="url">http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

      <xsl:if test="not($response/csw2:Capabilities/ogc:Filter_Capabilities)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Constraint[@name='IsoProfiles']/ows:Value='http://www.isotc211.org/2005/gmd')">
        <message>FAILURE: the third assertion failed</message>
        <fail/>
      </xsl:if>

    </code>
  </test>

  <test name="csw:CorrectRequestResponse.GetRecords-ValidResponseStructures">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      The response to a GetRecords request (sent via HTTP/SOAP/POST/XML) must satisfy
      the applicable assertions:

      the XML representation is valid structured concerning the CSW 2.0.2 AP ISO 1.0
      schemas. This response can be validated in two steps:
      1. validate each MD_Metadata entry in the Namespace
      &quot;http://www.isotc211.org/2005/gmd&quot; separate with
      http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
      2. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within
      the &quot;http://www.opengis.net/cat/csw/2.0.2&quot; namespace) with
      http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords
                   xmlns="http://www.opengis.net/cat/csw/2.0.2"
                   xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                   xmlns:gmd="http://www.isotc211.org/2005/gmd"
                   outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd"
                   resultType="results" service="CSW" version="2.0.2" startPosition="1"
                   maxRecords="2">
                  <csw:Query typeNames="gmd:MD_Metadata">
                    <csw:ElementSetName>summary</csw:ElementSetName>
                  </csw:Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="url">http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="url">http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <xsl:template match="csw2:GetRecordByIdResponse | csw2:GetRecordsResponse">
    <xsl:copy>
      <xsl:copy-of select="csw2:RequestId | csw2:SearchStatus" />
      <xsl:for-each select="csw2:SearchResults">
        <xsl:copy>
          <xsl:copy-of select="@*" />
        </xsl:copy>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>

  <test name="csw:CorrectRequestResponse.GetRecords-ValidFilter">
    <param name="csw.GetRecords.soap.url"/>
    <param name="csw.GetRecordById.soap.url"/>
    <assertion>
      The GetRecords request with a filter statement must satisfy the applicable
      assertions:
      1. the filter request is understood by the server and no exception concerning the
      request is thrown
      2. the response includes one metadata entry
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2"
                            xmlns:ogc="http://www.opengis.net/ogc"
                            xmlns:gmd="http://www.isotc211.org/2005/gmd"
                            xmlns:iso="http://www.opengis.net/cat/csw/apiso/1.0"
                            xmlns:ows="http://www.opengis.net/ows"
                            xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                            xmlns:dc="http://purl.org/dc/elements/1.1/"
                            xmlns:dct="http://purl.org/dc/terms/"
                            xmlns:gml="http://www.opengis.net/gml"
                            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="http://www.opengis.net/cat/csw/2.0.2
                                                http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd" service="CSW" version="2.0.2"
                            resultType="results" outputFormat="application/xml"
                            outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1"
                            maxRecords="5">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">full</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:Or>
                          <ogc:And>
                            <ogc:PropertyIsLike escapeChar="\" singleChar="?" wildCard="*">
                              <ogc:PropertyName>iso:title</ogc:PropertyName>
                              <ogc:Literal>*water*</ogc:Literal>
                            </ogc:PropertyIsLike>
                            <ogc:PropertyIsEqualTo>
                              <ogc:PropertyName>iso:type</ogc:PropertyName>
                              <ogc:Literal>dataset</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThanOrEqualTo>
                              <ogc:PropertyName>iso:RevisionDate</ogc:PropertyName>
                              <ogc:Literal>2006-06-09</ogc:Literal>
                            </ogc:PropertyIsGreaterThanOrEqualTo>
                            <ogc:BBOX>
                              <ogc:PropertyName>iso:BoundingBox</ogc:PropertyName>
                              <gml:Envelope>
                                <gml:lowerCorner>8 49</gml:lowerCorner>
                                <gml:upperCorner>14 51</gml:upperCorner>
                              </gml:Envelope>
                            </ogc:BBOX>
                          </ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>iso:type</ogc:PropertyName>
                            <ogc:Literal>application</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                        </ogc:Or>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/csw2:GetRecordsResponse/csw2:SearchResults[@numberOfRecordsMatched='0'])">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <call-test name="csw:CorrectRequestResponse.GetRecordById-ValidResponseStructures">
        <with-param name="csw.GetRecordById.soap.url"
                    select="$csw.GetRecordById.soap.url"/>
        <with-param name="the.id" select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse/iso19115:fileIdentifier" />
      </call-test>

    </code>
  </test>

  <test name="csw:CorrectRequestResponse.GetRecordById-ValidResponseStructures">
    <param name="csw.GetRecordById.soap.url"/>
    <param name="the.id" />
    <assertion>
      The response to a GetRecordById request (sent via HTTP/SOAP/POST/XML)  must
      satisfy the applicable assertions:

      the XML representation is valid structured concerning the CSW 2.0.2 AP ISO 1.0
      schemas. This response can be validated in two steps:
      1. validate the MD_Metadata entry in the Namespace
      &quot;http://www.isotc211.org/2005/gmd&quot; separate with
      http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
      2. validate the CSW 2.0.2 response frame (the GetRecordByIdResponse element
      within the &quot;http://www.opengis.net/cat/csw/2.0.2&quot; namespace) with
      http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecordById request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecordById.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecordById xmlns="http://www.opengis.net/cat/csw/2.0.2" service="CSW"
                               version="2.0.2" outputSchema="http://www.isotc211.org/2005/gmd">
                  <Id><xsl:value-of select="$the.id" /></Id>
                </GetRecordById>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:copy-of select="$response//gmd:MD_Metadata"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="url">http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordByIdResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="url">http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <test name="csw:InterfaceBindings.DescribeRecord-ValidResponseStructure">
    <param name="csw.DescribeRecord.soap.url"/>
    <assertion>
      The response to a DescribeRecord request must satisfy the applicable assertions:
      1. the XML record can be validated against the CSW 2.0.2 AP ISO 1.0 schemas (see
      http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd)
      2. the XML record must include at a minimum one &quot;SchemaComponent&quot; element:
         1. The first element contains the schema for the ISO19115/19139 data
      identification definition (Namespace: http://www.isotc211.org/2005/gmd):
      &lt;csw:SchemaComponent parentSchema="gmd.xsd"
                           schemaLanguage="http://www.w3.org/XML/Schema"
                           targetNamespace="http://www.isotc211.org/2005/gmd"&gt;
        &lt;xs:schema elementFormDefault="qualified"
                   targetNamespace="http://www.isotc211.org/2005/gmd"
                   version="0.1" xmlns:gco="http://www.isotc211.org/2005/gco"
                   xmlns:gmd="http://www.isotc211.org/2005/gmd"
                   xmlns:mgd="http://www.mis.hessen.de/2007/extendedISO"
                   xmlns:xlink="http://www.w3.org/1999/xlink"
                   xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;
          ...
<!--           2. The second element contains the schema for the ISO19119 service identification -->
<!--           definition (Namespace: http://www.isotc211.org/2005/srv): -->
<!--           &lt;csw:SchemaComponent parentSchema="gmd.xsd" -->
<!--                                schemaLanguage="http://www.w3.org/XML/Schema" -->
<!--                                targetNamespace="http://www.isotc211.org/2005/gmd"&gt; -->
<!--             &lt;xs:schema elementFormDefault="qualified" -->
<!--                        targetNamespace="http://www.isotc211.org/2005/srv" -->
<!--                        version="0.1" xmlns:gco="http://www.isotc211.org/2005/gco" -->
<!--                        xmlns:gmd="http://www.isotc211.org/2005/gmd" -->
<!--                        xmlns:srv="http://www.isotc211.org/2005/srv" -->
<!--                        xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt; -->
<!--               ... -->
    </assertion>
    <comment>Pass if the response of the DescribeRecord request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions:</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.DescribeRecord.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <DescribeRecord xmlns="http://www.opengis.net/cat/csw/2.0.2"
                                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                                schemaLanguage="http://www.w3.org/XML/Schema" service="CSW" version="2.0.2">
                </DescribeRecord>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:copy-of select="$response/soap:Envelope/soap:Body/csw2:DescribeRecordResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="url">http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

      <xsl:if test="not($response/soap:Envelope/soap:Body/csw2:DescribeRecordResponse/csw2:SchemaComponent/xs:schema[@targetNamespace='http://www.isotc211.org/2005/gmd'])">
        <message>FAILURE: assertion 2.1. does not hold</message>
        <fail/>
      </xsl:if>

<!--       <xsl:if test="not($response/soap:Envelope/soap:Body/csw2:DescribeRecordResponse/csw2:SchemaComponent/xs:schema[@targetNamespace='http://www.isotc211.org/2005/srv'])"> -->
<!--         <message>FAILURE: assertion 2.2. does not hold</message> -->
<!--         <fail/> -->
<!--       </xsl:if> -->

    </code>
  </test>

</package>
