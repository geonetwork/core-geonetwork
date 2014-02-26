<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork" xmlns:saxon="http://saxon.sf.net/"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:void="http://www.w3.org/TR/void/" xmlns:dcat="http://www.w3.org/ns/dcat#"
  xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/"
  xmlns:dctype="http://purl.org/dc/dcmitype/" xmlns:skos="http://www.w3.org/2004/02/skos/core#"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" extension-element-prefixes="saxon"
  exclude-result-prefixes="#all">

  <!-- 
    Create reference block to metadata record and dataset to be added in dcat:Catalog usually.
  -->
  <!-- FIME : $url comes from a global variable. -->
  <xsl:template match="simpledc" mode="record-reference">
    <dcat:dataset rdf:resource="{$url}/resource/{dc:identifier}"/>
    <dcat:record rdf:resource="{$url}/metadata/{dc:identifier}"/>
  </xsl:template>


  <!--
    Convert DC record to DCAT
    -->
  <xsl:template match="simpledc" mode="to-dcat">
    <!-- Catalogue records
      "A record in a data catalog, describing a single dataset."
    -->
    <dcat:CatalogRecord rdf:about="{$url}/metadata/{dc:identifier}">
      <!-- Link to a dcat:Dataset or a rdf:Description for services and feature catalogue. -->
      <foaf:primaryTopic rdf:resource="{$url}/resource/{dc:identifier}"/>
    </dcat:CatalogRecord>
    <!-- Catalogue records
      "A record in a data catalog, describing a single dataset."
    -->
    <dcat:Dataset rdf:about="{$url}/metadata/{dc:identifier}">
      <xsl:copy-of select="dc:*|dct:*"/>
    </dcat:Dataset>
  </xsl:template>

  <xsl:template match="simpledc" mode="references"/>

  <xsl:template mode="simpledc" match="gui|request|metadata"/>

</xsl:stylesheet>
