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
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:regcd="http://inspire.ec.europa
                .eu/codelist_register/codelist"
                xmlns:regmdcd="http://inspire.ec.europa.eu/metadata-codelist_register/metadata-codelist"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <!-- Convert a combination of codelist from Registry tools into one SKOS thesaurus.

  The input document structure MUST be the following:
  <pre>
    <documents>
      <codelist xmlns="http://inspire.ec.europa.eu/codelist_register/codelist"
                id="http://inspire.ec.europa.eu/codelist/BreakLineTypeValue"
      ...
      <codelist xmlns="http://inspire.ec.europa.eu/codelist_register/codelist"
                id="http://inspire.ec.europa.eu/codelist/BreakLineTypeValue"
      ...
    </documents>
  </pre>

  Each codelist in a different language.

  The thesaurus is identified by the first codelist id attribute.

  -->


  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>
  <xsl:variable name="now"
                select="format-dateTime(current-dateTime(),$df)"/>

  <xsl:variable name="thesaurusId"
                select="/documents/*[1]/@id"/>

  <xsl:variable name="registerEnglishTitle"
                select="/documents/*[*:language = 'en']/*:label[@xml:lang = 'en']"/>

  <xsl:variable name="statusSuffix"
                select="'/status/valid'"/>

  <xsl:variable name="hasBroaderNarrowerLinks"
                select="count(//*[1]/*:containeditems/*/*:parents) > 0"/>

  <xsl:variable name="thesaurusTitles">
    <title thesaurus="GEMET - INSPIRE themes, version 1.0"
           register="INSPIRE theme register"
           date="2008-06-01"/>
    <title thesaurus="Classification of spatial data services"
           register="Classification of spatial data services"
           date="2008-12-03"/>
    <title thesaurus="Spatial scope"
           register="Spatial scope"
           date="2019-05-22"/>
    <title thesaurus="INSPIRE priority data set"
           register="INSPIRE priority data set"
           date="2018-04-04"/>
    <title thesaurus="IACS data"
           register="IACS data"
           date="2021-06-08"/>
  </xsl:variable>

  <xsl:variable name="isInspireThemes"
                select="$thesaurusId = 'http://inspire.ec.europa.eu/theme'"/>

  <xsl:variable name="thesaurusTitle"
                select="$thesaurusTitles/title[@register = $registerEnglishTitle]/@thesaurus"/>

  <xsl:variable name="thesaurusDate"
                select="$thesaurusTitles/title[@register = $registerEnglishTitle]/@date"/>

  <xsl:template match="/documents">
    <rdf:RDF xmlns:skos="http://www.w3.org/2004/02/skos/core#"
             xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:dcterms="http://purl.org/dc/terms/">
      <skos:ConceptScheme rdf:about="{$thesaurusId}">

        <!-- Thesaurus title can be forced to another value
        than the register title. For example, INSPIRE theme
        thesaurus title as a well known name that has to be
        used. -->
        <xsl:choose>
          <xsl:when test="normalize-space($thesaurusTitle) = ''">
            <xsl:apply-templates mode="concept-scheme"
                                 select="*/*:label"/>
          </xsl:when>
          <xsl:otherwise>
            <dc:title><xsl:value-of select="$thesaurusTitle"/></dc:title>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates mode="concept-scheme"
                             select="*/*:definition"/>


        <dcterms:issued><xsl:value-of select="if ($thesaurusDate != '') then $thesaurusDate else $now"/></dcterms:issued>

        <!-- Add top concepts for all items with no parent -->
        <xsl:if test="$hasBroaderNarrowerLinks">
          <xsl:for-each select="*[1]/*:containeditems/*[not(*:parents)]">
            <skos:hasTopConcept rdf:resource="{@id}"/>
          </xsl:for-each>
        </xsl:if>
      </skos:ConceptScheme>

      <!-- We assume that the first codelist contains the full
      list of items to describes and that the following contains
      translations for each items of the first one. -->
      <xsl:apply-templates mode="concept"
                           select="*[1]/*:containeditems/*"/>

    </rdf:RDF>
  </xsl:template>


  <xsl:template mode="concept-scheme"
                match="*/*:label[@xml:lang = ../*:language]">

    <dc:title>
      <xsl:copy-of select="@xml:lang"/>
      <xsl:value-of select="."/>
    </dc:title>
  </xsl:template>


  <xsl:template mode="concept-scheme"
                match="*/*:definition[@xml:lang = ../*:language]">
    <dc:description>
      <xsl:copy-of select="@xml:lang"/>
      <xsl:value-of select="."/>
    </dc:description>
  </xsl:template>


  <xsl:template mode="concept-scheme"
                match="*"/>




  <xsl:template mode="concept"
                match="*:containeditems/*[ends-with(*:status/@id, $statusSuffix)]">

    <xsl:variable name="conceptId"
                  select="@id"/>

    <xsl:variable name="items"
                  select="/documents/*[1]/*:containeditems/*"/>

    <skos:Concept rdf:about="{$conceptId}">
      <xsl:for-each select="//*:containeditems/*[@id = $conceptId]">
        <!-- Only add the label if the codelist requested language match.
        If you request something in french and no translation are available,
        english is set. -->
        <xsl:if test="*:label/@xml:lang = ../../*:language">
          <skos:prefLabel>
            <xsl:copy-of select="*:label/@xml:lang"/>
            <xsl:value-of select="*:label"/>
          </skos:prefLabel>
        </xsl:if>
        <xsl:if test="*:definition/@xml:lang = ../../*:language">
          <skos:scopeNote>
            <xsl:copy-of select="*:definition/@xml:lang"/>
            <xsl:value-of select="*:definition"/>
          </skos:scopeNote>
        </xsl:if>
      </xsl:for-each>

      <xsl:if test="$isInspireThemes">
        <skos:altLabel><xsl:value-of select="tokenize($conceptId, '/')[last()]"/></skos:altLabel>
      </xsl:if>
      <skos:inScheme rdf:resource="{$thesaurusId}"/>


      <!-- Add broader and narrower links -->
      <xsl:if test="$hasBroaderNarrowerLinks">
        <xsl:for-each select="*:parents/*:parent">
          <skos:broader rdf:resource="{@id}"/>
        </xsl:for-each>

        <xsl:for-each select="$items[*:parents/*:parent/@id = $conceptId]">
          <skos:narrower rdf:resource="{@id}"/>
        </xsl:for-each>
      </xsl:if>
    </skos:Concept>
  </xsl:template>

  <xsl:template mode="concept"
                match="*"/>
</xsl:stylesheet>
