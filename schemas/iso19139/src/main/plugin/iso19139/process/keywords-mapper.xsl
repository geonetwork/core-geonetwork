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
                xmlns:exslt="http://exslt.org/common" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                version="2.0" exclude-result-prefixes="exslt">

  <!-- The keyword separator -->
  <xsl:param name="separator">;</xsl:param>
  <!-- The keyword to search for -->
  <xsl:param name="search">key1;key2/</xsl:param>
  <!-- The keyword to put in -->
  <xsl:param name="replace">newkey1;newkey2</xsl:param>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

  <!-- Build a map from the inputs parameters -->
  <xsl:variable name="map">
    <xsl:for-each select="tokenize($search, $separator)">
      <xsl:variable name="pos" select="position()"/>
      <map key="{.}" value="{tokenize($replace, $separator)[position() = $pos]}"/>
    </xsl:for-each>
  </xsl:variable>

  <!-- Map all keywords to new value.
      If no new value define, current value is used. -->
  <xsl:template match="gmd:keyword" priority="2">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>

      <xsl:variable name="mapNodes" select="exslt:node-set($map)"/>
      <xsl:variable name="currentValue" select="gco:CharacterString"/>
      <xsl:variable name="newValue" select="$mapNodes/map[@key=$currentValue]/@value"/>
      <!--<xsl:message>Mapping '<xsl:value-of select="$currentValue"/>' with '<xsl:value-of select="$newValue"/>'</xsl:message>-->
      <gco:CharacterString>
        <xsl:choose>
          <xsl:when test="$newValue!=''">
            <xsl:value-of select="$newValue"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$currentValue"/>
          </xsl:otherwise>
        </xsl:choose>
      </gco:CharacterString>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
