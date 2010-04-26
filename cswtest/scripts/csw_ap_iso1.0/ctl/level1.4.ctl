<?xml version="1.0" encoding="UTF-8"?>
<package
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns="http://www.occamlab.com/ctl"
   xmlns:parsers="http://www.occamlab.com/te/parsers"
   xmlns:p="http://teamengine.sourceforge.net/parsers"
   xmlns:saxon="http://saxon.sf.net/"
   xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:csw2="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:iso19115="http://schemas.opengis.net/iso19115full"
   xmlns:ows="http://www.opengis.net/ows"
   xmlns:ogc="http://www.opengis.net/ogc"
   xmlns:gmd="http://www.isotc211.org/2005/gmd"
   xmlns:gco="http://www.isotc211.org/2005/gco"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:dct="http://purl.org/dc/terms/"
   xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
   xmlns:xlink="http://www.w3.org/1999/xlink"
   xmlns:xi="http://www.w3.org/2001/XInclude">

  <test name="csw:level1.4">
    <param name="csw.GetCapabilities.document"/>
    <assertion>Run tests for level 1.4 compliance.</assertion>
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

      <call-test name="csw:CorrectRequestResponse.GetRecords-ServicesFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectRequestResponse.GetRecords-ServicesOpOnFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

    </code>
  </test>

  <test name="csw:CorrectRequestResponse.GetRecords-ServicesFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for WMS-service metadata records which boundingBox satisfies a specific
      spatial filter (s. query below) and which title is like “Topographische
      Karte” or which includes a keyword of value “Topographische Karte”.
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the
         request is thrown
      2. the response includes 1 ‘brief’ metadata entry returned in the
         http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW
         2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace
            “http://www.isotc211.org/2005/gmd” separate with
            http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within
            the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
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
                            xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0"
                            xmlns:iso="http://www.opengis.net/cat/csw/apiso/1.0"
                            xmlns:ows="http://www.opengis.net/ows"
                            xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                            xmlns:dc="http://purl.org/dc/elements/1.1/"
                            xmlns:dct="http://purl.org/dc/terms/"
                            xmlns:gml="http://www.opengis.net/gml"
                            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="http://www.opengis.net/cat/csw/2.0.2
                                                http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd"
                            service="CSW" version="2.0.2"
                            resultType="results" outputFormat="application/xml"
                            outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1"
                            maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">brief</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>iso:type</ogc:PropertyName>
                            <ogc:Literal>service</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>iso:ServiceType</ogc:PropertyName>
                            <ogc:Literal>WMS</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:BBOX>
                            <ogc:PropertyName>iso:BoundingBox</ogc:PropertyName>
                            <gml:Envelope>
                              <gml:lowerCorner>7.30 49.30</gml:lowerCorner>
                              <gml:upperCorner>10.70 51.70</gml:upperCorner>
                            </gml:Envelope>
                          </ogc:BBOX>
                          <ogc:Or>
                            <ogc:PropertyIsEqualTo>
                              <ogc:PropertyName>iso:subject</ogc:PropertyName>
                              <ogc:Literal>Topographische Karte</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLike escapeChar="\" singleChar="?" wildCard="*">
                              <ogc:PropertyName>apiso:title</ogc:PropertyName>
                              <ogc:Literal>*Topographische Karte*</ogc:Literal>
                            </ogc:PropertyIsLike>
                          </ogc:Or>
                        </ogc:And>
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

      <xsl:if test="not(count($response//gmd:MD_Metadata)=1)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

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

  <test name="csw:CorrectRequestResponse.GetRecords-ServicesOpOnFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for WMS- and WFS-services which operate on a specific dataset.
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the
         request is thrown
      2. the response includes 2 ‘full’ metadata entries returned in the
         http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW
         2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace
            “http://www.isotc211.org/2005/gmd” separate with
            http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within
            the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
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
                            xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0"
                            xmlns:iso="http://www.opengis.net/cat/csw/apiso/1.0"
                            xmlns:ows="http://www.opengis.net/ows"
                            xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                            xmlns:dc="http://purl.org/dc/elements/1.1/"
                            xmlns:dct="http://purl.org/dc/terms/"
                            xmlns:gml="http://www.opengis.net/gml"
                            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="http://www.opengis.net/cat/csw/2.0.2
                                                http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd"
                            service="CSW" version="2.0.2"
                            resultType="results" outputFormat="application/xml"
                            outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1"
                            maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">full</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>iso:type</ogc:PropertyName>
                            <ogc:Literal>service</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:Or>
                            <ogc:And>
                              <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>iso:ServiceType</ogc:PropertyName>
                                <ogc:Literal>WMS</ogc:Literal>
                              </ogc:PropertyIsEqualTo>
                              <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>iso:OperatesOn</ogc:PropertyName>
                                <ogc:Literal>0C12204F-5626-4A2E-94F4-514424F093A1</ogc:Literal>
                              </ogc:PropertyIsEqualTo>
                            </ogc:And>
                            <ogc:And>
                              <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>iso:ServiceType</ogc:PropertyName>
                                <ogc:Literal>WFS</ogc:Literal>
                              </ogc:PropertyIsEqualTo>
                              <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>iso:OperatesOn</ogc:PropertyName>
                                <ogc:Literal>0C12204F-5626-4A2E-94F4-514424F093A1</ogc:Literal>
                              </ogc:PropertyIsEqualTo>
                            </ogc:And>
                          </ogc:Or>
                        </ogc:And>
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

      <xsl:if test="not(count($response//gmd:MD_Metadata)=2)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

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

</package>
