<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<!--
Mapping between :
- CSW 2.0.2 to ISO19119.
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:inspire_common="http://inspire.ec.europa.eu/schemas/common/1.0"
                xmlns:inspire_ds="http://inspire.ec.europa.eu/schemas/inspire_ds/1.0"
                version="2.0"
                xmlns="http://www.isotc211.org/2005/gmd"
                extension-element-prefixes="csw">

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <xsl:param name="uuid">uuid</xsl:param>
  <xsl:param name="lang">eng</xsl:param>
  <xsl:param name="topic"/>

  <xsl:include href="resp-party.xsl"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="csw:Capabilities">
    <MD_Metadata>
      <fileIdentifier>
        <gco:CharacterString>
          <xsl:value-of select="$uuid"/>
        </gco:CharacterString>
      </fileIdentifier>
      <language>
        <LanguageCode codeList="http://www.loc.gov/standards/iso639-2/"
                      codeListValue="{$lang}"/>
        <!-- English is default. Not available in GetCapabilities.
                Selected by user from GUI -->
      </language>
      <characterSet>
        <MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode"
                             codeListValue="utf8"/>
      </characterSet>
      <hierarchyLevel>
        <MD_ScopeCode codeList="./resources/codeList.xml#MD_ScopeCode"
                      codeListValue="service"/>
      </hierarchyLevel>
      <!-- TODO : link to ExtendedCapa/ResourceType -->
      <xsl:choose>
        <xsl:when test="ows:ServiceProvider">
          <xsl:for-each select="
                        ows:ServiceProvider">
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

      <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>
      <dateStamp>
        <gco:DateTime>
          <xsl:value-of select="format-dateTime(current-dateTime(),$df)"/>
        </gco:DateTime>
      </dateStamp>
      <metadataStandardName>
        <gco:CharacterString>ISO 19119/2005</gco:CharacterString>
      </metadataStandardName>
      <metadataStandardVersion>
        <gco:CharacterString>1.0</gco:CharacterString>
      </metadataStandardVersion>
      <identificationInfo>
        <srv:SV_ServiceIdentification>
          <xsl:apply-templates select="ows:ServiceIdentification"/>
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
                      <xsl:value-of
                        select="//ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"
                      />
                    </URL>
                  </linkage>
                  <protocol>
                    <gco:CharacterString>application/xml</gco:CharacterString>
                  </protocol>
                  <description>
                    <gco:CharacterString>
                      <xsl:value-of
                        select="//ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"
                      />
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


  <xsl:template match="ows:ServiceIdentification">
    <citation>
      <CI_Citation>
        <title>
          <gco:CharacterString>
            <xsl:value-of select="ows:Title"/>
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
    <abstract>
      <gco:CharacterString>
        <xsl:value-of select="ows:Abstract"/>
      </gco:CharacterString>
    </abstract>
    <purpose>
      <gco:CharacterString>
        <xsl:for-each
          select="//ows:Constraint[@name='SupportedISOQueryables' or @name='AdditionalQueryables']/ows:Value">
          <xsl:value-of select="."/>
          <xsl:if test="position()!=last()">,</xsl:if>
        </xsl:for-each>
      </gco:CharacterString>
    </purpose>
    <status>
      <MD_ProgressCode codeList="./resources/codeList.xml#MD_ProgressCode"
                       codeListValue="completed"/>
    </status>
    <xsl:for-each select="//ows:ServiceProvider">
      <pointOfContact>
        <CI_ResponsibleParty>
          <xsl:apply-templates select="." mode="RespParty"/>
        </CI_ResponsibleParty>
      </pointOfContact>
    </xsl:for-each>
    <xsl:for-each-group select="//ows:Parameter[@name='outputSchema']/ows:Value" group-by=".">
      <resourceFormat>
        <MD_Format>
          <name>
            <gco:CharacterString>
              <xsl:value-of select="."/>
            </gco:CharacterString>
          </name>
          <version gco:nilReason="inapplicable">
            <gco:CharacterString/>
          </version>
        </MD_Format>
      </resourceFormat>
    </xsl:for-each-group>
    <descriptiveKeywords>
      <xsl:for-each select="ows:Keywords">
        <MD_Keywords>
          <xsl:for-each select="ows:Keyword">
            <keyword>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </keyword>
          </xsl:for-each>
        </MD_Keywords>
      </xsl:for-each>
      <xsl:for-each
        select="//inspire_ds:ExtendedCapabilities/inspire_common:MandatoryKeyword[@xsi:type='inspire_common:classificationOfSpatialDataService']">
        <MD_Keywords>
          <xsl:for-each select="inspire_common:KeywordValue">
            <keyword>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </keyword>
          </xsl:for-each>
          <type>
            <MD_KeywordTypeCode
              codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_KeywordTypeCode"
              codeListValue="theme"/>
          </type>
          <thesaurusName>
            <CI_Citation>
              <title>
                <gco:CharacterString>INSPIRE Service taxonomy</gco:CharacterString>
              </title>
              <alternateTitle gco:nilReason="missing">
                <gco:CharacterString/>
              </alternateTitle>
              <date>
                <CI_Date>
                  <date>
                    <gco:Date>2010-04-22</gco:Date>
                  </date>
                  <dateType>
                    <CI_DateTypeCode
                      codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
                      codeListValue="publication"/>
                  </dateType>
                </CI_Date>
              </date>
            </CI_Citation>
          </thesaurusName>
        </MD_Keywords>
      </xsl:for-each>
    </descriptiveKeywords>
    <resourceConstraints>
      <MD_LegalConstraints>
        <useLimitation>
          <gco:CharacterString>
            <xsl:value-of select="ows:Fees"/>
          </gco:CharacterString>
        </useLimitation>
        <accessConstraints>
          <MD_RestrictionCode
            codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_RestrictionCode"
            codeListValue="otherRestrictions"/>
        </accessConstraints>
        <otherConstraints>
          <gco:CharacterString>
            <xsl:value-of select="ows:AccessConstraints"/>
          </gco:CharacterString>
        </otherConstraints>
      </MD_LegalConstraints>
    </resourceConstraints>

    <srv:serviceType>
      <gco:LocalName codeSpace="www.w3c.org">
        <xsl:choose>
          <xsl:when test="//*:ExtendedCapabilities/inspire_common:SpatialDataServiceType">
            <xsl:value-of select="//*:ExtendedCapabilities/inspire_common:SpatialDataServiceType"/>
          </xsl:when>
          <xsl:otherwise>OGC:CSW</xsl:otherwise>
        </xsl:choose>
      </gco:LocalName>
    </srv:serviceType>
    <srv:serviceTypeVersion>
      <gco:CharacterString>
        <xsl:value-of select="/csw:Capabilities/@version"/>
      </gco:CharacterString>
    </srv:serviceTypeVersion>


    <srv:couplingType>
      <srv:SV_CouplingType codeList="./resources/codeList.xml#SV_CouplingType"
                           codeListValue="tight">
        <xsl:choose>
          <xsl:when test="name(.)='wps:Capabilities' or name(.)='wps1:Capabilities'"
          >loosely
          </xsl:when>
          <xsl:otherwise>tight</xsl:otherwise>
        </xsl:choose>
      </srv:SV_CouplingType>
    </srv:couplingType>

    <xsl:for-each select="//ows:OperationsMetadata/ows:Operation">
      <srv:containsOperations>
        <srv:SV_OperationMetadata>
          <srv:operationName>
            <gco:CharacterString>
              <xsl:value-of select="@name"/>
            </gco:CharacterString>
          </srv:operationName>
          <xsl:for-each select="ows:DCP/ows:HTTP/*">
            <srv:DCP>
              <srv:DCPList codeList="./resources/codeList.xml#DCPList">
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
        </srv:SV_OperationMetadata>
      </srv:containsOperations>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
