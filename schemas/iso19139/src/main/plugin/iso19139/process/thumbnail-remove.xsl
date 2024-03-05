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
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="#all"
                xmlns:digestUtils="java:org.apache.commons.codec.digest.DigestUtils"
                xmlns:exslt="http://exslt.org/common"
                version="2.0">

  <!--
      Usage:
        thumbnail_url is the url to be removed - it is for backwards compatibility.  Will not be used if resourceHash is set.
        resourceHash is hash value of the object to be removed which will ensure the correct value is removed. It will override the usage of thumbnail_url
        resourceIdx is the index location of the object to be removed - can be used when duplicate entries exists to ensure the correct one is removed.

      example:
        thumbnail-from-url-remove?thumbnail_url=http://geonetwork.org/thumbnails/image.png
    -->

  <xsl:param name="thumbnail_url" select="''"/>
  <xsl:param name="resourceHash" select="''"/>
  <xsl:param name="resourceIdx" select="''"/>

  <!-- Remove the thumbnail define in thumbnail_url parameter -->
  <!-- Note: first part of the match needs to match the xsl:for-each select from extract-relations.xsl in order to get the position() to match -->
  <xsl:template
    priority="4"
    match="*//gmd:graphicOverview
         [$resourceIdx = '' or (count(preceding::gmd:graphicOverview) + 1) = xs:integer($resourceIdx)]
         [    ($resourceHash != '' or ($thumbnail_url != '' and normalize-space(gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString) = normalize-space($thumbnail_url)))
          and ($resourceHash = '' or digestUtils:md5Hex(string(exslt:node-set(.))) = $resourceHash)]"/>

  <!-- Do a copy of every node and attribute -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
