<?xml version="1.0" encoding="UTF-8"?>
<package
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns="http://www.occamlab.com/ctl"
   xmlns:parsers="http://www.occamlab.com/te/parsers"
   xmlns:p="http://teamengine.sourceforge.net/parsers"
   xmlns:gmd="http://www.isotc211.org/2005/gmd"
   xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
   xmlns:saxon="http://saxon.sf.net/"
   xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:csw2="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:ows="http://www.opengis.net/ows"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:dct="http://purl.org/dc/terms/"
   xmlns:xi="http://www.w3.org/2001/XInclude"
   xmlns:xsd="http://www.w3.org/2001/XMLSchema">

  <suite name="csw:csw_2.0.2_ap_iso_1.0">
    <title>CSW 2.0.2 AP ISO 1.0 Compliance Test Suite</title>
    <description>
      Validates a CSW 2.0.2 catalogue implementation against the ISO 1.0
      application profile.
    </description>
    <starting-test>csw:csw-main</starting-test>
  </suite>

  <test name="csw:csw-main">
    <assertion>Run the CSW 2.0.2 AP ISO 1.0 compliance tests</assertion>
    <code>

<!--       <xsl:variable name="mytest"> -->
<!--         <request> -->
<!--           <url> -->
<!--             <xsl:value-of select="'file:/tmp/test.xml'"/> -->
<!--           </url> -->
<!--           <method>GET</method> -->
<!--         </request> -->
<!--       </xsl:variable> -->

<!--       <xsl:for-each select="$mytest//gmd:MD_Metadata"> -->
<!--         <call-test name="ctl:XMLValidatingParser"> -->
<!--           <with-param name="doc"><xsl:copy-of select="."/></with-param> -->
<!--           <with-param name="instruction"> -->
<!--             <parsers:schemas> -->
<!--               <parsers:schema type="url">http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema> -->
<!--             </parsers:schemas> -->
<!--           </with-param> -->
<!--         </call-test> -->
<!--       </xsl:for-each> -->

<!--       <call-test name="ctl:XMLValidatingParser"> -->
<!--         <with-param name="doc"><xsl:apply-templates select="$mytest/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param> -->
<!--         <with-param name="instruction"> -->
<!--           <parsers:schemas> -->
<!--             <parsers:schema type="url">http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd</parsers:schema> -->
<!--           </parsers:schemas> -->
<!--         </with-param> -->
<!--       </call-test> -->

      <!--RI: http://www.crisalis-tech.com:8081/geonetwork/srv/en/csw-->
      <!--http://geonetwork.mysdi.org:8081/geonetwork/srv/en/csw-->
      <!--http://geobrain.laits.gmu.edu:8099/LAITSCSW2/discovery-->
      <!--xsl:variable name="form-values">
        <form height="600" width="800">
          <body>
            <h2>CSW Catalogue 2.0.2 - Test setup</h2>
            <h3>Service metadata</h3>
            <p>
              Please provide a URL from which a capabilities document can
              be retrieved. Modify the URL template below to specify the
              location of an OGC capabilities document for the CSW
              implementation under test (this can refer to a static document
              or to a service endpoint).
            </p>
            <br/>
            <table border="2" padding="4" bgcolor="#00ffff">
              <tr>
                <td align="left">Capabilities URL</td>
                <td align="center">
                  <input name="capabilities.url" size="128"
                         type="text"
                          value="http://gdi-de.sdisuite.de/soapServices/CSWStartup?request=GetCapabilities&amp;service=CSW" />
                </td>
              </tr>
            </table>
            <br />
            <input type="submit" value="Start"/>
          </body>
        </form>
      </xsl:variable-->

      <!-- Populate global variables from form data -->
      <!--xsl:variable name="csw.capabilities.url"
                    select="$form-values/values/value[@key='capabilities.url']"/-->
					
	  <xsl:variable name="csw.capabilities.url">
		http://localhost:8080/geonetwork/srv/en/csw?request=GetCapabilities&amp;service=CSW
	  </xsl:variable>
     
	 
      <!-- Attempt to retrieve capabilities document -->
      <xsl:variable name="csw.GetCapabilities.document">
        <request>
          <url>
            <xsl:value-of select="$csw.capabilities.url"/>
          </url>
          <method>GET</method>
        </request>
      </xsl:variable>

      <xsl:choose>
        <xsl:when test="not($csw.GetCapabilities.document/csw2:Capabilities)">
          <message>FAILURE: Did not receive a csw:Capabilities document! Skipping remaining tests.</message>
          <fail/>
        </xsl:when>
        <xsl:otherwise>
          <call-test name="csw:level1.1">
            <with-param name="csw.GetCapabilities.document" select="$csw.GetCapabilities.document"/>
          </call-test>
          <call-test name="csw:level1.2">
            <with-param name="csw.GetCapabilities.document" select="$csw.GetCapabilities.document"/>
          </call-test>
          <call-test name="csw:level1.3">
            <with-param name="csw.GetCapabilities.document" select="$csw.GetCapabilities.document"/>
          </call-test>
          <call-test name="csw:level1.4">
            <with-param name="csw.GetCapabilities.document" select="$csw.GetCapabilities.document"/>
          </call-test>
          <call-test name="csw:level1.5">
            <with-param name="csw.GetCapabilities.document" select="$csw.GetCapabilities.document"/>
          </call-test>
        </xsl:otherwise>
      </xsl:choose>
    </code>
  </test>
</package>
