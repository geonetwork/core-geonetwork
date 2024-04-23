<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:dcatap="http://data.europa.eu/r5r/"
                xmlns:eli="http://data.europa.eu/eli/ontology"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:dct="http://purl.org/dc/terms/"
                exclude-result-prefixes="#all">
  <xsl:import href="../eu-dcat-ap/eu-dcat-ap-core.xsl"/>

  <xsl:variable name="hvdCategoryThesaurusKey"
                select="'http://data.europa.eu/bna/asd487ae75'"/>

  <xsl:variable name="hvdApplicableLegislationThesaurusKey"
                select="'http://data.europa.eu/r5r/applicableLegislation'"/>

  <xsl:variable name="euHvdDataCategories"
                select="document('vocabularies/high-value-dataset-category.rdf')"/>

  <xsl:variable name="euHvdApplicableLegislation"
                select="document('vocabularies/high-value-dataset-applicable-legislation.rdf')"/>


  <xsl:template mode="iso19115-3-to-dcat-resource"
                match="mdb:MD_Metadata">
    <xsl:call-template name="iso19115-3-to-dcat-ap-resource"/>

    <xsl:apply-templates mode="iso19115-3-to-dcat"
                         select="mdb:identificationInfo/*/mri:resourceConstraints/mco:MD_LegalConstraints/mco:reference"/>
  </xsl:template>


  <!--
  Dataset

  conforms to	Standard	0..*	An implementing rule or other specification.	The provided information should enable to the verification whether the detailed information requirements by the HVD is satisfied. For more usage suggestions see section on specific data requirements.	Link	A
  contact point	Kind	0..*	Contact information that can be used for sending comments about the Dataset.		Link	A
  dataset distribution	Distribution	1..*	An available Distribution for the Dataset.	The HVD IR is a quality improvement of existing datasets. The intention is that HVD datasets are publicly and open accessible. Therefore a Distribution is expected to be present. (Article 3.1)	Link	A

  DataService

  contact point	Kind	1..*	Contact information that can be used for sending comments about the Data Service.	Article 3.4 requires the designation of a point of contact for an API.	Link	P
  documentation	Document	1..*	A page that provides additional information about the Data Service.	Quality of service covers a broad spectrum of aspects. The HVD regulation does not list any mandatory topic. Therefore quality of service information is considered part of the generic documentation of a Data Service.		P
  endpoint description	Resource	0..*	A description of the services available via the end-points, including their operations, parameters etc.	The property gives specific details of the actual endpoint instances, while dct:conformsTo is used to indicate the general standard or specification that the endpoints implement.
  Article 3.3 requires to provide API documentation in a Union or internationally recognised open, human-readable and machine-readable format.	Link	E
  endpoint URL	Resource	1..*	The root location or primary endpoint of the service (an IRI).	The endpoint URL SHOULD be persistent. This means that publishers should do everything in their power to maintain the value stable and existing.	Link	E
  HVD category	Concept	1..*	The HVD category to which this Data Service belongs.			P
  licence	Licence Document	0..1	A licence under which the Data service is made available.	Article 3.3 specifies that the terms of use should be provided. According to the guidelines for legal Information in DCAT-AP HVD this is fullfilled by providing by preference a licence. As alternative rights can be used.	Link	E
  rights	Rights statement	0..*	A statement that specifies rights associated with the Distribution.	Article 3.3 specifies that the terms of use should be provided. According to the guidelines for legal Information in DCAT-AP HVD this is fullfilled by providing by preference a licence. As alternative rights can be used.		P
  serves dataset	Dataset	1..*	This property refers to a collection of data that this data service can distribute.	An API in the context of HVD is not a standalone resource. It is used to open up HVD datasets. Therefore each Data Service is at least tightly connected with a Dataset.	Link	E


  Distribution

  access service	Data Service	0..*	A data service that gives access to the distribution of the dataset		Link	A
  access URL	Resource	1..*	A URL that gives access to a Distribution of the Dataset.	The resource at the access URL contains information about how to get the Dataset. In accordance to the DCAT guidelines it is preferred to also set the downloadURL property if the URL is a reference to a downloadable resource.	Link	A
  applicable legislation	Legal Resource	1..*	The legislation that mandates the creation or management of the Distribution	For HVD the value must include the ELI http://data.europa.eu/eli/reg_impl/2023/138/oj.
  As multiple legislations may apply to the resource the maximum cardinality is not limited.		P
  licence	Licence Document	0..1	A licence under which the Distribution is made available.	Article 4.3 specifies that High-value datasets should be made available for reuse. According to the guidelines for legal Information in DCAT-AP HVD this is fullfilled by providing by preference a licence. As alternative rights can be used.	Link	E
  linked schemas	Standard	0..*	An established schema to which the described Distribution conforms.	The provided information should enable to the verification whether the detailed information requirements by the HVD is satisfied. For more usage suggestions see section on specific data requirements.	Link	A
  rights	Rights statement	0..*	A statement that specifies rights associated with the Distribution.	Article 4.3 specifies that High-value datasets should be made available for reuse. According to the guidelines for legal Information in DCAT-AP HVD this is fullfilled by providing by preference a licence. As alternative rights can be used.	Link	E
  -->

  <!--
  HVD Category	Concept	1..*	The HVD category to which this Dataset belongs.			P
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:descriptiveKeywords[*/mri:thesaurusName/*/cit:title/*/@xlink:href = $hvdCategoryThesaurusKey]">
    <xsl:for-each select="*/mri:keyword[*/text() != '']">

      <xsl:variable name="category"
                    as="xs:string?"
                    select="current()/*/text()"/>
      <xsl:variable name="hvdCategory"
                    select="$euHvdDataCategories/rdf:RDF/*[skos:prefLabel/normalize-space(.) = $category]"/>
      <xsl:if test="$hvdCategory">
        <dcatap:hvdCategory>
          <skos:Concept rdf:about="{$hvdCategory/@rdf:about}">
            <xsl:copy-of select="$hvdCategory/skos:prefLabel[@xml:lang = $languages/@iso2code]"/>
          </skos:Concept>
        </dcatap:hvdCategory>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>


  <!--
  applicable legislation	Legal Resource	1..*	The legislation that mandates the creation or management of the Data Service.	For HVD the value MUST include the ELI http://data.europa.eu/eli/reg_impl/2023/138/oj.
  As multiple legislations may apply to the resource the maximum cardinality is not limited.		P
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:descriptiveKeywords[*/mri:thesaurusName/*/cit:title/*/@xlink:href = $hvdApplicableLegislationThesaurusKey]">
    <xsl:for-each select="*/mri:keyword[*/text() != '']">

      <xsl:variable name="legislation"
                    as="xs:string?"
                    select="current()/*/text()"/>
      <xsl:variable name="applicableLegislation"
                    select="$euHvdApplicableLegislation/rdf:RDF/*[skos:prefLabel/normalize-space() = $legislation]"/>
      <xsl:if test="$applicableLegislation">
        <dcatap:applicableLegislation rdf:resource="{$applicableLegislation/@rdf:about}">
          <!--<eli:LegalResource>
            <xsl:for-each select="$applicableLegislation/skos:prefLabel[@xml:lang = $languages/@iso2code]">
              <dct:title xml:lang="{@xml:lang}">
                <xsl:value-of select="."/>
              </dct:title>
            </xsl:for-each>
            <xsl:for-each select="$applicableLegislation/skos:scopeNote[@xml:lang = $languages/@iso2code]">
              <dct:description xml:lang="{@xml:lang}">
                <xsl:value-of select="."/>
              </dct:description>
            </xsl:for-each>
          </eli:LegalResource>-->
        </dcatap:applicableLegislation>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:resourceConstraints/mco:MD_LegalConstraints/mco:reference">
    <xsl:variable name="href"
                  select="*/cit:title/*/@xlink:href"/>
    <xsl:if test="$href">
      <dcatap:applicableLegislation rdf:resource="{$href}">
        <!--<eli:LegalResource>
          <xsl:for-each select="*/cit:title">
            <xsl:call-template name="rdf-localised">
              <xsl:with-param name="nodeName" select="'dct:title'"/>
            </xsl:call-template>
          </xsl:for-each>
        </eli:LegalResource>-->
      </dcatap:applicableLegislation>
    </xsl:if>
  </xsl:template>


  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:distributionInfo//mrd:onLine">
    <xsl:call-template name="iso19115-3-to-dcat-distribution">
      <xsl:with-param name="additionalProperties">
        <xsl:if test="$isCopyingDatasetInfoToDistribution">
          <xsl:apply-templates mode="iso19115-3-to-dcat"
                               select="ancestor::mdb:MD_Metadata/mdb:identificationInfo/*/mri:resourceConstraints/mco:MD_LegalConstraints/mco:reference"/>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>
