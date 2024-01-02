<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                exclude-result-prefixes="#all">
  <!-- http://data.europa.eu/930/ -->

  <xsl:import href="../dcat/dcat-core.xsl"/>
  <xsl:import href="eu-geodcat-ap-variables.xsl"/>

  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue">
    <xsl:call-template name="iso19115-3-to-dcat-metadataScope"/>

    <xsl:for-each select="$inspireResourceTypeVocabularyToIso[. = current()]/@key">
      <dct:type rdf:resource="{concat($inspireResourceTypeCodelistUri, current())}"/>
    </xsl:for-each>
  </xsl:template>


  <!-- TODO: CatalogRecord / Optional properties: https://semiceu.github.io/GeoDCAT-AP/releases/#optional-properties-for-catalogue-record -->

  <!-- TODO: Dataset / ReferenceSystem -->


</xsl:stylesheet>
