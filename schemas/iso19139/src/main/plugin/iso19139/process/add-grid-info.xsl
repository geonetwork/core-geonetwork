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
Stylesheet used to add vector information field to a metadata record.
-->
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:template match="gmd:MD_DataIdentification">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="gmd:citation"/>
      <xsl:apply-templates select="gmd:abstract"/>
      <xsl:apply-templates select="gmd:purpose"/>
      <xsl:apply-templates select="gmd:credit"/>
      <xsl:apply-templates select="gmd:status"/>
      <xsl:apply-templates select="gmd:pointOfContact"/>
      <xsl:apply-templates select="gmd:resourceMaintenance"/>
      <xsl:apply-templates select="gmd:graphicOverview"/>
      <xsl:apply-templates select="gmd:resourceFormat"/>
      <xsl:apply-templates select="gmd:descriptiveKeywords"/>
      <xsl:apply-templates select="gmd:resourceSpecificUsage"/>
      <xsl:apply-templates select="gmd:resourceConstraints"/>
      <xsl:apply-templates select="gmd:aggregationInfo"/>


      <!-- Force spatial resolution to be grid. -->
      <!-- This means removing any vector codelist value or other. -->
      <!--<xsl:apply-templates select="gmd:spatialRepresentationType"/>-->
      <gmd:spatialRepresentationType>
        <gmd:MD_SpatialRepresentationTypeCode
          codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_SpatialRepresentationTypeCode"
          codeListValue="grid"/>
      </gmd:spatialRepresentationType>


      <!-- Preserve any spatial resolution having a distance -->
      <xsl:apply-templates select="gmd:spatialResolution[.//gmd:distance]"/>

      <!-- And add an empty one if none -->
      <xsl:if test="not(gmd:spatialResolution)">

        <gmd:spatialResolution>
          <gmd:MD_Resolution>
            <gmd:distance>
              <gco:Distance uom=""/>
            </gmd:distance>
          </gmd:MD_Resolution>
        </gmd:spatialResolution>
      </xsl:if>

      <!-- This means removing any resolutions. -->


      <xsl:apply-templates select="gmd:language"/>
      <xsl:apply-templates select="gmd:characterSet"/>
      <xsl:apply-templates select="gmd:topicCategory"/>
      <xsl:apply-templates select="gmd:environmentDescription"/>
      <xsl:apply-templates select="gmd:extent"/>
      <xsl:apply-templates select="gmd:supplementalInformation"/>
    </xsl:copy>
  </xsl:template>


  <!-- Remove any MD_VectorSpatialRepresentation spatial representation info related to vector. -->
  <xsl:template match="gmd:spatialRepresentationInfo[gmd:MD_VectorSpatialRepresentation]"/>


  <!-- Add spatial representation info for a vector -->
  <xsl:template match="/gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="gmd:fileIdentifier"/>
      <xsl:apply-templates select="gmd:language"/>
      <xsl:apply-templates select="gmd:characterSet"/>
      <xsl:apply-templates select="gmd:parentIdentifier"/>
      <xsl:apply-templates select="gmd:hierarchyLevel"/>
      <xsl:apply-templates select="gmd:hierarchyLevelName"/>
      <xsl:apply-templates select="gmd:contact"/>
      <xsl:apply-templates select="gmd:dateStamp"/>
      <xsl:apply-templates select="gmd:metadataStandardName"/>
      <xsl:apply-templates select="gmd:metadataStandardVersion"/>
      <xsl:apply-templates select="gmd:dataSetURI"/>
      <xsl:apply-templates select="gmd:locale"/>
      <xsl:apply-templates select="gmd:spatialRepresentationInfo"/>

      <xsl:if test="not(gmd:spatialRepresentationInfo[gmd:MD_Georectified])">
        <!-- applicable for gridded datasets, used to describe time and vertical axis -->
        <gmd:spatialRepresentationInfo>
          <gmd:MD_GridSpatialRepresentation>
            <gmd:numberOfDimensions>
              <!--  MANDATORY, CONSTANT: 4 axis always (x,y,z,t)-->
              <gco:Integer>4</gco:Integer>
            </gmd:numberOfDimensions>
            <gmd:axisDimensionProperties>
              <gmd:MD_Dimension>
                <gmd:dimensionName>
                  <gmd:MD_DimensionNameTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_DimensionNameTypeCode" codeListValue="vertical"/>
                </gmd:dimensionName>
                <!-- OPTIONAL: number of vertical levels -->
                <gmd:dimensionSize>
                  <gco:Integer></gco:Integer>
                </gmd:dimensionSize>
              </gmd:MD_Dimension>
            </gmd:axisDimensionProperties>
            <gmd:axisDimensionProperties>
              <gmd:MD_Dimension>
                <gmd:dimensionName>
                  <gmd:MD_DimensionNameTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_DimensionNameTypeCode" codeListValue="time"/>
                </gmd:dimensionName>
                <gmd:dimensionSize/>
                <!-- OPTIONAL: temporal resolution (mean period between each time steps) -->
                <gmd:resolution>
                  <gco:Measure uom=""></gco:Measure>
                </gmd:resolution>
              </gmd:MD_Dimension>
            </gmd:axisDimensionProperties>
            <gmd:cellGeometry>
              <gmd:MD_CellGeometryCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_CellGeometryCode" codeListValue="surface"/>
            </gmd:cellGeometry>
            <gmd:transformationParameterAvailability>
              <gco:Boolean>false</gco:Boolean>
            </gmd:transformationParameterAvailability>
          </gmd:MD_GridSpatialRepresentation>
        </gmd:spatialRepresentationInfo>
      </xsl:if>

      <xsl:apply-templates select="gmd:referenceSystemInfo"/>
      <xsl:apply-templates select="gmd:metadataExtensionInfo"/>
      <xsl:apply-templates select="gmd:identificationInfo"/>
      <xsl:apply-templates select="gmd:contentInfo"/>
      <xsl:apply-templates select="gmd:distributionInfo"/>
      <xsl:apply-templates select="gmd:dataQualityInfo"/>
      <xsl:apply-templates select="gmd:portrayalCatalogueInfo"/>
      <xsl:apply-templates select="gmd:metadataConstraints"/>
      <xsl:apply-templates select="gmd:applicationSchemaInfo"/>
      <xsl:apply-templates select="gmd:metadataMaintenance"/>
      <xsl:apply-templates select="gmd:series"/>
      <xsl:apply-templates select="gmd:describes"/>
      <xsl:apply-templates select="gmd:propertyType"/>
      <xsl:apply-templates select="gmd:featureType"/>
      <xsl:apply-templates select="gmd:featureAttribute"/>
      <xsl:apply-templates select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd' and
                                     namespace-uri()!='http://www.isotc211.org/2005/srv']"/>
    </xsl:copy>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
