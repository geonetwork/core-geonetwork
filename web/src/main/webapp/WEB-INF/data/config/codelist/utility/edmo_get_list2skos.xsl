<xsl:stylesheet version="2.0" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:skos="http://www.w3.org/2004/02/skos/core#" xmlns:dc="http://purl.org/dc/terms/"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:grg="http://www.isotc211.org/schemas/grg/" xmlns:owl="http://www.w3.org/2002/07/owl#"
  xmlns:void="http://rdfs.org/ns/void#" xmlns:gml="http://www.opengis.net/gml#"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="#all">

  <xsl:output encoding="UTF-8" indent="yes" method="xml"/>

  <xsl:variable name="thesaurusId" select="'http://seadatanet.maris2.nl/webservices/edmo#'"/>
  <xsl:template match="/">

    <rdf:RDF xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:foaf="http://xmlns.com/foaf/0.1/"
      xmlns:dcterms="http://purl.org/dc/terms/" xmlns:skos="http://www.w3.org/2004/02/skos/core#"
      xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
      xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">

      <skos:ConceptScheme rdf:about="{$thesaurusId}">
        <dc:title>SDN EDMO Identifier</dc:title>
        <dc:description> SKOS thesaurus created from
          http://seadatanet.maris2.nl/webservices/edmo/ws_edmo_get_list. </dc:description>
        <dcterms:issued>2014-10-08</dcterms:issued>
        <dcterms:modified>2014-10-08</dcterms:modified>
      </skos:ConceptScheme>

      <xsl:apply-templates/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="Organisation" priority="2">
    <skos:Concept rdf:about="{$thesaurusId}{n_code}">
      <skos:prefLabel xml:lang="en"><xsl:value-of select="name"/></skos:prefLabel>
      <skos:definition xml:lang="en"/>
      <skos:prefLabel xml:lang="fr"><xsl:value-of select="name"/></skos:prefLabel>
      <skos:definition xml:lang="fr"/>
    </skos:Concept>
  </xsl:template>
</xsl:stylesheet>
