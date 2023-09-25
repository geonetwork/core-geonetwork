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
<xsl:stylesheet xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                version="3.0"
                exclude-result-prefixes="#all">
  <xsl:function name="gn-fn-metadata:get-dublin-core-title">
    <xsl:param name="metadata" as="node()"/>
    <xsl:value-of select="$metadata/descendant::node()/dc:title[1][text() != '']"/>
  </xsl:function>
  <xsl:function name="gn-fn-metadata:get-dublin-core-is-service">
    <xsl:param name="metadata" as="node()"/>
  </xsl:function>
  <xsl:function name="gn-fn-metadata:get-dublin-core-language">
    <xsl:param name="metadata" as="node()"/>
    <xsl:value-of select="$metadata/descendant::node()/dc:language[1]"/>
  </xsl:function>
  <!-- No multilingual support in Dublin core -->
  <xsl:function name="gn-fn-metadata:get-dublin-core-other-languages-as-json">
    <xsl:param name="metadata" as="node()"/>
  </xsl:function>
  <xsl:function name="gn-fn-metadata:get-dublin-core-other-languages">
    <xsl:param name="metadata" as="node()"/>
  </xsl:function>
  <xsl:function name="gn-fn-metadata:get-dublin-core-online-source-config">
    <xsl:param name="metadata" as="node()"/>
    <xsl:param name="pattern"/>
  </xsl:function>
  <xsl:function name="gn-fn-metadata:get-dublin-core-extents-as-json">
    <xsl:param name="metadata" as="node()"/>
    []
  </xsl:function>
</xsl:stylesheet>
