<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                exclude-result-prefixes="#all">

  <!-- Create CatalogueRecord -->
  <xsl:template mode="iso19115-3-to-dcat-catalog-record"
                match="mdb:MD_Metadata">
    <xsl:variable name="properties" as="node()*">
      <xsl:apply-templates mode="iso19115-3-to-dcat"
                           select="mdb:metadataIdentifier
                                      |mdb:identificationInfo/*/mri:citation/*/cit:title
                                      |mdb:identificationInfo/*/mri:abstract
                                      |mdb:dateInfo/*[cit:dateType/*/@codeListValue = 'creation']/cit:date
                                      |mdb:dateInfo/*[cit:dateType/*/@codeListValue = 'revision']/cit:date
                                      |mdb:metadataStandard"/>
    </xsl:variable>

    <xsl:call-template name="rdf-build-catalogue-record">
      <xsl:with-param name="properties" select="$properties"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template name="rdf-build-catalogue-record">
    <xsl:param name="properties"
               as="node()*"/>

    <foaf:isPrimaryTopicOf>
      <rdf:Description>
        <rdf:type rdf:resource="http://www.w3.org/ns/dcat#CatalogRecord"/>
        <xsl:copy-of select="$properties"/>
      </rdf:Description>
    </foaf:isPrimaryTopicOf>
  </xsl:template>
</xsl:stylesheet>
