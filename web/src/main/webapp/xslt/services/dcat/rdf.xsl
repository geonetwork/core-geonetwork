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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:saxon="http://saxon.sf.net/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:void="http://www.w3.org/TR/void/"
                xmlns:dcat="http://www.w3.org/ns/dcat#" xmlns:dct="http://purl.org/dc/terms/"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                version="2.0"
                extension-element-prefixes="saxon" exclude-result-prefixes="#all">

  <xsl:output indent="yes"/>

  <xsl:include href="../../common/base-variables.xsl"/>
  <xsl:include href="../../common/profiles-loader-tpl-rdf.xsl"/>

  <xsl:variable name="port">
    <xsl:choose>
      <xsl:when test="$env/system/server/protocol = 'https'">
        <xsl:value-of select="$env/system/server/securePort"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$env/system/server/port"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="url"
                select="concat($env/system/server/protocol, '://',
                          $env/system/server/host,
                          if (($env/system/server/protocol = 'http' and $port = '80') or
                              ($env/system/server/protocol = 'https' and $port = '443')) then '' else concat(':', $port),
                          /root/gui/url)"/>

  <xsl:variable name="resourcePrefix" select="$env/metadata/resourceIdentifierPrefix"/>

  <!-- TODO: should use Java language code mapper -->
  <xsl:variable name="iso2letterLanguageCode" select="substring(/root/gui/language, 1, 2)"/>


  <xsl:template match="/">
    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
             xmlns:foaf="http://xmlns.com/foaf/0.1/"
             xmlns:void="http://www.w3.org/TR/void/" xmlns:dcat="http://www.w3.org/ns/dcat#"
             xmlns:dct="http://purl.org/dc/terms/"
             xmlns:skos="http://www.w3.org/2004/02/skos/core#">
      <!-- Metadata element -->

      <xsl:call-template name="catalogue"/>

      <xsl:apply-templates mode="to-dcat" select="/root/*|/root/search/response/*"/>

      <xsl:apply-templates mode="references" select="/root/*|/root/search/response/*"/>
    </rdf:RDF>

  </xsl:template>


  <xsl:template name="catalogue">


    <!-- First, the local catalog description using dcat:Catalog.
      "Typically, a web-based data catalog is represented as a single instance of this class."
      ... also describe harvested catalogues if harvested records are in the current dump.
    -->
    <dcat:Catalog rdf:about="{$resourcePrefix}/catalogs/{$env/system/site/siteId}">

      <!-- A name given to the catalog. -->
      <dct:title xml:lang="{$iso2letterLanguageCode}">
        <xsl:value-of select="$env/system/site/name"/>
      </dct:title>

      <!-- free-text account of the catalog. -->
      <dct:description/>

      <rdfs:label xml:lang="{$iso2letterLanguageCode}">
        <xsl:value-of select="$env/system/site/name"/> (<xsl:value-of
        select="$env/system/site/organization"/>)
      </rdfs:label>

      <!-- The homepage of the catalog -->
      <foaf:homepage>
        <xsl:value-of select="/root/gui/nodeUrl"/>
      </foaf:homepage>

      <!-- FIXME : void:Dataset -->
      <void:openSearchDescription><xsl:value-of select="/root/gui/nodeUrl"/>eng/portal.opensearch</void:openSearchDescription>
      <void:uriLookupEndpoint><xsl:value-of select="/root/gui/nodeUrl"/>eng/rdf.search?any=</void:uriLookupEndpoint>


      <!-- The entity responsible for making the catalog online. -->
      <dct:publisher rdf:resource="{$resourcePrefix}/organizations/{encode-for-uri($env/system/site/organization)}"/>

      <!-- The knowledge organization system (KOS) used to classify catalog's datasets.
      -->
      <xsl:for-each select="/root/gui/thesaurus/thesauri/thesaurus">
        <dcat:themes rdf:resource="{$resourcePrefix}/registries/vocabularies/{key}"/>
      </xsl:for-each>


      <!-- The language of the catalog. This refers to the language used
        in the textual metadata describing titles, descriptions, etc.
        of the datasets in the catalog.

        http://www.ietf.org/rfc/rfc3066.txt

        Multiple values can be used. The publisher might also choose to describe
        the language on the dataset level (see dataset language).
      -->
      <dct:language>
        <xsl:value-of select="/root/gui/language"/>
      </dct:language>


      <!-- This describes the license under which the catalog can be used/reused and not the datasets.
        Even if the license of the catalog applies to all of its datasets it should be
        replicated on each dataset.-->

      <!-- TODO using VoIDx
      <dct:license>
      </dct:license>
      -->


      <!-- The geographical area covered by the catalog.
      <dct:Location>

       </dct:Location> -->

      <!-- List all catalogue records
        <dcat:dataset rdf:resource="http://localhost:8080/geonetwork/dataset/1"/>
        <dcat:record rdf:resource="http://localhost:8080/geonetwork/metadata/1"/>
      -->
      <xsl:apply-templates mode="record-reference" select="/root/*|/root/search/response/*"/>

    </dcat:Catalog>

    <!-- Organization in charge of the catalogue defined in the administration
    > system configuration -->
    <foaf:Organization rdf:about="{$resourcePrefix}/organizations/{encode-for-uri($env/system/site/organization)}">
      <foaf:name>
        <xsl:value-of select="$env/system/site/organization"></xsl:value-of>
      </foaf:name>
    </foaf:Organization>

    <!-- ConceptScheme describes all thesaurus available in the catalogue
      * Resource identifier is a local identifier for local thesaurus or public URI if external
    -->
    <xsl:for-each select="/root/gui/thesaurus/thesauri/thesaurus">
      <skos:ConceptScheme rdf:about="{$resourcePrefix}/registries/vocabularies/{key}">
        <dct:title>
          <xsl:value-of select="title"/>
        </dct:title>
        <!-- TODO : add conceptSchemes
          <dc:description>Thesaurus name.</dc:description>
          <dc:creator>
          <foaf:Organization>
          <foaf:name>Thesaurus org</foaf:name>
          </foaf:Organization>
          </dc:creator>-->
        <dct:uri><xsl:value-of select="$url"/>/srv/eng/thesaurus.download?ref=<xsl:value-of
          select="key"/>
        </dct:uri>
        <!--
          <dct:issued>2008-06-01</dct:issued>
          <dct:modified>2008-06-01</dct:modified>-->
      </skos:ConceptScheme>
    </xsl:for-each>
  </xsl:template>


  <!-- Avoid request and gui tag in template modes related to metadata conversion -->
  <xsl:template mode="record-reference" match="gui|request|metadata|translations|search|summary"/>

  <xsl:template mode="to-dcat" match="gui|request|metadata|translations|search|summary"/>

  <xsl:template mode="references" match="gui|request|metadata|translations|search|summary"/>

  <xsl:template mode="record-reference" match="metadata" priority="2">
    <dcat:dataset rdf:resource="{$resourcePrefix}/datasets/{geonet:info/uuid}"/>
    <dcat:record rdf:resource="{$resourcePrefix}/records/{geonet:info/uuid}"/>
  </xsl:template>


</xsl:stylesheet>
