<?xml version="1.0" encoding="UTF-8"?>
<!--
Mapping between :
- WMS 1.0.0
- WMS 1.1.1
- WMS 1.3.0
- WCS 1.0.0
- WFS 1.0.0
- WFS 1.1.0
- WPS 0.4.0
- WPS 1.0.0
... to ISO19119.
 -->
<xsl:stylesheet xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:wcs="http://www.opengis.net/wcs"
                xmlns:wms="http://www.opengis.net/wms"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:owsg="http://www.opengeospatial.net/ows"
                xmlns:ows11="http://www.opengis.net/ows/1.1"
                xmlns:wps="http://www.opengeospatial.net/wps"
                xmlns:wps1="http://www.opengis.net/wps/1.0.0"
                version="2.0"
                xmlns="http://www.isotc211.org/2005/gmd"
                extension-element-prefixes="wcs ows wfs ows11 wps wps1 owsg">

  <!-- ============================================================================= -->

  <xsl:param name="uuid"></xsl:param>
  <xsl:param name="lang">eng</xsl:param>
  <xsl:param name="topic"></xsl:param>

  <!-- ============================================================================= -->

  <xsl:include href="resp-party.xsl"/>
  <xsl:include href="ref-system.xsl"/>
  <xsl:include href="identification.xsl"/>

  <!-- ============================================================================= -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <!-- ============================================================================= -->

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="WMT_MS_Capabilities|wfs:WFS_Capabilities|wcs:WCS_Capabilities|
         wps:Capabilities|wps1:Capabilities|wms:WMS_Capabilities">

    <xsl:variable name="ows">
      <xsl:choose>
        <xsl:when test="(local-name(.)='WFS_Capabilities' and namespace-uri(.)='http://www.opengis.net/wfs' and @version='1.1.0')
          or (local-name(.)='Capabilities' and namespace-uri(.)='http://www.opengeospatial.net/wps')
          or (local-name(.)='Capabilities' and namespace-uri(.)='http://www.opengis.net/wps/1.0.0')">
          <xsl:text>true</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>false</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

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
        <MD_ScopeCode
          codeList="./resources/codeList.xml#MD_ScopeCode"
          codeListValue="service"/>
      </hierarchyLevel>

      <!-- mdHrLvName -->

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <xsl:choose>
        <xsl:when test="Service/ContactInformation|
          wfs:Service/wfs:ContactInformation|
          wms:Service/wms:ContactInformation|
                    ows:ServiceProvider|
          owsg:ServiceProvider|
          ows11:ServiceProvider">
          <xsl:for-each select="Service/ContactInformation|
            wfs:Service/wfs:ContactInformation|
            wms:Service/wms:ContactInformation|
                        ows:ServiceProvider|
            owsg:ServiceProvider|
            ows11:ServiceProvider">
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


      <!--mdExtInfo-->
      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <identificationInfo>
        <srv:SV_ServiceIdentification>
          <xsl:apply-templates select="." mode="SrvDataIdentification">
            <xsl:with-param name="topic">
              <xsl:value-of select="$topic"/>
            </xsl:with-param>
            <xsl:with-param name="ows">
              <xsl:value-of select="$ows"/>
            </xsl:with-param>
          </xsl:apply-templates>
        </srv:SV_ServiceIdentification>
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
              <onLine>
                <CI_OnlineResource>
                  <linkage>
                    <URL>
                      <xsl:choose>
                        <xsl:when test="$ows='true'">
                          <xsl:value-of select="//ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href|
                                                  //ows11:Operation[@name='GetCapabilities']/ows11:DCP/ows11:HTTP/ows11:Get/@xlink:href"/>
                        </xsl:when>
                        <xsl:when test="name(.)='WMS_Capabilities'">
                          <xsl:value-of
                            select="//wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href"/>
                        </xsl:when>
                        <xsl:when test="name(.)='WFS_Capabilities'">
                          <xsl:value-of
                            select="//wfs:GetCapabilities/wfs:DCPType/wfs:HTTP/wfs:Get/@onlineResource"/>
                        </xsl:when>
                        <xsl:when test="name(.)='WMT_MS_Capabilities'">
                          <xsl:value-of
                            select="//GetCapabilities/DCPType/HTTP/Get/OnlineResource[1]/@xlink:href"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of
                            select="//wcs:GetCapabilities//wcs:OnlineResource[1]/@xlink:href"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </URL>
                  </linkage>
                  <protocol>
                    <gco:CharacterString>
                      <xsl:choose>
                        <xsl:when
                          test="name(.)='WMT_MS_Capabilities' or name(.)='WMS_Capabilities'">
                          application/vnd.ogc.wms_xml
                        </xsl:when>
                        <xsl:otherwise>WWW:LINK-1.0-http--link</xsl:otherwise>
                      </xsl:choose>
                    </gco:CharacterString>
                  </protocol>
                  <description>
                    <gco:CharacterString>
                      <xsl:choose>
                        <xsl:when test="$ows='true'">
                          <xsl:value-of
                            select="//ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
                        </xsl:when>
                        <xsl:when test="name(.)='WMS_Capabilities'">
                          <xsl:value-of
                            select="//wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href"/>
                        </xsl:when>
                        <xsl:when test="name(.)='WFS_Capabilities'">
                          <xsl:value-of
                            select="//wfs:GetCapabilities/wfs:DCPType/wfs:HTTP/wfs:Get/@onlineResource"/>
                        </xsl:when>
                        <xsl:when test="name(.)='WMT_MS_Capabilities'">
                          <xsl:value-of select="//GetCapabilities//OnlineResource[1]/@xlink:href"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of
                            select="//wcs:GetCapabilities//wcs:OnlineResource[1]/@xlink:href"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </gco:CharacterString>
                  </description>
                </CI_OnlineResource>
              </onLine>
            </MD_DigitalTransferOptions>
          </transferOptions>
        </MD_Distribution>
      </distributionInfo>
      <!--dqInfo-->
      <dataQualityInfo>
        <DQ_DataQuality>
          <scope>
            <DQ_Scope>
              <level>
                <MD_ScopeCode codeListValue="service"
                              codeList="./resources/codeList.xml#MD_ScopeCode"/>
              </level>
              <levelDescription>
                <MD_ScopeDescription>
                  <attributes/>
                </MD_ScopeDescription>
              </levelDescription>
            </DQ_Scope>
          </scope>
          <lineage>
            <LI_Lineage>
              <statement gco:nilReason="missing">
                <gco:CharacterString/>
              </statement>
            </LI_Lineage>
          </lineage>
        </DQ_DataQuality>
      </dataQualityInfo>
      <!--mdConst -->
      <!--mdMaint-->

    </MD_Metadata>
  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
