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
Stylesheet used to remove a reference to a online resource.
-->
<xsl:stylesheet xmlns:geonet="http://www.fao.org/geonetwork" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <xsl:param name="url"/>
  <xsl:param name="name"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template
    match="geonet:*|gmd:onLine[normalize-space(gmd:CI_OnlineResource/gmd:linkage/gmd:URL) = $url and normalize-space(gmd:CI_OnlineResource/gmd:name/gco:CharacterString) = normalize-space($name)]"
    priority="2"/>
  <xsl:template
    match="geonet:*|gmd:onLine[normalize-space(gmd:CI_OnlineResource/gmd:linkage/gmd:URL) = $url and count(gmd:CI_OnlineResource/gmd:name/gmd:PT_FreeText/gmd:textGroup[gmd:LocalisedCharacterString = $name]) > 0]"
    priority="2"/>
  <xsl:template
    match="geonet:*|gmd:onLine[normalize-space(gmd:CI_OnlineResource/gmd:linkage/gmd:URL) = $url and normalize-space(gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString) = 'WWW:DOWNLOAD-1.0-http--download']"
    priority="2"/>
</xsl:stylesheet>
