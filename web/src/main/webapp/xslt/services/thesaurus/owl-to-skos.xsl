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
                xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:terms="http://purl.org/dc/terms/"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="#all"
                version="2.0">

  <!-- Convert OWL ontology into SKOS RDF/XML format supported by GeoNetwork.  -->
  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:template match="/">
    <xsl:apply-templates mode="owl-to-skos"
                         select=".//owl:Ontology"/>
  </xsl:template>

  <xsl:template mode="owl-to-skos"
                match="owl:Ontology">
    <rdf:RDF>
      <xsl:namespace name="skos" select="'http://www.w3.org/2004/02/skos/core#'"/>
      <xsl:namespace name="rdf" select="'http://www.w3.org/1999/02/22-rdf-syntax-ns#'"/>
      <xsl:namespace name="dc" select="'http://purl.org/dc/elements/1.1/'"/>
      <xsl:namespace name="terms" select="'http://purl.org/dc/terms/'"/>
      <skos:ConceptScheme rdf:about="{@rdf:about}">
        <xsl:copy-of select="terms:*|skos:*[local-name() != 'hasTopConcept']" copy-namespaces="no"/>

        <!--
        Custom case for Mobility DCAT theme vocabulary top concepts.
        https://mobilitydcat-ap.github.io/controlled-vocabularies/mobility-theme/latest/index.html#/

        The vocabulary contains 2 top concepts:
        <skos:hasTopConcept rdf:resource="https://w3id.org/mobilitydcat-ap/mobility-theme/data-content-category"/>
        <skos:hasTopConcept rdf:resource="https://w3id.org/mobilitydcat-ap/mobility-theme/data-content-sub-category"/>
        which are not really needed for browsing the main categories and sub categories.

        Use the narrower terms of the "content category" top concept as the top concepts of the scheme
        to facilitate keyword selection in editor and generate proper facet hierarchy in search.
        -->
        <xsl:variable name="mobilityThemeTopConcept"
                      select="../owl:NamedIndividual[@rdf:about = 'https://w3id.org/mobilitydcat-ap/mobility-theme/data-content-category']"/>
        <xsl:choose>
          <xsl:when test="$mobilityThemeTopConcept">
            <xsl:for-each select="$mobilityThemeTopConcept/skos:narrower">
              <skos:hasTopConcept rdf:resource="{@rdf:resource}"/>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="skos:hasTopConcept" copy-namespaces="no"/>
          </xsl:otherwise>
        </xsl:choose>
      </skos:ConceptScheme>

      <xsl:apply-templates mode="owl-to-skos"
                           select="../owl:NamedIndividual[skos:prefLabel]"/>
    </rdf:RDF>
  </xsl:template>

  <xsl:variable name="excludedConcepts"
                select="(
                'https://w3id.org/mobilitydcat-ap/mobility-theme/data-content-category',
                'https://w3id.org/mobilitydcat-ap/mobility-theme/data-content-sub-category'
                )"
                as="xs:string*"/>

  <xsl:template mode="owl-to-skos"
                match="owl:NamedIndividual[@rdf:about = $excludedConcepts]
                            |skos:broader[@rdf:resource = $excludedConcepts]"/>

  <xsl:template mode="owl-to-skos"
                match="owl:NamedIndividual">
    <skos:Concept rdf:about="{@rdf:about}">
      <xsl:apply-templates mode="owl-to-skos" select="skos:*"/>
    </skos:Concept>
  </xsl:template>

  <xsl:template mode="owl-to-skos"
                match="@*|node()">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*|node()" mode="owl-to-skos"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
