<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:reg="http://standards.iso.org/iso/19115/-3/reg/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                exclude-result-prefixes="#all">

  <!--
  RDF Property:	dcat:theme
  Type:	owl:ObjectProperty
  Definition:	A main category of the resource. A resource can have multiple themes.
  Sub-property of:	dcterms:subject
  Usage note:	The set of themes used to categorize the resources are organized in a skos:ConceptScheme, skos:Collection, owl:Ontology or similar, describing all the categories and their relations in the catalog.

  RDF Property:	dcat:keyword
  Definition:	A keyword or tag describing the resource.
  Range:	rdfs:Literal
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:descriptiveKeywords">
    <xsl:for-each select="*/mri:keyword[*/text() != '']">
      <!-- TODO: Dispatch between theme and keyword
        In SEMICeu the choice is made if "In case the concept's URI is NOT provided", then keyword else theme
       <dcat:theme xmlns:dcat="http://www.w3.org/ns/dcat#" rdf:parseType="Resource">
        <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
        <skos:prefLabel xmlns:skos="http://www.w3.org/2004/02/skos/core#" xml:lang="fr">BDInfraSIGNO</skos:prefLabel>
        <skos:inScheme xmlns:skos="http://www.w3.org/2004/02/skos/core#" rdf:resource="https://metawal.wallonie.be/thesaurus/infrasig"/>
      </dcat:theme>
       -->
      <xsl:apply-templates mode="iso19115-3-to-dcat"
                           select="."/>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
