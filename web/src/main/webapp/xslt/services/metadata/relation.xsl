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
  Create a simple XML tree for relation description.
  <relations>
    <relation type="related|services|children">
      + super-brief representation.
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:gco="http://www.isotc211.org/2005/gco"
  xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:util="java:org.fao.geonet.util.XslUtil"
  xmlns:exslt="http://exslt.org/common"
  exclude-result-prefixes="#all">

  <xsl:include href="../../common/profiles-loader-tpl-brief.xsl"/>
  <xsl:include href="../../common/profiles-loader-relations.xsl"/>

  <xsl:template match="/">
    <relations>
      <!-- This is a hack to preserve the JSON output to be an array
      like it use to be. -->
      <xsl:namespace name="geonet" select="'http://www.fao.org/geonetwork'"/>
      <xsl:apply-templates mode="relation" select="/root/relations/*"/>
    </relations>
  </xsl:template>

  <xsl:template mode="relation" match="related|services|datasets|children|parent|sources|fcats|hasfeaturecat|siblings|associated|source|hassource">
    <xsl:apply-templates mode="relation" select="response/*">
      <xsl:with-param name="type" select="name(.)"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="relation" match="sibling">
    <xsl:apply-templates mode="relation" select="*">
      <xsl:with-param name="type" select="'sibling'"/>
      <xsl:with-param name="subType" select="@initiative"/>
      <xsl:with-param name="association" select="@association"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Bypass summary elements -->
  <xsl:template mode="relation" match="summary" priority="99"/>


  <!-- In Lucene only mode, metadata are retrieved from 
  the index and pass as a simple XML with one level element.
  Make a simple copy here. -->
  <xsl:template mode="superBrief" match="metadata">
    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="relation" match="*">
    <xsl:param name="type"/>
    <xsl:param name="subType" select="''"/>
    <xsl:param name="association" select="''"/>

    <!-- Fast output doesn't produce a full metadata record -->
    <xsl:variable name="md">
      <xsl:apply-templates mode="superBrief" select="."/>
    </xsl:variable>
    <xsl:variable name="metadata" select="exslt:node-set($md)"/>

    <relation type="{$type}">
			<xsl:if test="normalize-space($subType)!=''">
				<xsl:attribute name="subType">
					<xsl:value-of select="$subType"/>		
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="normalize-space($association)!=''">
				<xsl:attribute name="association">
					<xsl:value-of select="$association"/>
				</xsl:attribute>
			</xsl:if>
      <xsl:copy-of select="$metadata" copy-namespaces="no"/>
    </relation>
  </xsl:template>
  
  <!-- Add the default title as title. This may happen
  when title is retrieve from index and the record is
  not available in current language. eg. iso19110 records
  are only indexed with no language info. -->
  <xsl:template mode="superBrief" match="metadata">
    <xsl:copy-of select="*"/>
    <xsl:if test="not(title)">
      <title><xsl:value-of select="defaultTitle"/></title>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
