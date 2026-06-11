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

  <xsl:variable name="isoContactRoleToDcatCommonNames"
                as="node()*">
    <entry key="dct:creator" as="foaf">author</entry>
    <entry key="dct:publisher" as="foaf">publisher</entry>
    <entry key="dcat:contactPoint" as="vcard">pointOfContact</entry>
    <entry key="dct:rightsHolder" as="foaf">owner</entry>
    <entry key="geodcatap:custodian" as="vcard">custodian</entry>
    <entry key="geodcatap:distributor" as="vcard">distributor</entry>
    <entry key="geodcatap:originator" as="vcard">originator</entry>
    <entry key="geodcatap:principalInvestigator" as="vcard">principalInvestigator</entry>
    <entry key="geodcatap:processor" as="vcard">processor</entry>
    <entry key="geodcatap:resourceProvider" as="vcard">resourceProvider</entry>
    <entry key="geodcatap:user" as="vcard">user</entry>
  </xsl:variable>

  <xsl:variable name="ianaCharsetToIso"
                as="node()*">
    <entry key="ISO-10646-UCS-2">ucs2</entry>
    <entry key="ISO-10646-UCS-4">ucs4</entry>
    <entry key="UTF-7">utf7</entry>
    <entry key="UTF-8">utf8</entry>
    <entry key="UTF-16">utf16</entry>
    <entry key="ISO-8859-1">8859part1</entry>
    <entry key="ISO-8859-2">8859part2</entry>
    <entry key="ISO-8859-3">8859part3</entry>
    <entry key="ISO-8859-4">8859part4</entry>
    <entry key="ISO-8859-5">8859part5</entry>
    <entry key="ISO-8859-6">8859part6</entry>
    <entry key="ISO-8859-7">8859part7</entry>
    <entry key="ISO-8859-8">8859part8</entry>
    <entry key="ISO-8859-9">8859part9</entry>
    <entry key="ISO-8859-10">8859part10</entry>
    <entry key="ISO-8859-11">8859part11</entry>
    <entry key="ISO-8859-12">8859part12</entry>
    <entry key="ISO-8859-13">8859part13</entry>
    <entry key="ISO-8859-14">8859part14</entry>
    <entry key="ISO-8859-15">8859part15</entry>
    <entry key="ISO-8859-16">8859part16</entry>
    <!-- Mapping to be verified: multiple candidates are available in the IANA register for jis -->
    <entry key="JIS_Encoding">jis</entry>
    <entry key="Shift_JIS">shiftJIS</entry>
    <entry key="EUC-JP">eucJP</entry>
    <entry key="US-ASCII">usAscii</entry>
    <!-- Mapping to be verified: multiple candidates are available in the IANA register ebcdic  -->
    <entry key="IBM037">ebcdic</entry>
    <entry key="EUC-KR">eucKR</entry>
    <entry key="Big5">big5</entry>
    <entry key="GB2312">GB2312</entry>
  </xsl:variable>
</xsl:stylesheet>
