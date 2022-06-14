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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="1.0"
                exclude-result-prefixes="gmd srv gco">

  <xsl:param name="lang"/>
  <xsl:variable name="metadata"
                select="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']"/>

  <xsl:include href="../../layout/utility-tpl-multilingual.xsl"/>

  <xsl:template match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">

    <xsl:variable name="info" select="geonet:info"/>
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="$lang"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="identification"
                  select="gmd:identificationInfo/gmd:MD_DataIdentification|
                          gmd:identificationInfo/*[contains(@gco:isoType, 'MD_DataIdentification')]|
                          gmd:identificationInfo/srv:SV_ServiceIdentification|
                          gmd:identificationInfo/*[contains(@gco:isoType, 'SV_ServiceIdentification')]"/>


    <csw:BriefRecord>

      <xsl:for-each select="gmd:fileIdentifier">
        <dc:identifier>
          <xsl:value-of select="gco:CharacterString"/>
        </dc:identifier>
      </xsl:for-each>

      <!-- DataIdentification -->
      <xsl:for-each select="$identification/gmd:citation/gmd:CI_Citation">
        <xsl:for-each select="gmd:title">
          <dc:title>
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </dc:title>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:for-each select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue">
        <dc:type>
          <xsl:value-of select="."/>
        </dc:type>
      </xsl:for-each>

      <!-- bounding box -->
      <xsl:for-each select="$identification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox|
        $identification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">

        <ows:BoundingBox crs="urn:ogc:def:crs:EPSG:6.6:4326">
          <ows:LowerCorner>
            <xsl:value-of
              select="concat(gmd:southBoundLatitude/gco:Decimal, ' ', gmd:westBoundLongitude/gco:Decimal)"/>
          </ows:LowerCorner>

          <ows:UpperCorner>
            <xsl:value-of
              select="concat(gmd:northBoundLatitude/gco:Decimal, ' ', gmd:eastBoundLongitude/gco:Decimal)"/>
          </ows:UpperCorner>
        </ows:BoundingBox>
      </xsl:for-each>

    </csw:BriefRecord>
  </xsl:template>

  <xsl:template match="*">
    <xsl:apply-templates select="*"/>
  </xsl:template>

</xsl:stylesheet>
