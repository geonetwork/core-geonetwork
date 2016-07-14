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
                xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core"
                version="2.0">


  <!-- Return mimetype according to protocol and linkage extension -->
  <xsl:function name="gn-fn-core:protocolMimeType" as="xs:string">
    <xsl:param name="linkage" as="xs:string"/>
    <xsl:param name="protocol" as="xs:string?"/>
    <xsl:param name="mimeType" as="xs:string?"/>

    <xsl:choose>
      <xsl:when
        test="(starts-with($protocol,'WWW:LINK-') or starts-with($protocol,'WWW:DOWNLOAD-')) and $mimeType!=''">
        <xsl:value-of select="$mimeType"/>
      </xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:LINK')">text/html</xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD') and contains($linkage,'.jpg')">
        image/jpeg
      </xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD') and contains($linkage,'.png')">
        image/png
      </xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD') and contains($linkage,'.gif')">
        image/gif
      </xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD') and contains($linkage,'.doc')">
        application/word
      </xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD') and contains($linkage,'.zip')">
        application/zip
      </xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD') and contains($linkage,'.pdf')">
        application/pdf
      </xsl:when>
      <xsl:when test="starts-with($protocol,'GLG:KML') and contains($linkage,'.kml')">
        application/vnd.google-earth.kml+xml
      </xsl:when>
      <xsl:when test="starts-with($protocol,'GLG:KML') and contains($linkage,'.kmz')">
        application/vnd.google-earth.kmz
      </xsl:when>
      <xsl:when test="starts-with($protocol,'OGC:WMS')">application/vnd.ogc.wms_xml</xsl:when>
      <xsl:when test="$protocol='ESRI:AIMS-'">application/vnd.esri.arcims_axl</xsl:when>
      <xsl:when test="$protocol!=''">
        <xsl:value-of select="$protocol"/>
      </xsl:when>
      <!-- fall back to the default content type -->
      <xsl:otherwise>text/plain</xsl:otherwise>
    </xsl:choose>
  </xsl:function>


  <!-- Search for any of the searchStrings provided -->
  <xsl:function name="gn-fn-core:contains-any-of" as="xs:boolean">
    <xsl:param name="arg" as="xs:string?"/>
    <xsl:param name="searchStrings" as="xs:string*"/>

    <xsl:sequence
      select="
      some $searchString in $searchStrings
      satisfies contains($arg,$searchString)
      "
    />
  </xsl:function>
</xsl:stylesheet>
