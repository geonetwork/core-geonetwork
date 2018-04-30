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
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:grg="http://www.isotc211.org/schemas/grg/"
                xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:void="http://rdfs.org/ns/void#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:registry="http://inspire.ec.europa.eu/codelist_register/codelist"
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
                select="/documents/registry:codelist[1]/@id"/>


  <xsl:template match="/documents">


    <rdf:RDF>
      <skos:ConceptScheme rdf:about="{$thesaurusId}">
        <xsl:apply-templates mode="concept-scheme"
                             select="registry:codelist/registry:label|
                                     registry:codelist/registry:definition"/>
        <dcterms:issued><xsl:value-of select="$now"/></dcterms:issued>
        <dcterms:modified><xsl:value-of select="$now"/></dcterms:modified>

      </skos:ConceptScheme>


      <!-- We assume that the first codelist contains the full
      list of items to describes and that the following contains
      translations for each items of the first one. -->
      <xsl:apply-templates mode="concept"
                           select="registry:codelist[1]/registry:containeditems/registry:value"/>

    </rdf:RDF>
  </xsl:template>


  <xsl:template mode="concept-scheme"
                match="registry:codelist/registry:label">
    <dc:title>
      <xsl:copy-of select="@xml:lang"/>
      <xsl:value-of select="."/>
    </dc:title>
  </xsl:template>

  <xsl:template mode="concept-scheme"
                match="registry:codelist/registry:definition">
    <dc:description>
      <xsl:copy-of select="@xml:lang"/>
      <xsl:value-of select="."/>
    </dc:description>
  </xsl:template>



  <xsl:template mode="concept"
                match="registry:containeditems/registry:value">
    <xsl:variable name="conceptId"
                  select="@id"/>

    <skos:Concept rdf:about="{$conceptId}">
      <xsl:for-each select="//registry:containeditems/registry:value[@id =$conceptId]">
        <skos:prefLabel>
          <xsl:copy-of select="registry:label/@xml:lang"/>
          <xsl:value-of select="registry:label"/>
        </skos:prefLabel>
        <skos:scopeNote>
          <xsl:copy-of select="registry:definition/@xml:lang"/>
          <xsl:value-of select="registry:definition"/>
        </skos:scopeNote>
      </xsl:for-each>
      <skos:inScheme rdf:resource="{$thesaurusId}"/>
    </skos:Concept>
  </xsl:template>

</xsl:stylesheet>
