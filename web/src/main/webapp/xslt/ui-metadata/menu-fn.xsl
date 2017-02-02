<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2017 Food and Agriculture Organization of the
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
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <!-- Evaluate XPath expression to
                    see if view should be displayed
                    according to the metadata record or
                    the session information. -->
  <xsl:function name="gn-fn-metadata:check-viewtab-visibility" as="xs:boolean">
    <xsl:param name="root" as="node()?"/>
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="metadata"/>
    <xsl:param name="serviceInfo"/>
    <xsl:param name="displayIfRecord" as="xs:string?"/>
    <xsl:param name="displayIfServiceInfo" as="xs:string?"/>
    <xsl:param name="displayIfSetting" as="xs:string?"/>

    <xsl:variable name="isInRecord" as="xs:boolean">
      <xsl:choose>
        <xsl:when test="$displayIfRecord">
          <saxon:call-template name="{concat('evaluate-', $schema, '-boolean')}">
            <xsl:with-param name="base" select="$metadata"/>
            <xsl:with-param name="in" select="concat('/../', $displayIfRecord)"/>
          </saxon:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="false()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="isInServiceInfo" as="xs:boolean">
      <xsl:choose>
        <xsl:when test="$displayIfServiceInfo">
          <saxon:call-template name="{concat('evaluate-', $schema, '-boolean')}">
            <xsl:with-param name="base" select="$serviceInfo"/>
            <xsl:with-param name="in" select="concat('/', $displayIfServiceInfo)"/>
          </saxon:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="false()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="isInSetting" as="xs:boolean">
      <xsl:choose>
        <xsl:when test="$displayIfSetting">
          <xsl:call-template name="evaluate-boolean">

            <xsl:with-param name="base" select="$root//root/gui/settings"/>
            <xsl:with-param name="in" select="concat('/', $displayIfSetting)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="false()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:value-of select="(( $displayIfRecord      and $isInRecord )     or not ($displayIfRecord ))     and
                          (( $displayIfServiceInfo and $isInServiceInfo) or not ($displayIfServiceInfo)) and
                          (( $displayIfSetting     and $isInSetting)     or not ($displayIfSetting))"/>

  </xsl:function>


  <!-- Evaluate XPath returning a boolean value. -->
  <xsl:template name="evaluate-boolean">
      <xsl:param name="base" as="node()"/>
      <xsl:param name="in"/>

      <xsl:value-of select="saxon:evaluate(concat('$p1', $in), $base)"/>
  </xsl:template>

</xsl:stylesheet>
