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
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:template mode="get-dcat2-localized-string"
                match="dct:title|dct:description">
    <value lang="{if (@xml:lang) then @xml:lang else $lang}">
      <xsl:value-of select="."/>
    </value>
  </xsl:template>

  <xsl:template mode="relation"
                match="metadata/rdf:RDF/dcat:*">
    <thumbnails>
      <xsl:for-each select=".//dcat:Distribution[dct:format/*/skos:prefLabel = 'WWW:OVERVIEW']">
        <xsl:variable name="url"
                      select="dcat:accessURL|dcat:downloadURL"/>
        <item>
          <id>
            <xsl:value-of select="$url"/>
          </id>
          <url>
            <value lang="{$lang}">
              <xsl:value-of select="$url"/>
            </value>
          </url>
          <title>
            <xsl:apply-templates mode="get-dcat2-localized-string"
                                 select="dct:title"/>
          </title>
          <description>
            <xsl:apply-templates mode="get-dcat2-localized-string"
                                 select="dct:description"/>
          </description>
          <protocol>
            <xsl:value-of select="dct:format/*/skos:prefLabel"/>
          </protocol>
          <type>thumbnail</type>
        </item>
      </xsl:for-each>
    </thumbnails>
    <onlines>
      <xsl:for-each select=".//dcat:Distribution[dct:format != 'WWW:OVERVIEW']">
        <xsl:variable name="url"
                      select="dcat:accessURL|dcat:downloadURL"/>
        <item>
          <id>
            <xsl:value-of select="$url"/>
          </id>
          <url>
            <value lang="{$lang}">
              <xsl:value-of select="$url"/>
            </value>
          </url>
          <title>
            <xsl:apply-templates mode="get-dcat2-localized-string"
                                 select="dct:title"/>
          </title>
          <description>
            <xsl:apply-templates mode="get-dcat2-localized-string"
                                 select="dct:description"/>
          </description>
          <protocol>
            <xsl:value-of select="dct:format/*/skos:prefLabel"/>
          </protocol>
          <type>onlinesrc</type>
        </item>
      </xsl:for-each>
    </onlines>
  </xsl:template>

</xsl:stylesheet>
