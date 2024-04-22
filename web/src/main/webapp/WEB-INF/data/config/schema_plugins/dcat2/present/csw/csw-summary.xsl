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
                xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="1.0">

  <xsl:template match="dataset">
    <xsl:variable name="info" select="geonet:info"/>
    <csw:SummaryRecord>

      <xsl:for-each select="dc:identifier">
        <dc:identifier>
          <xsl:value-of select="."/>
        </dc:identifier>
      </xsl:for-each>

      <dc:title>
        <xsl:for-each select="dc:title">
          <xsl:value-of select="."/>
        </xsl:for-each>
      </dc:title>

      <xsl:for-each select="dc:type">
        <dc:type>
          <xsl:value-of select="."/>
        </dc:type>
      </xsl:for-each>

      <xsl:for-each select="dc:subject">
        <dc:subject>
          <xsl:value-of select="."/>
        </dc:subject>
      </xsl:for-each>

      <xsl:for-each select="dc:format">
        <dc:format>
          <xsl:value-of select="."/>
        </dc:format>
      </xsl:for-each>

      <xsl:for-each select="dc:relation">
        <dc:relation>
          <xsl:value-of select="."/>
        </dc:relation>
      </xsl:for-each>

      <xsl:for-each select="dct:modified">
        <dct:modified>
          <xsl:value-of select="substring-before(.,'T')"/>
        </dct:modified>
      </xsl:for-each>

      <!--
            <xsl:for-each select="dct:modified">
              <dct:modified><xsl:value-of select="."/></dct:modified>
            </xsl:for-each>
      -->
      <xsl:for-each select="dct:abstract">
        <dct:abstract>
          <xsl:value-of select="."/>
        </dct:abstract>
      </xsl:for-each>

      <xsl:for-each select="dc:creator">
        <dc:creator>
          <xsl:value-of select="."/>
        </dc:creator>
      </xsl:for-each>

      <xsl:for-each select="dc:contributor">
        <dc:contributor>
          <xsl:value-of select="."/>
        </dc:contributor>
      </xsl:for-each>

      <xsl:for-each select="dc:publisher">
        <dc:publisher>
          <xsl:value-of select="."/>
        </dc:publisher>
      </xsl:for-each>

      <xsl:for-each select="dc:source">
        <dc:source>
          <xsl:value-of select="."/>
        </dc:source>
      </xsl:for-each>

      <xsl:for-each select="dc:language">
        <dc:language>
          <xsl:value-of select="."/>
        </dc:language>
      </xsl:for-each>

      <xsl:for-each select="dct:spatial">
        <dct:spatial>
          <xsl:value-of select="."/>
        </dct:spatial>
      </xsl:for-each>

      <xsl:for-each select="dc:rights">
        <dc:rights>
          <xsl:value-of select="."/>
        </dc:rights>
      </xsl:for-each>
    </csw:SummaryRecord>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
