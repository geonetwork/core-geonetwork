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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="text" indent="no" media-type="application/json"/>

  <xsl:include href="../common/utility-tpl.xsl"/>

  <xsl:variable name="siteURL"
                select="concat(/root/gui/env/server/protocol,'://',/root/gui/env/server/host,':',/root/gui/env/server/port, /root/gui/locService)"/>

  <xsl:template match="/">
    ["<xsl:value-of select="/root/request/q"/>"
    <!-- Add Completions (required) -->
    ,
    [
    <xsl:for-each
      select="/root/items/item">
      <xsl:variable name="value">
        <xsl:call-template name="replaceString">
          <xsl:with-param name="expr" select="@term"/>
          <xsl:with-param name="pattern" select="'&quot;'"/>
          <xsl:with-param name="replacement" select="'\&quot;'"/>
        </xsl:call-template>
        <xsl:if test="/root/request/withFrequency">(<xsl:value-of select="@freq"/>)
        </xsl:if>
      </xsl:variable>
      "<xsl:value-of select="normalize-space($value)"/>"
      <xsl:if test="position()!=last()"
      >,
      </xsl:if>
    </xsl:for-each>
    ]
    <!-- Add Descriptions (not required)
      @freq is the number of occurences of this term in the index (could be more than the number of results)
      ,
      [<xsl:for-each select="/root/items/item"> "<xsl:value-of select="@freq"/>" <xsl:if test="position()!=last()">,</xsl:if>
      </xsl:for-each> ]
    -->
    <!-- Query URLs (not required)
      ,
      [<xsl:for-each select="/root/items/item"> "<xsl:value-of
      select="concat($siteURL, '/rss.search?any=', @term)"/>" <xsl:if test="position()!=last()"
      >,</xsl:if>
      </xsl:for-each> ]
    -->
    ]
  </xsl:template>
</xsl:stylesheet>
