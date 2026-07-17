<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
Stylesheet used to add a publication date of a metadata record.
-->
<xsl:stylesheet xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- The publication date  -->
  <xsl:param name="publicationDate"/>

  <xsl:template match="mdb:MD_Metadata">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of
        select="mdb:metadataIdentifier|
               mdb:defaultLocale|
               mdb:parentMetadata|
               mdb:metadataScope|
               mdb:contact
               "/>

      <xsl:choose>
        <xsl:when test="string($publicationDate)">
          <mdb:dateInfo>
            <cit:CI_Date>
              <cit:date>
                <gco:DateTime><xsl:value-of select="$publicationDate"/></gco:DateTime>
              </cit:date>
              <cit:dateType>
                <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="publication"/>
              </cit:dateType>
            </cit:CI_Date>
          </mdb:dateInfo>

          <xsl:apply-templates select="mdb:dateInfo[not(cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication')]" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="mdb:dateInfo" />
        </xsl:otherwise>
      </xsl:choose>

      <xsl:copy-of
        select="mdb:metadataStandard|
               mdb:metadataProfile|
               mdb:alternativeMetadataReference|
               mdb:otherLocale|
               mdb:metadataLinkage|
                mdb:spatialRepresentationInfo|
                mdb:referenceSystemInfo|
                mdb:metadataExtensionInfo|
                mdb:identificationInfo|
                mdb:contentInfo|
                mdb:distributionInfo|
                mdb:dataQualityInfo|
                mdb:resourceLineage|
                mdb:portrayalCatalogueInfo|
                mdb:metadataConstraints|
                mdb:applicationSchemaInfo|
                mdb:metadataMaintenance|
                mdb:acquisitionInformation
               "/>

    </xsl:copy>
  </xsl:template>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template
    match="geonet:*"
    priority="2"/>
</xsl:stylesheet>
