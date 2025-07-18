<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                exclude-result-prefixes="#all">

  <xsl:import href="../dcat/dcat-core.xsl"/>
  <xsl:import href="eu-dcat-ap-core-dataset.xsl"/>

  <xsl:param name="multipleAccrualPeriodicityAllowed"
             as="xs:string"
             select="'false'"/>

  <!--
   If true, all resource constraints are preserved in the output.
   The first one is a license, others are dct:rights.
   See iso19115-3-to-dcat-license in dcat-core-access-and-use.xsl
   -->
  <xsl:variable name="isPreservingAllResourceConstraints"
                as="xs:boolean"
                select="false()"/>

  <!--
  If true, the mapping of resource constraints to the EU DCAT-AP vocabulary is enabled.
  eg. http://creativecommons.org/licenses/by/4.0/ is replaced by http://publications.europa.eu/resource/authority/licence/CC_BY_4.0
  -->
  <xsl:variable name="isMappingResourceConstraintsToEuVocabulary"
                as="xs:boolean"
                select="true()"/>

  <!--
  If true, the ISO resource scope is preserved in the output.
  See iso19115-3-to-dcat-metadataScope in dcat-core.xsl
  -->
  <xsl:variable name="isPreservingIsoType"
                as="xs:boolean"
                select="false()"/>


  <xsl:template name="create-namespaces-eu-dcat-ap">
    <xsl:call-template name="create-namespaces"/>
    <xsl:namespace name="dcatap" select="'http://data.europa.eu/r5r/'"/>
    <xsl:namespace name="eli" select="'http://data.europa.eu/eli/ontology'"/>
  </xsl:template>

  <!--
  Catalogue Record
  [o]	title	Literal	0..*	A name given to the Catalogue Record.	This property can be repeated for parallel language versions of the name.
  [o]	description	Literal	0..*	A free-text account of the record. This property can be repeated for parallel language versions of the description.
  [o]	listing date	Temporal Literal	0..1	The date on which the description of the Dataset was included in the Catalogue.
  [o]	modification date	Temporal Literal	1	The most recent date on which the Catalogue entry was changed or modified.
  [o]	application profile	Standard	0..1	An Application Profile that the Dataset's metadata conforms to.
  [o]	primary topic	Catalogued Resource	1	A link to the Dataset, Data service or Catalog described in the record.	A catalogue record will refer to one entity in a catalogue. This can be either a Dataset or a Data Service. To ensure an unambigous reading of the cardinality the range is set to Catalogued Resource. However it is not the intend with this range to require the explicit use of the class Catalogued Record. As abstract class, an subclass should be used.
  -->
  <xsl:template mode="iso19115-3-to-dcat-catalog-record"
                name="iso19115-3-to-eu-dcat-ap-catalog-record"
                match="mdb:MD_Metadata">
    <xsl:param name="additionalProperties"
               as="node()*"/>

    <xsl:variable name="properties" as="node()*">
      <!--
      [o]	language	Linguistic system	0..*	A language used in the textual metadata describing titles, descriptions, etc. of the Dataset.	This property can be repeated if the metadata is provided in multiple languages.
      -->
      <xsl:apply-templates mode="iso19115-3-to-dcat"
                           select="mdb:defaultLocale
                                  |mdb:otherLocale"/>
      <!--
      [o]	change type	Concept	0..1	The status of the catalogue record in the context of editorial flow of the dataset and data service descriptions.

      Not supported. Could be Draft status.
      -->

      <!--
      [o]	source metadata	Catalogue Record	0..1	The original metadata that was used in creating metadata for the Dataset.
      In GeoDCAT-AP, this MAY refer to an INSPIRE / [ISO-19115] record that was transformed into the current Geo/DCAT-AP one.
      -->
      <xsl:apply-templates mode="iso19115-3-to-eu-dcat-ap"
                           select="mdb:metadataLinkage[*/cit:linkage/*/text() != '']"/>

      <xsl:copy-of select="$additionalProperties"/>

      <dct:conformsTo>
        <dct:Standard rdf:about="https://www.w3.org/TR/vocab-dcat/"/>
      </dct:conformsTo>
    </xsl:variable>

    <xsl:call-template name="iso19115-3-to-dcat-catalog-record">
      <xsl:with-param name="additionalProperties" select="$properties"/>
    </xsl:call-template>
  </xsl:template>

  <!--
   https://github.com/SEMICeu/DCAT-AP/issues/16
   -->
  <xsl:template mode="iso19115-3-to-eu-dcat-ap"
                match="mdb:MD_Metadata/mdb:metadataLinkage">
    <dct:source>
      <rdf:Description rdf:about="{*/cit:linkage/*/text()}">
        <rdf:type rdf:resource="http://www.w3.org/ns/dcat#CatalogRecord"/>
        <xsl:apply-templates mode="iso19115-3-to-dcat"
                             select="ancestor::mdb:MD_Metadata/mdb:metadataStandard
                                    |mdb:dateInfo/*[cit:dateType/*/@codeListValue = 'creation']/cit:date
                                    |mdb:dateInfo/*[cit:dateType/*/@codeListValue = 'revision']/cit:date
                                    |ancestor::mdb:MD_Metadata/mdb:defaultLocale/*/lan:characterEncoding/*/@codeListValue"/>
      </rdf:Description>
    </dct:source>
  </xsl:template>

  <!-- GeoDCAT-AP Optional properties (ignored in DCAT-AP)
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:MD_Metadata/mdb:defaultLocale/*/lan:characterEncoding/*/@codeListValue"/>



  <!--
  In ISO, license may be described in more than one elements (and could also define license per various scopes).
  EU DCAT-AP restrict it to one.
  TODO: Discuss

  Path=<http://purl.org/dc/terms/license>
    Message: maxCount[1]: Invalid cardinality: expected max 1: Got count = 4

   Options:
   * combine all licenses condition in one
   * keep only first.
   * Use dct:license for the first useLimitation and then dct:rights?
  -->


  <!-- [o]	provenance	Provenance Statement	0..*	A statement about the lineage of a Dataset.
  In DCAT, adms:versionNotes is used, see dcat/dcat-core-lineage.xsl -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:resourceLineage/*/mrl:statement">
    <dct:provenance>
      <dct:ProvenanceStatement>
        <xsl:call-template name="rdf-localised">
          <xsl:with-param name="nodeName" select="'dct:description'"/>
        </xsl:call-template>
      </dct:ProvenanceStatement>
    </dct:provenance>
  </xsl:template>



  <!--
  applicable legislation	Legal Resource	1..*	The legislation that mandates the creation or management of the Data Service.
  **For HVD the value MUST include the ELI http://data.europa.eu/eli/reg_impl/2023/138/oj.**
  As multiple legislations may apply to the resource the maximum cardinality is not limited.

  See DCAT-AP
  applicable legislation	Legal Resource	0..*	The legislation that mandates the creation or management of the Catalog.

  To create valid HVD document, a keyword anchor or a title href of mri:resourceConstraints/mco:MD_LegalConstraints/mco:reference
  in the ISO record MUST define the ELI http://data.europa.eu/eli/reg_impl/2023/138/oj.
  -->


  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:distributionInfo//mrd:onLine">
    <xsl:call-template name="iso19115-3-to-dcat-distribution">
      <xsl:with-param name="additionalProperties">
        <!-- In HVD applicable legislation	Legal Resource	1..* -->
        <xsl:apply-templates mode="iso19115-3-to-dcat"
                             select="ancestor::mdb:MD_Metadata/mdb:identificationInfo/*/mri:descriptiveKeywords/*/mri:keyword[starts-with(*/@xlink:href, 'http://data.europa.eu/eli')]"/>


        <xsl:if test="$isCopyingDatasetInfoToDistribution = false()">
          <xsl:apply-templates mode="iso19115-3-to-dcat"
                               select="ancestor::mdb:MD_Metadata/mdb:identificationInfo/*/mri:resourceConstraints/mco:MD_LegalConstraints/mco:reference"/>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>
