<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:mobilitydcatap="https://w3id.org/mobilitydcat-ap"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                exclude-result-prefixes="#all">

  <!--
  RDF Property:	dcat:distribution
  Definition:	An available distribution of the dataset.
  Sub-property of:	dcterms:relation
  Domain:	dcat:Dataset
  Range:	dcat:Distribution

  TODO https://mobilitydcat-ap.github.io/mobilityDCAT-AP/releases/index.html#mandatory-properties-for-distribution
  -->
  <xsl:variable name="mobilityThemeThesaurusKey"
                select="'https://w3id.org/mobilitydcat-ap/mobility-data-standard/1.0.0'"/>

  <!--<xsl:template mode="iso19115-3-to-dcat"
                name="iso19115-3-to-dcat-distribution"
                match="mdb:distributionInfo//mrd:onLine">
    <dcat:distribution>
      <dcat:Distribution>
        <mobilitydcatap:mobilityDataStandard>
          <skos:Concept>

          </skos:Concept>
        </mobilitydcatap:mobilityDataStandard>
      </dcat:Distribution>
    </dcat:distribution>
  </xsl:template>-->
</xsl:stylesheet>
