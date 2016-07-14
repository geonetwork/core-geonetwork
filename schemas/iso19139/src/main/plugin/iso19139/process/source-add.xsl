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
Stylesheet used to update metadata adding a reference to a source record.
-->
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="2.0">

  <!-- Source metadata record UUID -->
  <xsl:param name="sourceUuid"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:MD_Metadata | *[contains(@gco:isoType, 'MD_Metadata')]" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="
        gmd:fileIdentifier | gmd:language | gmd:characterSet | gmd:parentIdentifier | gmd:hierarchyLevel |
        gmd:hierarchyLevelName | gmd:contact | gmd:dateStamp | gmd:metadataStandardName | gmd:metadataStandardVersion |
        gmd:dataSetURI | gmd:locale | gmd:spatialRepresentationInfo | gmd:referenceSystemInfo | gmd:metadataExtensionInfo |
        gmd:identificationInfo | gmd:contentInfo | gmd:distributionInfo"/>
      <xsl:choose>
        <xsl:when test="gmd:dataQualityInfo">
          <xsl:apply-templates select="gmd:dataQualityInfo"/>
        </xsl:when>
        <xsl:otherwise>
          <gmd:dataQualityInfo>
            <gmd:DQ_DataQuality>
              <gmd:lineage>
                <gmd:LI_Lineage>
                  <gmd:source uuidref="{$sourceUuid}"/>
                </gmd:LI_Lineage>
              </gmd:lineage>
            </gmd:DQ_DataQuality>
          </gmd:dataQualityInfo>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="
        gmd:portrayalCatalogueInfo | gmd:metadataConstraints | gmd:applicationSchemaInfo | gmd:metadataMaintenance |
        gmd:series | gmd:describes | gmd:propertyType | gmd:featureType | gmd:featureAttribute"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:dataQualityInfo/*[not(gmd:lineage)]" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="*[name() != 'gmd:lineage']"/>
      <gmd:lineage>
        <gmd:LI_Lineage>
          <gmd:source uuidref="{$sourceUuid}"/>
        </gmd:LI_Lineage>
      </gmd:lineage>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:LI_Lineage|*[contains(@gco:isoType, 'LI_Lineage')]" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of select="gmd:statement|gmd:processStep|gmd:source"/>

      <!-- Only one parent identifier allowed
      - overwriting existing one. -->
      <gmd:source uuidref="{$sourceUuid}"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>
</xsl:stylesheet>
