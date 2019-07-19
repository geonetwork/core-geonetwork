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
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                version="2.0">

  <xsl:output method="xml" indent="no"/>

  <xsl:template match="/" priority="2">
    <gml:GeometryCollection>
      <xsl:apply-templates/>
    </gml:GeometryCollection>
  </xsl:template>

  <xsl:template match="*">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text()"/>

  <xsl:template
    match="gmd:EX_BoundingPolygon[string(gmd:extentTypeCode/gco:Boolean) != 'false' and string(gmd:extentTypeCode/gco:Boolean) != '0']"
    priority="2">
    <!-- Index geometries which are
    * not multicurve
    * not empty (Empty geometry cause issue with shapefile index https://github.com/geonetwork/core-geonetwork/issues/259)
          -->
    <xsl:for-each select="gmd:polygon/(gml:*|gml320:*)[
                            local-name() != 'MultiCurve' and
                            count(*) > 0 and
                            .//(gml:posList|gml320:posList) != '']">
      <xsl:copy-of select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="gmd:EX_GeographicBoundingBox[not(../../gmd:geographicElement/gmd:EX_BoundingPolygon)]" priority="2">
    <xsl:variable name="w" select="./gmd:westBoundLongitude/gco:Decimal/text()"/>
    <xsl:variable name="e" select="./gmd:eastBoundLongitude/gco:Decimal/text()"/>
    <xsl:variable name="n" select="./gmd:northBoundLatitude/gco:Decimal/text()"/>
    <xsl:variable name="s" select="./gmd:southBoundLatitude/gco:Decimal/text()"/>
    <xsl:if test="$w!='' and $e!='' and $n!='' and $s!=''">
      <gml:Polygon>
        <gml:exterior>
          <gml:LinearRing>
            <gml:coordinates><xsl:value-of select="$w"/>,<xsl:value-of select="$n"/>, <xsl:value-of
              select="$e"/>,<xsl:value-of select="$n"/>, <xsl:value-of select="$e"/>,<xsl:value-of
              select="$s"/>, <xsl:value-of select="$w"/>,<xsl:value-of select="$s"/>, <xsl:value-of
              select="$w"/>,<xsl:value-of select="$n"/>
            </gml:coordinates>
          </gml:LinearRing>
        </gml:exterior>
      </gml:Polygon>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
