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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ows="http://www.opengis.net/ows"
                xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:gml="http://www.opengis.net/gml"
                version="1.0">
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/" priority="2">
    <gml:GeometryCollection>
      <!-- csw:Record contains ows:BoundingBox element.
                Example:
                <ows:BoundingBox crs="urn:x-ogc:def:crs:EPSG:6.11:4326">
                  <ows:LowerCorner>47.595 -4.097</ows:LowerCorner>
                  <ows:UpperCorner>51.217 0.889</ows:UpperCorner>
                </ows:BoundingBox>

                TODO : handle CRS
            -->
      <xsl:variable name="lc" select="/csw:Record/ows:BoundingBox/ows:LowerCorner"/>
      <xsl:variable name="uc" select="/csw:Record/ows:BoundingBox/ows:UpperCorner"/>
      <xsl:variable name="n" select="substring-after($uc,' ')"/>
      <xsl:variable name="s" select="substring-after($lc,' ')"/>
      <xsl:variable name="e" select="substring-before($uc,' ')"/>
      <xsl:variable name="w" select="substring-before($lc,' ')"/>
      <xsl:if test="$w!='' and $e!='' and $n!='' and $s!=''">
        <gml:Polygon>
          <gml:exterior>
            <gml:LinearRing>
              <gml:coordinates><xsl:value-of select="$w"/>,<xsl:value-of select="$n"/>,
                <xsl:value-of select="$e"/>,<xsl:value-of select="$n"/>, <xsl:value-of select="$e"/>,<xsl:value-of
                  select="$s"/>, <xsl:value-of select="$w"/>,<xsl:value-of select="$s"/>,
                <xsl:value-of select="$w"/>,<xsl:value-of select="$n"/>
              </gml:coordinates>
            </gml:LinearRing>
          </gml:exterior>
        </gml:Polygon>
      </xsl:if>
    </gml:GeometryCollection>
  </xsl:template>
</xsl:stylesheet>
