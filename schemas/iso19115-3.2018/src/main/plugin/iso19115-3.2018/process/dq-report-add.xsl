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
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
  xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
  exclude-result-prefixes="#all">

  <xsl:import href="../layout/utility-fn.xsl"/>

  <xsl:param name="url"/>
  <xsl:param name="name"/>
  <xsl:param name="desc"/>
  <xsl:param name="type"/>

  <!-- Target element to update. The key is based on the concatenation
  of URL+title -->
  <xsl:param name="updateKey"/>

  <xsl:variable name="mainLang"
                select="/mdb:MD_Metadata/mdb:defaultLocale/*/lan:language/*/@codeListValue"
                as="xs:string"/>

  <xsl:variable name="useOnlyPTFreeText"
                select="count(//*[lan:PT_FreeText and not(gco:CharacterString)]) > 0"
                as="xs:boolean"/>

  <xsl:variable name="metadataIdentifier"
                select="/mdb:MD_Metadata/mdb:metadataIdentifier[position() = 1]/mcc:MD_Identifier/mcc:code/gco:CharacterString"/>

  <xsl:template match="/mdb:MD_Metadata|*[contains(@gco:isoType, 'mdb:MD_Metadata')]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="mdb:metadataIdentifier"/>
      <xsl:apply-templates select="mdb:defaultLocale"/>
      <xsl:apply-templates select="mdb:parentMetadata"/>
      <xsl:apply-templates select="mdb:metadataScope"/>
      <xsl:apply-templates select="mdb:contact"/>
      <xsl:apply-templates select="mdb:dateInfo"/>
      <xsl:apply-templates select="mdb:metadataStandard"/>
      <xsl:apply-templates select="mdb:metadataProfile"/>
      <xsl:apply-templates select="mdb:alternativeMetadataReference"/>
      <xsl:apply-templates select="mdb:otherLocale"/>
      <xsl:apply-templates select="mdb:metadataLinkage"/>
      <xsl:apply-templates select="mdb:spatialRepresentationInfo"/>
      <xsl:apply-templates select="mdb:referenceSystemInfo"/>
      <xsl:apply-templates select="mdb:metadataExtensionInfo"/>
      <xsl:apply-templates select="mdb:identificationInfo"/>
      <xsl:apply-templates select="mdb:contentInfo"/>
      <xsl:apply-templates select="mdb:distributionInfo"/>


      <xsl:choose>
        <xsl:when test="$type = 'qualityReport' or $type = 'qualitySpecification'">
          <xsl:variable name="hasDQ"
                        select="count(mdb:dataQualityInfo) > 0"/>

          <xsl:choose>
            <xsl:when test="$hasDQ">
              <xsl:for-each select="mdb:dataQualityInfo">
                <xsl:copy>
                  <xsl:apply-templates select="@*"/>
                  <mdq:DQ_DataQuality>
                    <xsl:apply-templates select="*/mdq:scope"/>
                    <xsl:apply-templates select="*/mdq:standaloneQualityReport"/>
                    <!-- Insert report in the first DQ section -->
                    <xsl:if test="position() = 1 and $type = 'qualityReport'">
                      <xsl:call-template name="create-dq-standalone-report"/>
                    </xsl:if>
                    <xsl:apply-templates select="*/mdq:report"/>
                    <xsl:if test="position() = 1 and $type = 'qualitySpecification'">
                      <xsl:call-template name="create-dq-specification-report"/>
                    </xsl:if>
                  </mdq:DQ_DataQuality>
                </xsl:copy>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <mdb:dataQualityInfo>
                <mdq:DQ_DataQuality>
                  <mdq:scope>
                    <mcc:MD_Scope>
                      <mcc:level>
                        <mcc:MD_ScopeCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_ScopeCode"
                                          codeListValue=""/>
                      </mcc:level>
                    </mcc:MD_Scope>
                  </mdq:scope>
                  <xsl:if test="$type = 'qualityReport' and $updateKey = ''">
                    <xsl:call-template name="create-dq-standalone-report"/>
                  </xsl:if>
                  <xsl:if test="$type = 'qualitySpecification' and $updateKey = ''">
                    <xsl:call-template name="create-dq-specification-report"/>
                  </xsl:if>
                </mdq:DQ_DataQuality>
              </mdb:dataQualityInfo>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="mdb:dataQualityInfo"/>
        </xsl:otherwise>
      </xsl:choose>



      <!-- Add lineage production report -->
      <xsl:choose>
        <xsl:when test="$type = 'lineage'">
          <xsl:variable name="hasLineage" select="count(mdb:resourceLineage) > 0"/>

          <xsl:choose>
            <xsl:when test="$hasLineage">
              <xsl:for-each select="mdb:resourceLineage">
                <xsl:copy>
                  <xsl:apply-templates select="@*"/>
                  <mrl:LI_Lineage>
                    <xsl:apply-templates select="*/mrl:statement"/>
                    <xsl:apply-templates select="*/mrl:scope"/>
                    <xsl:apply-templates select="*/mrl:additionalDocumentation"/>
                    <!-- Insert lineage report in the first lineage bloc after
                    existing documentation -->
                    <xsl:if test="position() = 1  and $updateKey = ''">
                      <xsl:call-template name="create-dq-lineage-report"/>
                    </xsl:if>
                    <xsl:apply-templates select="*/mrl:source"/>
                    <xsl:apply-templates select="*/mrl:processStep"/>
                  </mrl:LI_Lineage>
                </xsl:copy>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <mdb:resourceLineage>
                <mrl:LI_Lineage>
                  <mrl:statement>
                    <gco:CharacterString/>
                  </mrl:statement>
                  <mrl:scope>
                    <mcc:MD_Scope>
                      <mcc:level>
                        <mcc:MD_ScopeCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_ScopeCode"
                                          codeListValue="dataset"/>
                      </mcc:level>
                    </mcc:MD_Scope>
                  </mrl:scope>
                  <xsl:if test="$updateKey = ''">
                    <xsl:call-template name="create-dq-lineage-report"/>
                  </xsl:if>
                </mrl:LI_Lineage>
              </mdb:resourceLineage>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="mdb:resourceLineage"/>
        </xsl:otherwise>
      </xsl:choose>


      <xsl:apply-templates select="mdb:portrayalCatalogueInfo"/>
      <xsl:apply-templates select="mdb:metadataConstraints"/>
      <xsl:apply-templates select="mdb:applicationSchemaInfo"/>
      <xsl:apply-templates select="mdb:metadataMaintenance"/>
      <xsl:apply-templates select="mdb:acquisitionInformation"/>
      
    </xsl:copy>
  </xsl:template>


  <xsl:template match="mdq:report[concat(
                          mdq:DQ_DomainConsistency/mdq:result/mdq:DQ_ConformanceResult/mdq:specification/
                          cit:CI_Citation/cit:onlineResource/cit:CI_OnlineResource/cit:linkage/gco:CharacterString,
                          mdq:DQ_DomainConsistency/mdq:result/mdq:DQ_ConformanceResult/mdq:specification/
                          cit:CI_Citation/cit:title/gco:CharacterString) =
                          normalize-space($updateKey)]">
      <xsl:call-template name="create-dq-specification-report"/>
  </xsl:template>

  <xsl:template name="create-dq-specification-report">
    <mdq:report>
      <mdq:DQ_DomainConsistency>
        <mdq:result>
          <mdq:DQ_ConformanceResult>
            <mdq:specification>
              <cit:CI_Citation>
                <cit:title>
                  <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($name, $mainLang, $useOnlyPTFreeText)"/>
                </cit:title>
                <cit:onlineResource>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($url, $mainLang, $useOnlyPTFreeText)"/>
                    </cit:linkage>
                  </cit:CI_OnlineResource>
                </cit:onlineResource>
              </cit:CI_Citation>
            </mdq:specification>
            <xsl:if test="$desc">
              <mdq:explanation>
                <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($desc, $mainLang, $useOnlyPTFreeText)"/>
              </mdq:explanation>
            </xsl:if>
            <mdq:pass>
              <gco:Boolean>true</gco:Boolean>
            </mdq:pass>
          </mdq:DQ_ConformanceResult>
        </mdq:result>
      </mdq:DQ_DomainConsistency>
    </mdq:report>
  </xsl:template>


  <xsl:template match="mdq:standaloneQualityReport[concat(
                          mdq:DQ_StandaloneQualityReportInformation/mdq:reportReference/
                          cit:CI_Citation/cit:onlineResource/cit:CI_OnlineResource/cit:linkage/gco:CharacterString,
                          mdq:DQ_StandaloneQualityReportInformation/mdq:reportReference/
                          cit:CI_Citation/cit:title/gco:CharacterString) =
                          normalize-space($updateKey)]">
    <xsl:call-template name="create-dq-standalone-report"/>
  </xsl:template>

  <xsl:template name="create-dq-standalone-report">
    <mdq:standaloneQualityReport>
      <mdq:DQ_StandaloneQualityReportInformation>
        <mdq:reportReference>
          <cit:CI_Citation>
            <cit:title>
              <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($name, $mainLang, $useOnlyPTFreeText)"/>
            </cit:title>
            <cit:onlineResource>
              <cit:CI_OnlineResource>
                <cit:linkage>
                  <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($url, $mainLang, $useOnlyPTFreeText)"/>
                </cit:linkage>
              </cit:CI_OnlineResource>
            </cit:onlineResource>
          </cit:CI_Citation>
        </mdq:reportReference>
        <xsl:if test="$desc">
          <mdq:abstract>
            <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($desc, $mainLang, $useOnlyPTFreeText)"/>
          </mdq:abstract>
        </xsl:if>
      </mdq:DQ_StandaloneQualityReportInformation>
    </mdq:standaloneQualityReport>
  </xsl:template>

  <xsl:template match="mrl:additionalDocumentation[concat(
                          cit:CI_Citation/cit:onlineResource/cit:CI_OnlineResource/cit:linkage/gco:CharacterString,
                          cit:CI_Citation/cit:title/gco:CharacterString) =
                          normalize-space($updateKey)]">
    <xsl:call-template name="create-dq-lineage-report"/>
  </xsl:template>

  <xsl:template name="create-dq-lineage-report">
    <mrl:additionalDocumentation>
      <cit:CI_Citation>
        <cit:title>
          <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($name, $mainLang, $useOnlyPTFreeText)"/>
        </cit:title>
        <cit:onlineResource>
          <cit:CI_OnlineResource>
            <cit:linkage >
              <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($url, $mainLang, $useOnlyPTFreeText)"/>
            </cit:linkage>
            <cit:protocol>
              <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
            </cit:protocol>
            <xsl:if test="$desc">
              <cit:description>
                <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($desc, $mainLang, $useOnlyPTFreeText)"/>
              </cit:description>
            </xsl:if>
          </cit:CI_OnlineResource>
        </cit:onlineResource>
      </cit:CI_Citation>
    </mrl:additionalDocumentation>
  </xsl:template>
  
  <!-- Remove geonet:* elements. -->
  <xsl:template match="gn:*" priority="2"/>
  
  <!-- Copy everything. -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
