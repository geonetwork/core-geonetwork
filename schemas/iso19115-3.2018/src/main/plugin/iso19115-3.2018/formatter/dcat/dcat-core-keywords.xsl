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
      <xsl:apply-templates mode="iso19115-3-to-dcat"
                           select="."/>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
