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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn-fn-iso19110="http://geonetwork-opensource.org/xsl/functions/profiles/iso19110"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                version="2.0"
                exclude-result-prefixes="#all">


  <!-- Get field type based on editor configuration.
  Search by element name or the child element name (the one
  containing the value). ISO19139 defined types are returned
  if not found.

  The child element take priority if defined.
  -->
  <xsl:function name="gn-fn-iso19110:getFieldType" as="xs:string">
    <!-- The container element -->
    <xsl:param name="name" as="xs:string"/>
    <!-- The element containing the value eg. gco:Date -->
    <xsl:param name="childName" as="xs:string?"/>
    <xsl:param name="xpath" as="xs:string?"/>

    <xsl:variable name="iso19110type"
                  select="gn-fn-metadata:getFieldType($editorConfig, $name, $childName, $xpath)"/>

    <xsl:choose>
      <xsl:when test="$iso19110type = $defaultFieldType">
        <xsl:value-of
          select="gn-fn-metadata:getFieldType($iso19139EditorConfig, $name, $childName, $xpath)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$iso19110type"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
