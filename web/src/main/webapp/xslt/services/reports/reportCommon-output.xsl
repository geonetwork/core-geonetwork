<?xml version="1.0" encoding="UTF-8" ?>
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:exslt="http://exslt.org/common"
                version="1.0"
                exclude-result-prefixes="xsl exslt geonet">

  <!-- Field separator
       To use tab instead of semicolon, use "&#009;".
   -->
  <!--<xsl:variable name="sep" select="'&#009;'"/>-->
  <xsl:variable name="sep">
    <xsl:text>,</xsl:text>
  </xsl:variable>

  <xsl:variable name="apos">&#x27;</xsl:variable>

  <!-- Intra field separator -->
  <xsl:variable name="internalSep" select="'###'"/>


  <!-- A template to add a new line \n with no extra space. -->
  <xsl:template name="newLine">
<xsl:text>
</xsl:text>
  </xsl:template>


  <xsl:template name="content" match="/">

    <xsl:if test="count(/root/response/record) = 0">
      No records found
    </xsl:if>

    <xsl:call-template name="csvHeader">
      <xsl:with-param name="record" select="/root/response/record[1]"/>
    </xsl:call-template>

    <xsl:for-each select="/root/response/record">

      <xsl:for-each select="*">
        <xsl:variable name="value" select="normalize-space(.)"/>
        <xsl:choose>
          <xsl:when test="contains($value, '&quot;')">
            <xsl:variable name="x" select="replace($value, '&quot;',  '&quot;&quot;')"/>
            <xsl:value-of select="concat('&quot;', $x, '&quot;')"/>
          </xsl:when>
          <xsl:when test="contains($value, $sep)">
            <xsl:value-of select="concat('&quot;', $value, '&quot;')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$value"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="name(.) = name(following-sibling::node())">
            <xsl:value-of select="$internalSep"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$sep"/>
          </xsl:otherwise>
        </xsl:choose>

      </xsl:for-each>

      <xsl:call-template name="newLine"/>
    </xsl:for-each>
  </xsl:template>


</xsl:stylesheet>
