<?xml version="1.0" encoding="UTF-8"?>
<package
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns="http://www.occamlab.com/ctl"
   xmlns:parsers="http://www.occamlab.com/te/parsers"
   xmlns:p="http://teamengine.sourceforge.net/parsers"
   xmlns:saxon="http://saxon.sf.net/"
   xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:csw2="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:ows="http://www.opengis.net/ows"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:dct="http://purl.org/dc/terms/"
   xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
   xmlns:xlink="http://www.w3.org/1999/xlink"
   xmlns:xi="http://www.w3.org/2001/XInclude">

  <test name="csw:level1.1">
    <param name="csw.GetCapabilities.document"/>
    <assertion>Run tests for level 1.1 compliance.</assertion>
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

      <call-test name="csw:InterfaceBindings.GetCapabilities-GetMethod">
        <with-param name="csw.GetCapabilities.get.url" select="$csw.GetCapabilities.get.url"/>
      </call-test>

      <call-test name="csw:InterfaceBindings.GetRecords-SOAPPOSTMethod">
        <with-param name="csw.GetRecords.post.url" select="$csw.GetRecords.post.url"/>
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:InterfaceBindings.GetRecordById-SOAPMethod">
        <with-param name="csw.GetRecordById.soap.url" select="$csw.GetRecordById.soap.url"/>
      </call-test>

      <call-test name="csw:InterfaceBindings.GetRecordById-GETMethod">
        <with-param name="csw.GetRecordById.get.url" select="$csw.GetRecordById.get.url"/>
      </call-test>

      <call-test name="csw:InterfaceBindings.DescribeRecord-SOAPPOSTMethod">
        <with-param name="csw.DescribeRecord.post.url" select="$csw.DescribeRecord.post.url"/>
        <with-param name="csw.DescribeRecord.soap.url" select="$csw.DescribeRecord.soap.url"/>
      </call-test>

      <call-test name="csw:InterfaceBindings.GetRecords-InvalidRequest">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:InterfaceBindings.GetRecordById-InvalidVersion">
        <with-param name="csw.GetRecordById.soap.url" select="$csw.GetRecordById.soap.url"/>
      </call-test>

    </code>
  </test>

  <test name="csw:InterfaceBindings.GetCapabilities-GetMethod">
    <param name="csw.GetCapabilities.get.url"/>
    <assertion>
      Verify that the GetCapabilities operation is implemented and supports the
      HTTP/GET/KVP method binding.
    </assertion>
    <comment>Pass if the response is a well formed XML Document with a root
      node named &quot;Capabilities&quot; which is defined within the
      &quot;http://www.opengis.net/cat/csw/2.0.2&quot; namespace.</comment>
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

      <xsl:if test="not($response/csw2:Capabilities)">
        <message>FAILURE: the response has no root element
        &quot;Capabilities&quot; in the
        &quot;http://www.opengis.net/cat/csw/2.0.2&quot; namespace</message>
        <fail/>
      </xsl:if>
    </code>
  </test>

  <test name="csw:InterfaceBindings.GetRecords-SOAPPOSTMethod">
    <param name="csw.GetRecords.post.url"/>
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Verify that the GetRecords operation is implemented and supports the
      HTTP/SOAP/POST/XML as well as the HTTP/POST/XML method binding.
    </assertion>
    <comment>Pass if the responses of the requests are well formed XML Documents
      with a root node named &quot;GetRecordsResponse&quot; which is defined within the
      &quot;http://www.opengis.net/cat/csw/2.0.2&quot; namespace.</comment>
    <code>
      <xsl:variable name="responsepost">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.post.url"/>
          </url>
          <method>POST</method>
          <body>
            <GetRecords
               xmlns="http://www.opengis.net/cat/csw/2.0.2"
               xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
               xmlns:gmd="http://www.isotc211.org/2005/gmd"
               outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd"
               resultType="results" service="CSW" version="2.0.2"
               startPosition="1" maxRecords="5">
              <csw:Query typeNames="gmd:MD_Metadata">
                <csw:ElementSetName>brief</csw:ElementSetName>
              </csw:Query>
            </GetRecords>
          </body>
        </request>
      </xsl:variable>
      <xsl:variable name="responsesoap">
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
                   resultType="results" service="CSW" version="2.0.2"
                   startPosition="1" maxRecords="5">
                  <csw:Query typeNames="gmd:MD_Metadata">
                    <csw:ElementSetName>brief</csw:ElementSetName>
                  </csw:Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="not($responsepost/csw2:GetRecordsResponse)">
        <message>FAILURE: the response to the HTTP/XML/POST request has no root
        element &quot;GetRecordsResponse&quot; in the
        &quot;http://www.opengis.net/cat/csw/2.0.2&quot; namespace</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($responsesoap/soap:Envelope/soap:Body/csw2:GetRecordsResponse)">
        <message>FAILURE: the response to the HTTP/SOAP/XML/POST request has no
        root element &quot;GetRecordsResponse&quot; in the
        &quot;http://www.opengis.net/cat/csw/2.0.2&quot; namespace</message>
        <fail/>
      </xsl:if>
    </code>
  </test>

  <test name="csw:InterfaceBindings.GetRecordById-SOAPMethod">
    <param name="csw.GetRecordById.soap.url"/>
    <assertion>
      Verify that the GetRecordById request is implemented and supports the
      HTTP/SOAP/POST/XML method binding.
    </assertion>
    <comment>Pass if the response of the request is a well formed XML Document
      with a root node named &quot;GetRecordByIdResponse&quot; which is defined
      within the &quot;http://www.opengis.net/cat/csw/2.0.2&quot;
      namespace.</comment>
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
                <GetRecordById
                   xmlns="http://www.opengis.net/cat/csw/2.0.2"  service="CSW" version="2.0.2"
                   outputSchema="http://www.opengis.net/cat/csw/2.0.2">
                  <Id>UUIDxyz</Id>
                </GetRecordById>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="not($response/soap:Envelope/soap:Body/csw2:GetRecordByIdResponse)">
        <message>FAILURE: the response to the HTTP/SOAP/XML/POST request has no
        root element &quot;GetRecordByIdResponse&quot; in the
        &quot;http://www.opengis.net/cat/csw/2.0.2&quot; namespace</message>
        <fail/>
      </xsl:if>
    </code>
  </test>

  <test name="csw:InterfaceBindings.GetRecordById-GETMethod">
    <param name="csw.GetRecordById.get.url"/>
    <assertion>
      Verify that the GetRecordById operation is implemented and supports the
      HTTP/GET/KVP method binding.
    </assertion>
    <comment>Pass if the response is a well formed XML Document with a root node
      named &quot;GetRecordByIdResponse&quot; which is defined within the
      &quot;http://www.opengis.net/cat/csw/2.0.2&quot; namespace.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecordById.get.url"/>
          </url>
          <method>GET</method>
          <param name="service">CSW</param>
          <param name="request">GetRecordById</param>
          <param name="ID">UUIDxyz</param>
          <param name="version">2.0.2</param>
        </request>
      </xsl:variable>

      <xsl:if test="not($response/csw2:GetRecordByIdResponse)">
        <message>FAILURE: the response has no root element
        &quot;GetRecordByIdResponse&quot; in the
        &quot;http://www.opengis.net/cat/csw/2.0.2&quot; namespace</message>
        <fail/>
      </xsl:if>
    </code>
  </test>

  <test name="csw:InterfaceBindings.DescribeRecord-SOAPPOSTMethod">
    <param name="csw.DescribeRecord.post.url"/>
    <param name="csw.DescribeRecord.soap.url"/>
    <assertion>
      Verify that the DescribeRecord operation is implemented and supports the
      HTTP/SOAP/POST/XML as well as the HTTP/POST/XML method binding.
    </assertion>
    <comment>Pass if the responses of the requests are well formed XML Documents
      with a root node named &quot;DescribeRecordResponse&quot; which is defined
      within the &quot;http://www.opengis.net/cat/csw/2.0.2&quot;
      namespace.</comment>
    <code>
      <xsl:variable name="responsepost">
        <request>
          <url>
            <xsl:value-of select="$csw.DescribeRecord.post.url"/>
          </url>
          <method>POST</method>
          <body>
            <DescribeRecord
               xmlns="http://www.opengis.net/cat/csw/2.0.2"
               xmlns:gmd="http://www.isotc211.org/2005/gmd"
               schemaLanguage="http://www.w3.org/XML/Schema" service="CSW" version="2.0.2">
              <TypeName>gmd:MD_Metadata</TypeName>
            </DescribeRecord>
          </body>
        </request>
      </xsl:variable>
      <xsl:variable name="responsesoap">
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
                <DescribeRecord
                   xmlns="http://www.opengis.net/cat/csw/2.0.2"
                   xmlns:gmd="http://www.isotc211.org/2005/gmd"
                   schemaLanguage="http://www.w3.org/XML/Schema" service="CSW" version="2.0.2">
                  <TypeName>gmd:MD_Metadata</TypeName>
                </DescribeRecord>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="not($responsepost/csw2:DescribeRecordResponse)">
        <message>FAILURE: the response to the HTTP/XML/POST request has no root
        element &quot;DescribeRecordResponse&quot; in the
        &quot;http://www.opengis.net/cat/csw/2.0.2&quot; namespace</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($responsesoap/soap:Envelope/soap:Body/csw2:DescribeRecordResponse)">
        <message>FAILURE: the response to the HTTP/SOAP/XML/POST request has no
        root element &quot;DescribeRecordResponse&quot; in the
        &quot;http://www.opengis.net/cat/csw/2.0.2&quot; namespace</message>
        <fail/>
      </xsl:if>
    </code>
  </test>

  <test name="csw:InterfaceBindings.GetRecords-InvalidRequest">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Verify that all of the following assertions hold for the response to an invalid
      GetRecords request (missing attribute &quot;typeNames&quot; of &quot;Query&quot;
      element:
      1.the response entity is a valid exception report having &quot;&lt;ows:ExceptionReport&gt;&quot;
        as the document element;
      2.the value of the exceptionCode attribute specifies the appropriate code value.
    </assertion>
    <comment>Pass if the response of the request holds all assertions.</comment>
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
                   maxRecords="5">
                  <csw:Query>
                    <csw:ElementSetName>brief</csw:ElementSetName>
                  </csw:Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="not($response/soap:Envelope/soap:Body/soap:Fault/soap:Detail/ows:ExceptionReport/ows:Exception[@exceptionCode='MissingParameterValue'])">
        <message>FAILURE: the response to the HTTP/SOAP/XML/POST request does
        not satisfy the assertions.</message>
        <fail/>
      </xsl:if>
    </code>
  </test>

  <test name="csw:InterfaceBindings.GetRecordById-InvalidVersion">
    <param name="csw.GetRecordById.soap.url"/>
    <assertion>
      Verify that all of the following assertions hold for the response to an invalid
      version of a GetRecordById request (HTTP/POST/SOAP/XML binding):
      1.the response entity is a valid exception report having &quot;&lt;ows:ExceptionReport&gt;&quot;
        as the document element;
      2.the value of the exceptionCode attribute specifies the appropriate code value.
    </assertion>
    <comment>Pass if the response of the request holds all assertions.</comment>
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
                <GetRecordById
                   xmlns="http://www.opengis.net/cat/csw/2.0.2"  service="CSW" version="2.0.1"
                   outputSchema="http://www.opengis.net/cat/csw/2.0.2">
                  <Id>UUIDxyz</Id>
                </GetRecordById>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>
      <xsl:if test="not($response/soap:Envelope/soap:Body/soap:Fault/soap:Detail/ows:ExceptionReport/ows:Exception[@exceptionCode='InvalidParameterValue'])">
        <message>FAILURE: the response to the HTTP/SOAP/XML/POST request does
        not satisfy the assertions.</message>
        <fail/>
      </xsl:if>
    </code>
  </test>

</package>
