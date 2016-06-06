<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:sos="http://www.opengis.net/sos/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:ows="http://www.opengis.net/ows/1.1"
                xmlns:crs="java:org.geotools.referencing.CRS"
                xmlns:defcrs="java:org.geotools.referencing.crs.DefaultGeographicCRS"
                xmlns:renv="java:org.geotools.geometry.jts.ReferencedEnvelope"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0"
                xmlns="http://www.isotc211.org/2005/gmd"
                extension-element-prefixes="crs defcrs renv">

  <!-- ============================================================================= -->

  <xsl:param name="uuid"></xsl:param>
  <xsl:param name="lang">eng</xsl:param>
  <xsl:param name="topic"></xsl:param>

  <!-- ============================================================================= -->

  <xsl:include href="resp-party.xsl"/>

  <!-- ============================================================================= -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <!-- ============================================================================= -->

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="sos:Capabilities">

    <MD_Metadata>

      <fileIdentifier>
        <gco:CharacterString>
          <xsl:value-of select="$uuid"/>
        </gco:CharacterString>
      </fileIdentifier>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <language>
        <gco:CharacterString>
          <xsl:value-of select="$lang"/>
        </gco:CharacterString>
        <!-- English is default. Not available in GetCapabilities.
                Selected by user from GUI -->
      </language>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <characterSet>
        <MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode"
                             codeListValue="utf8"/>
      </characterSet>

      <!-- parentIdentifier : service have no parent -->

      <!-- mdHrLv -->
      <hierarchyLevel>
        <MD_ScopeCode codeList="./resources/codeList.xml#MD_ScopeCode" codeListValue="service"/>
      </hierarchyLevel>

      <!-- mdHrLvName -->

      <xsl:choose>
        <xsl:when test="ows:ServiceProvider">
          <xsl:for-each select="ows:ServiceProvider">
            <contact>
              <CI_ResponsibleParty>
                <xsl:apply-templates select="." mode="RespParty"/>
              </CI_ResponsibleParty>
            </contact>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <contact gco:nilReason="missing"/>
        </xsl:otherwise>
      </xsl:choose>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>
      <dateStamp>
        <gco:DateTime>
          <xsl:value-of select="format-dateTime(current-dateTime(),$df)"/>
        </gco:DateTime>
      </dateStamp>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <metadataStandardName>
        <gco:CharacterString>ISO 19119/2005</gco:CharacterString>
      </metadataStandardName>

      <metadataStandardVersion>
        <gco:CharacterString>1.0</gco:CharacterString>
      </metadataStandardVersion>

      <!-- spatRepInfo-->

      <xsl:if test="//sos:ObservationOffering/gml:srsName">
        <referenceSystemInfo>
          <MD_ReferenceSystem>
            <referenceSystemIdentifier>
              <RS_Identifier>
                <code>
                  <!-- Add WGS84 if exist else add only the first one to avoid to have the full list of CRS supported.
                                    OGC Clients should use the GetCapabilities to get this information  -->
                  <xsl:choose>
                    <xsl:when test="//sos:ObservationOffering/gml:srsName[.='EPSG:4326']">
                      <gco:CharacterString>EPSG:4326</gco:CharacterString>
                    </xsl:when>
                    <xsl:otherwise>
                      <gco:CharacterString>
                        <xsl:value-of
                          select="//sos:ObservationOffering[gml:srsName][1]/gml:srsName[1]"/>
                      </gco:CharacterString>
                    </xsl:otherwise>
                  </xsl:choose>
                </code>
              </RS_Identifier>
            </referenceSystemIdentifier>
          </MD_ReferenceSystem>
        </referenceSystemInfo>
      </xsl:if>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <identificationInfo>
        <srv:SV_ServiceIdentification>

          <citation>
            <CI_Citation>
              <title>
                <gco:CharacterString>
                  <xsl:value-of select="ows:ServiceIdentification/ows:Title"/>
                </gco:CharacterString>
              </title>
              <date>
                <CI_Date>
                  <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>
                  <date>
                    <gco:DateTime>
                      <xsl:value-of select="format-dateTime(current-dateTime(),$df)"/>
                    </gco:DateTime>
                  </date>
                  <dateType>
                    <CI_DateTypeCode codeList="./resources/codeList.xml#CI_DateTypeCode"
                                     codeListValue="revision"/>
                  </dateType>
                </CI_Date>
              </date>
            </CI_Citation>
          </citation>

          <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

          <abstract>
            <gco:CharacterString>
              <xsl:value-of select="ows:ServiceIdentification/ows:Abstract"/>
            </gco:CharacterString>
          </abstract>

          <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

          <xsl:variable name="temporalExtent">
            <xsl:call-template name="temporal-element"/>
          </xsl:variable>

          <status>
            <xsl:choose>
              <xsl:when
                test="$temporalExtent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition/@indeterminatePosition[.='now']">
                <MD_ProgressCode codeList="./resources/codeList.xml#MD_ProgressCode"
                                 codeListValue="onGoing"/>
              </xsl:when>
              <xsl:otherwise>
                <MD_ProgressCode codeList="./resources/codeList.xml#MD_ProgressCode"
                                 codeListValue="completed"/>
              </xsl:otherwise>
            </xsl:choose>
          </status>

          <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

          <xsl:for-each select="ows:ServiceProvider">
            <pointOfContact>
              <CI_ResponsibleParty>
                <xsl:apply-templates select="." mode="RespParty"/>
              </CI_ResponsibleParty>
            </pointOfContact>
          </xsl:for-each>

          <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

          <xsl:for-each select="ows:ServiceIdentification/ows:Keywords">
            <descriptiveKeywords>
              <MD_Keywords>
                <xsl:for-each select="ows:Keyword">
                  <keyword>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </keyword>
                </xsl:for-each>

                <type>
                  <MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode"
                                      codeListValue="theme"/>
                </type>
              </MD_Keywords>
            </descriptiveKeywords>
          </xsl:for-each>

          <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

          <srv:serviceType>
            <gco:LocalName codeSpace="www.w3c.org">OGC:SOS</gco:LocalName>
          </srv:serviceType>

          <srv:serviceTypeVersion>
            <gco:CharacterString>
              <xsl:value-of select='@version'/>
            </gco:CharacterString>
          </srv:serviceTypeVersion>

          <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

          <srv:accessProperties>
            <MD_StandardOrderProcess>
              <fees>
                <gco:CharacterString>
                  <xsl:value-of select="ows:ServiceIdentification/ows:Fees"/>
                </gco:CharacterString>
              </fees>
            </MD_StandardOrderProcess>
          </srv:accessProperties>

          <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

          <srv:extent>
            <EX_Extent>
              <xsl:call-template name="geographic-element"/>
              <xsl:call-template name="temporal-element"/>
            </EX_Extent>
          </srv:extent>

          <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

          <srv:couplingType>
            <srv:SV_CouplingType codeList="#SV_CouplingType" codeListValue="tight">tight
            </srv:SV_CouplingType>
          </srv:couplingType>

          <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

          <xsl:for-each select="ows:OperationsMetadata/ows:Operation">
            <srv:containsOperations>
              <srv:SV_OperationMetadata>
                <srv:operationName>
                  <gco:CharacterString>
                    <xsl:value-of select="@name"/>
                  </gco:CharacterString>
                </srv:operationName>

                <!--  CHECKME : DCPType/SOAP ? -->

                <xsl:for-each select="ows:DCP/ows:HTTP/*">
                  <srv:DCP>
                    <srv:DCPList codeList="#DCPList">
                      <xsl:variable name="dcp">
                        <xsl:choose>
                          <xsl:when test="name(.)='ows:Get'">HTTP-GET</xsl:when>
                          <xsl:when test="name(.)='ows:Post'">HTTP-POST</xsl:when>
                          <xsl:otherwise>WebServices</xsl:otherwise>
                        </xsl:choose>
                      </xsl:variable>
                      <xsl:attribute name="codeListValue">
                        <xsl:value-of select="$dcp"/>
                      </xsl:attribute>
                    </srv:DCPList>
                  </srv:DCP>
                </xsl:for-each>

                <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

                <xsl:for-each-group select="ows:DCP/ows:HTTP/*" group-by="@xlink:href">
                  <srv:connectPoint>
                    <CI_OnlineResource>
                      <linkage>
                        <URL>
                          <xsl:value-of select="current-grouping-key()"/>
                        </URL>
                      </linkage>
                      <protocol>
                        <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                      </protocol>
                      <description>
                        <gco:CharacterString>
                          <xsl:value-of select="../../../@name"/>
                          <xsl:text> (</xsl:text>
                          <xsl:for-each select="current-group()">
                            <xsl:choose>
                              <xsl:when test="name(.)='ows:Get'">HTTP-GET</xsl:when>
                              <xsl:when test="name(.)='ows:Post'">HTTP-POST</xsl:when>
                              <xsl:otherwise>WebServices</xsl:otherwise>
                            </xsl:choose>
                            <xsl:if test="position()!=last()">
                              <xsl:text>, </xsl:text>
                            </xsl:if>
                          </xsl:for-each>
                          <xsl:text>)</xsl:text>
                        </gco:CharacterString>
                      </description>
                      <function>
                        <CI_OnLineFunctionCode
                          codeList="./resources/codeList.xml#CI_OnLineFunctionCode"
                          codeListValue="information"/>
                      </function>
                    </CI_OnlineResource>
                  </srv:connectPoint>
                </xsl:for-each-group>

              </srv:SV_OperationMetadata>
            </srv:containsOperations>
          </xsl:for-each>

        </srv:SV_ServiceIdentification>
      </identificationInfo>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <distributionInfo>
        <MD_Distribution>
          <distributionFormat>
            <MD_Format>
              <name gco:nilReason="missing">
                <gco:CharacterString/>
              </name>
              <version gco:nilReason="missing">
                <gco:CharacterString/>
              </version>
            </MD_Format>
          </distributionFormat>
          <transferOptions>
            <MD_DigitalTransferOptions>
              <onLine>
                <CI_OnlineResource>
                  <linkage>
                    <URL>
                      <xsl:value-of
                        select="ows:OperationsMetadata/ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
                    </URL>
                  </linkage>
                  <protocol>
                    <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                  </protocol>
                  <description>
                    <gco:CharacterString>
                      <xsl:value-of
                        select="ows:OperationsMetadata/ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
                    </gco:CharacterString>
                  </description>
                </CI_OnlineResource>
              </onLine>
            </MD_DigitalTransferOptions>
          </transferOptions>
        </MD_Distribution>
      </distributionInfo>

      <!--dqInfo-->

      <!--mdConst -->

      <!--mdMaint-->

    </MD_Metadata>

  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template name='geographic-element'>
    <xsl:variable name="bboxes">
      <xsl:call-template name="get-bboxes"/>
    </xsl:variable>

    <geographicElement>
      <xsl:choose>
        <xsl:when test="$bboxes/bbox[@unknown='true']">
          <xsl:call-template name="bounding-polygon"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="bounding-box">
            <xsl:with-param name="bboxes" select="$bboxes"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </geographicElement>

  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template name="get-bboxes">
    <xsl:for-each select="//sos:ObservationOffering/gml:boundedBy/gml:Envelope">
      <xsl:variable name="xmin" select="number(substring-before(gml:lowerCorner, ' '))"/>
      <xsl:variable name="ymin" select="number(substring-after(gml:lowerCorner, ' '))"/>
      <xsl:variable name="xmax" select="number(substring-before(gml:upperCorner, ' '))"/>
      <xsl:variable name="ymax" select="number(substring-after(gml:upperCorner, ' '))"/>

      <bbox xmlns="">
        <xsl:choose>
          <!-- Common case - urn:ogc:def:crs:epsg::4326 envelope -->
          <xsl:when test="@srsName='urn:ogc:def:crs:epsg::4326'">
            <southBL>
              <xsl:value-of select="$xmin"/>
            </southBL>
            <westBL>
              <xsl:value-of select="$ymin"/>
            </westBL>
            <northBL>
              <xsl:value-of select="$xmax"/>
            </northBL>
            <eastBL>
              <xsl:value-of select="$ymax"/>
            </eastBL>
          </xsl:when>
          <!-- Reproject envelope to WGS84 if relevant geotools functions are available -->
          <xsl:when
            test="function-available('crs:decode') and function-available('renv:new') and function-available('renv:transform')">
            <xsl:variable name="srs" select="crs:decode(@srsName)"/>
            <xsl:variable name="env" select="renv:new($xmin,$xmax, $ymin, $ymax, $srs)"/>
            <xsl:variable name="envWGS84" select="renv:transform($env,defcrs:WGS84(),true())"/>

            <southBL>
              <xsl:value-of select="renv:getMinY($envWGS84)"/>
            </southBL>
            <westBL>
              <xsl:value-of select="renv:getMinX($envWGS84)"/>
            </westBL>
            <northBL>
              <xsl:value-of select="renv:getMaxY($envWGS84)"/>
            </northBL>
            <eastBL>
              <xsl:value-of select="renv:getMaxX($envWGS84)"/>
            </eastBL>
          </xsl:when>
          <!-- Can't determine WGS84 bounding box  -->
          <xsl:otherwise>
            <xsl:attribute name="unknown">true</xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </bbox>
    </xsl:for-each>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template name="bounding-box">
    <xsl:param name="bboxes"/>

    <EX_GeographicBoundingBox>
      <westBoundLongitude>
        <gco:Decimal>
          <xsl:copy-of select="min($bboxes/bbox/westBL)"/>
        </gco:Decimal>
      </westBoundLongitude>
      <eastBoundLongitude>
        <gco:Decimal>
          <xsl:value-of select="max($bboxes/bbox/eastBL)"/>
        </gco:Decimal>
      </eastBoundLongitude>
      <southBoundLatitude>
        <gco:Decimal>
          <xsl:value-of select="min($bboxes/bbox/southBL)"/>
        </gco:Decimal>
      </southBoundLatitude>
      <northBoundLatitude>
        <gco:Decimal>
          <xsl:value-of select="max($bboxes/bbox/northBL)"/>
        </gco:Decimal>
      </northBoundLatitude>
    </EX_GeographicBoundingBox>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template name="bounding-polygon">
    <EX_BoundingPolygon>
      <xsl:for-each select="//sos:ObservationOffering/gml:boundedBy/gml:Envelope">
        <xsl:variable name="xmin" select="number(substring-before(gml:lowerCorner, ' '))"/>
        <xsl:variable name="ymin" select="number(substring-after(gml:lowerCorner, ' '))"/>
        <xsl:variable name="xmax" select="number(substring-before(gml:upperCorner, ' '))"/>
        <xsl:variable name="ymax" select="number(substring-after(gml:upperCorner, ' '))"/>

        <polygon>
          <gml:Polygon gml:id="{generate-id()}" srsName="{@srsName}">
            <gml:exterior>
              <gml:LinearRing>
                <gml:coordinates><xsl:value-of select="$ymin"/>,<xsl:value-of select="$xmax"/>,
                  <xsl:value-of select="$ymax"/>,<xsl:value-of select="$xmax"/>, <xsl:value-of
                    select="$ymax"/>,<xsl:value-of select="$xmin"/>, <xsl:value-of select="$ymin"/>,<xsl:value-of
                    select="$xmin"/>, <xsl:value-of select="$ymin"/>,<xsl:value-of select="$xmax"/>
                </gml:coordinates>
              </gml:LinearRing>
            </gml:exterior>
          </gml:Polygon>
        </polygon>
      </xsl:for-each>
    </EX_BoundingPolygon>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template name="temporal-element">
    <temporalElement>
      <EX_TemporalExtent>
        <extent>
          <gml:TimePeriod gml:id="{concat(generate-id(),'t')}">
            <gml:beginPosition>
              <xsl:choose>
                <xsl:when
                  test="//sos:ObservationOffering/sos:time/gml:TimePosition[@indeterminatePosition='now']|//sos:ObservationOffering/sos:time/gml:TimePeriod/gml:beginPosition[@indeterminatePosition='now']">
                  <xsl:attribute name="indeterminatePosition" select="now"/>
                </xsl:when>
                <xsl:when test="//sos:ObservationOffering/sos:time/gml:TimePosition[@indeterminatePosition='unknown']|
                        //sos:ObservationOffering/sos:time/gml:TimePeriod/gml:beginPosition[@indeterminatePosition='unknown']|
                        //sos:ObservationOffering/sos:time/gml:TimePeriod[gml:beginPosition='']|
                        //sos:ObservationOffering/sos:time[gml:TimePosition='']
                        ">
                  <xsl:attribute name="indeterminatePosition" select="unknown"/>
                </xsl:when>
                <xsl:otherwise>

                  <xsl:value-of
                    select="min((//sos:ObservationOffering/sos:time/gml:TimePeriod/xs:dateTime(gml:beginPosition),//sos:ObservationOffering/sos:time/xs:dateTime(gml:TimePosition)))"/>
                </xsl:otherwise>
              </xsl:choose>
            </gml:beginPosition>
            <gml:endPosition>
              <xsl:choose>
                <xsl:when
                  test="//sos:ObservationOffering/sos:time/gml:TimePosition[@indeterminatePosition='now']|//sos:ObservationOffering/sos:time/gml:TimePeriod/gml:endPosition[@indeterminatePosition='now']">
                  <xsl:attribute name="indeterminatePosition" select="'now'"/>
                </xsl:when>
                <xsl:when test="//sos:ObservationOffering/sos:time/gml:TimePosition[@indeterminatePosition='unknown']|
                              //sos:ObservationOffering/sos:time/gml:TimePeriod/gml:endPosition[@indeterminatePosition='unknown']|
                              //sos:ObservationOffering/sos:time/gml:TimePeriod[gml:beginPosition='']|
                              //sos:ObservationOffering/sos:time[gml:TimePosition='']">
                  <xsl:attribute name="indeterminatePosition" select="'unknown'"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of
                    select="max((//sos:ObservationOffering/sos:time/gml:TimePeriod/xs:dateTime(gml:beginPosition),//sos:ObservationOffering/sos:time/xs:dateTime(gml:TimePosition)))"/>
                </xsl:otherwise>
              </xsl:choose>
            </gml:endPosition>
          </gml:TimePeriod>
        </extent>
      </EX_TemporalExtent>
    </temporalElement>
  </xsl:template>

</xsl:stylesheet>
