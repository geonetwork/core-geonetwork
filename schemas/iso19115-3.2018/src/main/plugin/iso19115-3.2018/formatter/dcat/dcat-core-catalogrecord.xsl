<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:gn-fn-dcat="http://geonetwork-opensource.org/xsl/functions/dcat"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                exclude-result-prefixes="#all">

  <!-- Create CatalogueRecord -->
  <xsl:template mode="iso19115-3-to-dcat-catalog-record"
                name="iso19115-3-to-dcat-catalog-record"
                match="mdb:MD_Metadata">
    <xsl:param name="additionalProperties"
               as="node()*"/>
    <xsl:variable name="properties" as="node()*">
      <xsl:apply-templates mode="iso19115-3-to-dcat"
                           select="mdb:metadataIdentifier
                                  |mdb:identificationInfo/*/mri:citation/*/cit:title
                                  |mdb:identificationInfo/*/mri:abstract
                                  |mdb:dateInfo"/>
      <xsl:copy-of select="$additionalProperties"/>
    </xsl:variable>

    <xsl:call-template name="rdf-build-catalogue-record">
      <xsl:with-param name="properties" select="$properties"/>
      <xsl:with-param name="metadata" select="."/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template name="rdf-build-catalogue-record">
    <xsl:param name="properties"
               as="node()*"/>
    <xsl:param name="metadata"
               as="node()"/>

    <foaf:isPrimaryTopicOf>
      <rdf:Description rdf:about="{gn-fn-dcat:getRecordUri(.)}">
        <rdf:type rdf:resource="http://www.w3.org/ns/dcat#CatalogRecord"/>
        <xsl:copy-of select="$properties"/>
        <foaf:primaryTopic rdf:resource="{gn-fn-dcat:getResourceUri($metadata)}"/>
      </rdf:Description>
    </foaf:isPrimaryTopicOf>
  </xsl:template>
</xsl:stylesheet>
