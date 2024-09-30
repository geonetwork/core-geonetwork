<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:adms="http://www.w3.org/ns/adms#"
                xmlns:dcatap="http://data.europa.eu/r5r/"
                xmlns:dct="http://purl.org/dc/terms/"
                exclude-result-prefixes="#all">


  <xsl:variable name="euDataTheme"
                select="document('vocabularies/data-theme-skos.rdf')"/>

  <!--
  Theme mapping
  https://joinup.ec.europa.eu/collection/semantic-interoperability-community-semic/solution/dcat-application-profile-implementation-guidelines/discussion/di1-tools-dcat-ap
  https://github.com/SEMICeu/iso-19139-to-dcat-ap/blob/master/alignments/iso-topic-categories-to-inspire-themes.rdf
  -->
  <xsl:variable name="isoTopicToEuDcatApThemes"
                as="node()*">
    <entry key="http://publications.europa.eu/resource/authority/data-theme/AGRI">
      <inspire>http://inspire.ec.europa.eu/theme/af</inspire>
      <iso>farming</iso>
    </entry>
    <entry key="http://publications.europa.eu/resource/authority/data-theme/ECON">
      <inspire>http://inspire.ec.europa.eu/theme/cp</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/lu</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/mr</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/pf</inspire>
      <iso>economy</iso>
      <iso>planningCadastre</iso>
    </entry>
    <entry key="http://publications.europa.eu/resource/authority/data-theme/EDUC"></entry>
    <entry key="http://publications.europa.eu/resource/authority/data-theme/ENER">
      <inspire>http://inspire.ec.europa.eu/theme/er</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/mr</inspire>
    </entry>
    <entry key="http://publications.europa.eu/resource/authority/data-theme/ENVI">
      <inspire>http://inspire.ec.europa.eu/theme/hy</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/ps</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/lc</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/am</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/ac</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/br</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/ef</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/hb</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/lu</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/mr</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/nz</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/of</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/sr</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/so</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/sd</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/mf</inspire>
      <iso>biota</iso>
      <iso>environment</iso>
      <iso>inlandWaters</iso>
      <iso>oceans</iso>
      <iso>climatologyMeteorologyAtmosphere</iso>
    </entry>
    <entry key="http://publications.europa.eu/resource/authority/data-theme/GOVE">
      <inspire>http://inspire.ec.europa.eu/theme/au</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/us</inspire>
    </entry>
    <entry key="http://publications.europa.eu/resource/authority/data-theme/HEAL">
      <inspire>http://inspire.ec.europa.eu/theme/hh</inspire>
      <iso>health</iso>
    </entry>
    <entry key="http://publications.europa.eu/resource/authority/data-theme/INTR"></entry>
    <entry key="http://publications.europa.eu/resource/authority/data-theme/JUST"></entry>
    <entry key="http://publications.europa.eu/resource/authority/data-theme/OP_DATPRO"></entry>
    <entry key="http://publications.europa.eu/resource/authority/data-theme/REGI">
      <inspire>http://inspire.ec.europa.eu/theme/ad</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/rs</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/gg</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/cp</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/gn</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/el</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/ge</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/oi</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/bu</inspire>
      <iso>planningCadastre</iso>
      <iso>boundaries</iso>
      <iso>elevation</iso>
      <iso>imageryBaseMapsEarthCover</iso>
    </entry>
    <entry key="http://publications.europa.eu/resource/authority/data-theme/SOCI">
      <inspire>http://inspire.ec.europa.eu/theme/pd</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/su</inspire>
      <iso>location</iso>
      <iso>society</iso>
      <iso>disaster</iso>
      <iso>intelligenceMilitary</iso>
      <iso>extraTerrestrial</iso>
    </entry>
    <entry key="http://publications.europa.eu/resource/authority/data-theme/TECH">
      <inspire>http://inspire.ec.europa.eu/theme/hy</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/ge</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/oi</inspire>
      <inspire>http://inspire.ec.europa.eu/theme/mf</inspire>
      <iso>geoscientificInformation</iso>
    </entry>
    <entry key="http://publications.europa.eu/resource/authority/data-theme/TRAN">
      <inspire>http://inspire.ec.europa.eu/theme/tn</inspire>
      <iso>structure</iso>
      <iso>transportation</iso>
      <iso>utilitiesCommunication</iso>
    </entry>
  </xsl:variable>

  <!--
  Dataset

  ## DCAT
  Resource
  [o]	type	Concept	0..1	A type of the Dataset.	A recommended controlled vocabulary data-type is foreseen.	https://w3c.github.io/dxwg/dcat/#Property:resource_type

  [o]	title	Literal	1..*	A name given to the Dataset.	This property can be repeated for parallel language versions of the name.	Link
  [o]	description	Literal	1..*	A free-text account of the Dataset.	This property can be repeated for parallel language versions of the description.	Link
  [o]	identifier	Literal	0..*	The main identifier for the Dataset, e.g. the URI or other unique identifier in the context of the Catalogue.

  [o]	release date	Temporal Literal	0..1	The date of formal issuance (e.g., publication) of the Dataset.
  [o]	modification date	Temporal Literal	0..1	The most recent date on which the Dataset was changed or modified.

  [o]	version	Literal	0..*	The version indicator (name or identifier) of a resource.
  [o]	language	Linguistic system	0..*	A language of the Dataset.	This property can be repeated if there are multiple languages in the Dataset.	Link
  [o]	access rights	Rights statement	0..1	Information that indicates whether the Dataset is open data, has access restrictions or is not public.
  [o]	keyword	Literal	0..*	A keyword or tag describing the Dataset.

  [o]	contact point	Kind	0..*	Contact information that can be used for sending comments about the Dataset.
  [o]	creator	Agent	0..1	Ae entity responsible for producing the dataset.
  [o]	publisher	Agent	0..1	An entity (organisation) responsible for making the Dataset available.
  [o]	qualified attribution	Attribution	0..*	An Agent having some form of responsibility for the resource.

  [o]	has version	Dataset	0..*	A related Dataset that is a version, edition, or adaptation of the described Dataset.
  [o]	is referenced by	Resource	0..*	A related resource, such as a publication, that references, cites, or otherwise points to the dataset.
  [o]	is version of	Dataset	0..*	A related Dataset of which the described Dataset is a version, edition, or adaptation.
  [o]	qualified relation	Relationship	0..*	A description of a relationship with another resource.
  [o]	related resource	Resource	0..*	A related resource.
  [o]	conforms to	Standard	0..*	An implementing rule or other specification.
  [o]	provenance	Provenance Statement	0..*	A statement about the lineage of a Dataset.

  Dataset
  [o]	version notes	Literal	0..*	A description of the differences between this version and a previous version of the Dataset.	This property can be repeated for parallel language versions of the version notes.
  [o]	frequency	Frequency	0..1	The frequency at which the Dataset is updated.
  [o]	spatial resolution	xsd:decimal	0..*	The minimum spatial separation resolvable in a dataset, measured in meters.
  [o]	temporal resolution	xsd:duration	0..*	The minimum time period resolvable in the dataset.
  [o]	geographical coverage	Location	0..*	A geographic region that is covered by the Dataset.
  [o]	temporal coverage	Period of time	0..*	A temporal period that the Dataset covers.

  [o]	dataset distribution	Distribution	0..*	An available Distribution for the Dataset.
  [o]	landing page	Document	0..*	A web page that provides access to the Dataset, its Distributions and/or additional information.	It is intended to point to a landing page at the original data provider, not to a page on a site of a third party, such as an aggregator.	Link
  [o]	source	Dataset	0..*	A related Dataset from which the described Dataset is derived.


  ## TODO?
  [o]	documentation	Document	0..*	A page or document about this Dataset.
  [o]	sample	Distribution	0..*	A sample distribution of the dataset.
  [o]	was generated by	Activity	0..*	An activity that generated, or provides the business context for, the creation of the dataset.
  -->
  <xsl:template mode="iso19115-3-to-dcat-resource"
                name="iso19115-3-to-dcat-ap-resource"
                match="mdb:MD_Metadata"
                priority="2">
    <xsl:call-template name="iso19115-3-to-dcat-resource"/>

    <xsl:apply-templates mode="iso19115-3-to-eu-dcat-ap"
                         select="mdb:identificationInfo/*/mri:citation/*/cit:identifier
                                |mdb:resourceLineage/*/mrl:source"/>

    <xsl:call-template name="rdf-eu-dcat-ap-theme"/>
  </xsl:template>


  <!--
  [o]	other identifier	Identifier	0..*	A secondary identifier of the Dataset, such as MAST/ADS17, DataCite18, DOI19, EZID20 or W3ID21.
  -->
  <xsl:template mode="iso19115-3-to-eu-dcat-ap"
                match="cit:identifier">
    <xsl:variable name="code"
                  select="*/mcc:code/*/text()"/>
    <xsl:variable name="codeSpace"
                  select="*/mcc:codeSpace/*/text()"/>

    <adms:identifier>
      <rdf:Description>
        <rdf:type rdf:resource="http://www.w3.org/ns/adms#Identifier"/>
        <skos:notation>
          <xsl:value-of select="$code"/>
        </skos:notation>
        <xsl:if test="$codeSpace">
          <adms:schemaAgency>
            <xsl:value-of select="$codeSpace"/>
          </adms:schemaAgency>
        </xsl:if>
      </rdf:Description>
    </adms:identifier>
  </xsl:template>



  <!--
  applicable legislation	Legal Resource	0..*	The legislation that mandates the creation or management of the Dataset.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:descriptiveKeywords/*/mri:keyword[starts-with(*/@xlink:href, 'http://data.europa.eu/eli')]"
                priority="20">
    <dcatap:applicableLegislation rdf:resource="{*/@xlink:href}"/>
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


  <!--
  [o]	theme	Concept	0..*	A category of the Dataset.	A Dataset may be associated with multiple themes.	https://w3c.github.io/dxwg/dcat/#Property:resource_theme
  Vocabulary http://publications.europa.eu/resource/authority/data-theme
  -->
  <xsl:template name="rdf-eu-dcat-ap-theme">
    <xsl:variable name="values"
                  as="node()*"
                  select="mdb:identificationInfo/*/mri:topicCategory
                         |mdb:identificationInfo/*/mri:descriptiveKeywords/*/mri:keyword"/>

    <xsl:variable name="theme"
                  select="distinct-values($isoTopicToEuDcatApThemes[
                              */text() = $values/*/text()
                              or */text() = $values/*/@xlink:href]/@key)"/>
    <xsl:for-each select="$theme">
      <dcat:theme>
        <xsl:choose>
          <xsl:when test="$isExpandSkosConcept">
            <xsl:variable name="themeDescription"
                          select="$euDataTheme//skos:Concept[@rdf:about = current()]"/>
            <skos:Concept>
              <xsl:copy-of select="$themeDescription/@rdf:about"/>
              <xsl:copy-of select="$themeDescription/skos:prefLabel[@xml:lang = $languages/@iso2code]"
                           copy-namespaces="no"/>
            </skos:Concept>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="rdf:resource" select="current()"/>
          </xsl:otherwise>
        </xsl:choose>
      </dcat:theme>
    </xsl:for-each>
  </xsl:template>


  <xsl:template mode="iso19115-3-to-eu-dcat-ap"
                match="mrl:source">
    <dct:source>
      <xsl:call-template name="rdf-object-ref-attribute">
        <xsl:with-param name="isAbout" select="false()"/>
      </xsl:call-template>
    </dct:source>
  </xsl:template>

  <!--
   sh:resultMessage              "maxCount[1]: Invalid cardinality: expected max 1: Got count = 2" ;
   sh:resultPath                 dc:accrualPeriodicity ;
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:identificationInfo/*/mri:resourceMaintenance/*/mmi:maintenanceAndUpdateFrequency[position() > 1]"/>

  <!--
    Path=<http://www.w3.org/ns/dcat#spatialResolutionInMeters>
    Message: maxCount[1]: Invalid cardinality: expected max 1: Got count = 3
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:identificationInfo/*/mri:spatialResolution[count(preceding-sibling::mri:spatialResolution[*/mri:distance]) > 0]/*/mri:distance" priority="2"/>
</xsl:stylesheet>
