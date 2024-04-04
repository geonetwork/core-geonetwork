<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:cnt="http://www.w3.org/2011/content#"
                xmlns:geodcatap="http://data.europa.eu/930/"
                exclude-result-prefixes="#all">
  <!-- http://data.europa.eu/930/ -->

  <xsl:import href="../eu-dcat-ap/eu-dcat-ap-core.xsl"/>
  <xsl:import href="eu-geodcat-ap-variables.xsl"/>

  <xsl:template name="create-namespaces-eu-geodcat-ap">
    <xsl:call-template name="create-namespaces"/>
    <xsl:namespace name="dcatap" select="'http://data.europa.eu/r5r/'"/>
    <xsl:namespace name="geodcatap" select="'http://data.europa.eu/930/'"/>
    <xsl:namespace name="cnt" select="'http://www.w3.org/2011/content#'"/>
    <xsl:namespace name="eli" select="'http://data.europa.eu/eli/ontology'"/>
  </xsl:template>

  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue">
    <xsl:call-template name="iso19115-3-to-dcat-metadataScope"/>

    <xsl:for-each select="$inspireResourceTypeVocabularyToIso[. = current()]/@key">
      <dct:type rdf:resource="{concat($inspireResourceTypeCodelistUri, current())}"/>
    </xsl:for-each>
  </xsl:template>


  <xsl:template mode="iso19115-3-to-dcat-catalog-record"
                name="iso19115-3-to-eu-geodcat-ap-catalog-record"
                match="mdb:MD_Metadata">
    <xsl:param name="additionalProperties"
               as="node()*"/>

    <xsl:variable name="properties" as="node()*">
      <xsl:apply-templates mode="iso19115-3-to-dcat"
                           select="mdb:contact"/>

      <xsl:copy-of select="$additionalProperties"/>

      <dct:conformsTo>
        <dct:Standard rdf:about="http://data.europa.eu/930/"/>
      </dct:conformsTo>
    </xsl:variable>
    <xsl:call-template name="iso19115-3-to-eu-dcat-ap-catalog-record">
      <xsl:with-param name="additionalProperties" select="$properties"/>
    </xsl:call-template>
  </xsl:template>


  <!--
  +contact point
  dcat:contactPoint
  vcard:Kind
  This property contains contact information that can be used for sending comments about the Catalogue Record.
  0..n

  +creator
  dct:creator
  foaf:Agent
  This property refers to the Agent primarily responsible for producing the Catalogue Record.
  0..n

  +publisher
  dct:publisher
  foaf:Agent
  This property refers to an Agent (organisation) responsible for making the Catalogue Record available.
  0..1

  +rights holder
  dct:rightsHolder
  foaf:Agent
  This property refers to an Agent (organisation) holding rights on the Catalogue Record.
  0..n


  From ISO role
  +custodian
  geodcat:custodian
  foaf:Agent
  Party that accepts accountability and responsibility for the data and ensures appropriate care and maintenance of the resource [ISO-19115].
  0..n

  +distributor
  geodcat:distributor
  foaf:Agent
  Party who distributes the resource [ISO-19115].
  0..n

  +originator
  geodcat:originator
  foaf:Agent
  Party who created the resource [ISO-19115].
  0..n

  +principal investigator
  geodcat:principalInvestigator
  foaf:Agent
  Key party responsible for gathering information and conducting research [ISO-19115].
  0..n

  +processor
  geodcat:processor
  foaf:Agent
  Party who has processed the data in a manner such that the resource has been modified [ISO-19115].
  0..n

  +resource provider
  geodcat:resourceProvider
  foaf:Agent
  Party that supplies the resource [ISO-19115].
  0..n

  +user
  geodcat:user
  foaf:Agent
  Party who uses the resource [ISO-19115].
  0..n

  Managed by overriding variable isoContactRoleToDcatCommonNames
  -->

  <!-- CatalogRecord / Optional properties
  +character encoding
  cnt:characterEncoding
  rdfs:Literal
  This property SHOULD be used to specify the character encoding of the Catalogue Record, by using as value the character set names in the IANA register [IANA-CHARSETS].
  0..n
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:MD_Metadata/mdb:defaultLocale/*/lan:characterEncoding/*/@codeListValue">
    <xsl:variable name="characterEncoding"
                  select="$ianaCharsetToIso[text() = current()]/@key"/>
    <xsl:if test="$characterEncoding">
      <cnt:characterEncoding rdf:datatype="http://www.w3.org/2001/XMLSchema#string">
        <xsl:value-of select="$characterEncoding"/>
      </cnt:characterEncoding>
    </xsl:if>
  </xsl:template>

  <!--
  +creation date
  dct:created
  rdfs:Literal typed as xsd:date or xsd:dateTime
  This property contains the date on which the Catalogue Record has been first created.
  0..1

  TODO: ? dct:issued is used in DCAT
  -->

  <!-- TODO: Dataset / ReferenceSystem
   https://github.com/SEMICeu/GeoDCAT-AP/issues/94
   -->

  <!-- TODO: Distribution
   +representation technique
  adms:representationTechnique
  skos:Concept
  https://github.com/SEMICeu/GeoDCAT-AP/issues/96

  This property MAY be used to provide more information about the format in which an Distribution is released. This is different from the file format as, for example, a ZIP file (file format) could contain an XML schema (representation technique).

  In GeoDCAT-AP, this property SHOULD be used to express the spatial representation type (grid, vector, tin), by using the URIs of the corresponding code list operated by the INSPIRE Registry [INSPIRE-SRT].
   -->



  <!-- TODO: https://github.com/SEMICeu/GeoDCAT-AP/issues/95
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:spatialResolution/*/mri:equivalentScale">
    <geodcatap:spatialResolutionAsText>
      ...
    </geodcatap:spatialResolutionAsText>
  </xsl:template>-->
</xsl:stylesheet>
