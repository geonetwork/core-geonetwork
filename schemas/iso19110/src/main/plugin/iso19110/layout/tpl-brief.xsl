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
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gfc="http://www.isotc211.org/2005/gfc"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs" version="2.0">

  <!-- ===================================================================== -->
  <!-- === iso19110 brief formatting === -->
  <!-- ===================================================================== -->

  <xsl:template mode="superBrief" match="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType">
    <xsl:variable name="uuid" select="gn:info/uuid"/>
    <id>
      <xsl:value-of select="gn:info/id"/>
    </id>
    <uuid>
      <xsl:value-of select="$uuid"/>
    </uuid>
    <xsl:if test="gmx:name|gfc:name|gfc:typeName">
      <title>
        <xsl:value-of
          select="gmx:name/gco:CharacterString|gfc:name/gco:CharacterString|gfc:typeName/gco:LocalName"
        />
      </title>
    </xsl:if>
  </xsl:template>

  <xsl:template name="iso19110Brief">
    <metadata>
      <xsl:variable name="id" select="gn:info/id"/>
      <xsl:variable name="uuid" select="gn:info/uuid"/>

      <xsl:if test="gmx:name or gfc:name">
        <title>
          <xsl:value-of select="gmx:name/gco:CharacterString|gfc:name/gco:CharacterString"/>
        </title>
      </xsl:if>

      <xsl:if test="gmx:scope or gfc:scope">
        <abstract>
          <xsl:value-of select="gmx:scope/gco:CharacterString|gfc:scope/gco:CharacterString"/>
        </abstract>
      </xsl:if>

      <gn:info>
        <xsl:copy-of select="gn:info/*"/>
        <category internal="true">featureCatalogue</category>
      </gn:info>
    </metadata>
  </xsl:template>
</xsl:stylesheet>
