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
  xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
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


  <!-- Target element to update. The key is based on the concatenation
  of URL+Protocol+Name -->
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
      <xsl:apply-templates select="mdb:dataQualityInfo"/>
      <xsl:apply-templates select="mdb:resourceLineage"/>
      <xsl:apply-templates select="mdb:portrayalCatalogueInfo"/>
      <xsl:if test="$updateKey = ''">
        <xsl:call-template name="create-legend"/>
      </xsl:if>
      <xsl:apply-templates select="mdb:metadataConstraints"/>
      <xsl:apply-templates select="mdb:applicationSchemaInfo"/>
      <xsl:apply-templates select="mdb:metadataMaintenance"/>
      <xsl:apply-templates select="mdb:acquisitionInformation"/>

    </xsl:copy>
  </xsl:template>


  <xsl:template match="mdb:portrayalCatalogueInfo[concat(
                          */mpc:portrayalCatalogueCitation/
                          cit:CI_Citation/cit:onlineResource/cit:CI_OnlineResource/cit:linkage/gco:CharacterString,
                          */mpc:portrayalCatalogueCitation/
                          cit:CI_Citation/cit:title/gco:CharacterString) =
                          normalize-space($updateKey)]">
     <xsl:call-template name="create-legend"/>
  </xsl:template>


  <xsl:template name="create-legend">
      <mdb:portrayalCatalogueInfo>
          <mpc:MD_PortrayalCatalogueReference>
              <mpc:portrayalCatalogueCitation>
                  <cit:CI_Citation>
                      <xsl:if test="$name != ''">
                          <cit:title>
                              <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($name, $mainLang, $useOnlyPTFreeText)"/>
                          </cit:title>
                      </xsl:if>
                      <cit:onlineResource>
                          <cit:CI_OnlineResource>
                              <cit:linkage>
                                  <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($url, $mainLang, $useOnlyPTFreeText)"/>
                              </cit:linkage>
                              <xsl:if test="$desc != ''">
                                  <cit:description>
                                      <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($desc, $mainLang, $useOnlyPTFreeText)"/>
                                  </cit:description>
                              </xsl:if>
                          </cit:CI_OnlineResource>
                      </cit:onlineResource>
                  </cit:CI_Citation>
              </mpc:portrayalCatalogueCitation>
          </mpc:MD_PortrayalCatalogueReference>
      </mdb:portrayalCatalogueInfo>
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
