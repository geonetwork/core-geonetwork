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
                xmlns:gn-fn-dcat2="http://geonetwork-opensource.org/xsl/functions/profiles/dcat2"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- Function to get the inScheme URI based on the thesaurus key -->
  <xsl:function name="gn-fn-dcat2:getInSchemeURIByThesaurusId" as="xs:string">
    <xsl:param name="key"/>
    <xsl:variable name="inSchemeAuthorityBaseUrl" select="'http://publications.europa.eu/resource/authority/'"/>
    <xsl:variable name="inSchemeAdmsBaseUrl" select="'http://purl.org/adms/'"/>
    <xsl:variable name="inSchemeIanaBaseUrl" select="'https://www.iana.org/assignments/'"/>
    <xsl:variable name="inSchemeGeonetworkBaseUrl" select="'https://registry.geonetwork-opensource.org/dcat/'"/>
    <xsl:variable name="keyPrefix" select="'external.theme.'"/>
    <xsl:choose>
      <xsl:when test="$key = concat($keyPrefix,'org.purl.adms.publisher-type')">
        <xsl:value-of select="concat($inSchemeAdmsBaseUrl,'publishertype/1.0')"/>
      </xsl:when>
      <xsl:when test="$key = concat($keyPrefix,'eu.europa.data-theme')">
        <xsl:value-of select="concat($inSchemeAuthorityBaseUrl,'data-theme')"/>
      </xsl:when>
      <xsl:when test="$key = concat($keyPrefix,'eu.europa.frequency')">
        <xsl:value-of select="concat($inSchemeAuthorityBaseUrl,'frequency')"/>
      </xsl:when>
      <xsl:when test="$key = concat($keyPrefix,'language')">
        <xsl:value-of select="concat($inSchemeAuthorityBaseUrl,'language')"/>
      </xsl:when>
      <xsl:when test="$key = concat($keyPrefix,'resource-type')">
        <xsl:value-of select="concat($inSchemeAuthorityBaseUrl,'resource-type')"/>
      </xsl:when>
      <xsl:when test="$key = concat($keyPrefix,'eu.europa.file-type')">
        <xsl:value-of select="concat($inSchemeAuthorityBaseUrl,'file-type')"/>
      </xsl:when>
      <xsl:when test="$key = concat($keyPrefix,'org.iana.media-type')">
        <xsl:value-of select="concat($inSchemeIanaBaseUrl,'media-type')"/>
      </xsl:when>
      <xsl:when test="$key = concat($keyPrefix,'status')">
        <xsl:value-of select="concat($inSchemeAdmsBaseUrl,'status/1.0')"/>
      </xsl:when>
      <xsl:when test="$key = concat($keyPrefix,'org.purl.adms.license-type')">
        <xsl:value-of select="concat($inSchemeAdmsBaseUrl,'licencetype/1.0')"/>
      </xsl:when>
      <xsl:when test="$key = concat($keyPrefix, 'eu.europa.access-right')">
        <xsl:value-of select="concat($inSchemeAuthorityBaseUrl,'access-right')"/>
      </xsl:when>
      <xsl:when test="$key = concat($keyPrefix, 'dcat-type')">
        <xsl:value-of select="concat($inSchemeGeonetworkBaseUrl,'type')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message select="concat($key, ' not found. Check process-utility.xsl.')"/>
        <xsl:value-of select="$key"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
