<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:reg="http://standards.iso.org/iso/19115/-3/reg/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:adms="http://www.w3.org/ns/adms#"
                xmlns:gn-fn-dcat="http://geonetwork-opensource.org/xsl/functions/dcat"
                xmlns:dct="http://purl.org/dc/terms/"
                exclude-result-prefixes="#all">

  <xsl:import href="dcat-commons.xsl"/>
  <xsl:import href="dcat-variables.xsl"/>
  <xsl:import href="dcat-utils.xsl"/>

  <xsl:import href="dcat-core-catalog.xsl"/>
  <xsl:import href="dcat-core-catalogrecord.xsl"/>
  <xsl:import href="dcat-core-resource.xsl"/>
  <xsl:import href="dcat-core-dataservice.xsl"/>
  <xsl:import href="dcat-core-dataset.xsl"/>
  <xsl:import href="dcat-core-contact.xsl"/>
  <xsl:import href="dcat-core-keywords.xsl"/>
  <xsl:import href="dcat-core-access-and-use.xsl"/>
  <xsl:import href="dcat-core-distribution.xsl"/>
  <xsl:import href="dcat-core-associated.xsl"/>
  <xsl:import href="dcat-core-lineage.xsl"/>

  <!-- Current record is an ISO metadata
  and can be an ISO19139 record before ISO19115-3 conversion. -->
  <xsl:variable name="metadata"
                as="node()"
                select="(/root/mdb:MD_Metadata|/mdb:MD_Metadata|/root/gmd:MD_Metadata|/gmd:MD_Metadata)"/>

  <!-- Extract languages from ISO19115.3-2018 mdb:MD_Metadata -->
  <xsl:template mode="get-language"
                match="mdb:MD_Metadata"
                as="node()*">
    <xsl:variable name="defaultLanguage"
                  select="$metadata/mdb:defaultLocale/*"/>
    <xsl:for-each select="$defaultLanguage">
      <xsl:variable name="iso3code"
                    as="xs:string?"
                    select="lan:language/*/@codeListValue"/>
      <language id="{@id}"
                iso3code="{$iso3code}"
                iso2code="{util:twoCharLangCode($iso3code)}"
                default=""/>
    </xsl:for-each>
    <xsl:for-each select="$metadata/mdb:otherLocale/*[not(@id = $defaultLanguage/@id)]">
      <language id="{@id}"
                iso3code="{lan:language/*/@codeListValue}"
                iso2code="{util:twoCharLangCode(lan:language/*/@codeListValue)}"/>
    </xsl:for-each>
  </xsl:template>

  <!-- Extract languages from ISO19139 gmd:MD_Metadata -->
  <xsl:template mode="get-language"
                match="gmd:MD_Metadata"
                as="node()*">
    <xsl:variable name="defaultLanguage"
                  select="$metadata/gmd:language"/>

    <xsl:for-each select="$defaultLanguage">
      <xsl:variable name="iso3code"
                    as="xs:string?"
                    select="gmd:LanguageCode/@codeListValue"/>
      <language id="{util:twoCharLangCode($iso3code)}"
                iso3code="{$iso3code}"
                iso2code="{util:twoCharLangCode($iso3code)}"
                default=""/>
    </xsl:for-each>
    <xsl:for-each select="$metadata/gmd:locale/*[not(@id = $defaultLanguage/@id)]">
      <language id="{util:twoCharLangCode(gmd:languageCode/*/@codeListValue)}"
                iso3code="{gmd:languageCode/*/@codeListValue}"
                iso2code="{util:twoCharLangCode(gmd:languageCode/*/@codeListValue)}"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:variable name="languages"
                as="node()*">
    <xsl:apply-templates mode="get-language"
                         select="$metadata"/>
  </xsl:variable>

  <xsl:variable name="resourcePrefix"
                select="concat(util:getSettingValue('nodeUrl'), 'api/records/')"
                as="xs:string"/>
  <!-- GeoNetwork historical DCAT export was using a setting
                  select="util:getSettingValue('metadata/resourceIdentifierPrefix')"/>-->

  <xsl:function name="gn-fn-dcat:getRecordUri" as="xs:string">
    <xsl:param name="metadata" as="node()"/>

    <xsl:variable name="metadataLinkage"
                  select="$metadata/mdb:metadataLinkage/*/cit:linkage/(gco:CharacterString|gcx:Anchor)/text()"
                  as="xs:string?"/>

    <xsl:variable name="metadataIdentifier"
                  as="node()?">
      <xsl:apply-templates mode="iso19115-3-to-dcat"
                           select="$metadata/mdb:metadataIdentifier"/>
    </xsl:variable>
    <!-- TODO: Should we consider DOI? It may be encoded in metadata linkage (not available in ISO19139) -->

    <xsl:value-of select="if($metadataLinkage) then $metadataLinkage
                            else if (string($metadataIdentifier) and starts-with($metadataIdentifier, 'http')) then $metadataIdentifier
                            else if (string($metadataIdentifier)) then concat($resourcePrefix, encode-for-uri($metadataIdentifier))
                            else concat($resourcePrefix, encode-for-uri($metadata/mdb:metadataIdentifier/*/mcc:code/*/text()))"
                 />
  </xsl:function>

  <xsl:function name="gn-fn-dcat:getResourceUri" as="xs:string">
    <xsl:param name="metadata" as="node()"/>

    <xsl:variable name="catalogRecordUri"
                        select="gn-fn-dcat:getRecordUri($metadata)"
                        as="xs:string"/>

    <xsl:variable name="resourceIdentifier"
                  as="node()?">
      <xsl:for-each select="($metadata/mdb:identificationInfo/*/mri:citation/*/cit:identifier[starts-with(*/mcc:codeSpace/*/text(), 'http')])[1]">
        <xsl:call-template name="iso19115-3-to-dcat-identifier"/>
      </xsl:for-each>
    </xsl:variable>

    <xsl:value-of select="if(string($resourceIdentifier) and starts-with($resourceIdentifier, 'http')) then $resourceIdentifier
                                      else concat($catalogRecordUri, '#resource')"
    />
  </xsl:function>


  <!-- Create resource -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:MD_Metadata">
    <rdf:Description rdf:about="{gn-fn-dcat:getResourceUri(.)}">
      <xsl:apply-templates mode="iso19115-3-to-dcat"
                           select="mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue"/>

      <xsl:apply-templates mode="iso19115-3-to-dcat-catalog-record"
                           select="."/>

      <xsl:apply-templates mode="iso19115-3-to-dcat-resource"
                           select="."/>

      <!-- Dataset, DatasetSeries
       Unsupported:
       * prov:wasGeneratedBy (Could be associated resource of type project?)
       -->
      <xsl:apply-templates mode="iso19115-3-to-dcat"
                           select="mdb:identificationInfo/*/mri:resourceMaintenance
                                  |mdb:identificationInfo/*/mri:spatialResolution/*/mri:distance
                                  |mdb:identificationInfo/*/mri:temporalResolution/*
                                  |mdb:identificationInfo/*/mri:extent/*/gex:geographicElement/gex:EX_GeographicBoundingBox
                                  |mdb:identificationInfo/*/mri:extent/*/gex:geographicElement/gex:EX_GeographicDescription
                                  |mdb:identificationInfo/*/mri:extent/*/gex:temporalElement/*/gex:extent
                                  |mdb:distributionInfo//mrd:onLine
                                  |.//mpc:portrayalCatalogueCitation/*/cit:onlineResource
                                  |.//mrl:additionalDocumentation//cit:onlineResource
                                  |.//mdq:reportReference//cit:onlineResource
                                  |.//mdq:reportReference/*/cit:title[gcx:Anchor/@xlink:href]
                                  |.//mdq:specification//cit:onlineResource
                                  |.//mdq:specification/*/cit:title[gcx:Anchor/@xlink:href]
                                  |.//mrc:featureCatalogueCitation//cit:onlineResource
                                  |mdb:identificationInfo/*/mri:graphicOverview
                           "/>

      <!-- DataService -->
      <xsl:apply-templates mode="iso19115-3-to-dcat"
                           select="mdb:identificationInfo/*/srv:containsOperations/*/srv:connectPoint/*/cit:linkage
                                  |mdb:identificationInfo/*/srv:operatesOn
                           "/>
    </rdf:Description>
  </xsl:template>


  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:identificationInfo/*/mri:citation/*/cit:title
                      |mdb:identificationInfo/*/mri:citation/*/cit:edition
                      |mdb:identificationInfo/*/mri:abstract
                      |mdb:metadataStandard/*/cit:title
                      |mdb:metadataStandard/*/cit:edition
                      |mdb:resourceLineage/*/mrl:statement
                      |mrd:onLine/*/cit:name
                      |mrd:onLine/*/cit:description
                      |cit:onlineResource/*/cit:name
                      |cit:onlineResource/*/cit:description
                      |mri:graphicOverview/*/mcc:fileDescription
                      ">
    <xsl:variable name="xpath"
                  as="xs:string"
                  select="string-join(current()/ancestor-or-self::*[name() != 'root']/name(), '/')"/>
    <xsl:variable name="dcatElementName"
                  as="node()?"
                  select="$isoToDcatCommonNames[. = $xpath]"/>

    <xsl:choose>
      <xsl:when test="$dcatElementName and $dcatElementName/@isMultilingual = 'false'">
        <xsl:call-template name="rdf-not-localised">
          <xsl:with-param name="nodeName" select="$dcatElementName/@key"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dcatElementName">
        <xsl:call-template name="rdf-localised">
          <xsl:with-param name="nodeName" select="$dcatElementName/@key"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message>WARNING: Unmatched XPath <xsl:value-of select="$xpath"/>. Check dcat-variables.xsl and add the element to isoToDcatCommonNames.</xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:dateInfo/*/cit:date
                      |cit:CI_Citation/cit:date/*/cit:date
                      |mrd:distributionOrderProcess/*/mrd:plannedAvailableDateTime">
    <xsl:param name="dateType"
                  as="xs:string?"
                  select="../cit:dateType/*/@codeListValue"/>
    <xsl:variable name="dcatElementName"
                  as="xs:string?"
                  select="$isoDateTypeToDcatCommonNames[. = $dateType]/@key"/>

    <xsl:choose>
      <xsl:when test="string($dcatElementName)">
        <xsl:call-template name="rdf-date">
          <xsl:with-param name="nodeName" select="$dcatElementName"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message>WARNING: Unmatched date type <xsl:value-of select="$dateType"/>. If needed, add this type in dcat-variables.xsl and add the element to map to in isoDateTypeToDcatCommonNames.</xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!--
  RDF Property:	dcterms:identifier
  Definition:	A unique identifier of the resource being described or cataloged.
  Range:	rdfs:Literal
  Usage note:	The identifier might be used as part of the IRI of the resource, but still having it represented explicitly is useful.
  Usage note:	The identifier is a text string which is assigned to the resource to provide an unambiguous reference within a particular context.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                name="iso19115-3-to-dcat-identifier"
                match="mdb:metadataIdentifier
                      |mdb:identificationInfo/*/mri:citation/*/cit:identifier
                      |cit:identifier">
    <xsl:variable name="code"
                  select="*/mcc:code/*/text()"/>
    <xsl:variable name="codeAnchor"
                  select="*/mcc:code/*/@xlink:href"/>
    <xsl:variable name="codeSpace"
                  select="*/mcc:codeSpace/*/text()"/>
    <xsl:variable name="isUrn"
                  as="xs:boolean"
                  select="starts-with($codeSpace, 'urn:')"/>
    <xsl:variable name="separator"
                  as="xs:string"
                  select="if ($isUrn) then ':' else '/'"/>

    <xsl:variable name="codeWithPrefix"
                  select="if (string($codeSpace))
                          then concat($codeSpace,
                                      (if (ends-with($codeSpace, $separator)) then '' else $separator),
                                      $code)
                          else if ($codeAnchor) then $codeAnchor
                          else $code"/>

    <xsl:variable name="codeType"
                  select="if (matches($codeWithPrefix, '^https?://|urn:'))
                          then 'anyURI' else 'string'"/>
    <dct:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#{$codeType}">
      <xsl:value-of select="$codeWithPrefix"/>
    </dct:identifier>
  </xsl:template>


  <!--
  RDF Property:	dcterms:conformsTo
  Definition:	An established standard to which the described resource conforms.
  Range:	dcterms:Standard (A basis for comparison; a reference point against which other things can be evaluated.)
  Usage note:	This property SHOULD be used to indicate the model, schema, ontology, view or profile that the catalog record metadata conforms to.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:metadataStandard">
    <dct:conformsTo>
      <dct:Standard>
        <rdf:type rdf:resource="http://purl.org/dc/terms/Standard"/>
        <xsl:apply-templates mode="iso19115-3-to-dcat"
                             select="*/cit:title"/>
        <xsl:apply-templates mode="iso19115-3-to-dcat"
                             select="*/cit:edition"/>
      </dct:Standard>
    </dct:conformsTo>
  </xsl:template>


  <!--
  Definition:	A language of the resource. This refers to the natural language used for textual metadata (i.e., titles, descriptions, etc.) of a cataloged resource (i.e., dataset or service) or the textual values of a dataset distribution

  Range:
  dcterms:LinguisticSystem
  Resources defined by the Library of Congress (ISO 639-1, ISO 639-2) SHOULD be used.

  If a ISO 639-1 (two-letter) code is defined for language, then its corresponding IRI SHOULD be used; if no ISO 639-1 code is defined, then IRI corresponding to the ISO 639-2 (three-letter) code SHOULD be used.

  Usage note:	Repeat this property if the resource is available in multiple languages.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:defaultLocale
                      |mri:otherLocale
                      |mdb:defaultLocale
                      |mdb:otherLocale">
    <xsl:variable name="languageValue"
                  as="xs:string?"
                  select="if ($isoToDcatLanguage[@key = current()/*/lan:language/*/@codeListValue])
                          then $isoToDcatLanguage[@key = current()/*/lan:language/*/@codeListValue]
                          else */lan:language/*/@codeListValue"/>

    <dct:language>
      <!-- TO CHECK: In DCAT, maybe we should use another base URI? -->
      <dct:LinguisticSystem rdf:about="{concat($europaPublicationLanguage, upper-case($languageValue))}"/>
    </dct:language>
  </xsl:template>

  <!--
  RDF Property:	dcterms:conformsTo
  Definition:	An established standard to which the described resource conforms.
  Range: dcterms:Standard ("A basis for comparison; a reference point against which other things can be evaluated." [DCTERMS])
  Usage note:	This property SHOULD be used to indicate the model, schema, ontology, view or profile that the cataloged resource content conforms to.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:dataQualityInfo/*/mdq:report/*/mdq:result[mdq:DQ_ConformanceResult and mdq:DQ_ConformanceResult/mdq:pass/*/text() = 'true']">
    <dct:conformsTo>
      <dct:Standard>
        <xsl:choose>
          <xsl:when test="*/mdq:specification/*/cit:title/gcx:Anchor/@xlink:href">
            <xsl:attribute name="rdf:about" select="*/mdq:specification/*/cit:title/gcx:Anchor/@xlink:href"/>
          </xsl:when>
          <xsl:when test="*/mdq:specification/@xlink:href">
            <xsl:attribute name="rdf:about" select="*/mdq:specification/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <rdf:type rdf:resource="http://purl.org/dc/terms/Standard"/>
            <xsl:for-each select="*/mdq:specification/*">
              <xsl:for-each select="cit:title">
                <xsl:call-template name="rdf-localised">
                  <xsl:with-param name="nodeName" select="'rdfs:label'"/>
                </xsl:call-template>
              </xsl:for-each>
              <xsl:apply-templates mode="iso19115-3-to-dcat"
                                   select="cit:date/*[cit:dateType/*/@codeListValue = $isoDateTypeToDcatCommonNames/text()]/cit:date"/>
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>
      </dct:Standard>
    </dct:conformsTo>
  </xsl:template>


  <!--
  Definition:	The nature or genre of the resource.
  Sub-property of:	dc:type
  Range:	rdfs:Class
  Usage note:	The value SHOULD be taken from a well governed and broadly recognised controlled vocabulary, such as:
  DCMI Type vocabulary [DCTERMS]
  [ISO-19115-1] scope codes
  Datacite resource types [DataCite]
  PARSE.Insight content-types used by re3data.org [RE3DATA-SCHEMA] (see item 15 contentType)
  MARC intellectual resource types
  Some members of these controlled vocabularies are not strictly suitable for datasets or data services (e.g., DCMI Type Event, PhysicalObject; [ISO-19115-1] CollectionHardware, CollectionSession, Initiative, Sample, Repository), but might be used in the context of other kinds of catalogs defined in DCAT profiles or applications.
  -->
  <xsl:template name="iso19115-3-to-dcat-metadataScope"
                mode="iso19115-3-to-dcat"
                match="mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue">
    <xsl:variable name="dcmiType"
                  select="$dcmiTypeVocabularyToIso[. = current()]/@key"
                  as="xs:string?"/>
    <xsl:variable name="dcatType"
                  select="$dcatResourceTypeToIso[. = current()]/@key"
                  as="xs:string?"/>

    <xsl:choose>
      <xsl:when test="$dcatType">
        <rdf:type rdf:resource="http://www.w3.org/ns/dcat#{$dcatType}"/>
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

    <!--
    SHACL rule
    Value must be an instance of skos:Concept
    Location:
    [Focus node] - [https://xyz/geonetwork/srv/api/records/7fe2f305] -
    [Result path] - [http://purl.org/dc/terms/type]
    Test:
    [Value] - [http://purl.org/dc/dcmitype/Dataset]
    -->
    <xsl:if test="$dcmiType">
      <dct:type>
        <skos:Concept rdf:about="http://purl.org/dc/dcmitype/{$dcmiType}">
          <skos:prefLabel><xsl:value-of select="$dcmiType"/></skos:prefLabel>
        </skos:Concept>
      </dct:type>
    </xsl:if>
    <xsl:if test="$isPreservingIsoType and current() != ''">
      <dct:type>
        <skos:Concept rdf:about="{concat($isoCodeListBaseUri, current())}">
          <skos:prefLabel><xsl:value-of select="current()"/></skos:prefLabel>
        </skos:Concept>
      </dct:type>
    </xsl:if>
    <!-- TODO: Add mapping to Datacite https://schema.datacite.org/meta/kernel-4.1/include/datacite-resourceType-v4.1.xsd ?-->
  </xsl:template>


  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:referenceSystemInfo/*/mrs:referenceSystemIdentifier/*"/>
</xsl:stylesheet>
