<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/2.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:mic="http://standards.iso.org/iso/19115/-3/mic/1.0"
                xmlns:mil="http://standards.iso.org/iso/19115/-3/mil/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mai="http://standards.iso.org/iso/19115/-3/mai/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xpath-default-namespace="http://www.isotc211.org/2005/gmd"
                xmlns:gn-fn-index="http://geonetwork-opensource.org/xsl/functions/index"
                xmlns:schema-org-fn="http://geonetwork-opensource.org/xsl/functions/schema-org"
                xmlns:gn="http://www.fao.org/geonetwork"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">
<!--
  Convert an ISO19139 records in JSON-LD format


  This JSON-LD can be embeded in an HTML page using
  ```
  <html>
    <script type="application/ld+json">
     {json-ld}
    </script>
  </html>
  ```


   Based on https://schema.org/Dataset


   Tested with https://search.google.com/structured-data/testing-tool

   TODO: Add support to translation https://bib.schema.org/workTranslation
   -->

  <!-- Used for json escape string -->
  <xsl:import href="common/index-utils.xsl"/>


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


  <!-- Define the root element of the resources
      and a catalogue id. -->
  <!--<xsl:param name="baseUrl"
             select="'https://data.geocatalogue.fr/id/'"/>
     <xsl:variable name="catalogueName"
             select="'/geocatalogue'"/>
  -->
  <xsl:param name="baseUrl"
             select="util:getSettingValue('nodeUrl')"/>
  <xsl:variable name="catalogueName"
                select="''"/>

  <!-- Schema.org document can't really contain
  translated text. So we can produce the JSON-LD
  in one of the language defined in the metadata record.

  Add the lang parameter to the formatter URL `?lang=fr`
  to force a specific language. If translation not available,
  the default record language is used.
  -->
  <xsl:param name="lang"
             select="''"/>

  <xsl:variable name="defaultLanguage"
                select="//mdb:MD_Metadata/mdb:defaultLocale/*/lan:language/*/@codeListValue"/>

  <!-- TODO: Convert language code eng > en_US ? -->

  <xsl:variable name="requestedLanguageExist"
                select="$lang != ''
                        and count(//mdb:MD_Metadata/mdb:otherLocale/*[lan:language/*/@codeListValue = $lang]/@id) > 0"/>

  <xsl:variable name="requestedLanguage"
                select="if ($requestedLanguageExist)
                        then $lang
                        else $defaultLanguage"/>

  <xsl:variable name="requestedLanguageId"
                select="concat('#', //mdb:MD_Metadata/mdb:otherLocale/*[lan:language/*/@codeListValue = $requestedLanguage]/@id)"/>



  <xsl:template name="getJsonLD"
                mode="getJsonLD" match="mdb:MD_Metadata">
  {
    "@context": "http://schema.org/",
    <xsl:variable name="hierarchyLevels"
                  select="distinct-values(mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue[. != ''])"/>
    <xsl:for-each select="$hierarchyLevels">
      "@type": "<xsl:value-of select="schema-org-fn:getType(., 'schema:')"/>",
    </xsl:for-each>
    <xsl:if test="count($hierarchyLevels) = 0">
      "@type": "schema:Dataset",
    </xsl:if>
    <!-- TODO: Use the identifier property to attach any relevant Digital Object identifiers (DOIs). -->
    "@id": "<xsl:value-of select="concat($baseUrl, 'api/records/', mdb:metadataIdentifier[1]/*/mcc:code/*/text())"/>",
    "includedInDataCatalog":[{"@type":"DataCatalog","url":"<xsl:value-of select="concat($baseUrl, 'search#', $catalogueName)"/>","name":"<xsl:value-of select="$catalogueName"/>"}],
    <!-- TODO: is the dataset language or the metadata language ? -->
    "inLanguage":"<xsl:value-of select="if ($requestedLanguage  != '') then $requestedLanguage else $defaultLanguage"/>",
    <!-- TODO: availableLanguage -->
    "name": <xsl:apply-templates mode="toJsonLDLocalized"
                                 select="mdb:identificationInfo/*/mri:citation/*/cit:title"/>,

    <!-- An alias for the item. -->
    "alternateName": [
    <xsl:for-each select="mdb:identificationInfo/*/mri:citation/*/cit:alternateTitle">
       <xsl:apply-templates mode="toJsonLDLocalized" select="."/><xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>],
    "dateCreated": [
    <xsl:for-each select="mdb:identificationInfo/*/mri:citation/*/cit:date[*/cit:dateType/*/@codeListValue='creation']/*/cit:date/*/text()">
       "<xsl:value-of select="."/>"<xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>],
    "dateModified": [
    <xsl:for-each select="mdb:identificationInfo/*/mri:citation/*/cit:date[*/cit:dateType/*/@codeListValue='revision']/*/cit:date/*/text()">
    "<xsl:value-of select="."/>"<xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>],
    "datePublished": [
    <xsl:for-each select="mdb:identificationInfo/*/mri:citation/*/cit:date[*/cit:dateType/*/@codeListValue='publication']/*/cit:date/*/text()">
      "<xsl:value-of select="."/>"<xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>],
    "thumbnailUrl": [
    <xsl:for-each select="mdb:identificationInfo/*/mri:graphicOverview/*/mcc:fileName/*[. != '']">
    "<xsl:value-of select="."/>"<xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>],
    "description": <xsl:apply-templates mode="toJsonLDLocalized" select="mdb:identificationInfo/*/mri:abstract"/>,

    <!-- TODO: Add citation as defined in DOI landing pages -->
    <!-- TODO: Add identifier, DOI if available or URL or text -->

    <xsl:for-each select="mdb:identificationInfo/*/mri:citation/*/cit:edition/gco:CharacterString[. != '']">
      "version": "<xsl:value-of select="."/>",
    </xsl:for-each>


    <!-- Build a flat list of all keywords even if grouped in thesaurus. -->
    "keywords":[
      <xsl:for-each select="mdb:identificationInfo/*/mri:descriptiveKeywords/*/mri:keyword">
        <xsl:apply-templates mode="toJsonLDLocalized"
                             select=".">
          <xsl:with-param name="asArray" select="false()"/>
        </xsl:apply-templates>
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>
    ],


    <!--
    TODO: Dispatch in author, contributor, copyrightHolder, editor, funder,
    producer, provider, sponsor
    TODO: sourceOrganization
      <xsl:variable name="role" select="*/gmd:role/gmd:CI_RoleCode/@codeListValue" />
      <xsl:choose>
        <xsl:when test="$role='resourceProvider'">provider</xsl:when>
        <xsl:when test="$role='custodian'">provider</xsl:when>
        <xsl:when test="$role='owner'">copyrightHolder</xsl:when>
        <xsl:when test="$role='user'">user</xsl:when>
        <xsl:when test="$role='distributor'">publisher</xsl:when>
        <xsl:when test="$role='originator'">sourceOrganization</xsl:when>
        <xsl:when test="$role='pointOfContact'">provider</xsl:when>
        <xsl:when test="$role='principalInvestigator'">producer</xsl:when>
        <xsl:when test="$role='processor'">provider</xsl:when>
        <xsl:when test="$role='publisher'">publisher</xsl:when>
        <xsl:when test="$role='author'">author</xsl:when>
        <xsl:otherwise>provider</xsl:otherwise>
      </xsl:choose>

    -->
    "publisher": [
      <xsl:for-each select="mdb:identificationInfo/*/mri:pointOfContact/*/cit:party">
        {
        <!-- TODO: Id could also be website if set -->
        <xsl:variable name="id"
                      select=".//cit:electronicMailAddress/*/text()[1]"/>
        "@id":"<xsl:value-of select="$id"/>",
        "@type":"Organization"
        <xsl:for-each select=".//cit:CI_Organisation/cit:name">
          ,"name": <xsl:apply-templates mode="toJsonLDLocalized"
                                       select="."/>
        </xsl:for-each>
        <xsl:if test=".//cit:electronicMailAddress">
            ,"email":  [<xsl:for-each select=".//cit:electronicMailAddress">
            <xsl:apply-templates mode="toJsonLDLocalized" select="."/>
            <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>]
        </xsl:if>

        <!-- TODO: only if children available -->
        ,"contactPoint": {
          "@type" : "PostalAddress"
          <xsl:for-each select="cit:contactInfo/*/cit:address/*/cit:country">
            ,"addressCountry": <xsl:apply-templates mode="toJsonLDLocalized"
                                                   select="."/>
          </xsl:for-each>
          <xsl:for-each select="cit:contactInfo/*/cit:address/*/cit:city">
            ,"addressLocality": <xsl:apply-templates mode="toJsonLDLocalized"
                                                   select="."/>
          </xsl:for-each>
          <xsl:for-each select="cit:contactInfo/*/cit:address/*/cit:postalCode">
            ,"postalCode": <xsl:apply-templates mode="toJsonLDLocalized"
                                                   select="."/>
          </xsl:for-each>
          <xsl:for-each select="cit:contactInfo/*/cit:address/*/cit:deliveryPoint">
            ,"streetAddress": <xsl:apply-templates mode="toJsonLDLocalized"
                                                   select="."/>
          </xsl:for-each>
          }
        }
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>
    ]

    <!--
    The overall rating, based on a collection of reviews or ratings, of the item.
    "aggregateRating": TODO
    -->

    <!--
    A downloadable form of this dataset, at a specific location, in a specific format.

    See https://schema.org/DataDownload
    -->
    <xsl:for-each select="mdb:distributionInfo">
    ,"distribution": [
      <xsl:for-each select=".//mrd:onLine/*[cit:linkage/gco:CharacterString != '']">
        <xsl:variable name="p" select="normalize-space(cit:protocol/*/text())"/>
        {
        "@type":"DataDownload",
        "contentUrl": "<xsl:value-of select="gn-fn-index:json-escape((cit:linkage/*/text())[1])" />"
        <xsl:if test="cit:protocol">,
          "encodingFormat": "<xsl:value-of select="gn-fn-index:json-escape(if ($p != '') then $p else cit:protocol/*/@xlink:href)"/>"
        </xsl:if>
        <xsl:if test="cit:name">,
          "name": <xsl:apply-templates mode="toJsonLDLocalized" select="cit:name"/>
        </xsl:if>
        <xsl:if test="cit:description">,
          "description": <xsl:apply-templates mode="toJsonLDLocalized" select="cit:description"/>
        </xsl:if>
        }
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>
    ]
    </xsl:for-each>

    <xsl:if test="count(mdb:distributionInfo/*/mrd:distributionFormat) > 0">
      ,"encodingFormat": [
      <xsl:for-each select="mdb:distributionInfo/*/mrd:distributionFormat/*/mrd:formatSpecificationCitation/
              cit:CI_Citation/cit:title[gco:CharacterString != '']">
        <xsl:apply-templates mode="toJsonLDLocalized"
                             select="."/>
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>
      ]
    </xsl:if>


    ,"spatialCoverage": [
    <xsl:for-each select="mdb:identificationInfo/*/mri:extent/*[gex:geographicElement]">
      {"@type":"Place",
        "description": [
        <xsl:for-each select="gex:description[count(.//text() != '') > 0]">
          <xsl:apply-templates mode="toJsonLDLocalized" select="."/>
          <xsl:if test="position() != last()">,</xsl:if></xsl:for-each>
          ],
        "geo": [
          <xsl:for-each select="gex:geographicElement/gex:EX_GeographicBoundingBox">
              {"@type":"GeoShape",
              "box": "<xsl:value-of select="string-join((
                                              gex:southBoundLatitude/gco:Decimal,
                                              gex:westBoundLongitude/gco:Decimal,
                                              gex:northBoundLatitude/gco:Decimal,
                                              gex:eastBoundLongitude/gco:Decimal
                                              ), ' ')"/>"
              }<xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
        ]
      }<xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>]

    ,"temporalCoverage": [
    <xsl:for-each select="mdb:identificationInfo/*/mri:extent/*/gex:temporalElement/*/gex:extent">
       "<xsl:value-of select="concat(
                                                  gml:TimePeriod/gml:beginPosition,
                                                  '/',
                                                  gml:TimePeriod/gml:endPosition
      )"/>"
      <xsl:if test="position() != last()">,</xsl:if>
      <!-- TODO: handle
      "temporalCoverage" : "2013-12-19/.."
      "temporalCoverage" : "2008"
      -->
    </xsl:for-each>]


    <xsl:if test="mdb:identificationInfo/*/mri:resourceConstraints/mco:MD_LegalConstraints/mco:otherConstraints">
      ,"license": [<xsl:for-each select="mdb:identificationInfo/*/mri:resourceConstraints/mco:MD_LegalConstraints/mco:otherConstraints">
          <xsl:choose>
            <xsl:when test="starts-with(normalize-space(string-join(gco:CharacterString/text(),'')),'http') or starts-with(normalize-space(string-join(gco:CharacterString/text(),'')),'//')">
              "<xsl:value-of select="normalize-space(string-join(gco:CharacterString/text(),''))"/>"
            </xsl:when>
            <xsl:when test="starts-with(string-join(gcx:Anchor/@xlink:href,''),'http') or starts-with(./@xlink:href,'//')">
              "<xsl:value-of select="string-join(gcx:Anchor/@xlink:href,'')"/>"
            </xsl:when>
            <xsl:otherwise>
              {
                "@type": "CreativeWork",
                "name": <xsl:apply-templates mode="toJsonLDLocalized" select="."/>
              }
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>]
    </xsl:if>
    <!-- TODO: When a dataset derives from or aggregates several originals, use the isBasedOn property. -->
    <!-- TODO: hasPart -->
  }
  </xsl:template>






  <xsl:template name="toJsonLDLocalized"
                mode="toJsonLDLocalized" match="*">
    <xsl:param name="asArray"
               select="true()"/>

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
                              gn-fn-index:json-escape(gmd:LocalisedCharacterString/text()),
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
                      select="lan:PT_FreeText/*/lan:LocalisedCharacterString[@id = $requestedLanguageId]/text()"/>
        <xsl:value-of select="concat('&quot;',
                              gn-fn-index:json-escape(
                                if ($requestedValue != '') then $requestedValue else (gco:CharacterString|gcx:Anchor)),
                              '&quot;')"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- A simple property value -->
        <xsl:value-of select="concat('&quot;',
                              gn-fn-index:json-escape(gco:CharacterString|gcx:Anchor),
                              '&quot;')"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
