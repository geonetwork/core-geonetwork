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
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs"
                version="2.0">

  <xsl:output method="text"/>

  <xsl:template match="/">
    <xsl:variable name="elements"
                  as="xs:string*"
                  select="'editor', 'fields', 'fieldsWithFieldset', 'multilingualFields',
                    'views', 'view', 'tab', 'flatModeExceptions', 'thesaurusList',
                    'section', 'field', 'text', 'template', 'action'"/>

    <xsl:text>.. _creating-custom-editor:&#13;&#13;</xsl:text>
    <xsl:text>Customizing editor&#13;</xsl:text>
    <xsl:text>##################&#13;&#13;&#13;</xsl:text>
    <xsl:text>.. warning::&#13;</xsl:text>
    <xsl:text>  This file is produced automatically from the geonetwork-core repository.</xsl:text>
    <xsl:text>  To make any suggestions to the content of the page please add your changes to</xsl:text>
    <xsl:text>  `core-geonetwork/schemas/config.editor.xsd &lt;https://github.com/geonetwork/core-geonetwork/blob/main/schemas/config-editor.xsd&gt;`__.&#13;&#13;</xsl:text>

    <xsl:for-each select="//xs:element[@name = $elements and not(ancestor::xs:element[@name = 'batchEditing'])]">

      <!-- Documentation for the current element -->
      <xsl:text>.. _creating-custom-editor-</xsl:text><xsl:value-of select="@name"/><xsl:text>:</xsl:text>
      <xsl:text>&#13;&#13;</xsl:text>
      <xsl:value-of select="xs:annotation/xs:documentation/text()"/>
      <xsl:text>&#13;&#13;</xsl:text>

      <xsl:if test="count(xs:complexType/xs:attribute) > 0">
        <xsl:text>Attributes:&#13;&#13;</xsl:text>
        <xsl:for-each select="xs:complexType/xs:attribute[@name]">
          <xsl:sort select="@use" order="descending"/>
          <xsl:text>- **</xsl:text><xsl:value-of select="@name"/><xsl:text>**</xsl:text>
          <xsl:value-of select="if (@use = 'required') then ' (Mandatory)' else ' (Optional)'"/>
          <xsl:value-of select="if (@fixed) then concat(' Fixed value: **', @fixed, '**') else ''"/>
          <xsl:text>&#13;&#13;</xsl:text>
          <xsl:value-of select="xs:annotation/xs:documentation/text()"/>
          <xsl:text>&#13;&#13;</xsl:text>
        </xsl:for-each>
        <xsl:for-each select="xs:complexType/xs:attribute[@ref]">
          <xsl:sort select="@use" order="descending"/>
          <xsl:variable name="attributeName" select="@ref"/>
          <xsl:for-each select="//xs:attribute[@name = $attributeName]">
            <xsl:text>- **</xsl:text><xsl:value-of select="@name"/><xsl:text>**</xsl:text>
            <xsl:value-of select="if (@use = 'required') then ' (Mandatory)' else ' (Optional)'"/>
            <xsl:value-of
              select="if (@fixed) then concat(' Fixed value: **', @fixed, '**') else ''"/>
            <xsl:text>&#13;&#13;</xsl:text>
            <xsl:value-of select="xs:annotation/xs:documentation/text()"/>
            <xsl:text>&#13;&#13;</xsl:text>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:if>
      <!-- Link to children -->
      <xsl:if test="count(xs:complexType/xs:sequence/xs:element[@ref = $elements]) > 0">
        <xsl:text>Child elements:&#13;&#13;</xsl:text>
        <xsl:for-each select="xs:complexType/xs:sequence/xs:element[@ref = $elements]">
          <xsl:text>- </xsl:text><xsl:value-of select="concat('**', @ref, '**')"/>
          <xsl:if test="@minOccurs = '0' and @maxOccurs = '1'">
            <xsl:text>, Optional element</xsl:text>
          </xsl:if>
          <xsl:if test="@minOccurs = '1' and @maxOccurs = '1'">
            <xsl:text>, Mandatory element</xsl:text>
          </xsl:if>
          <xsl:if test="@minOccurs = '0' and @maxOccurs = 'unbounded'">
            <xsl:text>, Zero or more</xsl:text>
          </xsl:if>
          <xsl:if test="@minOccurs = '1' and @maxOccurs = 'unbounded'">
            <xsl:text>, One or more</xsl:text>
          </xsl:if>
          <xsl:value-of select="concat(' (see :ref:`creating-custom-editor-', @ref, '`)')"/>
          <xsl:text>&#13;&#13;</xsl:text>
        </xsl:for-each>
      </xsl:if>
    </xsl:for-each>

  </xsl:template>
</xsl:stylesheet>
