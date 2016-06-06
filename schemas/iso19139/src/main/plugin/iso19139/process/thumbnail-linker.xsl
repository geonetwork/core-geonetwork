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
                xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="#all"
                version="2.0">

  <!--
      Usage:
        thumbnail_linker?prefix=http://geonetwork.org/thumbnails/
    -->

  <!-- Thumbnail base url (mandatory) -->
  <xsl:param name="prefix"/>
  <!-- Element to use for the file name. -->
  <xsl:param name="thumbnail_name" select="'gmd:fileIdentifier'"/>
  <xsl:param name="thumbnail_desc" select="''"/>
  <xsl:param name="thumbnail_type" select="''"/>
  <!-- File name extension -->
  <xsl:param name="suffix" select="'.png'"/>

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

      <xsl:call-template name="fill"/>

      <xsl:apply-templates select="gmd:graphicOverview"/>

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

    </xsl:copy>
  </xsl:template>

  <xsl:template name="fill">
    <xsl:variable name="id" select="normalize-space(//*[name(.)=$thumbnail_name])"/>
    <xsl:if test="$id!=''">
      <gmd:graphicOverview>
        <gmd:MD_BrowseGraphic>
          <gmd:fileName>
            <gco:CharacterString>
              <xsl:value-of select="concat($prefix, $id, $suffix)"/>
            </gco:CharacterString>
          </gmd:fileName>
          <xsl:if test="$thumbnail_desc!=''">
            <gmd:fileDescription>
              <gco:CharacterString>
                <xsl:value-of select="$thumbnail_desc"/>
              </gco:CharacterString>
            </gmd:fileDescription>
          </xsl:if>
          <xsl:if test="$thumbnail_type!=''">
            <gmd:fileType>
              <gco:CharacterString>
                <xsl:value-of select="$thumbnail_type"/>
              </gco:CharacterString>
            </gmd:fileType>
          </xsl:if>
        </gmd:MD_BrowseGraphic>
      </gmd:graphicOverview>
    </xsl:if>
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
