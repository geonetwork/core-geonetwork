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
                select="//(skos:ConceptScheme
                          |reg:RegisterItem[reg:itemClass/@rdf:resource = ('http://www.w3.org/2004/02/skos/core#ConceptScheme', 'http://purl.org/linked-data/registry#Register')])"/>

  <xsl:variable name="thesaurusId"
                select="$registryBase[1]/@rdf:about"/>

  <xsl:variable name="thesaurusDate"
                select="$registryBase[1]/dcterms:modified"/>

  <xsl:template match="/documents">

    <xsl:variable name="concepts">
      <xsl:apply-templates mode="concept"
                           select="*//(skos:Concept
                           |*[rdf:type/@rdf:resource='http://www.w3.org/2004/02/skos/core#Concept'])"/>
    </xsl:variable>

    <rdf:RDF xmlns:skos="http://www.w3.org/2004/02/skos/core#"
             xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:dcterms="http://purl.org/dc/terms/">
      <skos:ConceptScheme rdf:about="{$thesaurusId}">
        <xsl:apply-templates mode="concept-scheme"
                           select="$registryBase[1]/rdfs:label
                                   |$registryBase[1]/dcterms:description"/>


        <dcterms:issued><xsl:value-of select="if ($thesaurusDate != '') then $thesaurusDate else $now"/></dcterms:issued>

        <xsl:for-each select="distinct-values($concepts/*[skos:narrower and not(skos:broader)]/@rdf:about)">
          <skos:hasTopConcept rdf:resource="{.}"/>
        </xsl:for-each>
      </skos:ConceptScheme>

      <xsl:copy-of select="$concepts"/>
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
                match="skos:Concept
                      |*[rdf:type/@rdf:resource='http://www.w3.org/2004/02/skos/core#Concept']">

    <xsl:variable name="conceptId"
                  select="@rdf:about"/>

    <skos:Concept rdf:about="{$conceptId}">
      <xsl:if test="local-name() = 'RegisterItem'
                    or local-name(..) = 'hasTopConcept'
                    or skos:topConceptOf">
        <skos:inScheme rdf:resource="{$thesaurusId}"/>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="skos:prefLabel">
          <xsl:copy-of select="skos:prefLabel"
                       copy-namespaces="no"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="rdfs:label">
            <skos:prefLabel>
              <xsl:copy-of select="@xml:lang|text()"/>
            </skos:prefLabel>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:for-each select="dcterms:description">
        <skos:scopeNote>
          <xsl:copy-of select="@xml:lang|text()"/>
        </skos:scopeNote>
      </xsl:for-each>

      <xsl:for-each select="distinct-values(
                            skos:broader/skos:Concept/@rdf:about
                            |skos:broader/@rdf:resource
                            |skos:broader/*[rdf:type/@rdf:resource='http://www.w3.org/2004/02/skos/core#Concept']/@rdf:about
                            |//skos:broader[
                                ../name() = 'skos:Concept'
                                and /@rdf:about = $conceptId]/(@rdf:resource|skos:Concept/@rdf:about)
                            |//skos:Concept[skos:narrower[@rdf:resource = $conceptId or skos:Concept/@rdf:about = $conceptId]]/@rdf:about)">
        <skos:broader rdf:resource="{.}"/>
      </xsl:for-each>


      <xsl:for-each select="distinct-values(
                            skos:narrower/skos:Concept/@rdf:about
                            |skos:narrower/@rdf:resource
                            |skos:narrower/*[rdf:type/@rdf:resource='http://www.w3.org/2004/02/skos/core#Concept']/@rdf:about
                            |//skos:narrower[
                                ../name() = 'skos:Concept'
                                and /@rdf:about = $conceptId]/(@rdf:resource|skos:Concept/@rdf:about)
                            |//skos:Concept[skos:broader[@rdf:resource = $conceptId or skos:Concept/@rdf:about = $conceptId]]/@rdf:about
                            )">
        <skos:narrower rdf:resource="{.}"/>
      </xsl:for-each>

    </skos:Concept>
  </xsl:template>

  <xsl:template mode="concept"
                match="*"/>
</xsl:stylesheet>
