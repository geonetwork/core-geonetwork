<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
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
       <dcat:theme xmlns:dcat="http://www.w3.org/ns/dcat#">
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
