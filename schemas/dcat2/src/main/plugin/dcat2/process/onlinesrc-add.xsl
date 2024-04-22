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
<xsl:stylesheet xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:param name="protocol"/>
  <xsl:param name="url"/>
  <xsl:param name="name"/>
  <xsl:param name="desc"/>
  <xsl:param name="function"/>
  <xsl:param name="applicationProfile"/>
  <xsl:param name="catalogUrl"/>

  <!-- Target element to update. The key is based on the concatenation
  of URL+Protocol+Name -->
  <xsl:param name="updateKey"/>

  <xsl:variable name="separator" select="'\|'"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="dcat:Dataset">
    <xsl:copy>
      <xsl:apply-templates select="@*|*[name() != 'dcat:distribution']"/>
      <xsl:choose>
        <xsl:when test="$updateKey = ''">
          <!-- Copy existing, insert new -->
          <xsl:copy-of select="dcat:distribution"/>
          <xsl:call-template name="create-distribution"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="dcat:distribution[normalize-space($updateKey) !=
              concat((*/dcat:downloadURL|*/dcat:accessURL),
                     */dct:format/*/skos:prefLabel,
                     */dct:title)]"/>
          <!-- TODO: We lose all elements which are not set by the widget. -->
          <xsl:call-template name="create-distribution"/>
        </xsl:otherwise>
      </xsl:choose>

    </xsl:copy>
  </xsl:template>


  <xsl:template name="create-distribution">
    <!-- If the distribution(s) are accessible only through a landing page (i.e. direct download URLs are not known), then the landing page URL associated with the dcat:Dataset SHOULD be duplicated as access URL on a distribution (see § 5.7 Dataset available only behind some Web page).-->
    <dcat:distribution>
      <dcat:Distribution>
        <!-- dcat:accessURL SHOULD be used for the URL of a service or location that can provide access to this distribution, typically through a Web form, query or API call. -->
        <!-- dcat:downloadURL is preferred for direct links to downloadable resources. -->
        <xsl:variable name="isDirectDownload"
                      select="$function = 'download' or contains($protocol, 'DOWNLOAD')"/>
        <xsl:choose>
          <xsl:when test="$isDirectDownload">
            <dcat:downloadURL>
              <xsl:value-of select="$url"/>
            </dcat:downloadURL>
          </xsl:when>
          <xsl:otherwise>
            <dcat:accessURL>
              <xsl:value-of select="$url"/>
            </dcat:accessURL>
          </xsl:otherwise>
        </xsl:choose>
        <!--          <dcat:byteSize></dcat:byteSize>-->
        <!--          <dcat:compressFormat></dcat:compressFormat>-->



        <xsl:if test="$protocol">
          <!--            TODO: https://www.w3.org/TR/vocab-dcat-2/#Property:distribution_media_type-->
          <!--            <dcat:mediaType><xsl:value-of select="$protocol"/></dcat:mediaType>-->
        </xsl:if>
        <!--          <dcat:packageFormat></dcat:packageFormat>-->
        <!--          <dcat:spatialResolutionInMeters></dcat:spatialResolutionInMeters>-->
        <!--          <dcat:temporalResolution></dcat:temporalResolution>-->
        <!--          <dct:accessRights>-->
        <!--            <skos:Concept></skos:Concept>-->
        <!--          </dct:accessRights>-->
        <xsl:if test="$name">
          <xsl:call-template name="build-text-field">
            <xsl:with-param name="element" select="'dct:description'"/>
            <xsl:with-param name="value" select="$name"/>
          </xsl:call-template>
        </xsl:if>
        <xsl:if test="$desc">
          <xsl:call-template name="build-text-field">
            <xsl:with-param name="element" select="'dct:description'"/>
            <xsl:with-param name="value" select="$desc"/>
          </xsl:call-template>
        </xsl:if>

        <xsl:if test="$protocol">
          <dct:format>
            <skos:Concept>
              <skos:prefLabel>
                <xsl:value-of select="$protocol"/>
              </skos:prefLabel>
            </skos:Concept>
          </dct:format>
        </xsl:if>
        <!--          <dct:license>-->
        <!--            <dct:LicenseDocument></dct:LicenseDocument>-->
        <!--          </dct:license>-->
      </dcat:Distribution>
    </dcat:distribution>
  </xsl:template>


  <xsl:template name="build-text-field">
    <xsl:param name="element" as="xs:string"/>
    <xsl:param name="value" as="xs:string"/>

    <xsl:choose>
      <xsl:when test="contains($value, '#')">
        <xsl:for-each select="tokenize($value, $separator)">
          <xsl:variable name="descLang"
                        select="substring-before(., '#')"></xsl:variable>
          <xsl:variable name="descValue"
                        select="substring-after(., '#')"></xsl:variable>

          <!-- TODO: FIXME - UI sends Upper case lang code ? -->
          <xsl:element name="{$element}">
            <xsl:attribute name="xml:lang"
                           select="lower-case($descLang)"/>
            <xsl:value-of select="$descValue"/>
          </xsl:element>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="{$element}">
          <xsl:value-of select="$value"/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
