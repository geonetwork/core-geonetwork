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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
>

  <!-- ============================================================================================ -->

  <xsl:output indent="yes"/>

  <!-- ============================================================================================ -->

  <xsl:template match="simpledc">
    <oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
               xmlns:dc="http://purl.org/dc/elements/1.1/"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">

      <xsl:copy-of select="dc:title"/>
      <xsl:copy-of select="dc:creator"/>
      <xsl:copy-of select="dc:subject"/>
      <xsl:copy-of select="dc:description"/>
      <xsl:copy-of select="dc:publisher"/>
      <xsl:copy-of select="dc:contributor"/>
      <xsl:copy-of select="dc:date"/>
      <xsl:copy-of select="dc:type"/>
      <xsl:copy-of select="dc:format"/>
      <xsl:copy-of select="dc:identifier"/>
      <xsl:copy-of select="dc:source"/>
      <xsl:copy-of select="dc:language"/>
      <xsl:copy-of select="dc:relation"/>
      <xsl:copy-of select="dc:coverage"/>
      <xsl:copy-of select="dc:rights"/>
    </oai_dc:dc>
  </xsl:template>

  <!-- ============================================================================================ -->

  <xsl:template match="*">
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <!-- ============================================================================================ -->

</xsl:stylesheet>
