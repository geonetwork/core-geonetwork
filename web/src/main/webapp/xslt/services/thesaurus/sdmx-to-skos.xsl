<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
                xmlns:sdmx="http://www.w3.org/2002/07/sdmx#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:sdmxm="http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message"
                xmlns:sdmxs="http://www.sdmx.org/resources/sdmxml/schemas/v2_1/structure"
                xmlns:sdmxc="http://www.sdmx.org/resources/sdmxml/schemas/v2_1/common"
                exclude-result-prefixes="#all"
                version="2.0">

  <!-- Convert sdmx codelist into SKOS RDF/XML format supported by GeoNetwork.  -->
  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:template match="/">
    <xsl:apply-templates mode="sdmx-to-skos"
                         select=".//sdmxs:Codelist"/>
  </xsl:template>

  <xsl:template mode="sdmx-to-skos"
                match="sdmxs:Codelist">
    <rdf:RDF xmlns:skos="http://www.w3.org/2004/02/skos/core#"
             xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:dcterms="http://purl.org/dc/terms/">
      <skos:ConceptScheme rdf:about="{@urn}">
        <xsl:for-each select="sdmxc:Name">
          <dc:title xml:lang="{@xml:lang}"><xsl:value-of select="."/></dc:title>
        </xsl:for-each>

        <xsl:for-each select="(sdmxc:Annotations/*[sdmxc:AnnotationType = 'LAST_UPDATED']/sdmxc:AnnotationTitle)[1]">
          <dcterms:issued><xsl:value-of select="."/></dcterms:issued>
          <dcterms:modified><xsl:value-of select="."/></dcterms:modified>
        </xsl:for-each>
      </skos:ConceptScheme>

      <xsl:apply-templates mode="sdmx-to-skos"
                           select="sdmxs:Code"/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template mode="sdmx-to-skos"
                match="sdmxs:Code">
    <skos:Concept rdf:about="{@urn}">
      <xsl:for-each select="sdmxc:Name">
        <skos:prefLabel xml:lang="{@xml:lang}"><xsl:value-of select="."/></skos:prefLabel>
      </xsl:for-each>
    </skos:Concept>
  </xsl:template>
</xsl:stylesheet>
