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
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:template mode="relation" match="metadata[simpledc]" priority="99">
    <xsl:variable name="links"
                  select="*/descendant::*
                        [name(.) = 'dct:references' or name(.) = 'dc:relation']
                        [starts-with(., 'http') or contains(. , 'resources.get') or contains(., 'file.disclaimer')]"/>

    <xsl:if test="$links">
      <onlines>
        <xsl:for-each select="$links">
          <xsl:variable name="name" select="tokenize(., '/')[last()]"/>
          <item>
            <id>
              <xsl:value-of select="."/>
            </id>
            <url>
              <value lang="{$lang}">
                <xsl:value-of select="."/>
              </value>
            </url>
            <title>
              <value lang="{$lang}">
                <xsl:value-of select="$name"/>
              </value>
            </title>
            <xsl:choose>
              <xsl:when test="contains(. , 'resources.get') or contains(., 'file.disclaimer')">
                <protocol>
                  <xsl:value-of select="'WWW:DOWNLOAD-1.0-http--download'"/>
                </protocol>
              </xsl:when>
              <xsl:otherwise>
                <protocol>
                  <xsl:value-of select="'WWW:LINK'"/>
                </protocol>
              </xsl:otherwise>
            </xsl:choose>
          </item>
        </xsl:for-each>
      </onlines>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
