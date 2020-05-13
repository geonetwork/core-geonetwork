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
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:include href="../../common/base-variables.xsl"/>
  <xsl:include href="../../common/profiles-loader-thesaurus-transformation.xsl"/>
  <!-- Default template to use (ISO19139 keyword by default). -->
  <xsl:variable name="defaultTpl" select="'to-iso19139-keyword'"/>

  <xsl:variable name="serviceUrl" select="/root/nodeUrl"/>
  <xsl:variable name="node" select="/root/nodeId"/>

  <xsl:template match="/">
    <xsl:variable name="tpl"
                  select="if (/root/request/transformation and /root/request/transformation != '')
      then /root/request/transformation else $defaultTpl"/>

    <xsl:variable name="keywords"
                  select="/root/*[name() != 'gui' and name() != 'request']/keyword"/>

    <xsl:choose>
      <xsl:when test="$keywords">
        <xsl:for-each-group select="$keywords"
                            group-by="thesaurus/key">
          <saxon:call-template name="{$tpl}"/>
        </xsl:for-each-group>
      </xsl:when>
      <xsl:otherwise>
        <saxon:call-template name="{$tpl}"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>
</xsl:stylesheet>
