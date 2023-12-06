<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                exclude-result-prefixes="#all">

  <!-- TODO: move to eu-geodcat-ap -->
  <xsl:variable name="inspireBaseUri" select="'http://inspire.ec.europa.eu/'"/>
  <xsl:variable name="inspireCodelistUri" select="concat($inspireBaseUri,'metadata-codelist/')"/>
  <xsl:variable name="inspireSpatialDataServiceCategoryCodelistUri" select="concat($inspireCodelistUri,'SpatialDataServiceCategory/')"/>
  <xsl:variable name="inspireDegreeOfConformityCodelistUri" select="concat($inspireCodelistUri,'DegreeOfConformity/')"/>
  <xsl:variable name="inspireResourceTypeCodelistUri" select="concat($inspireCodelistUri,'ResourceType/')"/>
  <xsl:variable name="inspireResponsiblePartyRoleCodelistUri" select="concat($inspireCodelistUri,'ResponsiblePartyRole/')"/>
  <xsl:variable name="inspireSpatialDataServiceTypeCodelistUri" select="concat($inspireCodelistUri,'SpatialDataServiceType/')"/>
  <xsl:variable name="inspireTopicCategoryCodelistUri" select="concat($inspireCodelistUri,'TopicCategory/')"/>

  <xsl:variable name="inspireResourceTypeVocabularyToIso"
                as="node()*">
    <entry key="series">series</entry>
    <entry key="dataset">dataset</entry>
    <entry key="dataset">nonGeographicDataset</entry>
    <entry key="service">service</entry>
  </xsl:variable>

</xsl:stylesheet>
