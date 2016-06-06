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
Stylesheet used to update metadata adding a reference to a parent record.
-->
<xsl:stylesheet xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="2.0">

  <!-- Parent metadata record UUID -->
  <xsl:param name="parentUuid"/>

  <xsl:template match="/simpledc">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of
        select="dc:*|dct:*"/>

      <xsl:if test="not(dct:isPartOf[text() = $parentUuid])">
        <dct:isPartOf>
          <xsl:value-of select="$parentUuid"/>
        </dct:isPartOf>
      </xsl:if>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>
</xsl:stylesheet>
