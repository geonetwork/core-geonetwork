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

  <test name="csw:level1.3">
    <param name="csw.GetCapabilities.document"/>
    <assertion>Run tests for level 1.3 compliance.</assertion>
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

      <call-test name="csw:CorrectRequestResponse.GetCapabilities-CapabilitiesDocumentContent">
        <with-param name="csw.GetCapabilities.get.url" select="$csw.GetCapabilities.get.url"/>
      </call-test>

      <call-test name="csw:CorrectRequestResponse.GetRecords-AnyTextFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectRequestResponse.GetRecords-AndOrFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectRequestResponse.GetRecords-BBOXFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectRequestResponse.GetRecords-CollectionElementsFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectRequestResponse.GetRecordById-Full">
        <with-param name="csw.GetRecordById.soap.url" select="$csw.GetRecordById.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectRequestResponse.GetRecordById-GetKVPBrief">
        <with-param name="csw.GetRecordById.get.url" select="$csw.GetRecordById.get.url"/>
      </call-test>

    </code>
  </test>

  <test name="csw:CorrectRequestResponse.GetCapabilities-CapabilitiesDocumentContent">
    <param name="csw.GetCapabilities.get.url"/>
    <assertion>
      The response to a GetCapabilities request (HTTP/GET request where KVPs  must be
      defined as follows: service = &quot;CSW&quot;, request = &quot;GetCapabilities&quot;) must satisfy
      the applicable assertion:
      1. it contains the XML representation of a capabilities document, which can be
      validated against the XML schema defined for CSW 2.0.2 (see
      http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd)
      2. Certain entries must be included in the capabilities document at a minimum.
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

      <xsl:if test="not($response/csw2:Capabilities[@version='2.0.2'])">
        <message>FAILURE: the second assertion failed (1)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Constraint[@name='PostEncoding'])">
        <message>FAILURE: the second assertion failed (2)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Constraint[@name='IsoProfiles'])">
        <message>FAILURE: the second assertion failed (3)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Constraint[ows:Value='SOAP'])">
        <message>FAILURE: the second assertion failed (4)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Constraint[ows:Value='http://www.isotc211.org/2005/gmd'])">
        <message>FAILURE: the second assertion failed (5)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Operation[@name='GetRecords'])">
        <message>FAILURE: the second assertion failed (6)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Operation[@name='GetRecordById'])">
        <message>FAILURE: the second assertion failed (7)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Operation[@name='DescribeRecord'])">
        <message>FAILURE: the second assertion failed (8)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Operation/ows:Parameter[@name='typeName'])">
        <message>FAILURE: the second assertion failed (9)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Operation/ows:Parameter[@name='outputFormat'])">
        <message>FAILURE: the second assertion failed (10)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Operation/ows:Parameter[@name='outputSchema'])">
        <message>FAILURE: the second assertion failed (11)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Operation/ows:Parameter[@name='resultType'])">
        <message>FAILURE: the second assertion failed (12)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Operation/ows:Parameter[@name='ElementSetName'])">
        <message>FAILURE: the second assertion failed (13)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Operation/ows:Parameter[@name='CONSTRAINTLANGUAGE'])">
        <message>FAILURE: the second assertion failed (14)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:OperationsMetadata/ows:Operation/ows:Parameter[@name='schemaLanguage'])">
        <message>FAILURE: the second assertion failed (15)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:ServiceIdentification[ows:ServiceType='CSW'])">
        <message>FAILURE: the second assertion failed (16)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ows:ServiceIdentification[ows:ServiceTypeVersion='2.0.2'])">
        <message>FAILURE: the second assertion failed (17)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ogc:Filter_Capabilities/ogc:Scalar_Capabilities/ogc:ComparisonOperators[ogc:ComparisonOperator='Between'])">
        <message>FAILURE: the second assertion failed (18)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ogc:Filter_Capabilities/ogc:Scalar_Capabilities/ogc:ComparisonOperators[ogc:ComparisonOperator='EqualTo'])">
        <message>FAILURE: the second assertion failed (19)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ogc:Filter_Capabilities/ogc:Scalar_Capabilities/ogc:ComparisonOperators[ogc:ComparisonOperator='GreaterThan'])">
        <message>FAILURE: the second assertion failed (20)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ogc:Filter_Capabilities/ogc:Scalar_Capabilities/ogc:ComparisonOperators[ogc:ComparisonOperator='GreaterThanEqualTo'])">
        <message>FAILURE: the second assertion failed (21)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ogc:Filter_Capabilities/ogc:Scalar_Capabilities/ogc:ComparisonOperators[ogc:ComparisonOperator='LessThan'])">
        <message>FAILURE: the second assertion failed (22)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ogc:Filter_Capabilities/ogc:Scalar_Capabilities/ogc:ComparisonOperators[ogc:ComparisonOperator='LessThanEqualTo'])">
        <message>FAILURE: the second assertion failed (23)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ogc:Filter_Capabilities/ogc:Scalar_Capabilities/ogc:ComparisonOperators[ogc:ComparisonOperator='Like'])">
        <message>FAILURE: the second assertion failed (24)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ogc:Filter_Capabilities/ogc:Scalar_Capabilities/ogc:ComparisonOperators[ogc:ComparisonOperator='NotEqualTo'])">
        <message>FAILURE: the second assertion failed (25)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ogc:Filter_Capabilities/ogc:Scalar_Capabilities/ogc:ComparisonOperators[ogc:ComparisonOperator='NullCheck'])">
        <message>FAILURE: the second assertion failed (26)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/csw2:Capabilities/ogc:Filter_Capabilities/ogc:Spatial_Capabilities/ogc:SpatialOperators/ogc:SpatialOperator[@name='BBOX'])">
        <message>FAILURE: the second assertion failed (27)</message>
        <fail/>
      </xsl:if>

    </code>
  </test>

  <test name="csw:CorrectRequestResponse.GetRecords-AnyTextFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for dataset records where “TK50” is defined somewhere in the metadata document.
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the
         request is thrown
      2. the response includes 2 ‘brief’ metadata entries returned in the
         http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW
         2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace
            “http://www.isotc211.org/2005/gmd” separate with
            http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within
            the “http://www.opengis.net/cat/csw/2.0.2” namespace) with
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
                                                http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd" service="CSW" version="2.0.2"
                            resultType="results" outputFormat="application/xml"
                            outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1"
                            maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">brief</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsLike escapeChar="\" singleChar="?" wildCard="*">
                            <ogc:PropertyName>apiso:anyText</ogc:PropertyName>
                            <ogc:Literal>*TK50*</ogc:Literal>
                          </ogc:PropertyIsLike>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>iso:type</ogc:PropertyName>
                            <ogc:Literal>dataset</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
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

      <xsl:if test="not(count($response//gmd:MD_Metadata)&gt;=2)">
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

  <test name="csw:CorrectRequestResponse.GetRecords-AndOrFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for dataset metadata records including one of the keywords
      “Wasserschutzgebiet” or “Überschemmungsgebiet” and which datasets where revised
      after the 1. of June 2006.
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the
         request is thrown
      2. the response includes 1 ‘summary’ metadata entry returned in the
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
                    <ElementSetName typeNames="">summary</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>iso:type</ogc:PropertyName>
                            <ogc:Literal>dataset</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsGreaterThanOrEqualTo>
                            <ogc:PropertyName>apiso:RevisionDate</ogc:PropertyName>
                            <ogc:Literal>2006-06-01</ogc:Literal>
                          </ogc:PropertyIsGreaterThanOrEqualTo>
                          <ogc:Or>
                            <ogc:PropertyIsEqualTo>
                              <ogc:PropertyName>iso:subject</ogc:PropertyName>
                              <ogc:Literal>Wasserschutzgebiet</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                              <ogc:PropertyName>iso:subject</ogc:PropertyName>
                              <ogc:Literal>Überschwemmungsgebiet</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
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

  <test name="csw:CorrectRequestResponse.GetRecords-BBOXFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for dataset metadata records which boundingBox satisfies a specific
      spatial filter (s. query below).
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the
         request is thrown
      2. the response includes 3 ‘summary’ metadata entry returned in the
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
                    <ElementSetName typeNames="">summary</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>iso:type</ogc:PropertyName>
                            <ogc:Literal>dataset</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:BBOX>
                            <ogc:PropertyName>iso:BoundingBox</ogc:PropertyName>
                            <gml:Envelope>
                              <gml:lowerCorner>8.09 49.90</gml:lowerCorner>
                              <gml:upperCorner>8.20 50.10</gml:upperCorner>
                            </gml:Envelope>
                          </ogc:BBOX>
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

      <xsl:if test="not(count($response//gmd:MD_Metadata)=3)">
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

  <test name="csw:CorrectRequestResponse.GetRecords-CollectionElementsFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for dataset metadata records which belong to a specific dataset
      collection and which boundingBox satisfies a specific spatial filter
      (s. query below).
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the
         request is thrown
      2. the response includes 2 ‘brief’ metadata entries returned in the
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
                            <ogc:Literal>dataset</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>iso:ParentIdentifier</ogc:PropertyName>
                            <ogc:Literal>111c0076-b23f-76e5-c888-94327664111</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:Intersects>
                            <ogc:PropertyName>iso:BoundingBox</ogc:PropertyName>
                            <gml:Envelope>
                              <gml:lowerCorner>8.10 50.00</gml:lowerCorner>
                              <gml:upperCorner>8.50 51.00</gml:upperCorner>
                            </gml:Envelope>
                          </ogc:Intersects>
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

  <test name="csw:CorrectRequestResponse.GetRecordById-Full">
    <param name="csw.GetRecordById.soap.url"/>
    <assertion>
      Request a metadata records with a specific Id with ‘full’ elementSet.
      The response of the GetRecordById request must satisfy the applicable
      assertions:
      1. the request is understood by the server and no exception concerning the
         request is thrown
      2. the response includes 1 ‘full’ metadata entry returned in the
         http://www.isotc211.org/2005/gmd format
      3. The title must be ‘Bestandskarte hessischer Wasserschutzgebiete’
      4. the XML representation of the response is valid structured concerning the CSW
         2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace
            “http://www.isotc211.org/2005/gmd” separate with
            http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within
            the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
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
          <header name="SOAPAction">urn:unused</header>
          <header name="action">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecordById xmlns="http://www.opengis.net/cat/csw/2.0.2" service="CSW"
                               version="2.0.2" outputSchema="http://www.isotc211.org/2005/gmd"
                               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                               xsi:schemaLocation="http://www.opengis.net/cat/csw/2.0.2
                                                   http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd">
                  <Id>0C12204F-5626-4A2E-94F4-514424F093A1</Id>
                </GetRecordById>
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

      <xsl:if test="not(boolean($response//gmd:title/gco:CharacterString='Bestandskarte hessischer Wasserschutzgebiete'))">
        <message>FAILURE: the third assertion failed</message>
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

  <test name="csw:CorrectRequestResponse.GetRecordById-GetKVPBrief">
    <param name="csw.GetRecordById.get.url"/>
    <assertion>
      Request a metadata record with a specific Id with ‘brief’ elementSet.
      The response of the GetRecordById request must satisfy the applicable assertions:
      1. the request is understood by the server and no exception concerning the request is thrown
      2. the response includes 1 ‘brief’ metadata entry returned in the http://www.isotc211.org/2005/gmd format
      3. The title must be ‘DTK 50 - Blatt L5916-Frankfurt am Main West’
      4. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecordById request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecordById.get.url"/>
          </url>
          <method>GET</method>
          <param name="service">CSW</param>
          <param name="request">GetRecordById</param>
          <param name="elementSetName">brief</param>
          <param name="outputSchema">http://www.isotc211.org/2005/gmd</param>
          <param name="Id">486d9622-c29d-44e5-b878-44389740011</param>
          <param name="version">2.0.2</param>
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

      <xsl:if test="not(boolean($response//gmd:title/gco:CharacterString='DTK 50 - Blatt L5916-Frankfurt am Main West'))">
        <message>FAILURE: the third assertion failed</message>
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
