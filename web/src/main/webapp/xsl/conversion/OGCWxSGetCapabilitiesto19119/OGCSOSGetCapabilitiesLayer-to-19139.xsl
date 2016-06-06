<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:sos="http://www.opengis.net/sos/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:ows="http://www.opengis.net/ows/1.1"
                xmlns:crs="java:org.geotools.referencing.CRS"
                xmlns:defcrs="java:org.geotools.referencing.crs.DefaultGeographicCRS"
                xmlns:renv="java:org.geotools.geometry.jts.ReferencedEnvelope"
                version="2.0"
                xmlns="http://www.isotc211.org/2005/gmd"
                extension-element-prefixes="crs defcrs renv"
                exclude-result-prefixes="xsi sos ows">

  <!-- ============================================================================= -->

  <xsl:param name="uuid"/>
  <xsl:param name="Name"/>
  <xsl:param name="lang"/>
  <xsl:param name="topic"/>

  <!-- ============================================================================= -->

  <xsl:include href="resp-party.xsl"/>
  <xsl:include href="ref-system.xsl"/>

  <!-- ============================================================================= -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <!-- ============================================================================= -->

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="sos:Capabilities[//sos:ObservationOffering/gml:name=$Name]">

    <MD_Metadata>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

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
      </language>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <characterSet>
        <MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode"
                             codeListValue="utf8"/>
      </characterSet>

      <!-- parentIdentifier -->

      <!-- mdHrLv -->
      <hierarchyLevel>
        <MD_ScopeCode codeList="./resources/codeList.xml#MD_ScopeCode"
                      codeListValue="dataset"/>
      </hierarchyLevel>

      <!-- mdHrLvName -->

      <xsl:choose>
        <xsl:when test="//ows:ServiceProvider">
          <xsl:for-each select="//ows:ServiceProvider">
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
        <gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>
      </metadataStandardName>

      <metadataStandardVersion>
        <gco:CharacterString>1.0</gco:CharacterString>
      </metadataStandardVersion>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="//sos:ObservationOffering[gml:name=$Name]/gml:srsName">
        <referenceSystemInfo>
          <MD_ReferenceSystem>
            <xsl:call-template name="RefSystemTypes">
              <xsl:with-param name="srs" select="."/>
            </xsl:call-template>
          </MD_ReferenceSystem>
        </referenceSystemInfo>
      </xsl:for-each>

      <!--mdExtInfo-->

      <identificationInfo>
        <MD_DataIdentification>
          <xsl:apply-templates select="." mode="OfferingDataIdentification"/>
        </MD_DataIdentification>
      </identificationInfo>

      <!--contInfo-->

      <!--distInfo -->

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
              <xsl:if test="//ows:Operation[@name='GetObservation']/ows:DCP/ows:HTTP/ows:Post">
                <onLine>
                  <CI_OnlineResource>
                    <linkage>
                      <URL>
                        <xsl:value-of
                          select="//ows:Operation[@name='GetObservation']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
                      </URL>
                    </linkage>
                    <protocol>
                      <gco:CharacterString>OGC:SOS-1.0.0-http-post-observation</gco:CharacterString>
                    </protocol>
                    <name>
                      <gco:CharacterString>
                        <xsl:value-of select="$Name"/>
                      </gco:CharacterString>
                    </name>
                    <description>
                      <gco:CharacterString>
                        <xsl:value-of
                          select="//sos:ObservationOffering[gml:name=$Name]/gml:description"/>
                      </gco:CharacterString>
                    </description>
                  </CI_OnlineResource>
                </onLine>
              </xsl:if>
              <xsl:apply-templates mode="onlineResource"
                                   select="//sos:ObservationOffering[gml:name=$Name]/sos:responseFormat[//ows:Operation[@name='GetObservation']/ows:DCP/ows:HTTP/ows:Get/@xlink:href]"/>
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
  <!-- === OfferingDataIdentification === -->
  <!-- ============================================================================= -->

  <xsl:template match="*" mode="OfferingDataIdentification">
    <citation>
      <CI_Citation>
        <title>
          <gco:CharacterString>
            <xsl:value-of select="//sos:ObservationOffering[gml:name=$Name]/gml:name[1]"/>
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
        <xsl:for-each select="//sos:ObservationOffering[gml:name=$Name]/gml:name">
          <xsl:variable name="codeSpace" select="@codeSpace"/>
          <identifier>
            <MD_Identifier>
              <code>
                <xsl:choose>
                  <xsl:when test="normalize-space($codeSpace)=''">
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </xsl:when>
                  <xsl:otherwise>
                    <gmx:Anchor xlink:href="{$codeSpace}">
                      <xsl:value-of select="."/>
                    </gmx:Anchor>
                  </xsl:otherwise>
                </xsl:choose>
              </code>
            </MD_Identifier>
          </identifier>
        </xsl:for-each>
      </CI_Citation>
    </citation>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:choose>
      <xsl:when test="//sos:ObservationOffering[gml:name=$Name]/gml:description">
        <abstract>
          <gco:CharacterString>
            <xsl:value-of select="//sos:ObservationOffering[gml:name=$Name]/gml:description"/>
          </gco:CharacterString>
        </abstract>
      </xsl:when>
      <xsl:otherwise>
        <abstract gco:nilReason="missing">
          <gco:CharacterString/>
        </abstract>
      </xsl:otherwise>
    </xsl:choose>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:if test="//sos:ObservationOffering[gml:name=$Name]/sos:intendedApplication!=''">
      <purpose>
        <gco:CharacterString>
          <xsl:for-each
            select="//sos:ObservationOffering[gml:name=$Name]/sos:intendedApplication[normalize-space()!='']">
            <xsl:value-of select="."/>
            <xsl:if test="position() &lt; last()">,</xsl:if>
          </xsl:for-each>
        </gco:CharacterString>
      </purpose>
    </xsl:if>


    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <status>
      <xsl:choose>
        <xsl:when
          test="//sos:ObservationOffering[gml:name=$Name]/sos:time/(gml:TimePeriod/gml:endPosition|gml:TimeInstant)/@indeterminatePosition[.='now']">
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

    <xsl:for-each select="//ows:ServiceProvider">
      <pointOfContact>
        <CI_ResponsibleParty>
          <xsl:apply-templates select="." mode="RespParty"/>
        </CI_ResponsibleParty>
      </pointOfContact>
    </xsl:for-each>

    <!-- resMaint -->

    <!-- graphOver -->

    <!-- dsFormat-->

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:if test="//sos:ObservationOffering[gml:name=$Name]/sos:observedProperty">
      <descriptiveKeywords>
        <MD_Keywords>
          <xsl:for-each select="//sos:ObservationOffering[gml:name=$Name]/sos:observedProperty">
            <keyword>
              <gco:CharacterString>
                <xsl:value-of select="@xlink:href|gml:name"/>
              </gco:CharacterString>
            </keyword>
          </xsl:for-each>
          <type>
            <MD_KeywordTypeCode
              codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_KeywordTypeCode"
              codeListValue="theme">theme
            </MD_KeywordTypeCode>
          </type>
        </MD_Keywords>
      </descriptiveKeywords>
    </xsl:if>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        <xsl:if test="//sos:ObservationOffering[gml:name=$Name]/sos:procedure">
            <descriptiveKeywords>
                <MD_Keywords>
                    <xsl:for-each select="//sos:ObservationOffering[gml:name=$Name]/sos:procedure">
                        <keyword>
                            <gco:CharacterString><xsl:value-of select="@xlink:href"/></gco:CharacterString>
                        </keyword>
                    </xsl:for-each>
                    <type>
                        <MD_KeywordTypeCode codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_KeywordTypeCode" codeListValue="theme">theme</MD_KeywordTypeCode>
                    </type>
                </MD_Keywords>
            </descriptiveKeywords>
        </xsl:if>

        -->

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:if test="//sos:ObservationOffering[gml:name=$Name]/sos:featureOfInterest">
      <descriptiveKeywords>
        <MD_Keywords>
          <xsl:for-each select="//sos:ObservationOffering[gml:name=$Name]/sos:featureOfInterest">
            <keyword>
              <gco:CharacterString>
                <xsl:value-of select="@xlink:href"/>
              </gco:CharacterString>
            </keyword>
          </xsl:for-each>
          <type>
            <MD_KeywordTypeCode
              codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_KeywordTypeCode"
              codeListValue="place">place
            </MD_KeywordTypeCode>
          </type>
        </MD_Keywords>
      </descriptiveKeywords>
    </xsl:if>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <language gco:nilReason="missing">
      <gco:CharacterString/>
    </language>

    <characterSet>
      <MD_CharacterSetCode
        codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_CharacterSetCode"
        codeListValue=""/>
    </characterSet>

    <topicCategory>
      <MD_TopicCategoryCode>
        <xsl:value-of select="$topic"/>
      </MD_TopicCategoryCode>
    </topicCategory>

    <!-- dataExt -->

    <extent>
      <EX_Extent>
        <xsl:apply-templates mode="spatial-extent"
                             select="//sos:ObservationOffering[gml:name=$Name]/gml:boundedBy/gml:Envelope"/>
        <xsl:apply-templates mode="temporal-extent"
                             select="//sos:ObservationOffering[gml:name=$Name]/sos:time"/>
      </EX_Extent>
    </extent>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <!-- //sos:ObservationOffering[gml:name=$Name]/sos:procedure/@xlink:href -> Keyword or Aggregation? -->

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <!--  //sos:ObservationOffering[gml:name=$Name]/sos:observedProperty -> Keyword -->

  </xsl:template>

  <!-- ============================================================================= -->
  <!-- === Geographic Extent === -->
  <!-- ============================================================================= -->

  <xsl:template match='*' mode='spatial-extent'>
    <geographicElement>
      <xsl:variable name="xmin" select="number(substring-before(gml:lowerCorner, ' '))"/>
      <xsl:variable name="ymin" select="number(substring-after(gml:lowerCorner, ' '))"/>
      <xsl:variable name="xmax" select="number(substring-before(gml:upperCorner, ' '))"/>
      <xsl:variable name="ymax" select="number(substring-after(gml:upperCorner, ' '))"/>

      <xsl:choose>
        <!-- Common case - create bounding box for urn:ogc:def:crs:epsg::4326 envelope -->
        <xsl:when test="@srsName='urn:ogc:def:crs:epsg::4326'">
          <xsl:call-template name="bounding-box">
            <xsl:with-param name="westBL" select="$ymin"/>
            <xsl:with-param name="eastBL" select="$ymax"/>
            <xsl:with-param name="southBL" select="$xmin"/>
            <xsl:with-param name="northBL" select="$xmax"/>
          </xsl:call-template>
        </xsl:when>
        <!-- Reproject envelope to WGS84 and create a bounding box if using saxon and relevant geotools functions are available -->
        <xsl:when
          test="function-available('crs:decode') and function-available('renv:new') and function-available('renv:transform')">
          <xsl:variable name="srs" select="crs:decode(@srsName)"/>
          <xsl:variable name="env" select="renv:new($xmin,$xmax, $ymin, $ymax, $srs)"/>
          <xsl:variable name="envWGS84" select="renv:transform($env,defcrs:WGS84(),true())"/>

          <xsl:variable name="westBL" select="renv:getMinX($envWGS84)"/>
          <xsl:variable name="eastBL" select="renv:getMaxX($envWGS84)"/>
          <xsl:variable name="southBL" select="renv:getMinY($envWGS84)"/>
          <xsl:variable name="northBL" select="renv:getMaxY($envWGS84)"/>

          <xsl:call-template name="bounding-box">
            <xsl:with-param name="westBL" select="$westBL"/>
            <xsl:with-param name="eastBL" select="$eastBL"/>
            <xsl:with-param name="southBL" select="$southBL"/>
            <xsl:with-param name="northBL" select="$northBL"/>
          </xsl:call-template>
        </xsl:when>
        <!-- Otherwise just create a bounding polygon in the specified srs -->
        <xsl:otherwise>
          <EX_BoundingPolygon>
            <polygon>
              <gml:Polygon gml:id="{generate-id()}" srsName="{@srsName}">
                <gml:exterior>
                  <gml:LinearRing>
                    <gml:coordinates><xsl:value-of select="$ymin"/>,<xsl:value-of select="$xmax"/>,
                      <xsl:value-of select="$ymax"/>,<xsl:value-of select="$xmax"/>, <xsl:value-of
                        select="$ymax"/>,<xsl:value-of select="$xmin"/>, <xsl:value-of
                        select="$ymin"/>,<xsl:value-of select="$xmin"/>, <xsl:value-of
                        select="$ymin"/>,<xsl:value-of select="$xmax"/>
                    </gml:coordinates>
                  </gml:LinearRing>
                </gml:exterior>
              </gml:Polygon>
            </polygon>
          </EX_BoundingPolygon>
        </xsl:otherwise>
      </xsl:choose>
    </geographicElement>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template name="bounding-box">
    <xsl:param name="westBL"/>
    <xsl:param name="eastBL"/>
    <xsl:param name="southBL"/>
    <xsl:param name="northBL"/>

    <EX_GeographicBoundingBox>
      <westBoundLongitude>
        <gco:Decimal>
          <xsl:value-of select="$westBL"/>
        </gco:Decimal>
      </westBoundLongitude>
      <eastBoundLongitude>
        <gco:Decimal>
          <xsl:value-of select="$eastBL"/>
        </gco:Decimal>
      </eastBoundLongitude>
      <southBoundLatitude>
        <gco:Decimal>
          <xsl:value-of select="$southBL"/>
        </gco:Decimal>
      </southBoundLatitude>
      <northBoundLatitude>
        <gco:Decimal>
          <xsl:value-of select="$northBL"/>
        </gco:Decimal>
      </northBoundLatitude>
    </EX_GeographicBoundingBox>
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- === Temporal Extent === -->
  <!-- ============================================================================= -->

  <xsl:template match='*' mode='temporal-extent'>
    <temporalElement>
      <EX_TemporalExtent>
        <extent>
          <xsl:apply-templates select="*" mode="convert-gml"/>
        </extent>
      </EX_TemporalExtent>
    </temporalElement>
  </xsl:template>

  <xsl:template match='gml:TimePeriod|gml:TimeInstant' mode='convert-gml'>
    <xsl:copy copy-namespaces="no">
      <xsl:if test="not(@gml:id)">
        <xsl:attribute name="gml:id" select="generate-id()"/>
      </xsl:if>
      <xsl:apply-templates select="@*|node()" mode="convert-gml"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match='@*|node()' mode='convert-gml'>
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*|node()" mode="convert-gml"/>
    </xsl:copy>
  </xsl:template>


  <!-- ============================================================================= -->
  <!-- === Online resource === -->
  <!-- ============================================================================= -->

  <!-- Create as many online resources as output formats available for the offering
        to download observations using the GetObservation operation.

        SOS 1.0.0
    -->
  <xsl:template mode="onlineResource" match="*">
    <xsl:variable name="format" select="."/>
    <xsl:variable name="baseUrl"
                  select="//ows:Operation[@name='GetObservation']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
    <xsl:variable name="description">
      <xsl:choose>
        <xsl:when test="//sos:ObservationOffering[gml:name=$Name]/gml:description">
          <xsl:value-of select="//sos:ObservationOffering[gml:name=$Name]/gml:description"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$Name"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="url">
      <xsl:value-of select="$baseUrl"/>
      <xsl:if test="not(contains($baseUrl, '?'))">?</xsl:if>
      <xsl:text>&amp;request=GetObservation&amp;service=SOS&amp;offering=</xsl:text>
      <xsl:value-of select="$Name"/>
      <xsl:text>&amp;observedProperty=</xsl:text>
      <xsl:for-each
        select="//sos:ObservationOffering[gml:name=$Name]/sos:observedProperty/@xlink:href">
        <xsl:value-of select="."/>
        <xsl:if test="position()!=last()">,</xsl:if>
      </xsl:for-each>
      <xsl:text>&amp;responseFormat=</xsl:text>
      <xsl:value-of select="encode-for-uri(.)"/>
      <xsl:text>&amp;version=1.0.0</xsl:text>
    </xsl:variable>

    <xsl:call-template name="onlineResource">
      <xsl:with-param name="name" select="$Name"/>
      <xsl:with-param name="url" select="$url"/>
      <xsl:with-param name="title">
        <xsl:value-of select="$description"/>
        <xsl:text> (</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>)</xsl:text>
      </xsl:with-param>
      <xsl:with-param name="protocol" select="'OGC:SOS-1.0.0-http-get-observation'"/>
    </xsl:call-template>

  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template name="onlineResource">
    <xsl:param name="name"/>
    <xsl:param name="url"/>
    <xsl:param name="title"/>
    <xsl:param name="protocol"/>

    <onLine>
      <CI_OnlineResource>
        <linkage>
          <URL>
            <xsl:value-of select="$url"/>
          </URL>
        </linkage>
        <protocol>
          <gco:CharacterString>
            <xsl:value-of select="$protocol"/>
          </gco:CharacterString>
        </protocol>
        <name>
          <gco:CharacterString>
            <xsl:value-of select="$name"/>
          </gco:CharacterString>
        </name>
        <description>
          <gco:CharacterString>
            <xsl:value-of select="$title"/>
          </gco:CharacterString>
        </description>
      </CI_OnlineResource>
    </onLine>
  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
