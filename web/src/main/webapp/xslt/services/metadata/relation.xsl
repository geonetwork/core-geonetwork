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
  Create XML document containing all related items
  following relatedResponse.xsd.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:include href="../../common/profiles-loader-tpl-brief.xsl"/>
  <xsl:include href="../../common/profiles-loader-relations.xsl"/>

  <xsl:template match="/">
    <related>
      <!-- online and thumbnail are extracted from schema extract-relations.xsl -->
      <!--<xsl:message><xsl:copy-of select="."/></xsl:message>-->
      <xsl:apply-templates mode="relation" select="/root/relations/*"/>
    </related>
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


  <xsl:template mode="relation" match="related|services|datasets|children|
                       parent|sources|fcats|hasfeaturecats|
                       siblings|associated|sources|hassources">
    <xsl:variable name="type" select="name(.)"/>

    <xsl:if test="response/metadata|response/sibling">
      <xsl:element name="{$type}">
        <xsl:for-each select="response/metadata|response/sibling/*">

          <!-- Fast output doesn't produce a full metadata record -->
          <xsl:variable name="metadata">
            <xsl:apply-templates mode="superBrief" select="."/>
          </xsl:variable>

          <xsl:variable name="uuid"
                        select="if ($metadata/uuid != '') then $metadata/uuid else geonet:info/uuid"/>

          <item>
            <id>
              <xsl:value-of select="$uuid"/>
            </id>
            <title>
              <value lang="{$lang}">
                <xsl:value-of select="$metadata/title"/>
              </value>
            </title>
            <url>
              <xsl:value-of
                select="concat(util:getSettingValue('nodeUrl'), 'api/records/', $uuid)"/>
            </url>
            <description>
              <value lang="{$lang}">
                <xsl:value-of select="$metadata/abstract"/>
              </value>
            </description>

            <xsl:for-each select="$metadata/type">
              <mdType>
                <xsl:value-of select="."/>
              </mdType>
            </xsl:for-each>

            <xsl:if test="$type = 'siblings'">
              <associationType>
                <xsl:value-of select="../@initiative"/>
              </associationType>
              <initiativeType>
                <xsl:value-of select="../@association"/>
              </initiativeType>
            </xsl:if>
          </item>
        </xsl:for-each>
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <!-- Add the default title as title. This may happen
  when title is retrieve from index and the record is
  not available in current language. eg. iso19110 records
  are only indexed with no language info. -->
  <xsl:template mode="superBrief" match="metadata">
    <xsl:copy-of select="*"/>
    <xsl:if test="not(title)">
      <title>
        <xsl:value-of select="defaultTitle"/>
      </title>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
