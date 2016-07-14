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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                version="1.0">

  <!-- ================================================================= -->

  <xsl:template match="/root">
    <xsl:apply-templates select="gmd:MD_Metadata|*[contains(@gco:isoType, 'MD_Metadata')]"/>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="gmd:MD_Metadata|*[contains(@gco:isoType, 'MD_Metadata')]">
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
      <xsl:apply-templates select="gmd:referenceSystemInfo"/>
      <xsl:apply-templates select="gmd:metadataExtensionInfo"/>

      <xsl:choose>
        <xsl:when test="not(gmd:identificationInfo)">
          <gmd:identificationInfo>
            <gmd:MD_DataIdentification>
              <xsl:call-template name="fill"/>
            </gmd:MD_DataIdentification>
          </gmd:identificationInfo>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="gmd:identificationInfo"/>
        </xsl:otherwise>
      </xsl:choose>

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
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template
    match="gmd:MD_DataIdentification|*[contains(@gco:isoType, 'MD_DataIdentification')]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="gmd:citation"/>
      <xsl:apply-templates select="gmd:abstract"/>
      <xsl:apply-templates select="gmd:purpose"/>
      <xsl:apply-templates select="gmd:credit"/>
      <xsl:apply-templates select="gmd:status"/>
      <xsl:apply-templates select="gmd:pointOfContact"/>
      <xsl:apply-templates select="gmd:resourceMaintenance"/>
      <xsl:apply-templates
        select="gmd:graphicOverview[not(gmd:MD_BrowseGraphic/gmd:fileDescription) or gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString != /root/env/type]"/>

      <xsl:call-template name="fill"/>

      <xsl:apply-templates select="gmd:resourceFormat"/>
      <xsl:apply-templates select="gmd:descriptiveKeywords"/>
      <xsl:apply-templates select="gmd:resourceSpecificUsage"/>
      <xsl:apply-templates select="gmd:resourceConstraints"/>
      <xsl:apply-templates select="gmd:aggregationInfo"/>
      <xsl:apply-templates select="gmd:spatialRepresentationType"/>
      <xsl:apply-templates select="gmd:spatialResolution"/>
      <xsl:apply-templates select="gmd:language"/>
      <xsl:apply-templates select="gmd:characterSet"/>
      <xsl:apply-templates select="gmd:topicCategory"/>
      <xsl:apply-templates select="gmd:environmentDescription"/>
      <xsl:apply-templates select="gmd:extent"/>
      <xsl:apply-templates select="gmd:supplementalInformation"/>

      <xsl:copy-of select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd']"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template
    match="srv:SV_ServiceIdentification|*[contains(@gco:isoType, 'SV_ServiceIdentification')]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="gmd:citation"/>
      <xsl:apply-templates select="gmd:abstract"/>
      <xsl:apply-templates select="gmd:purpose"/>
      <xsl:apply-templates select="gmd:credit"/>
      <xsl:apply-templates select="gmd:status"/>
      <xsl:apply-templates select="gmd:pointOfContact"/>
      <xsl:apply-templates select="gmd:resourceMaintenance"/>
      <xsl:apply-templates
        select="gmd:graphicOverview[not(gmd:MD_BrowseGraphic/gmd:fileDescription) or gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString != /root/env/type]"/>

      <xsl:call-template name="fill"/>

      <xsl:apply-templates select="gmd:resourceFormat"/>
      <xsl:apply-templates select="gmd:descriptiveKeywords"/>
      <xsl:apply-templates select="gmd:resourceSpecificUsage"/>
      <xsl:apply-templates select="gmd:resourceConstraints"/>
      <xsl:apply-templates select="gmd:aggregationInfo"/>

      <xsl:apply-templates select="srv:*"/>

      <xsl:copy-of
        select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd' and namespace-uri()!='http://www.isotc211.org/2005/srv']"/>
    </xsl:copy>
  </xsl:template>


  <!-- ================================================================= -->

  <xsl:template name="fill">
    <gmd:graphicOverview>
      <gmd:MD_BrowseGraphic>
        <gmd:fileName>
          <xsl:variable name="metadataId"
                        select="/root/*/gmd:fileIdentifier/gco:CharacterString/text()"/>
          <gco:CharacterString>
            <xsl:value-of select="concat(
              /root/env/url, '/resources.get?',
              'uuid=', $metadataId,
              '&amp;fname=', /root/env/file)"/>
          </gco:CharacterString>
        </gmd:fileName>

        <gmd:fileDescription>
          <gco:CharacterString>
            <xsl:value-of select="/root/env/type"/>
          </gco:CharacterString>
        </gmd:fileDescription>
        <gmd:fileType>
          <gco:CharacterString>
            <xsl:value-of select="/root/env/ext"/>
          </gco:CharacterString>
        </gmd:fileType>
      </gmd:MD_BrowseGraphic>
    </gmd:graphicOverview>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

</xsl:stylesheet>
