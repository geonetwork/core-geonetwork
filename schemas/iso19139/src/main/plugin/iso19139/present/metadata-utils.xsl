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
    xmlns:gco="http://www.isotc211.org/2005/gco" 
    xmlns:gmx="http://www.isotc211.org/2005/gmx" 
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:srv="http://www.isotc211.org/2005/srv"
    xmlns:geonet="http://www.fao.org/geonetwork"
    xmlns:java="java:org.fao.geonet.util.XslUtil"
    version="2.0"
    exclude-result-prefixes="#all">

    <!-- TODO: Use by CSW but should use layout/utilities instead
    -->
    <!-- Template used to return a gco:CharacterString element
        in default metadata language or in a specific locale
        if exist. 
        FIXME : gmd:PT_FreeText should not be in the match clause as gco:CharacterString 
        is mandatory and PT_FreeText optional. Added for testing GM03 import.
    -->
    <xsl:template name="localised" mode="localised" match="*[gco:CharacterString or gmd:PT_FreeText]">
        <xsl:param name="langId"/>
        
        <xsl:choose>
            <xsl:when
                test="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId] and
                gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId] != ''">
                <xsl:value-of
                    select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId]"
                />
            </xsl:when>
            <xsl:when test="not(gco:CharacterString)">
                <!-- If no CharacterString, try to use the first textGroup available -->
                <xsl:value-of
                    select="gmd:PT_FreeText/gmd:textGroup[position()=1]/gmd:LocalisedCharacterString"
                />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="gco:CharacterString"/>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!-- Template used to match any other element eg. gco:Boolean, gco:Date
         when looking for localised strings -->
    <xsl:template mode="localised" match="*[not(gco:CharacterString or gmd:PT_FreeText)]">
        <xsl:param name="langId"/>
			<xsl:value-of select="*[1]"/>
	</xsl:template>

	<!-- Check if the element has hidden subelements -->
    <xsl:template mode="localised" match="*[@gco:nilReason='withheld' and count(./*) = 0 and count(./@*) = 1]" priority="100">
        <xsl:value-of select="/root/gui/strings/concealed"/>
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
            <xsl:otherwise>#<xsl:value-of select="upper-case($lang)"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- Get lang codeListValue in metadata PT_Locale section,  if not return eng by default -->
    <xsl:template name="getLangCode">
        <xsl:param name="md"/>
        <xsl:param name="langId"/>

          <xsl:choose>
            <xsl:when
                test="$md/gmd:locale/gmd:PT_Locale[@id=$langId]/gmd:languageCode/gmd:LanguageCode/@codeListValue"
                    ><xsl:value-of
                        select="$md/gmd:locale/gmd:PT_Locale[@id=$langId]/gmd:languageCode/gmd:LanguageCode/@codeListValue"
                /></xsl:when>
            <xsl:otherwise>eng</xsl:otherwise>            
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
