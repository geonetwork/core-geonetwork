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
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xslutil="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- Get the main metadata languages -->
  <xsl:template name="get-dcat2-language">
    <xsl:param name="languageIri"
               select="$metadata/dcat:CatalogRecord/dct:language[1]/@rdf:resource"
               required="no"/>

    <xsl:variable name="languageCode"
                  select="replace(
                            replace(
                              replace($languageIri, 'http://id.loc.gov/vocabulary/iso639-1/', ''),
                            'http://id.loc.gov/vocabulary/iso639-2/', ''),
                          'http://publications.europa.eu/resource/authority/language/', '')"/>

    <xsl:choose>
      <xsl:when test="string-length($languageCode) = 3">
        <xsl:value-of select="$languageCode"/>
      </xsl:when>
      <xsl:when test="string-length($languageCode) = 2">
        <xsl:value-of select="xslutil:threeCharLangCode($languageCode)"/>
      </xsl:when>
      <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Get the list of other languages in JSON -->
  <xsl:template name="get-dcat2-other-languages-as-json">
    <xsl:variable name="langs">
      <xsl:for-each select="$metadata/dcat:CatalogRecord/dct:language/@rdf:resource">

        <xsl:variable name="languageCode">
          <xsl:call-template name="get-dcat2-language">
            <xsl:with-param name="languageIri"
                            select="."/>
          </xsl:call-template>
        </xsl:variable>

        <xsl:if test="$languageCode != ''">
          <lang>
            <xsl:value-of
              select="concat('&quot;', $languageCode, '&quot;:&quot;#', upper-case($languageCode), '&quot;')"/>
          </lang>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <xsl:text>{</xsl:text><xsl:value-of select="string-join($langs/lang, ',')"/><xsl:text>}</xsl:text>
  </xsl:template>


  <!-- Get the list of other languages -->
  <xsl:template name="get-dcat2-other-languages">
    <xsl:choose>
      <xsl:when test="count($metadata/descendant::node()/*[@xml:lang != '']) > 1
                      or count($metadata/dcat:CatalogRecord/dct:language) > 1">
        <xsl:for-each select="$metadata/dcat:CatalogRecord/dct:language/@rdf:resource">
          <xsl:variable name="languageCode">
            <xsl:call-template name="get-dcat2-language">
              <xsl:with-param name="languageIri"
                              select="."/>
            </xsl:call-template>
          </xsl:variable>

          <xsl:if test="$languageCode != ''">
            <lang id="{upper-case($languageCode)}" code="{$languageCode}">
              <xsl:if test="position() = 1">
                <xsl:attribute name="default" select="''"/>
              </xsl:if>
            </lang>
          </xsl:if>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="mainLanguage">
          <xsl:call-template name="get-dcat2-language"/>
        </xsl:variable>
        <lang id="{upper-case($mainLanguage)}" code="{$mainLanguage}" default=""/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
