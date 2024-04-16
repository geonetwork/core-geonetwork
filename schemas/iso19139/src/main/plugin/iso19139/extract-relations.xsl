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

<!--
  Create a simple XML tree for relation description.
  <relations>
    <relation type="related|services|children">
      + super-brief representation.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:exslt="http://exslt.org/common"
                xmlns:gn-fn-rel="http://geonetwork-opensource.org/xsl/functions/relations"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:include href="../iso19139/convert/functions.xsl"/>

  <xsl:function name="gn-fn-rel:translate">
    <xsl:param name="el"/>
    <xsl:param name="lang"/>
    <xsl:variable name="textVal" select="$el/gco:CharacterString|$el/gmx:Anchor/text()"/>
    <xsl:choose>
      <xsl:when test="$textVal!=''">
        <xsl:value-of select="$textVal"/>
      </xsl:when>
      <xsl:when
        test="($el/gmd:PT_FreeText//gmd:LocalisedCharacterString[@locale = $lang][text() != ''])[1]">
        <xsl:value-of
          select="($el/gmd:PT_FreeText//gmd:LocalisedCharacterString[@locale = $lang][text() != ''])[1]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of
          select="($el/gmd:PT_FreeText//gmd:LocalisedCharacterString[text() != ''])[1]"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!-- Convert an element gco:CharacterString
  to the GN localized string structure -->
  <xsl:template mode="get-iso19139-localized-string" match="*">

    <xsl:variable name="mainLanguage">
      <xsl:call-template name="langId_from_gmdlanguage19139">
          <xsl:with-param name="gmdlanguage" select="ancestor::metadata/*[@gco:isoType='gmd:MD_Metadata' or name()='gmd:MD_Metadata']/gmd:language"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:for-each select="gco:CharacterString|gmx:Anchor|
                          gmd:PT_FreeText/*/gmd:LocalisedCharacterString">
      <xsl:variable name="localeId"
                    select="substring-after(@locale, '#')"/>

      <value lang="{if (@locale)
                  then ancestor::metadata/*[@gco:isoType='gmd:MD_Metadata' or name()='gmd:MD_Metadata']/gmd:locale/*[@id = $localeId]/gmd:languageCode/*/@codeListValue
                  else if ($mainLanguage) then $mainLanguage else $lang}">
        <xsl:copy-of select="@xlink:href"/>
        <xsl:value-of select="."/>
      </value>
    </xsl:for-each>
  </xsl:template>

  <!-- Relation contained in the metadata record has to be returned
  It could be a document or thumbnails
  -->
  <xsl:template mode="relation"
                match="metadata[gmd:MD_Metadata or *[contains(@gco:isoType, 'MD_Metadata')]]"
                priority="99">

    <xsl:variable name="mainLanguage">
      <xsl:call-template name="langId_from_gmdlanguage19139">
          <xsl:with-param name="gmdlanguage" select="*/gmd:language"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:if test="count(*//gmd:graphicOverview) > 0">
      <thumbnails>
        <xsl:for-each select="*//gmd:graphicOverview">
          <item>
            <id>
              <xsl:value-of select="gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
            </id>
            <idx>
              <xsl:value-of select="position()"/>
            </idx>
            <hash>
              <xsl:value-of select="util:md5Hex(string(exslt:node-set(.)))"/>
            </hash>
            <url>
              <xsl:apply-templates mode="get-iso19139-localized-string"
                                   select="gmd:MD_BrowseGraphic/gmd:fileName"/>
            </url>
            <title>
              <xsl:apply-templates mode="get-iso19139-localized-string"
                                   select="gmd:MD_BrowseGraphic/gmd:fileDescription"/>
            </title>
            <type>thumbnail</type>
          </item>
        </xsl:for-each>
      </thumbnails>
    </xsl:if>

    <xsl:if test="count(*//gmd:MD_DigitalTransferOptions/gmd:onLine[gmd:CI_OnlineResource[gmd:linkage/gmd:URL!='']]) > 0">
      <onlines>
        <xsl:for-each select="*//gmd:MD_DigitalTransferOptions/gmd:onLine">
          <xsl:if test="gmd:CI_OnlineResource[gmd:linkage/gmd:URL!='']">
            <item>
              <xsl:variable name="langCode">
                <xsl:value-of select="concat('#', upper-case(util:twoCharLangCode($lang, 'EN')))"/>
              </xsl:variable>
              <xsl:variable name="url" select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
              <id>
                <xsl:value-of select="$url"/>
              </id>
              <idx>
                <xsl:value-of select="position()"/>
              </idx>
              <hash>
                <xsl:value-of select="util:md5Hex(string(exslt:node-set(.)))"/>
              </hash>
              <title>
                <xsl:apply-templates mode="get-iso19139-localized-string"
                                     select="gmd:CI_OnlineResource/gmd:name"/>
              </title>
              <url>
                <value lang="{$mainLanguage}">
                  <xsl:value-of select="$url"/>
                </value>
              </url>
              <function>
                <xsl:value-of select="gmd:CI_OnlineResource/gmd:function/*/@codeListValue"/>
              </function>
              <applicationProfile>
                <xsl:value-of select="gmd:CI_OnlineResource/gmd:applicationProfile/*/text()"/>
              </applicationProfile>
              <description>
                <xsl:apply-templates mode="get-iso19139-localized-string"
                                     select="gmd:CI_OnlineResource/gmd:description"/>
              </description>
              <protocol>
                <xsl:value-of select="gn-fn-rel:translate(gmd:CI_OnlineResource/gmd:protocol, $langCode)"/>
              </protocol>
              <mimeType>
                <xsl:value-of select="if (gmd:CI_OnlineResource/*/gmx:MimeFileType)
                                  then gmd:CI_OnlineResource/*/gmx:MimeFileType/@type
                                  else if (starts-with(gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString, 'WWW:DOWNLOAD:'))
                                  then replace(gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString, 'WWW:DOWNLOAD:', '')
                                  else ''"/>
              </mimeType>
              <type>onlinesrc</type>
            </item>
          </xsl:if>
        </xsl:for-each>
      </onlines>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
