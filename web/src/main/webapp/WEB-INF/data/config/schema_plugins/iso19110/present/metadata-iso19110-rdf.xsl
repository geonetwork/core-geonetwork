<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork" 
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:void="http://www.w3.org/TR/void/" 
  xmlns:dcat="http://www.w3.org/ns/dcat#"
  xmlns:dc="http://purl.org/dc/elements/1.1/" 
  xmlns:dct="http://purl.org/dc/terms/"
  xmlns:dctype="http://purl.org/dc/dcmitype/" 
  xmlns:skos="http://www.w3.org/2004/02/skos/core#"
  xmlns:gco="http://www.isotc211.org/2005/gco" 
  xmlns:gfc="http://www.isotc211.org/2005/gfc"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  extension-element-prefixes="saxon" exclude-result-prefixes="#all">


  <!-- TODO : add Multilingual metadata support -->

  <!-- 
    Create reference block to metadata record and dataset to be added in dcat:Catalog usually.
  -->
  <!-- FIME : $url comes from a global variable. -->
  <xsl:template match="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType" mode="record-reference">
    <dcat:dataset rdf:resource="{$url}/resource/{@uuid}"/>
    <dcat:record rdf:resource="{$url}/metadata/{@uuid}"/>
  </xsl:template>
  
  
  <!--
    Convert ISO record to DCAT
    -->
  <xsl:template match="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType" mode="to-dcat">
    <!-- Catalogue records
      "A record in a data catalog, describing a single dataset."        
    -->
    <rdf:Description rdf:about="{$url}/metadata/{@uuid}">
      <dc:title><xsl:value-of select="gfc:name/gco:CharacterString"/></dc:title>
      
      <!-- Metadata change date.
      "The date is encoded as a literal in "YYYY-MM-DD" form (ISO 8601 Date and Time Formats)." -->
      <xsl:variable name="date" select="substring-before(gfc:versionDate/gco:DateTime, 'T')"/>
      <dct:issued><xsl:value-of select="$date"/></dct:issued>
      <dct:modified><xsl:value-of select="$date"/></dct:modified>
      
      <!-- Source relation -->
      <xsl:for-each select="/root/gui/relation/*/response/metadata">
        <dc:relation rdf:resource="{$url}/metadata/{geonet:info/uuid}"/>
      </xsl:for-each>
      
    </rdf:Description>
  </xsl:template>
  
  <xsl:template mode="references" match="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType"/>
  
</xsl:stylesheet>
