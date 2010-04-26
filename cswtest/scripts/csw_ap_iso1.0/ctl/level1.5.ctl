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

  <test name="csw:level1.5">
    <param name="csw.GetCapabilities.document"/>
    <assertion>Run tests for level 1.5 compliance.</assertion>
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

      <call-test name="csw:MultiLanguage.GetRecords-MultiLingual">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

    </code>
  </test>

  <test name="csw:MultiLanguage.GetRecords-MultiLingual">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      1. Search for an application with the german keyword “Überschwemmungsgebiet“.
         a. The response of the GetRecords request must satisfy the applicable assertions:
            i. the filter request is understood by the server and no exception concerning the
               request is thrown
            ii. the response includes 1  ‘full’ metadata entry returned in the
                http://www.isotc211.org/2005/gmd format
      2. Search for an application with the english keyword “flood area“.
         a. The response of the GetRecords request must satisfy the applicable assertions:
            i. the filter request is understood by the server and no exception concerning the
               request is thrown
            ii. the response includes 1  ‘full’ metadata entry returned in the
                http://www.isotc211.org/2005/gmd format
      3. The fileIdentifiers of both metadata entries must be the same.
    </assertion>
    <comment>Pass if the response of the GetRecords requests (sent via
      HTTP/SOAP/POST/XML) hold the relevant assertions.</comment>
    <code>
      <xsl:variable name="response1">
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
                            xsi:schemaLocation="http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd"
                            service="CSW" version="2.0.2" resultType="results"
                            outputFormat="application/xml"
                            outputSchema="http://www.isotc211.org/2005/gmd"
                            startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">full</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:Type</ogc:PropertyName>
                            <ogc:Literal>application</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:Subject</ogc:PropertyName>
                            <ogc:Literal>Überschwemmungsgebiet</ogc:Literal>
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

      <xsl:variable name="response2">
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
                            xsi:schemaLocation="http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd"
                            service="CSW" version="2.0.2" resultType="results"
                            outputFormat="application/xml"
                            outputSchema="http://www.isotc211.org/2005/gmd"
                            startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">full</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>application</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:subject</ogc:PropertyName>
                            <ogc:Literal>flood area</ogc:Literal>
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

      <xsl:if test="boolean($response1/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the assertion (1. a. i) failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response1//gmd:MD_Metadata)=1)">
        <message>FAILURE: the assertion (1. a. ii) failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="boolean($response2/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the assertion (2. a. i) failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response2//gmd:MD_Metadata)=1)">
        <message>FAILURE: the assertion (2. a. ii) failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response1//gmd:fileIdentifier/gco:CharacterString = $response2//gmd:fileIdentifier/gco:CharacterString)">
        <message>FAILURE: the assertion 3. failed.
          Values were:
          <xsl:value-of select="$response1//gmd:fileIdentifier/gco:CharacterString" />
          <xsl:value-of select="$response2//gmd:fileIdentifier/gco:CharacterString" />
        </message>
       <fail/>
      </xsl:if>

    </code>
  </test>

</package>
