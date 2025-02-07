<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                exclude-result-prefixes="#all">

  <!-- Resource
   Unsupported:
   * dcat:first|previous(sameAs replaces, previousVersion?)|next|last|hasVersion (using the Associated API, navigate to series and sort by date?)
   * dct:isReferencedBy (using the Associated API)
   * dcat:hasCurrentVersion  (using the Associated API)
   * dct:rights
   * odrl:hasPolicy
   -->
  <xsl:template mode="iso19115-3-to-dcat-resource"
                name="iso19115-3-to-dcat-resource"
                match="mdb:MD_Metadata">
    <xsl:apply-templates mode="iso19115-3-to-dcat"
                         select="mdb:identificationInfo/*/mri:citation/*/cit:title
                                  |mdb:identificationInfo/*/mri:abstract
                                  |mdb:identificationInfo/*/mri:citation/*/cit:identifier
                                  |mdb:identificationInfo/*/mri:citation/*/cit:date/*[cit:dateType/*/@codeListValue = $isoDateTypeToDcatCommonNames/text()]/cit:date
                                  |mdb:identificationInfo/*/mri:citation/*/cit:edition
                                  |mdb:identificationInfo/*/mri:defaultLocale
                                  |mdb:identificationInfo/*/mri:otherLocale
                                  |mdb:identificationInfo/*/mri:resourceConstraints/*
                                  |mdb:identificationInfo/*/mri:status
                                  |mdb:identificationInfo/*/mri:descriptiveKeywords
                                  |mdb:identificationInfo/*/mri:pointOfContact
                                  |mdb:identificationInfo/*/mri:associatedResource
                                  |mdb:dataQualityInfo/*/mdq:report/*/mdq:result[mdq:DQ_ConformanceResult and mdq:DQ_ConformanceResult/mdq:pass/*/text() = 'true']
                                  |mdb:resourceLineage/*/mrl:statement
                                  |mdb:metadataLinkage
                          "/>

  </xsl:template>

</xsl:stylesheet>
