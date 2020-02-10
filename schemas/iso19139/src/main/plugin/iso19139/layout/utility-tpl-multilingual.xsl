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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:xslutil="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">


  <!-- Get the main metadata languages -->
   <xsl:template name="get-iso19139-language">
    <xsl:variable name="isTemplate" select="$metadata/gn:info[position() = last()]/isTemplate"/>
    <xsl:choose>
      <xsl:when test="$isTemplate = 's' or $isTemplate = 't'">
        <xsl:value-of select="xslutil:getLanguage()" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$metadata/gmd:language/gco:CharacterString|
       $metadata/gmd:language/gmd:LanguageCode/@codeListValue"/>
      </xsl:otherwise>
    </xsl:choose>
   </xsl:template>

  <!-- Get the list of other languages in JSON -->
  <xsl:template name="get-iso19139-other-languages-as-json">

    <xsl:variable name="isTemplate" select="$metadata/gn:info[position() = last()]/isTemplate"/>
    <xsl:variable name="langs">
      <xsl:choose>
        <xsl:when test="$isTemplate = 's' or $isTemplate = 't'">

          <xsl:for-each select="distinct-values($metadata//gmd:LocalisedCharacterString/@locale)">
            <xsl:variable name="locale" select="string(.)"/>
            <xsl:variable name="langId" select="xslutil:threeCharLangCode(substring($locale,2,2))"/>
            <lang>
              <xsl:value-of select="concat('&quot;', $langId, '&quot;:&quot;', ., '&quot;')"/>
            </lang>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="mainLanguage">
            <xsl:call-template name="get-iso19139-language"/>
          </xsl:variable>
          <xsl:if test="$mainLanguage">
            <xsl:variable name="mainLanguageId"
                          select="($metadata/gmd:locale/gmd:PT_Locale[
                                gmd:languageCode/gmd:LanguageCode/@codeListValue = $mainLanguage]/@id)[1]"/>

            <lang>
              <xsl:value-of
                select="concat('&quot;', $mainLanguage, '&quot;:&quot;#', $mainLanguageId, '&quot;')"/>
            </lang>
          </xsl:if>

          <xsl:for-each
            select="$metadata/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue != $mainLanguage]">
            <lang>
              <xsl:value-of
                select="concat('&quot;', gmd:languageCode/gmd:LanguageCode/@codeListValue, '&quot;:&quot;#', @id, '&quot;')"/>
            </lang>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:text>{</xsl:text><xsl:value-of select="string-join($langs/lang, ',')"/><xsl:text>}</xsl:text>
  </xsl:template>

  <!-- Get the list of other languages -->
  <xsl:template name="get-iso19139-other-languages">
    <xsl:variable name="isTemplate" select="$metadata/gn:info[position() = last()]/isTemplate"/>
    <xsl:choose>
      <xsl:when test="$isTemplate = 's' or $isTemplate = 't'">

        <xsl:for-each select="distinct-values($metadata//gmd:LocalisedCharacterString/@locale)">
          <xsl:variable name="locale" select="string(.)"/>
          <xsl:variable name="langId" select="xslutil:threeCharLangCode(substring($locale,2,2))"/>
          <lang id="{substring($locale,2,2)}" code="{$langId}"/>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>

        <xsl:for-each select="$metadata/gmd:locale/gmd:PT_Locale">
          <lang id="{@id}" code="{gmd:languageCode/gmd:LanguageCode/@codeListValue}"/>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Template used to return a translation if one found, 
       or the text in default metadata language 
       or the first non empty text element.
    -->
  <xsl:template name="localised" mode="localised" match="*[gco:CharacterString or gmx:Anchor or gmd:PT_FreeText]">
    <xsl:param name="langId"/>

    <xsl:variable name="translation"
                  select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId]"/>

    <xsl:variable name="mainValue"
                  select="(gco:CharacterString|gmx:Anchor)[1]"/>

    <xsl:variable name="firstNonEmptyValue"
                  select="((gco:CharacterString|gmx:Anchor|gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString)[. != ''])[1]"/>

    <xsl:value-of select="if($translation != '')
                          then $translation
                          else (if($mainValue != '')
                                then $mainValue
                                else $firstNonEmptyValue)"/>
  </xsl:template>


  <!-- Map GUI language to iso3code -->
  <xsl:template name="getLangId">
    <xsl:param name="langGui"/>
    <xsl:param name="md"/>

    <xsl:call-template name="getLangIdFromMetadata">
      <xsl:with-param name="lang" select="$langGui"/>
      <xsl:with-param name="md" select="$md"/>
    </xsl:call-template>
  </xsl:template>


  <!-- Get lang #id in metadata PT_Locale section,  deprecated: if not return the 2 first letters
        of the lang iso3code in uper case.

         if not return the lang iso3code in uper case.
        -->
  <xsl:template name="getLangIdFromMetadata">
    <xsl:param name="md"/>
    <xsl:param name="lang"/>

    <xsl:choose>
      <xsl:when
        test="$md/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue = $lang]/@id"
      >#<xsl:value-of
        select="$md/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue = $lang]/@id"
      />
      </xsl:when>
      <xsl:otherwise>#<xsl:value-of select="upper-case($lang)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
