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
  Processing steps are :
  * update host and port

  Parameters:
  * process=xlinks-href-update (fixed value)
  * search : old host URL
  * replace : new host URL

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:exslt="http://exslt.org/common"
                version="2.0"
                exclude-result-prefixes="exslt">

  <xsl:param name="search">http://localhost:8084/</xsl:param>
  <xsl:param name="replace">http://localhost:8080/</xsl:param>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

  <!-- Replace url prefix. -->
  <xsl:template match="*[@xlink:href]" priority="2">
    <xsl:copy>
      <xsl:apply-templates select="@*[name(.) != 'xlink:href']"/>


      <xsl:attribute name="href" namespace="http://www.w3.org/1999/xlink">
        <xsl:variable name="url" select="@xlink:href"/>

        <xsl:choose>
          <xsl:when test="starts-with($url, $search)">
            <xsl:value-of select="$replace"/>
            <xsl:value-of select="substring-after($url, $search)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$url"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:apply-templates select="*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
