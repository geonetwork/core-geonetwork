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

<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all">

  <xsl:template mode="relation" match="metadata[simpledc]" priority="99">
    <xsl:for-each select="*/descendant::*
                        [name(.) = 'dct:references' or name(.) = 'dc:relation']
                        [starts-with(., 'http') or contains(. , 'resources.get') or contains(., 'file.disclaimer')]">
      <xsl:variable name="name" select="tokenize(., '/')[last()]"/>
      <relation type="onlinesrc">
        <id><xsl:value-of select="."/></id>
        <title>
          <xsl:value-of select="$name"/>
        </title>
        <url>
          <xsl:value-of select="."/>
        </url>
        <name>
          <xsl:value-of select="$name"/>
        </name>
        <abstract><xsl:value-of select="."/></abstract>
        <description>
          <xsl:value-of select="$name"/>
        </description>
        <xsl:choose>
          <xsl:when test="contains(. , 'resources.get') or contains(., 'file.disclaimer')">
            <protocol><xsl:value-of select="'WWW:DOWNLOAD-1.0-http--download'"/></protocol>
          </xsl:when>
          <xsl:otherwise>
            <protocol><xsl:value-of select="'WWW:LINK'"/></protocol>
          </xsl:otherwise>
        </xsl:choose>
      </relation>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>