<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2016 Food and Agriculture Organization of the
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
  ~ Author: Emanuele Tajariol (etj at geo-solutions dot it)
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:import href="../../iso19139/process/process-utility.xsl"/>

  <xsl:variable name="isService"
                select="boolean(/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification)"/>

  <!-- CC1 Rec 1 -->
  <!-- Add a gmd:dataQualityInfo with service scope -->

  <xsl:template match="gmd:MD_Metadata">
    <xsl:copy>

      <xsl:apply-templates select="node()[not(self::gmd:portrayalCatalogueInfo)
                                         and not(self::gmd:metadataConstraints)
                                         and not(self::gmd:applicationSchemaInfo)
                                         and not(self::gmd:metadataMaintenance)]"/>

      <gmd:dataQualityInfo>     <!-- 0..n -->
        <gmd:DQ_DataQuality>
          <gmd:scope>
            <gmd:DQ_Scope>
              <gmd:level>
                <gmd:MD_ScopeCode
                  codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_ScopeCode"
                  codeListValue="service"/>
              </gmd:level>
            </gmd:DQ_Scope>
          </gmd:scope>
          <gmd:report>
            <gmd:DQ_DomainConsistency>
              <gmd:result>
                <gmd:DQ_ConformanceResult>
                  <gmd:specification>
                    <gmd:CI_Citation>
                      <gmd:title>
                        <gmx:Anchor
                          xlink:href="http://inspire.ec.europa.eu/metadata-codelist/Category/invocable">
                          invocable
                        </gmx:Anchor>
                      </gmd:title>
                      <gmd:date>
                        <gmd:CI_Date>
                          <gmd:date>
                            <gco:Date>2014-12-11</gco:Date>
                          </gmd:date>
                          <gmd:dateType>
                            <gmd:CI_DateTypeCode
                              codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml #CI_DateTypeCode"
                              codeListValue="publication"/>
                          </gmd:dateType>
                        </gmd:CI_Date>
                      </gmd:date>
                    </gmd:CI_Citation>
                  </gmd:specification>
                  <gmd:explanation>
                    <gco:CharacterString>Conformant to the INSPIRE SDS specifications.
                    </gco:CharacterString>
                  </gmd:explanation>
                  <gmd:pass>
                    <gco:Boolean>true</gco:Boolean>
                  </gmd:pass>
                </gmd:DQ_ConformanceResult>
              </gmd:result>
            </gmd:DQ_DomainConsistency>
          </gmd:report>
        </gmd:DQ_DataQuality>
      </gmd:dataQualityInfo>

      <xsl:apply-templates select="gmd:portrayalCatalogueInfo"/>
      <xsl:apply-templates select="gmd:metadataConstraints"/>
      <xsl:apply-templates select="gmd:applicationSchemaInfo"/>
      <xsl:apply-templates select="gmd:metadataMaintenance"/>

    </xsl:copy>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
