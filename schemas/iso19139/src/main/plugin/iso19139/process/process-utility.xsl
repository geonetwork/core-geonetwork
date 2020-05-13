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
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                xmlns:exslt="http://exslt.org/common" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0" exclude-result-prefixes="exslt java">

  <!-- Language of the GUI -->
  <xsl:param name="guiLang" select="'eng'"/>

  <!-- Webapp name-->
  <xsl:param name="baseUrl" select="''"/>

  <!-- Catalog URL from protocol to lang -->
  <xsl:param name="catalogUrl" select="''"/>
  <xsl:param name="nodeId" select="''"/>

  <!-- Search for any of the searchStrings provided -->
  <xsl:function name="geonet:parseBoolean" as="xs:boolean">
    <xsl:param name="arg"/>
    <xsl:value-of
      select="if ($arg='on' or $arg=true() or $arg='true' or $arg='1') then true() else false()"/>
  </xsl:function>

  <!-- Return the message identified by the id in the required language
  or return the english message if not found. -->
  <xsl:function name="geonet:i18n" as="xs:string">
    <xsl:param name="loc"/>
    <xsl:param name="id"/>
    <xsl:param name="lang"/>
    <xsl:value-of
      select="if ($loc/msg[@id=$id and @xml:lang=$lang]) then $loc/msg[@id=$id and @xml:lang=$lang] else $loc/msg[@id=$id and @xml:lang='en']"/>
  </xsl:function>

  <!--
  Retrive a WMS capabilities document.
  -->
  <xsl:function name="geonet:get-wms-capabilities" as="node()">
    <xsl:param name="url" as="xs:string"/>
    <xsl:param name="version" as="xs:string"/>

    <xsl:copy-of
      select="geonet:get-wxs-capabilities($url, 'WMS', $version)"/>

  </xsl:function>

  <xsl:function name="geonet:get-wxs-capabilities" as="node()">
    <xsl:param name="url" as="xs:string"/>
    <xsl:param name="type" as="xs:string"/>
    <xsl:param name="version" as="xs:string"/>
    <xsl:variable name="sep" select="if (contains($url, '?')) then '&amp;' else '?'"/>

    <xsl:variable name="proxyhost"><xsl:value-of select="java:getSettingValue('system/proxy/host')"/></xsl:variable>
    <xsl:variable name="baseHost"><xsl:value-of select="java:getSettingValue('system/server/host')"/></xsl:variable>
    <xsl:variable name="protocol"><xsl:value-of select="java:getSettingValue('system/server/protocol')"/></xsl:variable>
    <xsl:variable name="basePort">
      <xsl:choose>
        <xsl:when test="$protocol = 'https'">
          <xsl:value-of select="java:getSettingValue('system/server/securePort')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="java:getSettingValue('system/server/port')"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
        
    <xsl:variable name="fullUrl"><xsl:value-of select="concat($url, $sep, 'SERVICE=', $type, '&amp;VERSION=', $version, '&amp;REQUEST=GetCapabilities')"/></xsl:variable>

    <xsl:copy-of select="java:getUrlContent($fullUrl)"/>

  </xsl:function>

  <!-- Create a GetMap request for the layer which could be used to set a thumbnail.
  TODO : add projection, width, heigth
  -->
  <xsl:function name="geonet:get-wms-thumbnail-url" as="xs:string">
    <xsl:param name="url" as="xs:string"/>
    <xsl:param name="version" as="xs:string"/>
    <xsl:param name="layer" as="xs:string"/>
    <xsl:param name="bbox" as="xs:string"/>

    <xsl:value-of
      select="concat($url, '?SERVICE=WMS&amp;VERSION=', $version, '&amp;REQUEST=GetMap&amp;SRS=EPSG:4326&amp;WIDTH=400&amp;HEIGHT=400&amp;FORMAT=image/png&amp;STYLES=&amp;LAYERS=', $layer, '&amp;BBOX=', $bbox)"/>

  </xsl:function>


  <!-- Create an ISO 19139 extent fragment -->
  <xsl:function name="geonet:make-iso-extent" as="node()">
    <xsl:param name="w" as="xs:string"/>
    <xsl:param name="s" as="xs:string"/>
    <xsl:param name="e" as="xs:string"/>
    <xsl:param name="n" as="xs:string"/>
    <xsl:param name="description" as="xs:string?"/>

    <gmd:EX_Extent>
      <xsl:if test="normalize-space($description)!=''">
        <gmd:description>
          <gco:CharacterString>
            <xsl:value-of select="$description"/>
          </gco:CharacterString>
        </gmd:description>
      </xsl:if>
      <gmd:geographicElement>
        <gmd:EX_GeographicBoundingBox>
          <gmd:westBoundLongitude>
            <gco:Decimal>
              <xsl:value-of select="$w"/>
            </gco:Decimal>
          </gmd:westBoundLongitude>
          <gmd:eastBoundLongitude>
            <gco:Decimal>
              <xsl:value-of select="$e"/>
            </gco:Decimal>
          </gmd:eastBoundLongitude>
          <gmd:southBoundLatitude>
            <gco:Decimal>
              <xsl:value-of select="$s"/>
            </gco:Decimal>
          </gmd:southBoundLatitude>
          <gmd:northBoundLatitude>
            <gco:Decimal>
              <xsl:value-of select="$n"/>
            </gco:Decimal>
          </gmd:northBoundLatitude>
        </gmd:EX_GeographicBoundingBox>
      </gmd:geographicElement>
    </gmd:EX_Extent>
  </xsl:function>

</xsl:stylesheet>
