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
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                version="1.0">

  <!-- ================================================================= -->

  <xsl:template match="/root">
    <xsl:apply-templates select="csw:Record"/>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="csw:Record">
    <csw:Record xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/">
      <xsl:apply-templates select="dc:title"/>
      <xsl:apply-templates select="dc:creator"/>
      <xsl:apply-templates select="dc:subject"/>
      <xsl:apply-templates select="dc:description"/>
      <xsl:apply-templates select="dc:publisher"/>
      <xsl:apply-templates select="dc:contributor"/>
      <xsl:apply-templates select="dc:date"/>
      <xsl:apply-templates select="dc:type"/>
      <xsl:apply-templates select="dc:format"/>

      <xsl:choose>
        <xsl:when test="not(dc:identifier)">
          <dc:identifier>
            <xsl:value-of select="/root/env/uuid"/>
          </dc:identifier>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="dc:identifier"/>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates select="dc:source"/>
      <xsl:apply-templates select="dc:language"/>
      <xsl:apply-templates select="dc:relation"/>
      <xsl:apply-templates select="dc:coverage"/>
      <xsl:apply-templates select="dc:rights"/>

      <xsl:apply-templates select="dct:abstract"/>
      <xsl:apply-templates select="dct:accessRights"/>
      <xsl:apply-templates select="dct:accrualMethod"/>
      <xsl:apply-templates select="dct:accrualPeriodicity"/>
      <xsl:apply-templates select="dct:accrualPolicy"/>
      <xsl:apply-templates select="dct:alternative"/>
      <xsl:apply-templates select="dct:audience"/>
      <xsl:apply-templates select="dct:available"/>
      <xsl:apply-templates select="dct:bibliographicCitation"/>
      <xsl:apply-templates select="dct:conformsTo"/>
      <xsl:apply-templates select="dct:created"/>
      <xsl:apply-templates select="dct:dateAccepted"/>
      <xsl:apply-templates select="dct:dateCopyrighted"/>
      <xsl:apply-templates select="dct:dateSubmitted"/>
      <xsl:apply-templates select="dct:educationLevel"/>
      <xsl:apply-templates select="dct:extent"/>
      <xsl:apply-templates select="dct:hasFormat"/>
      <xsl:apply-templates select="dct:hasPart"/>
      <xsl:apply-templates select="dct:hasVersion"/>
      <xsl:apply-templates select="dct:instructionalMethod"/>
      <xsl:apply-templates select="dct:isFormatOf"/>
      <xsl:apply-templates select="dct:isPartOf"/>
      <xsl:apply-templates select="dct:isReferencedBy"/>
      <xsl:apply-templates select="dct:isReplacedBy"/>
      <xsl:apply-templates select="dct:isRequiredBy"/>
      <xsl:apply-templates select="dct:issued"/>
      <xsl:apply-templates select="dct:isVersionOf"/>
      <xsl:apply-templates select="dct:license"/>
      <xsl:apply-templates select="dct:mediator"/>
      <xsl:apply-templates select="dct:medium"/>
      <dct:modified>
        <xsl:value-of select="/root/env/changeDate"/>
      </dct:modified>
      <xsl:apply-templates select="dct:provenance"/>
      <xsl:apply-templates select="dct:references"/>
      <xsl:apply-templates select="dct:replaces"/>
      <xsl:apply-templates select="dct:requires"/>
      <xsl:apply-templates select="dct:rightsHolder"/>
      <xsl:apply-templates select="dct:spatial"/>
      <xsl:apply-templates select="dct:tableOfContents"/>
      <xsl:apply-templates select="dct:temporal"/>
      <xsl:apply-templates select="dct:valid"/>

      <xsl:apply-templates select="ows:BoundingBox"/>
    </csw:Record>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="dc:identifier">
    <xsl:copy>
      <xsl:value-of select="/root/env/uuid"/>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

</xsl:stylesheet>
