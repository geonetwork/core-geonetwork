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
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                version="2.0">

  <xsl:output method="text"/>


  <xsl:template match="/">
    {
    "id": "<xsl:value-of select="root/error/@id"/>",
    "class": "<xsl:value-of select="root/error/class"/>",
    "service": "<xsl:value-of select="root/error/request/service"/>",
    "message": "<xsl:value-of
    select="java:encodeForHTML(root/error/message)"/>"
    }
  </xsl:template>

  <xsl:template match="stack">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="at">
    <xsl:value-of select="concat(@class, ' ', @file, '#', @line, ' ', @method)"/>
  </xsl:template>
  <xsl:template match="skip">
    <xsl:text>...</xsl:text>
  </xsl:template>

</xsl:stylesheet>
