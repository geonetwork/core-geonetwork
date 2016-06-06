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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:gml="http://www.opengis.net/gml" version="1.0">
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/" priority="2">
    <gml:GeometryCollection>
      <xsl:variable name="coverage" select="/simpledc/dc:coverage"/>
      <xsl:variable name="n" select="substring-after($coverage,'North ')"/>
      <xsl:variable name="north" select="substring-before($n,',')"/>
      <xsl:variable name="s" select="substring-after($coverage,'South ')"/>
      <xsl:variable name="south" select="substring-before($s,',')"/>
      <xsl:variable name="e" select="substring-after($coverage,'East ')"/>
      <xsl:variable name="east" select="substring-before($e,',')"/>
      <xsl:variable name="w" select="substring-after($coverage,'West ')"/>
      <xsl:variable name="west" select="substring-before($w,'. ')"/>
      <xsl:if test="$w!='' and $e!='' and $n!='' and $s!=''">
        <gml:Polygon>
          <gml:exterior>
            <gml:LinearRing>
              <gml:coordinates><xsl:value-of select="$west"/>,<xsl:value-of select="$north"/>,
                <xsl:value-of select="$east"/>,<xsl:value-of select="$north"/>, <xsl:value-of
                  select="$east"/>,<xsl:value-of select="$south"/>, <xsl:value-of
                  select="$west"/>,<xsl:value-of select="$south"/>, <xsl:value-of select="$west"/>,<xsl:value-of
                  select="$north"/>
              </gml:coordinates>
            </gml:LinearRing>
          </gml:exterior>
        </gml:Polygon>
      </xsl:if>
    </gml:GeometryCollection>
  </xsl:template>
</xsl:stylesheet>
