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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:geonet="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:param name="displayInfo"/>

  <!-- =================================================================== -->

  <!-- Convert ISO profile elements to their base type -->
  <xsl:template match="*[@gco:isoType]">
    <xsl:element name="{@gco:isoType}">
      <xsl:apply-templates select="@*[name() != 'gco:isoType']|*"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
    <xsl:variable name="info" select="geonet:info"/>
    <xsl:element name="{if (@gco:isoType) then @gco:isoType else name()}">
      <xsl:apply-templates select="gmd:fileIdentifier"/>
      <xsl:apply-templates select="gmd:language"/>
      <xsl:apply-templates select="gmd:characterSet"/>
      <xsl:apply-templates select="gmd:parentIdentifier"/>
      <xsl:apply-templates select="gmd:hierarchyLevel"/>
      <xsl:apply-templates select="gmd:hierarchyLevelName"/>
      <gmd:contact/>
      <xsl:apply-templates select="gmd:dateStamp"/>
      <xsl:apply-templates select="gmd:metadataStandardName"/>
      <xsl:apply-templates select="gmd:metadataStandardVersion"/>
      <xsl:apply-templates select="gmd:referenceSystemInfo"/>
      <xsl:apply-templates select="gmd:identificationInfo"/>
      <xsl:apply-templates select="gmd:distributionInfo"/>
      <xsl:apply-templates select="gmd:dataQualityInfo"/>

      <!-- GeoNetwork elements added when resultType is equal to results_with_summary -->
      <xsl:if test="$displayInfo = 'true'">
        <xsl:copy-of select="$info"/>
      </xsl:if>

    </xsl:element>
  </xsl:template>

  <!-- =================================================================== -->

  <xsl:template match="gmd:CI_Citation">
    <xsl:copy>
      <xsl:apply-templates select="gmd:title"/>
      <xsl:apply-templates select="gmd:date[gmd:CI_Date/gmd:dateType/
        gmd:CI_DateTypeCode/@codeListValue='revision']"/>
      <xsl:apply-templates select="gmd:identifier"/>
      <xsl:apply-templates select="gmd:citedResponsibleParty"/>
    </xsl:copy>
  </xsl:template>

  <!-- =================================================================== -->

  <xsl:template match="gmd:MD_Distribution">
    <xsl:copy>
      <xsl:apply-templates select="gmd:distributionFormat"/>
      <xsl:apply-templates select="gmd:transferOptions"/>
    </xsl:copy>
  </xsl:template>

  <!-- =================================================================== -->

  <xsl:template match="gmd:MD_DigitalTransferOptions">
    <xsl:copy>
      <xsl:apply-templates select="gmd:onLine"/>
    </xsl:copy>
  </xsl:template>

  <!-- =================================================================== -->

  <xsl:template match="gmd:CI_OnlineResource">
    <xsl:copy>
      <xsl:apply-templates select="gmd:linkage"/>
    </xsl:copy>
  </xsl:template>

  <!-- =================================================================== -->

  <xsl:template match="gmd:MD_Format">
    <xsl:copy>
      <xsl:apply-templates select="gmd:name"/>
      <xsl:apply-templates select="gmd:version"/>
    </xsl:copy>
  </xsl:template>

  <!-- =================================================================== -->

  <xsl:template match="gmd:DQ_DataQuality">
    <xsl:copy>
      <gmd:scope/>
      <xsl:apply-templates select="gmd:lineage"/>
    </xsl:copy>
  </xsl:template>

  <!-- =================================================================== -->

  <xsl:template match="gmd:EX_Extent">
    <xsl:copy>
      <xsl:apply-templates select="gmd:geographicElement[child::gmd:EX_GeographicBoundingBox]"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:EX_GeographicBoundingBox">
    <xsl:copy>
      <xsl:apply-templates select="gmd:westBoundLongitude"/>
      <xsl:apply-templates select="gmd:eastBoundLongitude"/>
      <xsl:apply-templates select="gmd:southBoundLatitude"/>
      <xsl:apply-templates select="gmd:northBoundLatitude"/>
    </xsl:copy>
  </xsl:template>

  <!-- =================================================================== -->

  <xsl:template match="gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='originator'
    or gmd:role/gmd:CI_RoleCode/@codeListValue='author'
    or gmd:role/gmd:CI_RoleCode/@codeListValue='publisher']">
    <xsl:copy>
      <xsl:apply-templates select="gmd:organisationName"/>
    </xsl:copy>
  </xsl:template>

  <!-- =================================================================== -->

  <xsl:template match="gmd:MD_LegalConstraints">
    <xsl:copy>
      <xsl:apply-templates select="gmd:accessConstraints"/>
    </xsl:copy>
  </xsl:template>

  <!-- =================================================================== -->

  <xsl:template match="gmd:MD_BrowseGraphic">
    <xsl:copy>
      <xsl:apply-templates select="gmd:fileName"/>
    </xsl:copy>
  </xsl:template>

  <!-- =================================================================== -->

  <xsl:template match="gmd:identificationInfo/gmd:MD_DataIdentification|
                       gmd:identificationInfo/*[contains(@gco:isoType, 'MD_DataIdentification')]|
                       gmd:identificationInfo/srv:SV_ServiceIdentification|
                       gmd:identificationInfo/*[contains(@gco:isoType, 'SV_ServiceIdentification')]">
    <xsl:element name="{if (@gco:isoType) then @gco:isoType else name()}">
      <gmd:citation>
        <gmd:CI_Citation>
          <xsl:apply-templates select="gmd:citation/gmd:CI_Citation/gmd:title"/>
          <xsl:apply-templates select="gmd:citation/gmd:CI_Citation/gmd:date"/>
        </gmd:CI_Citation>
      </gmd:citation>
      <xsl:apply-templates select="gmd:abstract"/>
      <xsl:apply-templates select="gmd:pointOfContact"/>
      <xsl:apply-templates select="gmd:graphicOverview"/>
      <xsl:apply-templates select="gmd:resourceConstraints"/>
      <xsl:apply-templates select="gmd:spatialRepresentationType"/>
      <xsl:apply-templates select="gmd:spatialResolution"/>
      <xsl:apply-templates select="gmd:language"/>
      <xsl:apply-templates select="gmd:characterSet"/>
      <xsl:apply-templates select="gmd:topicCategory"/>
      <xsl:apply-templates select="srv:serviceType"/>
      <xsl:apply-templates select="srv:serviceTypeVersion"/>
      <xsl:apply-templates select="*:extent[child::gmd:EX_Extent[child::gmd:geographicElement]]"/>
      <xsl:apply-templates select="srv:couplingType"/>
      <xsl:apply-templates select="srv:containsOperations"/>
    </xsl:element>
  </xsl:template>

  <!-- =================================================================== -->

  <xsl:template match="srv:SV_OperationMetadata">
    <xsl:copy>
      <xsl:apply-templates select="srv:operationName"/>
      <xsl:apply-templates select="srv:DCP"/>
      <xsl:apply-templates select="srv:connectPoint"/>
    </xsl:copy>
  </xsl:template>

  <!-- =================================================================== -->
  <!-- === copy template === -->
  <!-- =================================================================== -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>



