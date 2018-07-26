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
                xmlns:gn="http://www.fao.org/geonetwork" xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs" version="2.0">

  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
  <!-- dublin-core brief and superBrief formatting -->
  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
  <xsl:template mode="superBrief" match="simpledc">
    <id>
      <xsl:value-of select="gn:info/id"/>
    </id>
    <uuid>
      <xsl:value-of select="gn:info/uuid"/>
    </uuid>
    <xsl:if test="dc:title">
      <title>
        <xsl:value-of select="dc:title"/>
      </title>
    </xsl:if>
    <xsl:if test="dc:description">
      <abstract>
        <xsl:value-of select="dc:description"/>
      </abstract>
    </xsl:if>
  </xsl:template>

  <xsl:template name="dublin-coreBrief">
    <metadata>
      <xsl:if test="dc:title">
        <title>
          <xsl:value-of select="dc:title"/>
        </title>
      </xsl:if>
      <xsl:if test="dc:description">
        <abstract>
          <xsl:value-of select="dc:description"/>
        </abstract>
      </xsl:if>

      <xsl:for-each select="dc:subject[text()]">
        <keyword>
          <xsl:value-of select="."/>
        </keyword>
      </xsl:for-each>
      <xsl:for-each select="dc:identifier[text()]">
        <link type="url">
          <xsl:value-of select="."/>
        </link>
      </xsl:for-each>
      <!-- FIXME
      <image>IMAGE</image>
      -->
      <!-- TODO : ows:BoundingBox -->
      <xsl:variable name="coverage" select="dc:coverage[1]"/>
      <xsl:variable name="n" select="substring-after($coverage,'North ')"/>
      <xsl:variable name="north" select="substring-before($n,',')"/>
      <xsl:variable name="s" select="substring-after($coverage,'South ')"/>
      <xsl:variable name="south" select="substring-before($s,',')"/>
      <xsl:variable name="e" select="substring-after($coverage,'East ')"/>
      <xsl:variable name="east" select="substring-before($e,',')"/>
      <xsl:variable name="w" select="substring-after($coverage,'West ')"/>
      <xsl:variable name="west" select="substring-before($w,'. ')"/>
      <xsl:variable name="p" select="substring-after($coverage,'(')"/>
      <xsl:variable name="place" select="substring-before($p,')')"/>
      <xsl:if test="$n!=''">
        <geoBox>
          <westBL>
            <xsl:value-of select="$west"/>
          </westBL>
          <eastBL>
            <xsl:value-of select="$east"/>
          </eastBL>
          <southBL>
            <xsl:value-of select="$south"/>
          </southBL>
          <northBL>
            <xsl:value-of select="$north"/>
          </northBL>
        </geoBox>
      </xsl:if>

      <xsl:copy-of select="gn:*"/>
    </metadata>
  </xsl:template>
</xsl:stylesheet>
