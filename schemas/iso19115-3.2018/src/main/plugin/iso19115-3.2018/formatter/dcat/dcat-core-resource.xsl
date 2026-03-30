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
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:mdUtil="java:org.fao.geonet.api.records.MetadataUtils"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:pav="http://purl.org/pav/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                exclude-result-prefixes="#all">

  <xsl:variable name="nodeUrl"
                select="util:getSettingValue('nodeUrl')"/>

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

    <xsl:call-template name="related-record"/>

  </xsl:template>



<!--  <xsl:template mode="iso19115-3-to-eu-dcat-ap"
                match="mrl:source">
    <dct:source>
    Need to point to the Dataset and not the CatalogRecord
    See below
      <xsl:call-template name="rdf-object-ref-attribute">
        <xsl:with-param name="isAbout" select="false()"/>
      </xsl:call-template>
    </dct:source>
  </xsl:template>-->


  <xsl:template name="related-record">
    <xsl:variable name="associations"
                        select="mdUtil:getAssociatedAsXml(mdb:metadataIdentifier/*/mcc:code/*/text())"
                        as="node()?"/>

    <xsl:variable name="legislations"
                        select="mdb:identificationInfo/*/mri:descriptiveKeywords/*/mri:keyword[starts-with(*/@xlink:href, 'http://data.europa.eu/eli')]"/>

    <xsl:variable name="metadata" select="."/>

    <xsl:for-each select="$associations/relations/*">
      <xsl:sort select="@url"/>
      <xsl:variable name="resourceIdentifierWithHttpCodeSpace"
                          select="(root/resourceIdentifier[starts-with(codeSpace, 'http')])[1]"/>
      <xsl:variable name="recordUri"
                          select="if ($resourceIdentifierWithHttpCodeSpace)
                                       then concat($resourceIdentifierWithHttpCodeSpace/codeSpace, $resourceIdentifierWithHttpCodeSpace/code)
                                       else @url" />

      <xsl:choose>
        <xsl:when test="local-name() = 'parent'">
          <dcat:inSeries rdf:resource="{$recordUri}"/>
        </xsl:when>
        <xsl:when test="local-name() = 'children'">
          <dcat:seriesMember rdf:resource="{$recordUri}"/>
        </xsl:when>
        <xsl:when test="local-name() = 'brothersAndSisters'">
          <dct:relation rdf:resource="{$recordUri}"/>
        </xsl:when>
        <xsl:when test="local-name() = 'sources'">
          <dct:source rdf:resource="{$recordUri}"/>
        </xsl:when>
        <xsl:when test="local-name() = 'siblings' and not(@uuid = (../children/@uuid))">
          <xsl:variable name="associationType"
                        select="@associationType"/>
          <xsl:variable name="initiativeType"
                        select="@initiativeType"/>
          <xsl:variable name="dcTypeForAssociationAndInitiative"
                        as="xs:string?"
                        select="$isoAssociatedTypesToDcatCommonNames[@associationType = $associationType and @initiativeType = $initiativeType]/text()"/>
          <xsl:variable name="dcTypeForAssociation"
                        as="xs:string?"
                        select="$isoAssociatedTypesToDcatCommonNames[@associationType = $associationType and not(@initiativeType)]/text()"/>
          <xsl:variable name="elementType"
                        as="xs:string"
                        select="if ($dcTypeForAssociationAndInitiative)
                          then $dcTypeForAssociationAndInitiative
                          else if ($dcTypeForAssociation)
                          then $dcTypeForAssociation
                          else 'dct:relation'"/>

          <xsl:element name="{$elementType}">
            <xsl:attribute name="rdf:resource" select="$recordUri"/>
          </xsl:element>
        </xsl:when>
        <xsl:when test="local-name() = 'datasets'">
          <dcat:servesDataset>
            <dcat:Dataset rdf:about="{$recordUri}"/>
          </dcat:servesDataset>
        </xsl:when>
        <xsl:when test="local-name() = 'services'">

          <xsl:variable name="mainLink"
                        select="(root/link[not(function = ('information', 'dataQualityReport'))])[1]"/>

          <xsl:variable name="serviceUri"
                        select="if (root/resourceIdentifier) then concat(root/resourceIdentifier[1]/codeSpace, root/resourceIdentifier[1]/code) else ." />

          <xsl:choose>
            <!-- Only record with resourceType is service are mapped to a distribution.
            Other related services which can be software, applications are mapped to foaf:page -->
            <xsl:when test="root/resourceType = 'service'">
              <dcat:distribution>
                <dcat:Distribution>
                  <xsl:for-each select="$mainLink/urlObject/default">
                    <dcat:accessURL rdf:resource="{.}"/>
                    <dcat:accessService rdf:resource="{$serviceUri}"/>
                  </xsl:for-each>

                  <xsl:call-template name="rdf-index-field-localised">
                    <xsl:with-param name="nodeName" select="'dct:title'"/>
                    <xsl:with-param name="field" select="root/resourceTitleObject"/>
                  </xsl:call-template>

                  <xsl:call-template name="rdf-index-field-localised">
                    <xsl:with-param name="nodeName" select="'dct:description'"/>
                    <xsl:with-param name="field" select="root/resourceAbstractObject"/>
                  </xsl:call-template>
                  <!--
                   RDF Property:	dcterms:issued
                   Definition:	Date of formal issuance (e.g., publication) of the distribution.
                  -->
                  <xsl:for-each select="$metadata//mrd:MD_Distributor/mrd:distributionOrderProcess/*/mrd:plannedAvailableDateTime|
                                               $metadata/mdb:identificationInfo/*/mri:citation/*/cit:date/*[cit:dateType/*/@codeListValue = 'publication']">
                    <xsl:apply-templates mode="iso19115-3-to-dcat"
                                         select=".">
                      <xsl:with-param name="dateType" select="'publication'"/>
                    </xsl:apply-templates>
                  </xsl:for-each>

                  <!--
                  RDF Property:	dcterms:modified
                  Definition:	Most recent date on which the distribution was changed, updated or modified.
                  Range:	rdfs:Literal encoded using the relevant ISO 8601 Date and Time compliant string [DATETIME] and typed using the appropriate XML Schema datatype [XMLSCHEMA11-2] (xsd:gYear, xsd:gYearMonth, xsd:date, or xsd:dateTime).
                  -->
                  <xsl:for-each select="$metadata//mrd:MD_Distributor/mrd:distributionOrderProcess/*/mrd:plannedAvailableDateTime|
                                               $metadata/mdb:identificationInfo/*/mri:citation/*/cit:date/*[cit:dateType/*/@codeListValue = 'revision']">
                    <xsl:apply-templates mode="iso19115-3-to-dcat"
                                         select=".">
                      <xsl:with-param name="dateType" select="'revision'"/>
                    </xsl:apply-templates>
                  </xsl:for-each>

                  <xsl:apply-templates mode="iso19115-3-to-dcat"
                                       select="$metadata/mdb:identificationInfo/*/mri:resourceConstraints/*[mco:useConstraints]"/>
                  <xsl:apply-templates mode="iso19115-3-to-dcat"
                                       select="$metadata/mdb:identificationInfo/*/mri:resourceConstraints/*[mco:accessConstraints]"/>

                  <xsl:apply-templates mode="iso19115-3-to-dcat"
                                       select="$metadata/mdb:identificationInfo/*/mri:defaultLocale"/>

                  <xsl:apply-templates mode="iso19115-3-to-dcat"
                                       select="$legislations"/>

                  <xsl:call-template name="rdf-format-as-mediatype">
                    <xsl:with-param name="format" select="$mainLink/protocol"/>
                  </xsl:call-template>
                </dcat:Distribution>
              </dcat:distribution>
            </xsl:when>
            <xsl:otherwise>
              <foaf:page>
                <foaf:Document rdf:about="{$mainLink/urlObject/default}">
                  <dct:title><xsl:value-of select="root/resourceTitleObject/default"/></dct:title>
                  <dct:description xml:lang="fre"><xsl:value-of select="root/resourceAbstractObject/default"/></dct:description>
                </foaf:Document>
              </foaf:page>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <!-- TODO: other type of relations -->
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
