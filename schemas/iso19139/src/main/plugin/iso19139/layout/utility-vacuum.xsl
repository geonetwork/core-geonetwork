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
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- Vacuum utility. Remove empty elements from a metadata record:
  * All empty text element
  * All elements having no child text (ie. normalize-space return '') and
  have only empty attribute (or gco:nilReason=missing).

  The main function call the vacuum-iso19139 mode.
  -->
  <xsl:function name="gn-fn-iso19139:vacuum" as="node()">
    <xsl:param name="metadata" as="node()"/>
    <xsl:for-each select="$metadata/*">
      <xsl:apply-templates mode="vacuum-iso19139"
                           select="."/>
    </xsl:for-each>
  </xsl:function>


  <xsl:function name="gn-fn-iso19139:isElementOrChildEmpty"
                as="xs:boolean">
    <xsl:param name="element"/>

    <xsl:choose>
      <xsl:when test="$element">
        <!--<xsl:message>&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;</xsl:message>
        <xsl:message><xsl:copy-of select="$element"/></xsl:message>
        <xsl:message><xsl:copy-of select="normalize-space($element)"/></xsl:message>
        <xsl:message><xsl:copy-of select="count($element//@*[. != '' and . != 'missing'])"/></xsl:message>-->
        <xsl:value-of select="if (normalize-space($element) = '' and
                                  count($element//@*[. != '' and . != 'missing']) = 0)
                              then true() else false()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="false()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template mode="vacuum-iso19139"
                match="@*|node()">
    <xsl:variable name="isElementEmpty"
                  select="gn-fn-iso19139:isElementOrChildEmpty(.)"/>

    <xsl:choose>
      <xsl:when test="$isElementEmpty = true()">
        <!--    <empty>
              <xsl:copy-of select="."/>
            </empty>-->
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates mode="vacuum-iso19139"
                               select="@*|node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  This will not work as it will only remove elements and its parent.
  It will not handle complex empty element.
  <xsl:template mode="vacuum-iso19139"
                match="@*[. = '']|
                       *[gco:CharacterString/normalize-space(text()) = '']|
                       *[text() = '' and count(@*) = 0 and count(*) = 0]"
                priority="2"><empty/></xsl:template>-->

  <!-- Always remove gn:* elements. -->
  <xsl:template mode="vacuum-iso19139"
                match="gn:*" priority="2"/>

</xsl:stylesheet>
