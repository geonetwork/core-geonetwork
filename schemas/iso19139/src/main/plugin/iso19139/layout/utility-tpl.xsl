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
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gn="http://www.fao.org/geonetwork"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:include href="utility-tpl-multilingual.xsl"/>

  <xsl:template name="get-iso19139-is-service">
    <xsl:value-of
      select="count($metadata/gmd:identificationInfo/srv:SV_ServiceIdentification) > 0"/>
  </xsl:template>

  <xsl:template name="get-iso19139-title">
    <xsl:value-of select="$metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:title/gco:CharacterString"/>
  </xsl:template>


  <xsl:template name="get-iso19139-extents-as-json">[
    <xsl:for-each select="//gmd:geographicElement/gmd:EX_GeographicBoundingBox[
            number(gmd:westBoundLongitude/gco:Decimal)
            and number(gmd:southBoundLatitude/gco:Decimal)
            and number(gmd:eastBoundLongitude/gco:Decimal)
            and number(gmd:northBoundLatitude/gco:Decimal)
            and normalize-space(gmd:westBoundLongitude/gco:Decimal) != ''
            and normalize-space(gmd:southBoundLatitude/gco:Decimal) != ''
            and normalize-space(gmd:eastBoundLongitude/gco:Decimal) != ''
            and normalize-space(gmd:northBoundLatitude/gco:Decimal) != '']">
      <xsl:variable name="format" select="'#0.0000'"></xsl:variable>

      [
      <xsl:value-of select="format-number(gmd:westBoundLongitude/gco:Decimal, $format)"/>,
      <xsl:value-of select="format-number(gmd:southBoundLatitude/gco:Decimal, $format)"/>,
      <xsl:value-of select="format-number(gmd:eastBoundLongitude/gco:Decimal, $format)"/>,
      <xsl:value-of select="format-number(gmd:northBoundLatitude/gco:Decimal, $format)"/>
      ]
      <xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>
    ]
  </xsl:template>

  <xsl:template name="get-iso19139-online-source-config">
    <xsl:param name="pattern"/>
    <config>
      <xsl:for-each select="$metadata/descendant::gmd:onLine[
        matches(
        gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString,
        $pattern) and
        normalize-space(gmd:CI_OnlineResource/gmd:linkage/gmd:URL) != '']">
        <xsl:variable name="protocol"
                      select="string(gmd:CI_OnlineResource/gmd:protocol)"/>
        <xsl:variable name="fileName">
          <xsl:choose>
            <xsl:when test="matches($protocol, '^DB:.*')">
              <xsl:value-of select="concat(gmd:CI_OnlineResource/gmd:linkage/gmd:URL, '#',
                gmd:CI_OnlineResource/gmd:name/gco:CharacterString)"/>
            </xsl:when>
            <xsl:when test="matches($protocol, '^FILE:.*')">
              <xsl:value-of select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
            </xsl:when>
            <xsl:when test="matches($protocol, '^OGC:.*') and normalize-space(gmd:CI_OnlineResource/gmd:name/gco:CharacterString) != ''">
              <xsl:value-of select="normalize-space(gmd:CI_OnlineResource/gmd:name/gco:CharacterString)"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="substring-after(
              gmd:CI_OnlineResource/gmd:linkage/gmd:URL, 'attachments/')"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:if test="$fileName != ''">
          <resource>
            <ref>
              <xsl:value-of select="gn:element/@ref"/>
            </ref>
            <refParent>
              <xsl:value-of select="gn:element/@parent"/>
            </refParent>
            <name>
              <xsl:value-of select="$fileName"/>
            </name>
            <url>
              <xsl:value-of select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
            </url>
            <title>
              <xsl:value-of select="normalize-space($metadata/gmd:identificationInfo/*/
              gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString)"/>
            </title>
            <abstract>
              <xsl:value-of select="normalize-space($metadata/
              gmd:identificationInfo/*/gmd:abstract)"/>
            </abstract>
            <protocol>
              <xsl:value-of select="gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString"/>
            </protocol>
          </resource>
        </xsl:if>
      </xsl:for-each>
    </config>
  </xsl:template>
</xsl:stylesheet>
