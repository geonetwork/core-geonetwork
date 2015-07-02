<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                  xmlns:gco="http://www.isotc211.org/2005/gco"
                  xmlns:gmd="http://www.isotc211.org/2005/gmd"
                  xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                  xmlns:dcat="http://www.w3.org/ns/dcat#"
                  xmlns:dct="http://purl.org/dc/terms/"
                  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                  xmlns:foaf="http://xmlns.com/foaf/0.1/"
                  xmlns:void="http://www.w3.org/TR/void/"
                  xmlns:xslUtils="java:org.fao.geonet.util.XslUtil">

  <xsl:import href="../present/metadata-rdf.xsl" />

  <xsl:variable name="url" select="xslUtils:getSiteUrl()" />
  <xsl:variable name="language" select="xslUtils:getLanguage()"/>
  <xsl:variable name="iso2letterLanguageCode" select="xslUtils:twoCharLangCode($language)"/>

  <!-- ============================================================================================ -->

  <xsl:template match="gmd:MD_Metadata">
    <rdf:RDF xmlns:dct="http://purl.org/dc/terms/" xmlns:dctype="http://purl.org/dc/dcmitype/"
             xmlns:skos="http://www.w3.org/2004/02/skos/core#"
             xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:foaf="http://xmlns.com/foaf/0.1/"
             xmlns:void="http://www.w3.org/TR/void/"
             xmlns:dcat="http://www.w3.org/ns/dcat#">

      <xsl:call-template name="catalogue"/>

      <xsl:apply-templates select="." mode="to-dcat" />

      <xsl:apply-templates mode="references" select="."/>

    </rdf:RDF>
  </xsl:template>

  <xsl:template name="catalogue">


    <!-- First, the local catalog description using dcat:Catalog.
      "Typically, a web-based data catalog is represented as a single instance of this class."
      ... also describe harvested catalogues if harvested records are in the current dump.
    -->
    <dcat:Catalog rdf:about="{$url}">

      <!-- A name given to the catalog. -->
      <dct:title xml:lang="{$iso2letterLanguageCode}">
        <xsl:value-of select="/root/gui/env/site/name"/>
      </dct:title>

      <!-- free-text account of the catalog. -->
      <dct:description/>

      <rdfs:label xml:lang="{$iso2letterLanguageCode}">
        <xsl:value-of select="/root/gui/env/site/name"/> (<xsl:value-of select="/root/gui/env/site/organization"/>)</rdfs:label>

      <!-- The homepage of the catalog -->
      <foaf:homepage><xsl:value-of select="$url"/></foaf:homepage>

      <!-- FIXME : void:Dataset -->
      <void:openSearchDescription><xsl:value-of select="$url"/>/srv/eng/portal.opensearch</void:openSearchDescription>
      <void:uriLookupEndpoint><xsl:value-of select="$url"/>/srv/eng/rdf.search?any=</void:uriLookupEndpoint>


      <!-- The entity responsible for making the catalog online. -->
      <dct:publisher rdf:resource="{$url}/organization/0"/>

      <!-- The knowledge organization system (KOS) used to classify catalog's datasets.
      -->
      <xsl:for-each select="/root/gui/thesaurus/thesauri/thesaurus">
        <dcat:themes rdf:resource="{$url}/thesaurus/{key}"/>
      </xsl:for-each>


      <!-- The language of the catalog. This refers to the language used
        in the textual metadata describing titles, descriptions, etc.
        of the datasets in the catalog.
        http://www.ietf.org/rfc/rfc3066.txt
        Multiple values can be used. The publisher might also choose to describe
        the language on the dataset level (see dataset language).
      -->
      <dct:language><xsl:value-of select="$language"/></dct:language>


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
      <xsl:apply-templates mode="record-reference" select="/root/*"/>
    </dcat:Catalog>

    <!-- Organization in charge of the catalogue defined in the administration
    > system configuration -->
    <foaf:Organization rdf:about="{$url}/organization/0">
      <foaf:name><xsl:value-of select="/root/gui/env/site/organization"></xsl:value-of></foaf:name>
    </foaf:Organization>

    <!-- ConceptScheme describes all thesaurus available in the catalogue
      * Resource identifier is a local identifier for local thesaurus or public URI if external
    -->
    <xsl:for-each select="/root/gui/thesaurus/thesauri/thesaurus">
      <skos:ConceptScheme rdf:about="{$url}/thesaurus/{key}">
        <dct:title><xsl:value-of select="title"/></dct:title>
        <!-- TODO : add conceptSchemes
          <dc:description>Thesaurus name.</dc:description>
          <dc:creator>
          <foaf:Organization>
          <foaf:name>Thesaurus org</foaf:name>
          </foaf:Organization>
          </dc:creator>-->
        <dct:uri><xsl:value-of select="$url"/>/srv/eng/thesaurus.download?ref=<xsl:value-of select="key"/></dct:uri>
        <!--
          <dct:issued>2008-06-01</dct:issued>
          <dct:modified>2008-06-01</dct:modified>-->
      </skos:ConceptScheme>
    </xsl:for-each>
  </xsl:template>

  <!-- ============================================================================================ -->

  <xsl:template match="*">
    <xsl:apply-templates select="*"/>
  </xsl:template>
</xsl:stylesheet>