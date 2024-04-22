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

<xsl:stylesheet xmlns:dct="http://purl.org/dc/terms/"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
>
  <xsl:include href="../convert/functions.xsl"/>

  <xsl:param name="id"/>
  <xsl:param name="uuid"/>
  <xsl:param name="title"/>


  <xsl:variable name="allLanguages">
    <lang id="default" value="eng"/>
    <!--<xsl:for-each select="$otherLanguages">
      <lang id="{../../../@id}" value="{.}"/>
    </xsl:for-each>-->
  </xsl:variable>

  <!-- Subtemplate indexing -->
  <xsl:template match="/">
    <xsl:variable name="root" select="/"/>

    <xsl:apply-templates mode="index" select="$root">
      <xsl:with-param name="isoLangId" select="'eng'"/>

    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="index" match="dct:LicenseDocument">
    <xsl:param name="isoLangId"/>

    <xsl:variable name="title">
      <xsl:call-template name="index-lang-tag-oneval">
        <xsl:with-param name="tag" select="dct:title"/>
        <xsl:with-param name="langId" select="$isoLangId"/>
      </xsl:call-template>
    </xsl:variable>

    <doc>
      <resourceTitle><xsl:value-of select="if ($title != '') then $title else dct:identifier" /></resourceTitle>
    </doc>
  </xsl:template>

  <xsl:template mode="index" match="vcard:Individual">
    <xsl:param name="isoLangId"/>

    <xsl:variable name="title">
      <xsl:call-template name="index-lang-tag-oneval">
        <xsl:with-param name="tag" select="vcard:fn"/>
        <xsl:with-param name="langId" select="$isoLangId"/>
      </xsl:call-template>
    </xsl:variable>

    <doc>
      <resourceTitle><xsl:value-of select="if ($title != '') then $title else dct:identifier" /></resourceTitle>
    </doc>

  </xsl:template>
</xsl:stylesheet>
