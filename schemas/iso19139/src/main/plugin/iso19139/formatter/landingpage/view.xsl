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
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                version="2.0"
                exclude-result-prefixes="#all">
  <xsl:import href="../xsl-view/view.xsl"/>

  <xsl:variable name="standardName"
                select="$metadata/gmd:metadataStandardName/*/text()"/>

  <!--
  See client app config
  https://gitlab.ifremer.fr/sextant/geonetwork/-/blob/sextant-6.7.x/web-ui/src/main/resources/catalog/views/sextant/config.js#L218-275
  -->
  <xsl:variable name="view">
    <xsl:choose>
      <xsl:when test="$standardName = 'ISO 19115:2003/19139 - EMODNET - BATHYMETRY'
                      or $standardName = 'ISO 19115:2003/19139 - EMODNET - HYDROGRAPHY'">emodnetHydrography</xsl:when>
      <xsl:when test="$standardName = 'ISO 19115:2003/19139 - EMODNET - SDN'">sdn</xsl:when>
      <xsl:otherwise>sextant</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="template">
    <xsl:choose>
      <xsl:when test="$standardName = 'ISO 19115:2003/19139 - EMODNET - BATHYMETRY'"></xsl:when>
      <xsl:otherwise>sextant-summary-view</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="tabs" select="false"/>
</xsl:stylesheet>
