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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="2.0" exclude-result-prefixes="dc dct">
  <!--
   ============================================================================================
  -->
  <xsl:param name="mdChangeDate"/>
  <!--
   ============================================================================================
  -->
  <xsl:template match="simpledc">
    <gmd:MD_Metadata xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:ows="http://www.opengis.net/ows" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <gmd:fileIdentifier>
        <gco:CharacterString>
          <xsl:value-of select="dc:identifier"/>
        </gco:CharacterString>
      </gmd:fileIdentifier>
      <!--  metadata language  -->
      <xsl:for-each select="dc:language">
        <gmd:language>
          <gco:CharacterString>
            <xsl:value-of select="."/>
          </gco:CharacterString>
        </gmd:language>
      </xsl:for-each>
      <xsl:if test="not(dc:language)">
        <gmd:language>
          <gco:CharacterString>eng</gco:CharacterString>
        </gmd:language>
      </xsl:if>
      <gmd:hierarchyLevel>
        <gmd:MD_ScopeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_ScopeCode" codeListValue="{dc:type}"/>
      </gmd:hierarchyLevel>
      <gmd:contact/>
      <gmd:dateStamp>
        <gco:DateTime>
          <xsl:choose>
            <xsl:when test="string(dc:date)">
              <xsl:value-of select="dc:date"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$mdChangeDate"/>
            </xsl:otherwise>
          </xsl:choose>
        </gco:DateTime>
      </gmd:dateStamp>
      <gmd:identificationInfo>
        <gmd:MD_DataIdentification>
          <gmd:citation>
            <gmd:CI_Citation>
              <!--  title  -->
              <gmd:title>
                <gco:CharacterString>
                  <xsl:value-of select="dc:title"/>
                </gco:CharacterString>
              </gmd:title>
              <!--  revision date  -->
              <xsl:for-each select="dct:modified">
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:Date>
                        <xsl:value-of select="."/>
                      </gco:Date>
                    </gmd:date>
                    <gmd:dateType>
                      <gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode" codeListValue="revision"/>
                    </gmd:dateType>
                  </gmd:CI_Date>
                </gmd:date>
              </xsl:for-each>
              <xsl:if test="not(dct:modified)">
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:Date/>
                    </gmd:date>
                    <gmd:dateType>
                      <gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode" codeListValue="revision"/>
                    </gmd:dateType>
                  </gmd:CI_Date>
                </gmd:date>
              </xsl:if>
              <xsl:for-each select="dc:creator">
                <gmd:citedResponsibleParty>
                  <gmd:CI_ResponsibleParty>
                    <gmd:organisationName>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </gmd:organisationName>
                    <gmd:role>
                      <gmd:CI_RoleCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode" codeListValue="originator"/>
                    </gmd:role>
                  </gmd:CI_ResponsibleParty>
                </gmd:citedResponsibleParty>
              </xsl:for-each>
              <xsl:for-each select="dc:publisher">
                <gmd:citedResponsibleParty>
                  <gmd:CI_ResponsibleParty>
                    <gmd:organisationName>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </gmd:organisationName>
                    <gmd:role>
                      <gmd:CI_RoleCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode" codeListValue="publisher"/>
                    </gmd:role>
                  </gmd:CI_ResponsibleParty>
                </gmd:citedResponsibleParty>
              </xsl:for-each>
              <xsl:for-each select="dc:contributor">
                <gmd:citedResponsibleParty>
                  <gmd:CI_ResponsibleParty>
                    <gmd:organisationName>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </gmd:organisationName>
                    <gmd:role>
                      <gmd:CI_RoleCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode" codeListValue="author"/>
                    </gmd:role>
                  </gmd:CI_ResponsibleParty>
                </gmd:citedResponsibleParty>
              </xsl:for-each>
            </gmd:CI_Citation>
          </gmd:citation>
          <!--  description  -->
          <xsl:for-each select="dc:description">
            <gmd:abstract>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </gmd:abstract>
          </xsl:for-each>
          <!--  subject  -->
          <gmd:descriptiveKeywords>
            <gmd:MD_Keywords>
              <xsl:for-each select="dc:subject">
                <xsl:if test="string(.)">
                  <gmd:keyword>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </gmd:keyword>
                </xsl:if>
              </xsl:for-each>
            </gmd:MD_Keywords>
          </gmd:descriptiveKeywords>
          <!--  constraints  -->
          <xsl:for-each select="dc:rights">
            <gmd:resourceConstraints>
              <gmd:MD_LegalConstraints>
                <gmd:otherConstraints>
                  <gco:CharacterString>
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:otherConstraints>
              </gmd:MD_LegalConstraints>
            </gmd:resourceConstraints>
          </xsl:for-each>
          <!--  data language  -->
          <xsl:for-each select="dc:language">
            <gmd:language>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </gmd:language>
          </xsl:for-each>
          <xsl:if test="not(dc:language)">
            <gmd:language>
              <gco:CharacterString>ger</gco:CharacterString>
            </gmd:language>
          </xsl:if>
          <!--  bounding box  -->
          <xsl:for-each select="ows:BoundingBox">
            <gmd:extent>
              <gmd:EX_Extent>
                <gmd:geographicElement>
                  <gmd:EX_GeographicBoundingBox>
                    <gmd:westBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="tokenize(ows:UpperCorner, ' ')[0]"/>
                      </gco:Decimal>
                    </gmd:westBoundLongitude>
                    <gmd:eastBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="tokenize(ows:LowerCorner, ' ')[0]"/>
                      </gco:Decimal>
                    </gmd:eastBoundLongitude>
                    <gmd:southBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="tokenize(ows:LowerCorner, ' ')[1]"/>
                      </gco:Decimal>
                    </gmd:southBoundLatitude>
                    <gmd:northBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="tokenize(ows:UpperCorner, ' ')[1]"/>
                      </gco:Decimal>
                    </gmd:northBoundLatitude>
                  </gmd:EX_GeographicBoundingBox>
                </gmd:geographicElement>
              </gmd:EX_Extent>
            </gmd:extent>
          </xsl:for-each>
          <xsl:for-each select="dc:coverage">
            <xsl:variable name="bboxTokens" select="tokenize(., ',')"/>
            <gmd:extent>
              <gmd:EX_Extent>
                <gmd:geographicElement>
                  <gmd:EX_GeographicBoundingBox>
                    <gmd:westBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="normalize-space(replace($bboxTokens[4], 'West', ''))"/>
                      </gco:Decimal>
                    </gmd:westBoundLongitude>
                    <gmd:eastBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="normalize-space(replace($bboxTokens[3], 'East', ''))"/>
                      </gco:Decimal>
                    </gmd:eastBoundLongitude>
                    <gmd:southBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="normalize-space(replace($bboxTokens[2], 'South', ''))"/>
                      </gco:Decimal>
                    </gmd:southBoundLatitude>
                    <gmd:northBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="normalize-space(replace($bboxTokens[1], 'North', ''))"/>
                      </gco:Decimal>
                    </gmd:northBoundLatitude>
                  </gmd:EX_GeographicBoundingBox>
                </gmd:geographicElement>
              </gmd:EX_Extent>
            </gmd:extent>
          </xsl:for-each>
        </gmd:MD_DataIdentification>
      </gmd:identificationInfo>
      <!--  Lineage  -->
      <xsl:if test="dc:source">
        <gmd:dataQualityInfo>
          <gmd:DQ_DataQuality>
            <gmd:lineage>
              <gmd:LI_Lineage>
                <gmd:statement>
                  <gco:CharacterString>
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:statement>
              </gmd:LI_Lineage>
            </gmd:lineage>
          </gmd:DQ_DataQuality>
        </gmd:dataQualityInfo>
      </xsl:if>
      <!--  distribution format  -->
      <xsl:if test="dc:format">
        <gmd:distributionInfo>
          <gmd:MD_Distribution>
            <xsl:for-each select="dc:format">
              <gmd:distributionFormat>
                <gmd:MD_Format>
                  <gmd:name>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </gmd:name>
                  <gmd:version>
                    <gco:CharacterString/>
                  </gmd:version>
                </gmd:MD_Format>
              </gmd:distributionFormat>
            </xsl:for-each>
          </gmd:MD_Distribution>
        </gmd:distributionInfo>
      </xsl:if>
    </gmd:MD_Metadata>
  </xsl:template>
  <!--
   ============================================================================================
  -->
  <xsl:template match="*">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <!--
   ============================================================================================
  -->
  <xsl:template match="@xsi:noNamespaceSchemaLocation"/>
  <!--
   ============================================================================================
  -->
</xsl:stylesheet>
