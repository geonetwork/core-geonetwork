<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:mdUtil="java:org.fao.geonet.api.records.MetadataUtils"
                xmlns:gn-fn-dcat="http://geonetwork-opensource.org/xsl/functions/dcat"
                xmlns:schema-org-fn="http://geonetwork-opensource.org/xsl/functions/schema-org"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">
    <!--
      Convert an ISO records in JSON-LD format


      This JSON-LD can be embedded in an HTML page using
      ```
      <html>
        <script type="application/ld+json">
         {json-ld}
        </script>
      </html>
      ```

      Based on https://schema.org/Dataset
      and https://docs.mlcommons.org/croissant/docs/croissant-spec.html


      Tested with
      https://search.google.com/structured-data/testing-tool
      https://huggingface.co/spaces/luisoala/croissant-checker



      TODO: Add support to translation https://bib.schema.org/workTranslation
    -->

    <!-- Used for json escape string -->
    <xsl:import href="common/index-utils.xsl"/>
    <xsl:import href="jsonld-utils.xsl"/>

    <xsl:import href="../dcat/dcat-utils.xsl"/>

    <xsl:include href="../citation/base.xsl"/>
    <xsl:include href="../citation/common.xsl"/>

    <xsl:variable name="configuration"
                  select="/empty"/>

    <!-- Convert a hierarchy level into corresponding
    schema.org class. If no match, return http://schema.org/Thing

    Prefix are usually 'http://schema.org/' or 'schema:'.
     -->
    <xsl:function name="schema-org-fn:getType" as="xs:string">
        <xsl:param name="type" as="xs:string"/>
        <xsl:param name="prefix" as="xs:string"/>

        <xsl:variable name="map" as="node()+">
            <entry key="dataset" value="Dataset"/>
            <entry key="series" value="Dataset"/>
            <entry key="service" value="WebAPI"/>
            <entry key="application" value="SoftwareApplication"/>
            <entry key="collectionHardware" value="Thing"/>
            <entry key="nonGeographicDataset" value="Dataset"/>
            <entry key="dimensionGroup" value="TechArticle"/>
            <entry key="featureType" value="Dataset"/>
            <entry key="attribute" value="CreativeWork"/>
            <entry key="attributeType" value="Thing"/>
            <entry key="feature" value="Thing"/>
            <entry key="propertyType" value="Thing"/>
            <entry key="fieldSession" value="Project"/>
            <entry key="software" value="SoftwareApplication"/>
            <entry key="model" value="TechArticle"/>
            <entry key="tile" value="Dataset"/>
            <entry key="metadata" value="ArchiveComponent"/>
            <entry key="initiative" value="Thing"/>
            <entry key="sample" value="ArchiveComponent"/>
            <entry key="document" value="DigitalDocument"/>
            <entry key="repository" value="ArchiveComponent"/>
            <entry key="aggregate" value="ArchiveComponent"/>
            <entry key="product" value="Product"/>
            <entry key="collection" value="ArchiveComponent"/>
            <entry key="coverage" value="CreativeWork"/>
            <entry key="collectionSession" value="Project"/>
        </xsl:variable>


        <xsl:variable name="match"
                      select="$map[@key = $type]/@value"/>

        <xsl:variable name="prefixedBy"
                      select="if ($prefix = '') then 'http://schema.org/' else $prefix"/>

        <xsl:value-of select="if ($match != '')
                          then concat($prefixedBy, $match)
                          else concat($prefixedBy, 'Thing')"/>
    </xsl:function>



    <xsl:function name="schema-org-fn:buildColumnId" as="xs:string">
        <xsl:param name="featureTypeId" as="xs:string"/>
        <xsl:param name="carrierOfCharacteristics" as="node()?"/>
        <xsl:param name="position" as="xs:integer?"/>

        <xsl:value-of select="concat(
          $featureTypeId, '/',
          util:escapeForJson(($carrierOfCharacteristics/gfc:code/gco:CharacterString/text(), string($position))[1]))"/>
    </xsl:function>


    <!-- Define the root element of the resources
        and a catalogue id. -->
    <xsl:param name="baseUrl"
               select="util:getSettingValue('nodeUrl')"/>
    <xsl:variable name="catalogueName"
                  select="util:getSettingValue('system/site/name')"/>

    <!-- Schema.org document can't really contain
    translated text. So we can produce the JSON-LD
    in one of the language defined in the metadata record.

    Add the lang parameter to the formatter URL `?lang=fr`
    to force a specific language. If translation not available,
    the default record language is used.
    -->
    <xsl:param name="lang"
               select="''"/>


  <xsl:template name="iso19115-3.2018toJsonLD" as="xs:string*">
    <xsl:param name="record" as="node()"/>
    <xsl:param name="requestedLanguage" as="xs:string"/>

    <xsl:variable name="defaultLanguage"
                  select="(($record/mdb:defaultLocale/*/lan:language/*/@codeListValue, 'eng')[. != ''])[1]"/>

    <xsl:variable name="requestedLanguageId"
                  select="concat('#', $record/mdb:otherLocale/*[lan:language/*/@codeListValue = $requestedLanguage]/@id)"/>

    <xsl:for-each select="$record">
      {
      "@context": {
        "@language": <xsl:value-of select="schema-org-fn:toJsonText(util:twoCharLangCode($defaultLanguage))"/>,
        "@vocab": "https://schema.org/",
        "schema": "https://schema.org/",
        "sc": "https://schema.org/",
        "cr": "http://mlcommons.org/croissant/",
        "rai": "http://mlcommons.org/croissant/RAI/",
        "dct": "http://purl.org/dc/terms/",
        "citeAs": "cr:citeAs",
        "column": "cr:column",
        "conformsTo": "dct:conformsTo",
        "data": {
          "@id": "cr:data",
          "@type": "@json"
        },
        "dataType": {
          "@id": "cr:dataType",
          "@type": "@vocab"
        },
        "examples": {
          "@id": "cr:examples",
          "@type": "@json"
        },
        "extract": "cr:extract",
        "field": "cr:field",
        "fileProperty": "cr:fileProperty",
        "fileObject": "cr:fileObject",
        "fileSet": "cr:fileSet",
        "format": "cr:format",
        "includes": "cr:includes",
        "isLiveDataset": "cr:isLiveDataset",
        "jsonPath": "cr:jsonPath",
        "key": "cr:key",
        "md5": "cr:md5",
        "parentField": "cr:parentField",
        "path": "cr:path",
        "recordSet": "cr:recordSet",
        "references": "cr:references",
        "regex": "cr:regex",
        "repeated": "cr:repeated",
        "replace": "cr:replace",
        "separator": "cr:separator",
        "source": "cr:source",
        "subField": "cr:subField"
      },
      <xsl:variable name="hierarchyLevels"
                    select="distinct-values(mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue[. != ''])"/>
      <xsl:for-each select="$hierarchyLevels">
        "@type": <xsl:value-of select="schema-org-fn:toJsonText(schema-org-fn:getType(., 'schema:'))"/>,
      </xsl:for-each>
      <xsl:if test="count($hierarchyLevels) = 0">
        "@type": "schema:Dataset",
      </xsl:if>

      <xsl:variable name="resourceUri"
                    select="gn-fn-dcat:getResourceUri(.)"/>

      "@id": <xsl:value-of select="schema-org-fn:toJsonText($resourceUri)"/>,

      <!--
      The URL of the dataset. This generally corresponds to the Web page for the dataset.
      -->
      "url": <xsl:value-of select="schema-org-fn:toJsonText($resourceUri)"/>,

      "includedInDataCatalog": [{
      "@type": "DataCatalog",
      "url": <xsl:value-of select="schema-org-fn:toJsonText($baseUrl)"/>,
      "name": <xsl:value-of select="schema-org-fn:toJsonText($catalogueName)"/>
      }],

      <!-- The language of the content  -->
      <xsl:variable name="resourceLanguages"
                    select="mdb:identificationInfo/*/(mri:defaultLocale|mri:otherLocale)/*/lan:language/*/@codeListValue"/>
      <xsl:if test="$resourceLanguages">
        "inLanguage": [
        <xsl:for-each select="$resourceLanguages">
          <xsl:value-of select="schema-org-fn:toJsonText(util:twoCharLangCode(current()))"/>
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>],
      </xsl:if>

      <!-- TODO: availableLanguage -->
      "name": <xsl:value-of select="schema-org-fn:toJsonLDLocalized(mdb:identificationInfo/*/mri:citation/*/cit:title, $requestedLanguage, $requestedLanguageId)"/>,

      <!-- An alias for the item. -->
      <xsl:variable name="alternateNames"
                    select="mdb:identificationInfo/*/mri:citation/*/cit:alternateTitle[*/text() != '']"/>
      <xsl:if test="$alternateNames">
        "alternateName": [
        <xsl:for-each select="$alternateNames">
          <xsl:value-of select="schema-org-fn:toJsonLDLocalized(., $requestedLanguage, $requestedLanguageId)"/>
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>
        ],
      </xsl:if>

      "description": <xsl:value-of select="schema-org-fn:toJsonLDLocalized(mdb:identificationInfo/*/mri:abstract, $requestedLanguage, $requestedLanguageId)"/>,

      <!--
      Croissant datasets must declare that they conform to the versioned schema: http://mlcommons.org/croissant/1.0

      Maybe this should be added only if a feature catalogue and a download distribution is available
      (in such case, a fileObject and recordSet will be created).
      -->
      "conformsTo": "http://mlcommons.org/croissant/1.0",

      <!--
      TODO: Whether the dataset is a live dataset.
      "isLiveDataset": false,
      -->

      <xsl:variable name="useConstraints"
                    as="node()*">
        <xsl:copy-of
          select="mdb:identificationInfo/*/mri:resourceConstraints/*[mco:useConstraints]/mco:otherConstraints"/>
        <xsl:copy-of
          select="mdb:identificationInfo/*/mri:resourceConstraints/*[mco:useConstraints]/mco:useLimitation"/>
      </xsl:variable>

      <xsl:variable name="licences"
                    select="$useConstraints"/>
      <xsl:if test="$licences">
        "license": [
        <xsl:for-each select="$licences">
          <xsl:choose>
            <xsl:when test="(gcx:Anchor/@xlink:href[starts-with(., 'http')]
                                                |gco:CharacterString[starts-with(., 'http')])">
              "<xsl:value-of select="util:escapeForJson(
                                                gcx:Anchor/@xlink:href[starts-with(., 'http')]
                                                |gco:CharacterString[starts-with(., 'http')])"/>"
            </xsl:when>
            <xsl:otherwise>
              {
              "@type": "CreativeWork",
              "name": <xsl:value-of select="schema-org-fn:toJsonLDLocalized(., $requestedLanguage, $requestedLanguageId)"/>
              }
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>
        ],
      </xsl:if>


      <!--
      "A citation to the dataset itself, or a citation for a publication that describes the dataset. Ideally, citations should be expressed using the bibtex format.
      -->
      <xsl:variable name="citationInfo" as="node()?">
        <xsl:for-each select="$record">
          <xsl:call-template name="get-iso19115-3.2018-citation">
            <xsl:with-param name="metadata" select="."/>
            <xsl:with-param name="language" select="$defaultLanguage"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:variable>

      <xsl:variable name="bibTexCitation">
        <xsl:for-each select="$citationInfo">
          <xsl:call-template name="citation-bibtex"/>
        </xsl:for-each>
      </xsl:variable>
      "citeAs": "<xsl:value-of select="util:escapeForJson($bibTexCitation)"/>",


      <xsl:variable name="dateTypes" as="node()*">
        <entry key="creation">dateCreated</entry>
        <entry key="revision">dateModified</entry>
        <entry key="publication">datePublished</entry>
      </xsl:variable>

      <xsl:for-each select="$dateTypes">
        <xsl:variable name="datesForType"
                      select="mdb:identificationInfo/*/mri:citation/*/cit:date[*/cit:dateType/*/@codeListValue = current()/@key]/*/cit:date/*/text()"/>

        <xsl:if test="$datesForType">
          <xsl:value-of select="schema-org-fn:toJsonText(current()/text())"/>: [
          <xsl:for-each select="$datesForType">
            <xsl:value-of select="schema-org-fn:toJsonText(.)"/>
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          ],
        </xsl:if>
      </xsl:for-each>


      <xsl:for-each select="mdb:identificationInfo/*/mri:citation/*/cit:edition/gco:CharacterString[. != '']">
        "version": <xsl:value-of select="schema-org-fn:toJsonText(.)"/>,
      </xsl:for-each>


      <!-- Build a flat list of all keywords even if grouped in thesaurus. -->
      "keywords":[
      <xsl:for-each select="mdb:identificationInfo/*/mri:descriptiveKeywords/*/mri:keyword">
        <xsl:value-of select="schema-org-fn:toJsonLDLocalized(., $requestedLanguage, $requestedLanguageId)"/>
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>
      ]


      <xsl:variable name="isoRoleToSchemaOrg" as="node()*">
        <entry key="resourceProvider">provider</entry>
        <entry key="custodian">producer</entry>
        <entry key="owner">copyrightHolder</entry>
        <entry key="user"></entry>
        <entry key="distributor">publisher</entry>
        <entry key="originator">producer</entry>
        <entry key="pointOfContact">provider</entry>
        <entry key="principalInvestigator">producer</entry>
        <entry key="processor">provider</entry>
        <entry key="publisher">publisher</entry>
        <entry key="author">author</entry>
        <entry key="coAuthor">author</entry>
        <entry key="collaborator">contributor</entry>
        <entry key="editor">editor</entry>
        <entry key="mediator"></entry>
        <entry key="rightsHolder">copyrightHolder</entry>
        <entry key="contributor">contributor</entry>
        <entry key="funder">funder</entry>
        <entry key="sponsor">funder</entry>
        <entry key="stakeholder">contributor</entry>
      </xsl:variable>


      <xsl:variable name="contactList"
                    select="mdb:identificationInfo/*/mri:pointOfContact/*/cit:party"/>

      <xsl:for-each-group select="$contactList" group-by="../cit:role/*/@codeListValue">
        <xsl:variable name="contactType"
                      as="node()?"
                      select="$isoRoleToSchemaOrg[@key = current-grouping-key()]"/>

        <xsl:if test="$contactType">
          ,<xsl:value-of select="schema-org-fn:toJsonText($contactType/text())"/>: [
          <xsl:for-each select="current-group()">
            <xsl:variable name="id"
                          select="(.//cit:electronicMailAddress/*/text())[1]"/>
            {
            "@id": <xsl:value-of select="schema-org-fn:toJsonText($id)"/>,
            "@type":"Organization"
            <xsl:for-each select=".//cit:CI_Organisation/cit:name">
              ,"name":  <xsl:value-of select="schema-org-fn:toJsonLDLocalized(., $requestedLanguage, $requestedLanguageId)"/>
            </xsl:for-each>
            <xsl:if test=".//cit:electronicMailAddress">
              ,"email": [
              <xsl:for-each select=".//cit:electronicMailAddress">
                <xsl:value-of select="schema-org-fn:toJsonLDLocalized(., $requestedLanguage, $requestedLanguageId)"/>
                <xsl:if test="position() != last()">,</xsl:if>
              </xsl:for-each>
              ]
            </xsl:if>

            <!-- TODO: only if children available -->
            ,"contactPoint": {
              "@type" : "PostalAddress"
              <xsl:for-each select="cit:contactInfo/*/cit:address/*/cit:country">
                ,"addressCountry":
                <xsl:value-of select="schema-org-fn:toJsonLDLocalized(., $requestedLanguage, $requestedLanguageId)"/>
              </xsl:for-each>
              <xsl:for-each select="cit:contactInfo/*/cit:address/*/cit:city">
                ,"addressLocality":
                <xsl:value-of select="schema-org-fn:toJsonLDLocalized(., $requestedLanguage, $requestedLanguageId)"/>
              </xsl:for-each>
              <xsl:for-each select="cit:contactInfo/*/cit:address/*/cit:postalCode">
                ,"postalCode":
                <xsl:value-of select="schema-org-fn:toJsonLDLocalized(., $requestedLanguage, $requestedLanguageId)"/>
              </xsl:for-each>
              <xsl:for-each select="cit:contactInfo/*/cit:address/*/cit:deliveryPoint">
                ,"streetAddress":
                <xsl:value-of select="schema-org-fn:toJsonLDLocalized(., $requestedLanguage, $requestedLanguageId)"/>
              </xsl:for-each>
              }
            }
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          ]
        </xsl:if>
      </xsl:for-each-group>


      <!--
      The overall rating, based on a collection of reviews or ratings, of the item.
      "aggregateRating": TODO
      -->

      <!--
      A downloadable form of this dataset, at a specific location, in a specific format.

      See https://schema.org/DataDownload


      By contrast with schema.org/Dataset, Croissant requires the distribution property to have values of type FileObject or FileSet.
      -->

      <xsl:variable name="fileObjects"
                    select="mdb:distributionInfo//mrd:onLine/*[cit:linkage/gco:CharacterString != ''
                                      and (cit:protocol/gcx:MimeFileType/@type != ''
                                      or starts-with(cit:protocol/*/text(), 'WWW:DOWNLOAD')
                                      or cit:protocol/*/text() = ('OGC:WFS', 'OGC:WCS', 'ESRI:REST')
                                      )]"/>

      <xsl:for-each select="mdb:distributionInfo">
        ,"distribution": [
        <xsl:for-each select=".//mrd:onLine/* except $fileObjects">
          {
          "@type":"DataDownload",
          "@id": <xsl:value-of select="schema-org-fn:toJsonText((cit:linkage/*/text())[1])"/>,
          "contentUrl": <xsl:value-of select="schema-org-fn:toJsonText((cit:linkage/*/text())[1])"/>
          <!--      File size in (mega/kilo/…)bytes. Defaults to bytes if a unit is not specified.
                    "contentSize"-->
          <xsl:if test="cit:protocol">,
            <xsl:variable name="protocol" select="normalize-space(cit:protocol/*/text())"/>
            "encodingFormat": <xsl:value-of
              select="schema-org-fn:toJsonText(
                                                    if (cit:protocol/*/@xlink:href != '')
                                                    then cit:protocol/*/@xlink:href
                                                    else $protocol)"/>
          </xsl:if>
          <xsl:if test="cit:name">,
            "name":
            <xsl:value-of select="schema-org-fn:toJsonLDLocalized(cit:name, $requestedLanguage, $requestedLanguageId)"/>
          </xsl:if>
          <xsl:if test="cit:description">,
            "description":
            <xsl:value-of select="schema-org-fn:toJsonLDLocalized(cit:description., $requestedLanguage, $requestedLanguageId)"/>
          </xsl:if>
          }
          <xsl:if test="position() != last() or count($fileObjects) > 0">,</xsl:if>
        </xsl:for-each>

        <xsl:for-each select="$fileObjects">
          <xsl:variable name="protocol" select="normalize-space(cit:protocol/*/text())"/>
          <xsl:variable name="mimeType" select="cit:protocol/gcx:MimeFileType/@type"/>
          <xsl:variable name="format"
                        select="if (exists($mimeType)) then $mimeType else substring-after($protocol, 'WWW:DOWNLOAD:')"/>
          {
          "@type": "cr:FileObject",
          "@id": <xsl:value-of select="schema-org-fn:toJsonText((cit:linkage/*/text())[1])"/>,
          "contentUrl": <xsl:value-of select="schema-org-fn:toJsonText((cit:linkage/*/text())[1])"/>
          <!--      File size in (mega/kilo/…)bytes. Defaults to bytes if a unit is not specified.
                    "contentSize"-->
          <xsl:if test="cit:protocol">,
            "encodingFormat": <xsl:value-of
              select="schema-org-fn:toJsonText(
                                                if (cit:protocol/*/@xlink:href != '')
                                                then cit:protocol/*/@xlink:href
                                                else if ($format != '')
                                                then $format
                                                else $protocol)"/>
          </xsl:if>
          <xsl:if test="cit:name">,
            "name":
            <xsl:value-of select="schema-org-fn:toJsonLDLocalized(cit:name, $requestedLanguage, $requestedLanguageId)"/>
          </xsl:if>
          <xsl:if test="cit:description">,
            "description":
            <xsl:value-of select="schema-org-fn:toJsonLDLocalized(cit:description, $requestedLanguage, $requestedLanguageId)"/>
          </xsl:if>
          <!-- At least one of these properties should be defined: ['md5', 'sha256']. -->
          }
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>
        ]
      </xsl:for-each>

      <xsl:if test="count(mdb:distributionInfo/*/mrd:distributionFormat) > 0">
        ,"encodingFormat": [
        <xsl:for-each select="mdb:distributionInfo/*/mrd:distributionFormat/*/mrd:formatSpecificationCitation/
              cit:CI_Citation/cit:title[gco:CharacterString != '']">
          <xsl:value-of select="schema-org-fn:toJsonLDLocalized(., $requestedLanguage, $requestedLanguageId)"/>
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>
        ]
      </xsl:if>



      <xsl:variable name="isoToCroissantDataType" as="node()*">
        <entry key="string">Text</entry>
        <entry key="int">Integer</entry>
        <entry key="number">Integer</entry>
        <entry key="float">Float</entry>
        <entry key="boolean">Boolean</entry>
        <entry key="date">Date</entry>
        <entry key="dateTime">DateTime</entry>
        <entry key="geometry">GeoCoordinates</entry>
        <entry key="image">ImageObject</entry>
        <entry key="url">URL</entry>
      </xsl:variable>

      <xsl:variable name="featureTypes"
                    select="mdb:contentInfo/*/mrc:featureCatalogue/*/gfc:featureType/*"/>

      <xsl:if test="$featureTypes">
        ,"recordSet": [
        <xsl:for-each select="$featureTypes">
          <xsl:variable name="featureTypeId"
                        select="if (gfc:typeName/text() != '') then util:escapeForJson(gfc:typeName/text()) else 'featureType'"/>

          <!-- In ISO there is no strong relation between an online resource
          and the data model. For now, referencing the first fileObject.-->
          <xsl:variable name="fileObjectId"
                        select="$fileObjects[1]/util:escapeForJson((cit:linkage/*/text())[1])"
                        as="xs:string?"/>
          {
          "@type": "cr:RecordSet",
          "@id": <xsl:value-of select="schema-org-fn:toJsonText($featureTypeId)"/>,
          <!--
          https://docs.mlcommons.org/croissant/docs/croissant-spec.html#typing-recordsets
          Not 100% sure how a geometry type should be defined. For now, is one column is of type geometry,
          type the recordSet as GeoCoordinates.
          -->
          <xsl:variable name="hasGeometry"
                        select="count(gfc:carrierOfCharacteristics/*[
                                            matches(gfc:valueType/*/gco:aName/*/text(),
                                            '(gml:)?(multi)?geom|point|line|polygon(propertytype)?', 'i')]) > 0"/>
          <xsl:if test="$hasGeometry">
            "dataType": "sc:GeoCoordinates",
          </xsl:if>


          <xsl:variable name="idColumn"
                        select="(gfc:carrierOfCharacteristics/*[gfc:cardinality/*/text() = ('1..1')])[1]"/>

          <xsl:if test="$idColumn">
            "key": [{ "@id": <xsl:value-of select="schema-org-fn:toJsonText(schema-org-fn:buildColumnId($featureTypeId, $idColumn, gfc:carrierOfCharacteristics/*[generate-id() = $idColumn/generate-id()]/count(../preceding-sibling::gfc:carrierOfCharacteristics) + 1))"/> }],
          </xsl:if>
          "field": [
          <xsl:for-each select="gfc:carrierOfCharacteristics/*">
            {
            "@type": "cr:Field",
            "@id": <xsl:value-of select="schema-org-fn:toJsonText(schema-org-fn:buildColumnId($featureTypeId, current(), position()))"/>,

            <xsl:variable name="valueType"
                          select="gfc:valueType/*/gco:aName/*/text()"/>
            "dataType": <xsl:value-of select="schema-org-fn:toJsonText(($isoToCroissantDataType[@key = $valueType],  $valueType)[1])"/>
            <xsl:if test="gfc:memberName">,
              "name": <xsl:value-of select="schema-org-fn:toJsonText(gfc:memberName/text())"/>
            </xsl:if>
            <xsl:if test="gfc:definition">,
              "description":
              <xsl:value-of select="schema-org-fn:toJsonLDLocalized(gfc:definition, $requestedLanguage, $requestedLanguageId)"/>
            </xsl:if>
            ,"source": {
            "fileObject": { "@id": <xsl:value-of select="schema-org-fn:toJsonText($fileObjectId)"/> }
            }
            }<xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          ]
          }<xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>
        ]
      </xsl:if>

      ,"spatialCoverage": [
      <xsl:for-each select="mdb:identificationInfo/*/mri:extent/*[gex:geographicElement]">
        {"@type":"Place",
        "description": [
        <xsl:for-each select="gex:description[count(.//text() != '') > 0]">
          <xsl:value-of select="schema-org-fn:toJsonLDLocalized(., $requestedLanguage, $requestedLanguageId)"/>
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>
        ],
        "geo": [
        <xsl:for-each select="gex:geographicElement/gex:EX_GeographicBoundingBox">
          {"@type":"GeoShape",
          "box": <xsl:value-of select="schema-org-fn:toJsonText(string-join((
                                              gex:southBoundLatitude/gco:Decimal,
                                              gex:westBoundLongitude/gco:Decimal,
                                              gex:northBoundLatitude/gco:Decimal,
                                              gex:eastBoundLongitude/gco:Decimal
                                              ), ' '))"/>
          }
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>
        ]
        }
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>
      ]

      <xsl:variable name="temporalCoverage"
                    select="mdb:identificationInfo/*/mri:extent/*/gex:temporalElement/*/gex:extent"/>
      <xsl:if test="$temporalCoverage">
        ,"temporalCoverage": [
        <xsl:for-each select="$temporalCoverage">
          <xsl:value-of select="schema-org-fn:toJsonText(concat(
                                                      gml:TimePeriod/gml:beginPosition,
                                                      '/',
                                                      gml:TimePeriod/gml:endPosition
          ))"/>
          <xsl:if test="position() != last()">,</xsl:if>
          <!-- TODO: handle
          "temporalCoverage" : "2013-12-19/.."
          "temporalCoverage" : "2008"
          -->
        </xsl:for-each>
        ]
      </xsl:if>

      <xsl:call-template name="related-record">
        <xsl:with-param name="uuid" select="mdb:metadataIdentifier/*/mcc:code/*/text()"/>
      </xsl:call-template>
      }
    </xsl:for-each>
  </xsl:template>

  <xsl:function name="schema-org-fn:toJsonLDLocalized" as="xs:string">
      <xsl:param name="node" as="node()?"/>
      <xsl:param name="requestedLanguage"/>
      <xsl:param name="requestedLanguageId"/>

      <xsl:choose>
          <!--
          This https://json-ld.org/spec/latest/json-ld/#string-internationalization
          should be supported in JSON-LD for multilingual content but does not
          seems to be supported yet by https://search.google.com/structured-data/testing-tool

          Error is not a valid type for property.

          So for now, JSON-LD format will only provide one language.
          The main one or the requested and if not found, the default.

          <xsl:when test="gmd:PT_FreeText">
            &lt;!&ndash; An array of object with all translations &ndash;&gt;
            <xsl:if test="$asArray">[</xsl:if>
            <xsl:for-each select="gmd:PT_FreeText/gmd:textGroup">
              <xsl:variable name="languageId"
                            select="gmd:LocalisedCharacterString/@locale"/>
              <xsl:variable name="languageCode"
                            select="$metadata/gmd:locale/*[concat('#', @id) = $languageId]/gmd:languageCode/*/@codeListValue"/>
              {
              <xsl:value-of select="concat('&quot;@value&quot;: &quot;',
                                  util:escapeForJson(gmd:LocalisedCharacterString/text()),
                                  '&quot;')"/>,
              <xsl:value-of select="concat('&quot;@language&quot;: &quot;',
                                  $languageCode,
                                  '&quot;')"/>
              }
              <xsl:if test="position() != last()">,</xsl:if>
            </xsl:for-each>
            <xsl:if test="$asArray">]</xsl:if>
            &lt;!&ndash;<xsl:if test="position() != last()">,</xsl:if>&ndash;&gt;
          </xsl:when>-->
          <xsl:when test="$requestedLanguage != ''">
              <xsl:variable name="requestedValue"
                            select="$node/lan:PT_FreeText/*/lan:LocalisedCharacterString[@id = $requestedLanguageId]/text()"/>
              <xsl:value-of select="concat('&quot;',
                            util:escapeForJson(
                              if ($requestedValue != '') then $requestedValue else ($node/(gco:CharacterString|gcx:Anchor))),
                            '&quot;')"/>
          </xsl:when>
          <xsl:otherwise>
              <!-- A simple property value -->
              <xsl:value-of select="concat('&quot;',
                            util:escapeForJson($node/(gco:CharacterString|gcx:Anchor)),
                            '&quot;')"/>
          </xsl:otherwise>
      </xsl:choose>
    </xsl:function>


    <xsl:template name="related-record">
      <xsl:param name="uuid" as="xs:string"/>

        <xsl:variable name="associations"
                      select="mdUtil:getAssociatedAsXml($uuid)"
                      as="node()?"/>

        <xsl:variable name="relationTypeToSchemaOrg" as="node()*">
            <entry key="parent">isPartOf</entry>
            <entry key="children">hasPart</entry>
            <entry key="brothersAndSisters"></entry>
            <entry key="sources">isBasedOn</entry>
            <entry key="siblings">mentions</entry>
            <entry key="datasets"></entry>
            <entry key="services"></entry>
        </xsl:variable>

        <xsl:for-each-group select="$associations/relations/*" group-by="local-name()">
            <xsl:variable name="relationType"
                          select="current-grouping-key()"/>

            <xsl:variable name="schemaOrgType"
                          select="$relationTypeToSchemaOrg [@key = $relationType]"/>

            <xsl:if test="$schemaOrgType/text() != ''">
                ,<xsl:value-of select="schema-org-fn:toJsonText($schemaOrgType/text())"/>: [
                <xsl:for-each select="../*[local-name() = $relationType]">
                    <xsl:sort select="@url"/>
                    <xsl:variable name="resourceIdentifierWithHttpCodeSpace"
                                  select="(root/resourceIdentifier[starts-with(codeSpace, 'http')])[1]"/>
                    <xsl:variable name="recordUri"
                                  select="if ($resourceIdentifierWithHttpCodeSpace)
                                       then concat($resourceIdentifierWithHttpCodeSpace/codeSpace, $resourceIdentifierWithHttpCodeSpace/code)
                                       else if (@url) then @url
                                        else concat($baseUrl, 'api/records/', @uuid)" />
                    {
                        "@type": "CreativeWork",
                        "url": <xsl:value-of select="schema-org-fn:toJsonText($recordUri)"/>,
                        "name": <xsl:value-of select="schema-org-fn:toJsonText(root/resourceTitleObject/default)"/>
                    }
                    <xsl:if test="position() != last()">,</xsl:if>
                </xsl:for-each>
                ]
            </xsl:if>
        </xsl:for-each-group>
    </xsl:template>
</xsl:stylesheet>
