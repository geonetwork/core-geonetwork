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
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                version="1.0">

  <!-- ================================================================= -->

  <xsl:template match="/root">
    <xsl:apply-templates select="simpledc"/>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="simpledc">
    <xsl:copy>
      <xsl:apply-templates select="dc:title"/>
      <xsl:apply-templates select="dc:creator"/>
      <xsl:apply-templates select="dc:subject"/>
      <xsl:apply-templates select="dc:description"/>
      <xsl:apply-templates select="dc:publisher"/>
      <xsl:apply-templates select="dc:contributor"/>
      <xsl:apply-templates select="dc:date"/>
      <xsl:apply-templates select="dc:type"/>
      <xsl:apply-templates select="dc:format"/>

      <dc:identifier>
        <xsl:value-of select="/root/env/uuid"/>
      </dc:identifier>

      <xsl:apply-templates select="dc:source"/>
      <xsl:apply-templates select="dc:language"/>
      <xsl:apply-templates select="dc:relation"/>
      <xsl:apply-templates select="dc:coverage"/>
      <xsl:apply-templates select="dc:rights"/>

      <xsl:apply-templates select="dct:*"/>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

</xsl:stylesheet>
