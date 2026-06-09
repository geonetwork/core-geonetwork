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
Stylesheet used to remove a reference to a online resource.
-->
<xsl:stylesheet xmlns:geonet="http://www.fao.org/geonetwork" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:digestUtils="java:org.apache.commons.codec.digest.DigestUtils"
                xmlns:exslt="http://exslt.org/common"
                exclude-result-prefixes="#all"
                version="2.0">

  <!--
      Usage:
        url is used to identify the resource url to be removed - it is for backwards compatibility.  Will not be used if resourceHash is set.
        name is used to identify the resource name to be removed - it is for backwards compatibility.  Will not be used if resourceHash is set.
        resourceHash is hash value of the object to be removed which will ensure the correct value is removed. It will override the usage of url/name
        resourceIdx is the index location of the object to be removed - can be used when duplicate entries exists to ensure the correct one is removed.

      example:
        onlinesrc-remove?url=http://geonetwork.org/resource.txt&name=test
    -->

  <xsl:param name="resourceHash" select="''"/>
  <xsl:param name="resourceIdx" select="''"/>
  <xsl:param name="url" select="''"/>
  <xsl:param name="name" select="''"/>

  <!-- Remove the gmd:onLine define in url parameter  -->
  <!-- Note: first part of the match needs to match the xsl:for-each select from extract-relations.xsl in order to get the position() to match -->
  <!-- The unique identifier is marked with resourceIdx which is the position index and resourceHash which is hash code of the current node (combination of url, resource name, and description) -->
  <xsl:template match="//gmd:MD_DigitalTransferOptions/gmd:onLine" priority="2">

    <!-- Calculate the global position of the current gmd:onLine element -->
    <xsl:variable name="position" select="count(//gmd:MD_DigitalTransferOptions/gmd:onLine[current() >> .]) + 1" />

    <xsl:if test="not(
                      gmd:CI_OnlineResource[gmd:linkage/gmd:URL != ''] and
                      ($resourceIdx = '' or $position = xs:integer($resourceIdx)) and
                      ($resourceHash != '' or ($url != null and (normalize-space(gmd:CI_OnlineResource/gmd:linkage/gmd:URL) = $url and normalize-space(gmd:CI_OnlineResource/gmd:name/gco:CharacterString) = normalize-space($name)
                                                                or normalize-space(gmd:CI_OnlineResource/gmd:linkage/gmd:URL) = $url and count(gmd:CI_OnlineResource/gmd:name/gmd:PT_FreeText/gmd:textGroup[gmd:LocalisedCharacterString = $name]) > 0
                                                                or normalize-space(gmd:CI_OnlineResource/gmd:linkage/gmd:URL) = $url and normalize-space(gmd:CI_OnlineResource/gmd:protocol/*) = 'WWW:DOWNLOAD-1.0-http--download'))
                        )
                        and ($resourceHash = '' or digestUtils:md5Hex(normalize-space(.)) = $resourceHash)
                   )">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>

  <!-- Do a copy of every node and attribute -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
