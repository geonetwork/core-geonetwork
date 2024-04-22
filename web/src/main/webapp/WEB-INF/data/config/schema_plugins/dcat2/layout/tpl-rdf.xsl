<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">
  <!--
    Create reference block to metadata record and dataset to be added in dcat:Catalog usually.
  -->
  <xsl:template match="rdf:RDF"
                mode="record-reference">
    <dcat:dataset rdf:resource="{$serviceUrl}resource/{*/dct:identifier[1]}"/>
  </xsl:template>

  <xsl:template match="rdf:RDF" mode="to-dcat">
    <xsl:copy-of select="*"/>
  </xsl:template>

  <xsl:template match="rdf:RDF" mode="references"/>

  <xsl:template mode="rdf:RDF" match="gui|request|metadata"/>
</xsl:stylesheet>
