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

  <xsl:output method="text" version="1.0" encoding="utf-8" indent="no"/>

  <xsl:include href="reportCommon-output.xsl"/>

  <xsl:template name="csvHeader">
    <xsl:param name="record"/>

    <xsl:for-each select="$record/*">
      <xsl:choose>
        <xsl:when test="name(.) = name(following-sibling::node())">
        </xsl:when>
        <xsl:otherwise>
          <!--<xsl:value-of select="normalize-space(name(.))"/> -->
          <xsl:variable name="columnKey" select="normalize-space(name(.))"/>
          <xsl:value-of select="/root/gui/reports/internalRecords/*[name() = $columnKey]"/>

          <xsl:value-of select="$sep"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>

    <xsl:call-template name="newLine"/>
  </xsl:template>
</xsl:stylesheet>
