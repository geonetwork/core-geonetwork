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
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:reg="http://purl.org/linked-data/registry#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <!-- Convert LD-Registry RDF format into SKOS format for GeoNetwork. -->

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>
  <xsl:variable name="now"
                select="format-dateTime(current-dateTime(),$df)"/>

  <xsl:variable name="registryBase"
                select="//(skos:ConceptScheme|reg:RegisterItem)"/>

  <xsl:variable name="thesaurusId"
                select="$registryBase/@rdf:about"/>

  <xsl:variable name="thesaurusDate"
                select="$registryBase/dcterms:modified"/>

  <xsl:template match="/documents">

    <rdf:RDF xmlns:skos="http://www.w3.org/2004/02/skos/core#"
             xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:dcterms="http://purl.org/dc/terms/">
      <skos:ConceptScheme rdf:about="{$thesaurusId}">
        <xsl:apply-templates mode="concept-scheme"
                           select="$registryBase/rdfs:label
                                   |$registryBase/dcterms:description"/>


        <dcterms:issued><xsl:value-of select="if ($thesaurusDate != '') then $thesaurusDate else $now"/></dcterms:issued>
        <dcterms:modified><xsl:value-of select="if ($thesaurusDate != '') then $thesaurusDate else $now"/></dcterms:modified>

        <!-- Add top concepts for all items with no parent
        <xsl:for-each select="*[1]/*:containeditems/*[not(*:parents)]">
          <skos:hasTopConcept rdf:resource="{@id}"/>
        </xsl:for-each>-->
      </skos:ConceptScheme>

      <!-- We assume that the first codelist contains the full
      list of items to describes and that the following contains
      translations for each items of the first one. -->
      <xsl:apply-templates mode="concept"
                           select="*//skos:Concept"/>

    </rdf:RDF>
  </xsl:template>


  <xsl:template mode="concept-scheme"
                match="rdfs:label">
    <dc:title>
      <xsl:copy-of select="@xml:lang"/>
      <xsl:value-of select="."/>
    </dc:title>
  </xsl:template>


  <xsl:template mode="concept-scheme"
                match="dcterms:description">
    <dc:description>
      <xsl:copy-of select="@xml:lang"/>
      <xsl:value-of select="."/>
    </dc:description>
  </xsl:template>


  <xsl:template mode="concept-scheme"
                match="*"/>


  <xsl:template mode="concept"
                match="skos:Concept">

    <xsl:variable name="conceptId"
                  select="@rdf:about"/>

    <xsl:variable name="items"
                  select="/documents/*[1]/*:containeditems/*"/>

    <skos:Concept rdf:about="{$conceptId}">
      <xsl:copy-of select="skos:prefLabel|skos:inScheme"
                   copy-namespaces="no"/>
      <xsl:for-each select="dcterms:description">
        <skos:scopeNote>
          <xsl:copy-of select="@xml:lang|text()"/>
        </skos:scopeNote>
      </xsl:for-each>

      <!-- Add broader and narrower links
      <xsl:if test="$hasBroaderNarrowerLinks">
        <xsl:for-each select="*:parents/*:parent">
          <skos:broader rdf:resource="{@id}"/>
        </xsl:for-each>

        <xsl:for-each select="$items[*:parents/*:parent/@id = $conceptId]">
          <skos:narrower rdf:resource="{@id}"/>
        </xsl:for-each>
      </xsl:if>-->
    </skos:Concept>
  </xsl:template>

  <xsl:template mode="concept"
                match="*"/>
</xsl:stylesheet>
