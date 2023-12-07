<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="#all">

  <xsl:variable name="europaPublicationBaseUri" select="'http://publications.europa.eu/resource/authority/'"/>
  <xsl:variable name="europaPublicationCorporateBody" select="concat($europaPublicationBaseUri,'corporate-body/')"/>
  <xsl:variable name="europaPublicationCountry" select="concat($europaPublicationBaseUri,'country/')"/>
  <xsl:variable name="europaPublicationFrequency" select="concat($europaPublicationBaseUri,'frequency/')"/>
  <xsl:variable name="europaPublicationFileType" select="concat($europaPublicationBaseUri,'file-type/')"/>
  <xsl:variable name="europaPublicationLanguage" select="concat($europaPublicationBaseUri,'language/')"/>

  <xsl:variable name="isoCodeListBaseUri" select="'http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#'"/>

  <!-- Mapping ISO element path to corresponding DCAT names -->
  <xsl:variable name="isoToDcatCommonNames"
                as="node()*">
    <entry key="dct:title">mdb:MD_Metadata/mdb:identificationInfo/mri:MD_DataIdentification/mri:citation/cit:CI_Citation/cit:title</entry>
    <entry key="dct:title">mdb:MD_Metadata/mdb:metadataStandard/cit:CI_Citation/cit:title</entry>
    <entry key="dcat:version" isMultilingual="false">mdb:MD_Metadata/mdb:identificationInfo/mri:MD_DataIdentification/mri:citation/cit:CI_Citation/cit:edition</entry>
    <entry key="dcat:keyword">mdb:MD_Metadata/mdb:identificationInfo/mri:MD_DataIdentification/mri:descriptiveKeywords/mri:MD_Keywords/mri:keyword</entry>
    <entry key="dct:description">mdb:MD_Metadata/mdb:identificationInfo/mri:MD_DataIdentification/mri:abstract</entry>
    <entry key="owl:versionInfo">mdb:MD_Metadata/mdb:metadataStandard/cit:CI_Citation/cit:edition</entry>
  </xsl:variable>

  <xsl:variable name="isoDateTypeToDcatCommonNames"
                as="node()*">
    <entry key="dct:issued">creation</entry>
    <entry key="dct:issued">publication</entry>
    <entry key="dct:modified">revision</entry>
  </xsl:variable>

  <xsl:variable name="isoContactRoleToDcatCommonNames"
                as="node()*">
    <entry key="dct:creator" as="foaf">author</entry>
    <entry key="dct:publisher" as="foaf">publisher</entry>
    <entry key="dct:contactPoint" as="vcard">pointOfContact</entry>
    <entry key="dct:rightsHolder" as="foaf">owner</entry> <!-- TODO: Check if dcat or only in profile -->
    <!-- Others are prov:qualifiedAttribution -->
  </xsl:variable>


  <!-- DCAT resource type from ISO hierarchy level -->
  <xsl:variable name="dcatResourceTypeToIso"
                as="node()*">
    <entry key="DatasetSeries">series</entry>
    <entry key="Dataset">dataset</entry>
    <entry key="Dataset">nonGeographicDataset</entry>
    <entry key="Dataset"></entry>
    <entry key="DataService">service</entry>
    <entry key="Catalogue">?</entry>
  </xsl:variable>

  <!-- https://www.dublincore.org/specifications/dublin-core/dcmi-terms/#section-7 -->
  <xsl:variable name="dcmiTypeVocabularyToIso"
                as="node()*">
    <entry key="Collection">series</entry>
    <entry key="Dataset">dataset</entry>
    <entry key="Dataset">nonGeographicDataset</entry>
    <entry key="Event"></entry>
    <entry key="Image"></entry>
    <entry key="InteractiveResource"></entry>
    <entry key="MovingImage"></entry>
    <entry key="PhysicalObject"></entry>
    <entry key="Service">service</entry>
    <entry key="Software">software</entry>
    <entry key="Sound"></entry>
    <entry key="StillImage"></entry>
    <entry key="Text"></entry>
  </xsl:variable>

</xsl:stylesheet>
