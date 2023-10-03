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

  <xsl:import href="dcat-commons.xsl"/>
  <xsl:import href="dcat-variables.xsl"/>
  <xsl:import href="dcat-utils.xsl"/>

  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:MD_Metadata">
    <rdf:Description>
      <xsl:apply-templates mode="iso19115-3-to-dcat"
                           select="mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue"/>

      <foaf:isPrimaryTopicOf xmlns:foaf="http://xmlns.com/foaf/0.1/">
        <rdf:Description>
          <rdf:type rdf:resource="http://www.w3.org/ns/dcat#CatalogRecord"/>

          <xsl:apply-templates mode="iso19115-3-to-dcat"
                               select="mdb:metadataIdentifier/*/mcc:code"/>
        </rdf:Description>
      </foaf:isPrimaryTopicOf>

      <xsl:apply-templates mode="iso19115-3-to-dcat"
                           select="mdb:identificationInfo/*/mri:citation/*/cit:title"/>
    </rdf:Description>
  </xsl:template>


  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:metadataIdentifier/*/mcc:code">
    <dct:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#string">
      <xsl:value-of select="gco:CharacterString/text()"/>
    </dct:identifier>
  </xsl:template>


  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:identificationInfo/*/mri:citation/*/cit:title">
    <xsl:call-template name="rdf-localised">
      <xsl:with-param name="nodeName" select="'dct:title'"/>
    </xsl:call-template>
  </xsl:template>

  <!--<xsl:template mode="iso19115-3-to-dcat"
                match="mdb:MD_Metadata[not(mdb:metadataIdentifier)]">
    <xsl:call-template name="create-node-with-info">
      <xsl:with-param name="message"
                      select="'Metadata record does not contains metadata identifier.'"/>
    </xsl:call-template>
  </xsl:template>-->

  <xsl:template name="iso19115-3-to-dcat-metadataScope"
                mode="iso19115-3-to-dcat"
                match="mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue">
    <xsl:variable name="dcmiType"
                  select="$dcmiTypeVocabularyToIso[. = current()]"
                  as="xs:string?"/>

    <xsl:choose>
      <xsl:when test="$dcmiType">
        <rdf:type rdf:resource="http://purl.org/dc/dcmitype/{$dcmiType}"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="create-node-with-info">
          <xsl:with-param name="message"
                          select="concat('No DCMI type defined for value ', current(),
                          '. Default to Dataset.')"/>
          <xsl:with-param name="node">
            <rdf:type rdf:resource="http://purl.org/dc/dcmitype/Dataset"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
