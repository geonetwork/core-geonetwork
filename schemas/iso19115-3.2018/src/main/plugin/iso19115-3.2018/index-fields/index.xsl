<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2020 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:date-util="java:org.fao.geonet.utils.DateUtil"
                xmlns:index="java:org.fao.geonet.kernel.search.EsSearchManager"
                xmlns:gn-fn-index="http://geonetwork-opensource.org/xsl/functions/index"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:daobs="http://daobs.org"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all"
                version="2.0">

  <!-- TODO remove dependency on 19139-->
  <xsl:import href="../../iso19139/index-fields/fn.xsl"/>
  <xsl:import href="common/inspire-constant.xsl"/>
  <xsl:import href="common/index-utils.xsl"/>


  <xsl:output method="xml" indent="yes"/>

  <xsl:output name="default-serialize-mode"
              indent="no"
              omit-xml-declaration="yes"
              encoding="utf-8"
              escape-uri-attributes="yes"/>


  <!-- If identification creation, publication and revision date
    should be indexed as a temporal extent information (eg. in INSPIRE
    metadata implementing rules, those elements are defined as part
    of the description of the temporal extent). -->
  <xsl:variable name="useDateAsTemporalExtent" select="true()"/>

  <!-- Define if operatesOn type should be defined
  by analysis of protocol in all transfers options.
  -->
  <xsl:variable name="operatesOnSetByProtocol" select="false()"/>

  <xsl:variable name="processRemoteDocs" select="true()" />

  <!-- Define if search for regulation title should be strict or light. -->
  <xsl:variable name="inspireRegulationLaxCheck" select="false()"/>

  <!-- List of keywords to search for to flag a record as opendata.
   Do not put accents or upper case letters here as comparison will not
   take them in account. -->
  <xsl:variable name="openDataKeywords"
                select="'opendata|open data|donnees ouvertes'"/>

  <xsl:variable name="separator" as="xs:string"
                select="'|'"/>

  <!-- Parent may be encoded using an associatedResource.
  Define which association type should be considered as parent. -->
  <xsl:variable name="parentAssociatedResourceType" select="'partOfSeamlessDatabase'"/>

  <!-- To avoid Document contains at least one immense term
  in field="resourceAbstract" (whose UTF8 encoding is longer
  than the max length 32766. -->
  <xsl:variable name="maxFieldLength" select="32000" as="xs:integer"/>

  <xsl:variable name="siteUrl" select="util:getSiteUrl()" />

  <xsl:template match="/">
    <xsl:apply-templates mode="index"/>
  </xsl:template>


  <xsl:template match="mdb:MD_Metadata"
                mode="extract-uuid">
    <xsl:value-of
      select="mdb:metadataIdentifier/mcc:MD_Identifier/mcc:code/gco:CharacterString[. != '']"/>
  </xsl:template>


  <xsl:template mode="index-extra-fields" match="*"/>

  <xsl:template mode="index-extra-documents" match="*"/>



  <xsl:template match="mdb:MD_Metadata" mode="index">
    <!-- Main variables for the document -->
    <xsl:variable name="identifier" as="xs:string"
                  select="mdb:metadataIdentifier/mcc:MD_Identifier/mcc:code/gco:CharacterString[. != '']"/>

    <xsl:variable name="lastRevisionDate" as="xs:string?"
                  select="mdb:dateInfo/*[
                              cit:dateType/*/@codeListValue = 'revision'
                            ]/cit:date/gco:DateTime[gn-fn-index:is-isoDate(.)]"/>

    <xsl:variable name="mainLanguage" as="xs:string?"
                  select="mdb:defaultLocale/lan:PT_Locale/
                            lan:language/lan:LanguageCode/
                              @codeListValue[normalize-space(.) != '']"/>


    <xsl:variable name="otherLanguages" as="attribute()*"
                  select="mdb:otherLocale/lan:PT_Locale/
                            lan:language/lan:LanguageCode/
                              @codeListValue[normalize-space(.) != '']"/>

    <xsl:variable name="allLanguages">
      <xsl:if test="$mainLanguage != ''">
        <lang id="default" value="{$mainLanguage}"/>
        <xsl:for-each select="$otherLanguages">
          <lang id="{../../../@id}" value="{.}"/>
        </xsl:for-each>
      </xsl:if>
    </xsl:variable>

    <!-- Record is dataset if no hierarchyLevel -->
    <xsl:variable name="isDataset" as="xs:boolean"
                  select="
                      count(mdb:metadataScope[mdb:MD_MetadataScope/
                              mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue='dataset']) > 0 or
                      count(mdb:metadataScope) = 0"/>
    <xsl:variable name="isService" as="xs:boolean"
                  select="
                      count(mdb:metadataScope[mdb:MD_MetadataScope/
                              mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue='service']) > 0"/>

    <!--<xsl:message>#<xsl:value-of
      select="count(preceding-sibling::mdb:MD_Metadata)"/>. <xsl:value-of select="$identifier"/></xsl:message>-->

    <!-- Create a first document representing the main record. -->
    <doc>
      <xsl:copy-of select="gn-fn-index:add-field('docType', 'metadata')"/>
      <!-- Index the metadata document as XML -->
      <document>
        <!--<xsl:value-of select="saxon:serialize(., 'default-serialize-mode')"/>-->
      </document>

      <xsl:copy-of select="gn-fn-index:add-field('metadataIdentifier', $identifier)"/>

      <!--<xsl:if test="$pointOfTruthURLPattern != ''">
        &lt;!&ndash; TODO: add metadataLinkage&ndash;&gt;
        <pointOfTruthURL>
          <xsl:value-of
            select="replace($pointOfTruthURLPattern, '\{\{uuid\}\}', $identifier)"/>
        </pointOfTruthURL>
      </xsl:if>-->

      <xsl:for-each
        select="mdb:metadataStandard/cit:CI_Citation/cit:title">
        <xsl:copy-of select="gn-fn-index:add-multilingual-field('standardName', ., $allLanguages)"/>

        <xsl:for-each select="../cit:edition/*">
          <xsl:copy-of select="gn-fn-index:add-multilingual-field('standardVersion', ., $allLanguages)"/>
        </xsl:for-each>
      </xsl:for-each>


      <!-- Since GN sets the timezone in system/server/timeZone setting as Java system default
        timezone we can rely on XSLT functions to get current date in the right timezone -->
      <indexingDate>
        <xsl:value-of select="format-dateTime(current-dateTime(), $dateFormat)"/>
      </indexingDate>


      <!-- Indexing record information -->
      <!-- # Date -->
      <!-- Select first one because some records have 2 dates !
      eg. fr-784237539-bdref20100101-0105
      -->
      <xsl:for-each select="(mdb:dateInfo/
                              cit:CI_Date[cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'revision']/
                                cit:date/*[gn-fn-index:is-isoDate(.)])[1]">
        <dateStamp><xsl:value-of select="date-util:convertToISOZuluDateTime(normalize-space(.))"/></dateStamp>
      </xsl:for-each>


      <xsl:copy-of select="gn-fn-index:add-field('mainLanguage', $mainLanguage)"/>

      <xsl:for-each select="$otherLanguages">
        <xsl:copy-of select="gn-fn-index:add-field('otherLanguage', .)"/>
        <xsl:copy-of select="gn-fn-index:add-field('otherLanguageId', ../../../@id)"/>
      </xsl:for-each>


      <xsl:for-each select="mdb:defaultLocale/*/lan:characterEncoding/*[@codeListValue != '']">
        <xsl:copy-of select="gn-fn-index:add-codelist-field(
                                'cl_characterSet', ., $allLanguages)"/>
      </xsl:for-each>


      <xsl:choose>
        <xsl:when test="$isDataset">
          <resourceType>dataset</resourceType>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue[. != '']">
            <resourceType>
              <xsl:value-of select="."/>
            </resourceType>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>


      <!-- Indexing metadata contact -->
      <xsl:apply-templates mode="index-contact" select="mdb:contact">
        <xsl:with-param name="fieldSuffix" select="''"/>
      </xsl:apply-templates>

      <!-- Indexing all codelist

      Indexing method is:
      <gmd:accessConstraints>
        <gmd:MD_RestrictionCode codeListValue="otherRestrictions"
        is indexed as
        cl_accessConstraints:otherRestrictions

        Exclude some useless codelist like
        Contact role, Date type.
      -->
      <xsl:for-each-group select=".//*[@codeListValue != '' and
                            name() != 'cit:CI_RoleCode' and
                            name() != 'cit:CI_DateTypeCode' and
                            name() != 'lan:MD_CharacterSetCode' and
                            name() != 'lan:LanguageCode'
                            ]"
                          group-by="@codeListValue">
        <xsl:variable name="parentName"
                      select="local-name(..)"/>
        <xsl:variable name="fieldName"
                      select="concat('cl_', $parentName)"/>

        <xsl:copy-of select="gn-fn-index:add-codelist-field(
                                  $fieldName, ., $allLanguages)"/>
      </xsl:for-each-group>


      <!-- Indexing resource information
      TODO: Should we support multiple identification in the same record
      eg. nl db60a314-5583-437d-a2ff-1e59cc57704e
      Also avoid error when records contains multiple MD_IdentificationInfo
      or SRV_ServiceIdentification or a mix
      eg. de 8bb5334f-558b-982b-7b12-86ea486540d7
      -->
      <xsl:for-each select="mdb:identificationInfo[1]/*[1]">
        <xsl:for-each select="mri:citation/cit:CI_Citation">
          <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceTitle', cit:title, $allLanguages)"/>
          <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceAltTitle', cit:alternateTitle, $allLanguages)"/>

          <xsl:for-each select="cit:date/cit:CI_Date[
                                        cit:date/*/text() != ''
                                        and gn-fn-index:is-isoDate(cit:date/*/text())]">
            <xsl:variable name="dateType"
                          select="cit:dateType/cit:CI_DateTypeCode/@codeListValue"
                          as="xs:string?"/>
            <xsl:variable name="date"
                          select="string(cit:date/gco:Date|cit:date/gco:DateTime)"/>

            <xsl:variable name="zuluDateTime" as="xs:string?">
              <xsl:value-of select="date-util:convertToISOZuluDateTime(normalize-space($date))"/>
            </xsl:variable>
            <xsl:choose>
              <xsl:when test="$zuluDateTime != ''">
                <xsl:element name="{$dateType}DateForResource">
                  <xsl:value-of select="$zuluDateTime"/>
                </xsl:element>
                <xsl:element name="{$dateType}YearForResource">
                  <xsl:value-of select="substring($zuluDateTime, 0, 5)"/>
                </xsl:element>
                <xsl:element name="{$dateType}MonthForResource">
                  <xsl:value-of select="substring($zuluDateTime, 0, 8)"/>
                </xsl:element>
              </xsl:when>
              <xsl:otherwise>
                <indexingErrorMsg>Warning / Date <xsl:value-of select="$dateType"/> with value '<xsl:value-of select="$date"/>' was not a valid date format.</indexingErrorMsg>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>

          <xsl:for-each select="cit:date/cit:CI_Date[gn-fn-index:is-isoDate(cit:date/*/text())]">
              <xsl:variable name="dateType"
                            select="cit:dateType/cit:CI_DateTypeCode/@codeListValue"
                            as="xs:string?"/>
              <xsl:variable name="date"
                            select="string(cit:date/gco:Date|cit:date/gco:DateTime)"/>

            <xsl:variable name="zuluDate"
                          select="date-util:convertToISOZuluDateTime($date)"/>
            <xsl:if test="$zuluDate != ''">
              <resourceDate type="object">
                {"type": "<xsl:value-of select="$dateType"/>", "date": "<xsl:value-of select="$zuluDate"/>"}
              </resourceDate>
            </xsl:if>
          </xsl:for-each>


          <xsl:if test="$useDateAsTemporalExtent">
            <xsl:for-each-group select="cit:date/cit:CI_Date[gn-fn-index:is-isoDate(cit:date/*/text())]/cit:date/*/text()"
                                group-by=".">

              <xsl:variable name="zuluDate"
                            select="date-util:convertToISOZuluDateTime(.)"/>
              <xsl:if test="$zuluDate != ''">
                <resourceTemporalDateRange type="object">{
                  "gte": "<xsl:value-of select="$zuluDate"/>",
                  "lte": "<xsl:value-of select="$zuluDate"/>"
                  }</resourceTemporalDateRange>
              </xsl:if>
            </xsl:for-each-group>
          </xsl:if>

          <xsl:for-each select="cit:identifier/*">
            <resourceIdentifier type="object">{
              "code": "<xsl:value-of select="mcc:code/(gco:CharacterString|gcx:Anchor)"/>",
              "codeSpace": "<xsl:value-of select="mcc:codeSpace/(gco:CharacterString|gcx:Anchor)"/>",
              "link": "<xsl:value-of select="mcc:code/gcx:Anchor/@xlink:href"/>"
              }</resourceIdentifier>
          </xsl:for-each>

          <xsl:for-each
            select="cit:presentationForm/cit:CI_PresentationFormCode/@codeListValue[. != '']">
            <presentationForm>
              <xsl:value-of select="."/>
            </presentationForm>
          </xsl:for-each>

          <xsl:for-each select="cit:edition/*">
            <xsl:copy-of select="gn-fn-index:add-field('resourceEdition', .)"/>
          </xsl:for-each>
        </xsl:for-each>

        <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceAbstract', mri:abstract, $allLanguages)"/>



        <!-- # Characterset -->
        <xsl:if test="mri:defaultLocale/lan:PT_Locale/lan:characterEncoding/lan:MD_CharacterSetCode">
          <xsl:copy-of select="gn-fn-index:add-codelist-field(
                                  'cl_resourceCharacterSet', mri:defaultLocale/lan:PT_Locale/lan:characterEncoding/lan:MD_CharacterSetCode, $allLanguages)"/>
        </xsl:if>

        <!-- Indexing resource contact -->
        <xsl:apply-templates mode="index-contact"
                             select="mri:pointOfContact">
          <xsl:with-param name="fieldSuffix" select="'ForResource'"/>
        </xsl:apply-templates>


        <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceCredit', mri:credit[* != ''], $allLanguages)"/>

        <xsl:variable name="overviews"
                      select="mri:graphicOverview/mcc:MD_BrowseGraphic/
                                mcc:fileName/gco:CharacterString[. != '']"/>

        <xsl:copy-of select="gn-fn-index:add-field('hasOverview', if (count($overviews) > 0) then 'true' else 'false')"/>


        <xsl:for-each select="$overviews">
          <overview type="object">{
            "url": "<xsl:value-of select="normalize-space(.)"/>"
            <xsl:if test="$isStoringOverviewInIndex">
              <xsl:variable name="data"
                            select="util:buildDataUrl(., 140)"/>
              <xsl:if test="$data != ''">,
                "data": "<xsl:value-of select="$data"/>"
              </xsl:if>
            </xsl:if>
            <xsl:if test="count(../../mcc:fileDescription) > 0">,
              "text":
              <xsl:value-of select="gn-fn-index:add-multilingual-field('name', ../../mcc:fileDescription, $allLanguages, true())"/>
            </xsl:if>
            }</overview>
        </xsl:for-each>


        <xsl:for-each
          select="mri:defaultLocale/lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue">
          <resourceLanguage>
            <xsl:value-of select="."/>
          </resourceLanguage>
        </xsl:for-each>


        <xsl:variable name="inspireEnable" select="util:getSettingValue('system/inspire/enable')" />


        <xsl:if test="$inspireEnable = 'true'">
          <!-- TODO: create specific INSPIRE template or mode -->
          <!-- INSPIRE themes

          Select the first thesaurus title because some records
          may contains many even if invalid.

          Also get the first title at it may happen that a record
          have more than one.

          Select any thesaurus having the title containing "INSPIRE themes".
          Some records have "GEMET-INSPIRE themes" eg. sk:ee041534-b8f3-4683-b9dd-9544111a0712
          Some other "GEMET - INSPIRE themes"

          Take in account gmd:descriptiveKeywords or srv:keywords
          -->
          <xsl:variable name="inspireKeywords"
                        select="*/mri:MD_Keywords[
                        contains(lower-case(
                         (mri:thesaurusName/*/cit:title/*/text())[1]
                         ), 'gemet') and
                         contains(lower-case(
                         (mri:thesaurusName/*/cit:title/*/text())[1]
                         ), 'inspire')]
                    /mri:keyword"/>
          <xsl:for-each
            select="$inspireKeywords">
            <xsl:variable name="position" select="position()"/>
            <xsl:for-each select="gco:CharacterString[. != '']|
                                  gcx:Anchor[. != '']">

              <xsl:variable name="inspireTheme" as="xs:string"
                            select="index:analyzeField('synInspireThemes', text())"/>

              <inspireTheme_syn>
                <xsl:value-of select="text()"/>
              </inspireTheme_syn>
              <inspireTheme>
                <xsl:value-of select="$inspireTheme"/>
              </inspireTheme>

              <!--
              WARNING: Here we only index the first keyword in order
              to properly compute one INSPIRE annex.
              -->
              <xsl:if test="position() = 1">
                <inspireThemeFirst_syn>
                  <xsl:value-of select="text()"/>
                </inspireThemeFirst_syn>
                <inspireThemeFirst>
                  <xsl:value-of select="$inspireTheme"/>
                </inspireThemeFirst>
                <xsl:if test="$inspireTheme != ''">
                  <inspireAnnexForFirstTheme>
                    <xsl:value-of
                      select="index:analyzeField('synInspireAnnexes', $inspireTheme)"/>
                  </inspireAnnexForFirstTheme>
                  <xsl:variable name="inspireThemeUri" as="xs:string"
                                select="index:analyzeField('synInspireThemeUris', $inspireTheme)"/>
                  <inspireThemeUri>
                    <xsl:value-of select="$inspireThemeUri"/>
                  </inspireThemeUri>
                </xsl:if>
              </xsl:if>
              <inspireAnnex>
                <xsl:value-of
                  select="index:analyzeField('synInspireAnnexes', $inspireTheme)"/>
              </inspireAnnex>
            </xsl:for-each>
          </xsl:for-each>

          <inspireThemeNumber>
            <xsl:value-of
              select="count($inspireKeywords)"/>
          </inspireThemeNumber>

          <hasInspireTheme>
            <xsl:value-of
              select="if (count($inspireKeywords) > 0) then 'true' else 'false'"/>
          </hasInspireTheme>
        </xsl:if>


        <xsl:variable name="keywords"
                      select=".//mri:keyword[*/normalize-space() != '']"/>

        <xsl:if test="count($keywords) > 0">
          <tag type="object">
            [<xsl:for-each select="$keywords">
            <xsl:value-of select="gn-fn-index:add-multilingual-field('keyword', ., $allLanguages)/text()"/>
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>]
          </tag>
        </xsl:if>

        <xsl:variable name="isOpenData">
          <xsl:for-each select="$keywords/(
                                gco:CharacterString|
                                gcx:Anchor|
                                */lan:textGroup/lan:LocalisedCharacterString)">
            <xsl:if test="matches(
                            normalize-unicode(replace(normalize-unicode(
                              lower-case(normalize-space(text())), 'NFKD'), '\p{Mn}', ''), 'NFKC'),
                            $openDataKeywords)">
              <xsl:value-of select="'true'"/>
            </xsl:if>
          </xsl:for-each>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="normalize-space($isOpenData) != ''">
            <isOpenData>true</isOpenData>
          </xsl:when>
          <xsl:otherwise>
            <isOpenData>false</isOpenData>
          </xsl:otherwise>
        </xsl:choose>



        <!-- Index keywords by types -->
        <xsl:variable name="keywordTypes"
                      select="distinct-values(.//mri:descriptiveKeywords/*/
                                mri:type/*/@codeListValue[. != ''])"/>
        <xsl:variable name="geoDesciption"
                      select="//gex:geographicElement/gex:EX_GeographicDescription/
                                gex:geographicIdentifier/mcc:MD_Identifier/
                                  mcc:code[*/normalize-space(.) != '']
                              |//gex:EX_Extent/gex:description[*/normalize-space(.) != '']"/>

        <xsl:for-each select="$keywordTypes">
          <xsl:variable name="type"
                        select="."/>
          <xsl:variable name="keywordsForType"
                        select="$keywords[../mri:type/*/@codeListValue = $type]
                        |$geoDesciption[$type = 'place']"/>
          <xsl:element name="keywordType-{$type}">
            <xsl:attribute name="type" select="'object'"/>
            [<xsl:for-each select="$keywordsForType">
            <xsl:value-of select="gn-fn-index:add-multilingual-field('keyword', ., $allLanguages)/text()"/>
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>]
          </xsl:element>
        </xsl:for-each>



        <!-- Index all keywords having a specific thesaurus -->
        <xsl:for-each
          select="*/mri:MD_Keywords[mri:thesaurusName]">

          <xsl:variable name="thesaurusName"
                        select="mri:thesaurusName[1]/cit:CI_Citation/
                                  cit:title[1]/gco:CharacterString"/>

          <xsl:variable name="thesaurusId"
                        select="normalize-space(mri:thesaurusName/cit:CI_Citation/
                                  cit:identifier/mcc:MD_Identifier/
                                    mcc:code/(gco:CharacterString|gcx:Anchor)/text())"/>

          <xsl:variable name="key">
            <xsl:choose>
              <xsl:when test="$thesaurusId != ''">
                <xsl:value-of select="tokenize($thesaurusId, '\.')[last()]"/>
              </xsl:when>
              <!-- Try to build a thesaurus key based on the name
              by removing space - to be improved. -->
              <xsl:when test="normalize-space($thesaurusName) != ''">
                <xsl:value-of select="replace($thesaurusName, '[^a-zA-Z0-9]', '')"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>

          <xsl:if test="normalize-space($key) != ''">
            <xsl:variable name="keywords"
                          select="mri:keyword[*/normalize-space() != '']"/>

            <xsl:call-template name="build-thesaurus-fields">
              <xsl:with-param name="thesaurus" select="$key"/>
              <xsl:with-param name="thesaurusId" select="$thesaurusId"/>
              <xsl:with-param name="keywords" select="$keywords"/>
              <xsl:with-param name="mainLanguage" select="$mainLanguage"/>
              <xsl:with-param name="allLanguages" select="$allLanguages"/>
            </xsl:call-template>
          </xsl:if>
        </xsl:for-each>


        <allKeywords type="object">{
          <xsl:for-each-group select="*/mri:MD_Keywords"
                              group-by="mri:thesaurusName/*/cit:title/*/text()">
            <xsl:sort select="current-grouping-key()"/>
            <xsl:variable name="thesaurusName"
                          select="current-grouping-key()"/>

            <xsl:variable name="thesaurusId"
                          select="normalize-space(mri:thesaurusName/*/
                                    cit:identifier[position() = 1]/*/
                                      cit:code/(gco:CharacterString|gcx:Anchor)/text())"/>

            <xsl:variable name="key">
              <xsl:choose>
                <xsl:when test="$thesaurusId != ''">
                  <xsl:value-of select="$thesaurusId"/>
                </xsl:when>
                <!-- Try to build a thesaurus key based on the name
                by removing space - to be improved. -->
                <xsl:when test="normalize-space($thesaurusName) != ''">
                  <xsl:value-of select="replace($thesaurusName, ' ', '-')"/>
                </xsl:when>
              </xsl:choose>
            </xsl:variable>

            <xsl:if test="normalize-space($key) != ''">
              <xsl:variable name="thesaurusField"
                            select="replace($key, '[^a-zA-Z0-9]', '')"/>

              "<xsl:value-of select="$thesaurusField"/>": {
              "id": "<xsl:value-of select="gn-fn-index:json-escape($thesaurusId)"/>",
              "title": "<xsl:value-of select="gn-fn-index:json-escape($thesaurusName)"/>",
              "theme": "<xsl:value-of select="gn-fn-index:json-escape(mri:type/*/@codeListValue)"/>",
              "link": "<xsl:value-of select="gn-fn-index:json-escape(@xlink:href)"/>",
              "keywords": [
              <xsl:for-each select="mri:keyword[*/normalize-space() != '']">
                <xsl:value-of select="gn-fn-index:add-multilingual-field('keyword', ., $allLanguages)/text()"/>
                <xsl:if test="position() != last()">,</xsl:if>
              </xsl:for-each>
              ]}
              <xsl:if test="position() != last()">,</xsl:if>
            </xsl:if>
          </xsl:for-each-group>


          <xsl:variable name="keywordWithNoThesaurus"
                        select="//mri:MD_Keywords[
                                not(mri:thesaurusName)
                                or mri:thesaurusName/*/cit:title/*/text() = '']"/>
          <xsl:variable name="hasKeywordWithThesaurus"
                        select="count(*/mri:MD_Keywords[
                                  mri:thesaurusName/*/cit:title/*/text() != '']) > 0"/>

          <xsl:if test="$hasKeywordWithThesaurus and $keywordWithNoThesaurus">,</xsl:if>

          <xsl:variable name="types">
            <xsl:for-each select="distinct-values($keywordWithNoThesaurus//mri:type/*/@codeListValue[. != ''])">
              <type><xsl:value-of select="."/></type>
            </xsl:for-each>
            <xsl:if test="count($keywordWithNoThesaurus[not(mri:type) or mri:type/*/@codeListValue = '']) > 0">
              <type></type>
            </xsl:if>
          </xsl:variable>

          <xsl:for-each select="$types/*">
            <xsl:variable name="thesaurusType"
                          select="."/>
            <xsl:variable name="thesaurusField"
                          select="concat('otherKeywords-', $thesaurusType)"/>
            "<xsl:value-of select="$thesaurusField"/>": {
            "keywords": [
            <xsl:for-each select="$keywordWithNoThesaurus
                                    [if ($thesaurusType = '') then not(mri:type) or mri:type/*/@codeListValue = '' else mri:type/*/@codeListValue = $thesaurusType]
                                    /mri:keyword[*/normalize-space() != '']">
              <xsl:value-of select="gn-fn-index:add-multilingual-field('keyword', ., $allLanguages)/text()"/>
              <xsl:if test="position() != last()">,</xsl:if>
            </xsl:for-each>
            ]}
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          }</allKeywords>



        <xsl:for-each select="mri:topicCategory/mri:MD_TopicCategoryCode">
          <xsl:variable name="value" as="node()">
            <xsl:copy>
              <xsl:attribute name="codeListValue" select="."/>
            </xsl:copy>
          </xsl:variable>
          <xsl:copy-of select="gn-fn-index:add-codelist-field(
                                'cl_topic', $value, $allLanguages)"/>
        </xsl:for-each>



        <xsl:for-each select="mri:spatialResolution/mri:MD_Resolution">
          <xsl:for-each
            select="mri:equivalentScale/mri:MD_RepresentativeFraction/mri:denominator/gco:Integer[. castable as xs:decimal]">
            <resolutionScaleDenominator>
              <xsl:value-of select="."/>
            </resolutionScaleDenominator>
          </xsl:for-each>

          <xsl:for-each select="mri:distance/gco:Distance[. != '']">
            <resolutionDistance>
              <xsl:value-of select="concat(., ' ', @uom)"/>
            </resolutionDistance>
          </xsl:for-each>
        </xsl:for-each>

        <xsl:for-each
          select="mri:spatialRepresentationType/mcc:MD_SpatialRepresentationTypeCode/@codeListValue[. != '']">
          <spatialRepresentationType>
            <xsl:value-of select="."/>
          </spatialRepresentationType>
        </xsl:for-each>

        <xsl:for-each select="mri:resourceConstraints/*">
          <xsl:variable name="fieldPrefix" select="local-name()"/>

          <xsl:copy-of select="gn-fn-index:add-multilingual-field(concat($fieldPrefix, 'OtherConstraints'), mco:otherConstraints , $allLanguages)"/>

          <xsl:copy-of select="gn-fn-index:add-multilingual-field(concat($fieldPrefix, 'UseLimitation'), mco:useLimitation, $allLanguages)"/>
        </xsl:for-each>

        <xsl:for-each select="mri:resourceConstraints/mco:MD_LegalConstraints/mco:otherConstraints">
          <xsl:copy-of select="gn-fn-index:add-multilingual-field('license', ., $allLanguages)"/>
        </xsl:for-each>

        <xsl:if test="*/gex:EX_Extent/*/gex:EX_BoundingPolygon">
          <hasBoundingPolygon>true</hasBoundingPolygon>
        </xsl:if>

        <xsl:for-each select="*/gex:EX_Extent/*/gex:EX_BoundingPolygon/gex:polygon">
          <xsl:variable name="geojson"
                        select="util:gmlToGeoJson(
                                  saxon:serialize(gml:*, 'default-serialize-mode'),
                                  true(), 5)"/>
          <xsl:choose>
            <xsl:when test="$geojson = ''"></xsl:when>
            <xsl:when test="matches($geojson, '(Error|Warning):.*')">
              <shapeParsingError><xsl:value-of select="$geojson"/></shapeParsingError>
            </xsl:when>
            <xsl:otherwise>
              <shape type="object">
                <xsl:value-of select="$geojson"/>
              </shape>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>

        <xsl:for-each select="*/gex:EX_Extent">
          <!-- TODO: index bounding polygon -->
          <xsl:for-each select=".//gex:EX_GeographicBoundingBox[
                                ./gex:westBoundLongitude/gco:Decimal castable as xs:decimal and
                                ./gex:eastBoundLongitude/gco:Decimal castable as xs:decimal and
                                ./gex:northBoundLatitude/gco:Decimal castable as xs:decimal and
                                ./gex:southBoundLatitude/gco:Decimal castable as xs:decimal
                                ]">
            <xsl:variable name="format" select="'#0.000000'"></xsl:variable>

            <xsl:variable name="w"
                          select="format-number(./gex:westBoundLongitude/gco:Decimal/text(), $format)"/>
            <xsl:variable name="e"
                          select="format-number(./gex:eastBoundLongitude/gco:Decimal/text(), $format)"/>
            <xsl:variable name="n"
                          select="format-number(./gex:northBoundLatitude/gco:Decimal/text(), $format)"/>
            <xsl:variable name="s"
                          select="format-number(./gex:southBoundLatitude/gco:Decimal/text(), $format)"/>

            <!-- Example: ENVELOPE(-10, 20, 15, 10) which is minX, maxX, maxY, minY order
            http://wiki.apache.org/solr/SolrAdaptersForLuceneSpatial4
            https://cwiki.apache.org/confluence/display/solr/Spatial+Search

            bbox field type limited to one. TODO
            <xsl:if test="position() = 1">
              <bbox>
                <xsl:text>ENVELOPE(</xsl:text>
                <xsl:value-of select="$w"/>
                <xsl:text>,</xsl:text>
                <xsl:value-of select="$e"/>
                <xsl:text>,</xsl:text>
                <xsl:value-of select="$n"/>
                <xsl:text>,</xsl:text>
                <xsl:value-of select="$s"/>
                <xsl:text>)</xsl:text>
              </field>
            </xsl:if>
            -->
            <xsl:choose>
              <xsl:when test="-180 &lt;= number($e) and number($e) &lt;= 180 and
                              -180 &lt;= number($w) and number($w) &lt;= 180 and
                              -90 &lt;= number($s) and number($s) &lt;= 90 and
                              -90 &lt;= number($n) and number($n) &lt;= 90">
                <xsl:choose>
                  <xsl:when test="$e = $w and $s = $n">
                    <location><xsl:value-of select="concat($s, ',', $w)"/></location>
                  </xsl:when>
                  <xsl:when
                    test="($e = $w and $s != $n) or ($e != $w and $s = $n)">
                    <!-- Probably an invalid bbox indexing a point only -->
                    <location><xsl:value-of select="concat($s, ',', $w)"/></location>
                  </xsl:when>
                  <xsl:otherwise>
                    <geom type="object">
                      <xsl:text>{"type": "Polygon",</xsl:text>
                      <xsl:text>"coordinates": [[</xsl:text>
                      <xsl:value-of select="concat('[', $w, ',', $s, ']')"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat('[', $e, ',', $s, ']')"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat('[', $e, ',', $n, ']')"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat('[', $w, ',', $n, ']')"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat('[', $w, ',', $s, ']')"/>
                      <xsl:text>]]}</xsl:text>
                    </geom>

                    <location><xsl:value-of select="concat(
                                              (number($s) + number($n)) div 2,
                                              ',',
                                              (number($w) + number($e)) div 2)"/></location>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
            <!--<xsl:value-of select="($e + $w) div 2"/>,<xsl:value-of select="($n + $s) div 2"/></field>-->
          </xsl:for-each>


          <xsl:for-each select=".//gex:temporalElement/*/gex:extent/gml:TimePeriod">
            <xsl:variable name="start"
                          select="gml:beginPosition|gml:begin/gml:TimeInstant/gml:timePosition"/>
            <xsl:variable name="end"
                          select="gml:endPosition|gml:end/gml:TimeInstant/gml:timePosition"/>


            <xsl:variable name="zuluStartDate"
                          select="date-util:convertToISOZuluDateTime($start)"/>
            <xsl:variable name="zuluEndDate"
                          select="date-util:convertToISOZuluDateTime($end)"/>

            <xsl:if test="$zuluStartDate != '' and $zuluEndDate != ''">
              <resourceTemporalDateRange type="object">{
                "gte": "<xsl:value-of select="$zuluStartDate"/>"
                <xsl:if test="$start &lt; $end and not($end/@indeterminatePosition = 'now')">
                  ,"lte": "<xsl:value-of select="$zuluEndDate"/>"
                </xsl:if>
                }</resourceTemporalDateRange>
              <resourceTemporalExtentDateRange type="object">{
                "gte": "<xsl:value-of select="$zuluStartDate"/>"
                <xsl:if test="$start &lt; $end and not($end/@indeterminatePosition = 'now')">
                  ,"lte": "<xsl:value-of select="$zuluEndDate"/>"
                </xsl:if>
                }</resourceTemporalExtentDateRange>
            </xsl:if>

            <xsl:if test="$start &gt; $end">
              <indexingErrorMsg>Warning / Field resourceTemporalDateRange /
                Lower range bound '<xsl:value-of select="."/>' can not be
                greater than upper bound '<xsl:value-of select="$end"/>'.
                Date range not indexed.</indexingErrorMsg>
            </xsl:if>
          </xsl:for-each>

          <xsl:for-each select=".//gex:verticalElement/*">
            <xsl:variable name="min"
                          select="gex:minimumValue/*/text()"/>
            <xsl:variable name="max"
                          select="gex:maximumValue/*/text()"/>

            <resourceVerticalRange type="object">{
              "gte": "<xsl:value-of select="normalize-space($min)"/>"
              <xsl:if test="$min &lt; $max">
                ,"lte": "<xsl:value-of select="normalize-space($max)"/>"
              </xsl:if>
              }</resourceVerticalRange>
          </xsl:for-each>
        </xsl:for-each>



        <!-- Service information -->
        <xsl:for-each select="srv:serviceType/gco:ScopedName">
          <serviceType>
            <xsl:value-of select="text()"/>
          </serviceType>
          <xsl:if test="$inspireEnable = 'true'">
            <xsl:variable name="inspireServiceType" as="xs:string"
                          select="index:analyzeField(
                                    'keepInspireServiceTypes', text())"/>
            <xsl:if test="$inspireServiceType != ''">
              <inspireServiceType>
                <xsl:value-of select="lower-case($inspireServiceType)"/>
              </inspireServiceType>
            </xsl:if>
          </xsl:if>
          <xsl:if test="following-sibling::srv:serviceTypeVersion">
            <serviceTypeAndVersion>
              <xsl:value-of select="concat(
                        text(),
                        $separator,
                        following-sibling::srv:serviceTypeVersion/gco:CharacterString/text())"/>
            </serviceTypeAndVersion>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>


      <xsl:for-each select="mdb:referenceSystemInfo/*">
        <xsl:for-each select="mrs:referenceSystemIdentifier/*">
          <xsl:variable name="crs" select="(mcc:description/*/text()|mcc:code/*/text())[1]"/>

          <xsl:if test="$crs != ''">
            <coordinateSystem>
              <xsl:value-of select="$crs"/>
            </coordinateSystem>
          </xsl:if>

          <crsDetails type="object">{
            "code": "<xsl:value-of select="gn-fn-index:json-escape(mcc:code/*/text())"/>",
            "codeSpace": "<xsl:value-of select="gn-fn-index:json-escape(mcc:codeSpace/*/text())"/>",
            "name": "<xsl:value-of select="gn-fn-index:json-escape(mcc:description/*/text())"/>",
            "url": "<xsl:value-of select="gn-fn-index:json-escape(mcc:code/*/@xlink:href)"/>"
            }</crsDetails>
        </xsl:for-each>
      </xsl:for-each>


      <!-- INSPIRE Conformity -->
      <xsl:variable name="legalTextList"
                    select="if ($isService) then $eu9762009 else $eu10892010"/>

      <xsl:for-each-group select="mdb:dataQualityInfo/*/mdq:report"
                          group-by="*/mdq:result/*/mdq:specification/cit:CI_Citation/
                                        cit:title/gco:CharacterString">

        <xsl:variable name="title" select="current-grouping-key()"/>
        <xsl:variable name="matchingEUText"
                      select="if ($inspireRegulationLaxCheck)
                              then daobs:search-in-contains($legalTextList/*, $title)
                              else daobs:search-in($legalTextList/*, $title)"/>

        <xsl:variable name="pass"
                      select="*/mdq:result/*/mdq:pass/gco:Boolean"/>

        <xsl:if test="count($matchingEUText) = 1">
          <inspireConformResource>
            <xsl:value-of select="$pass"/>
          </inspireConformResource>
        </xsl:if>

        <xsl:if test="string($title)">
          <specificationConformance type="object">{
            "title": "<xsl:value-of select="gn-fn-index:json-escape($title)" />",
            <xsl:if test="string(*/mdq:result/*/mdq:specification/cit:CI_Citation/cit:date/cit:CI_Date/cit:date/gco:Date)">
            "date": "<xsl:value-of select="*/mdq:result/*/mdq:specification/cit:CI_Citation/cit:date/cit:CI_Date/cit:date/gco:Date" />",
            </xsl:if>
            <xsl:if test="*/mdq:result/*/mdq:specification/*/cit:title/@xlink:href">
              "link": "<xsl:value-of select="*/mdq:result/*/mdq:specification/*/cit:title/@xlink:href"/>",
            </xsl:if>
            <xsl:if test="*/mdq:result/*/mdq:explanation/*/text() != ''">
              "explanation": "<xsl:value-of select="gn-fn-index:json-escape((*/mdq:result/*/mdq:explanation/*/text())[1])" />",
            </xsl:if>
            "pass": "<xsl:value-of select="$pass" />"
            }
          </specificationConformance>
        </xsl:if>

        <xsl:element name="conformTo_{replace(normalize-space($title), '[^a-zA-Z0-9]', '')}">
          <xsl:value-of select="$pass"/>
        </xsl:element>
      </xsl:for-each-group>



      <xsl:variable name="jsonFeatureTypes">[
        <xsl:for-each select="mdb:contentInfo//gfc:FC_FeatureCatalogue/gfc:featureType">{

          "typeName" : "<xsl:value-of select="gn-fn-index:json-escape(gfc:FC_FeatureType/gfc:typeName/*/text())"/>",
          "definition" :"<xsl:value-of select="gn-fn-index:json-escape(gfc:FC_FeatureType/gfc:definition/*/text())"/>",
          "code" :"<xsl:value-of select="gn-fn-index:json-escape(gfc:FC_FeatureType/gfc:code/*/text())"/>",
          "isAbstract" :"<xsl:value-of select="gfc:FC_FeatureType/gfc:isAbstract/*/text()"/>",
          "aliases" : "<xsl:value-of select="gn-fn-index:json-escape(gfc:FC_FeatureType/gfc:aliases/*/text())"/>"
          <!--"inheritsFrom" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:inheritsFrom/*/text()"/>",
          "inheritsTo" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:inheritsTo/*/text()"/>",
          "constrainedBy" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:constrainedBy/*/text()"/>",
          "definitionReference" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:definitionReference/*/text()"/>",-->
          <!-- Index attribute table as JSON object -->
          <xsl:variable name="attributes"
                        select="*/gfc:carrierOfCharacteristics"/>
          <xsl:if test="count($attributes) > 0">
            ,"attributeTable" : [
            <xsl:for-each select="$attributes">
              <!-- TODO: Add multilingual support-->
              {"name": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:memberName/gco:CharacterString/text())"/>",
              "definition": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:definition/gco:CharacterString/text())"/>",
              "code": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:code/*/text())"/>",
              "link": "<xsl:value-of select="*/gfc:code/*/@xlink:href"/>",
              "type": "<xsl:value-of select="*/gfc:valueType/gco:TypeName/gco:aName/*/text()"/>"
              <xsl:if test="*/gfc:listedValue">
                ,"values": [
                <xsl:for-each select="*/gfc:listedValue">{
                  "label": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:label/gco:CharacterString/text())"/>",
                  "code": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:code/*/text())"/>",
                  "definition": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:definition/gco:CharacterString/text())"/>"}
                  <xsl:if test="position() != last()">,</xsl:if>
                </xsl:for-each>
                ]
              </xsl:if>
              }
              <xsl:if test="position() != last()">,</xsl:if>
            </xsl:for-each>
            ]
          </xsl:if>
          }
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>
        ]
      </xsl:variable>

      <featureTypes type="object">
        <xsl:value-of select="$jsonFeatureTypes"/>
      </featureTypes>

      <xsl:for-each select="mdb:contentInfo/*/mrc:featureCatalogueCitation[@uuidref != '']">
        <xsl:variable name="xlink"
                      select="@xlink:href"/>
        <xsl:copy-of select="gn-fn-index:build-record-link(@uuidref, $xlink, @xlink:title, 'fcats')"/>
      </xsl:for-each>


      <xsl:for-each select="mdb:resourceLineage/*">
        <xsl:copy-of select="gn-fn-index:add-multilingual-field('lineage', mrl:lineage/mrl:LI_Lineage/
                                mrl:statement, $allLanguages)"/>

        <xsl:for-each select=".//mrl:source[@uuidref != '']">
          <xsl:variable name="xlink"
                        select="@xlink:href"/>
          <hassource><xsl:value-of select="@uuidref"/></hassource>
          <xsl:copy-of select="gn-fn-index:build-record-link(@uuidref, $xlink, @xlink:title, 'sources')"/>
        </xsl:for-each>
      </xsl:for-each>


      <xsl:for-each select="mdb:dataQualityInfo/*">
        <!-- Indexing measure value -->
        <xsl:for-each select="mdq:report/*[
                normalize-space(mdq:nameOfMeasure/gco:CharacterString) != '']">
          <xsl:variable name="measureName"
                        select="replace(normalize-space(mdq:nameOfMeasure/gco:CharacterString), '[^a-zA-Z0-9]', '')"/>
          <xsl:for-each select="mdq:result/mdq:DQ_QuantitativeResult/mdq:value">
            <xsl:if test=". != ''">
              <xsl:element name="measure_{$measureName}">
                <xsl:value-of select="."/>
              </xsl:element>
            </xsl:if>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:for-each>


      <xsl:for-each select="mdb:distributionInfo/*">
        <xsl:for-each select="mrd:distributionFormat/*/
                                mrd:formatSpecificationCitation/*/cit:title/*/text()">
          <format>
            <xsl:value-of select="."/>
          </format>
        </xsl:for-each>

        <xsl:for-each select="mrd:transferOptions/*/
                                mrd:onLine/*[cit:linkage/gco:CharacterString != '']">
          <xsl:variable name="transferGroup"
                        select="count(ancestor::mrd:transferOptions/preceding-sibling::mrd:transferOptions)"/>

          <xsl:variable name="protocol" select="cit:protocol/*/text()"/>

          <linkUrl>
            <xsl:value-of select="cit:linkage/gco:CharacterString"/>
          </linkUrl>
          <linkProtocol>
            <xsl:value-of select="$protocol"/>
          </linkProtocol>
          <xsl:element name="linkUrlProtocol{replace($protocol, '[^a-zA-Z0-9]', '')}">
            <xsl:value-of select="cit:linkage/*/text()"/>
          </xsl:element>
          <link type="object">{
            "protocol":"<xsl:value-of select="gn-fn-index:json-escape(cit:protocol/*/text())"/>",
            "url":"<xsl:value-of select="gn-fn-index:json-escape(cit:linkage/*/text())"/>",
            "name":"<xsl:value-of select="gn-fn-index:json-escape((cit:name/*/text())[1])"/>",
            "description":"<xsl:value-of select="gn-fn-index:json-escape((cit:description/*/text())[1])"/>",
            "function":"<xsl:value-of select="cit:function/cit:CI_OnLineFunctionCode/@codeListValue"/>",
            "applicationProfile":"<xsl:value-of select="gn-fn-index:json-escape(cit:applicationProfile/gco:CharacterString/text())"/>",
            "group": <xsl:value-of select="$transferGroup"/>
            }
          </link>

          <xsl:if test="$operatesOnSetByProtocol and normalize-space($protocol) != ''">
            <xsl:if test="daobs:contains($protocol, 'wms')">
              <recordOperatedByType>view</recordOperatedByType>
            </xsl:if>
            <xsl:if test="daobs:contains($protocol, 'wfs') or
                          daobs:contains($protocol, 'wcs') or
                          daobs:contains($protocol, 'download')">
              <recordOperatedByType>download</recordOperatedByType>
            </xsl:if>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:call-template name="index-operatesOn"/>

      <!-- Index more fields in this element -->
      <xsl:apply-templates mode="index-extra-fields" select="."/>

      <xsl:variable name="recordLinks"
                    select="mdb:parentMetadata[@uuidref != '']"/>
      <xsl:choose>
        <xsl:when test="count($recordLinks) > 0">
          <xsl:for-each select="$recordLinks">
            <parentUuid><xsl:value-of select="@uuidref"/></parentUuid>
            <recordGroup><xsl:value-of select="@uuidref"/></recordGroup>
            <xsl:copy-of select="gn-fn-index:build-record-link(@uuidref, @xlink:href, @xlink:title, 'parent')"/>
            <!--
            TODOES - Need more work with routing
            <recordJoin type="object">{"name": "children", "parent": "<xsl:value-of select="gn-fn-index:json-escape(.)"/>"}</recordLink>-->
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <recordGroup><xsl:value-of select="$identifier"/></recordGroup>
        </xsl:otherwise>
      </xsl:choose>


      <xsl:for-each select=".//mri:associatedResource/*">
        <xsl:variable name="code"
                      select="if (mri:metadataReference/@uuidref != '')
                              then mri:metadataReference/@uuidref
                              else mri:metadataReference/cit:CI_Citation/cit:identifier/mcc:MD_Identifier/mcc:code/*/text()"/>
        <xsl:if test="$code != ''">
          <xsl:variable name="xlink"
                        select="mri:metadataReference/@xlink:href"/>
          <xsl:variable name="associationType"
                        select="mri:associationType/*/@codeListValue"/>
          <xsl:if test="$associationType = $parentAssociatedResourceType">
            <parentUuid><xsl:value-of select="$code"/></parentUuid>
            <xsl:copy-of select="gn-fn-index:build-record-link($code, $xlink, @xlink:title, 'parent')"/>
          </xsl:if>

          <xsl:variable name="initiativeType"
                        select="mri:initiativeType/*/@codeListValue"/>
          <xsl:variable name="properties">
            <properties>
              <p name="associationType" value="{$associationType}"/>
              <p name="initiativeType" value="{$initiativeType}"/>
            </properties>
          </xsl:variable>
          <xsl:copy-of select="gn-fn-index:build-record-link($code, $xlink, @xlink:title, 'siblings', $properties)"/>
          <agg_associated><xsl:value-of select="$code"/></agg_associated>
        </xsl:if>
      </xsl:for-each>

      <xsl:variable name="indexingTimeRecordLink"
                    select="util:getSettingValue('system/index/indexingTimeRecordLink')" />
      <xsl:if test="$indexingTimeRecordLink = 'true'">
        <xsl:variable name="parentUuid"
                      select=".//mri:associatedResource/*[mri:associationType/*/@codeListValue = $parentAssociatedResourceType]/mri:metadataReference/@uuidref[. != '']"/>
        <xsl:variable name="recordsLinks"
                      select="util:getTargetAssociatedResourcesAsNode(
                                        $identifier,
                                        if ($parentUuid) then $parentUuid else mdb:parentMetadata[@uuidref != '']/@uuidref)"/>
        <xsl:copy-of select="$recordsLinks//recordLink"/>
      </xsl:if>
    </doc>

    <!-- Index more documents for this element -->
    <xsl:apply-templates mode="index-extra-documents" select="."/>
  </xsl:template>



  <xsl:template mode="index-contact" match="*[cit:CI_Responsibility]">
    <xsl:param name="fieldSuffix" select="''" as="xs:string"/>

    <xsl:variable name="organisationName"
                  select="(.//cit:CI_Organisation/cit:name/gco:CharacterString)[1]"
                  as="xs:string*"/>
    <xsl:variable name="uuid" select="@uuid"/>
    <xsl:variable name="elementName" select="name()"/>

    <xsl:variable name="role"
                  select="replace(*[1]/cit:role/*/@codeListValue, ' ', '')"
                  as="xs:string?"/>
    <xsl:variable name="logo" select="(.//cit:logo/*/mcc:fileName/*)[1]"/>
    <xsl:variable name="website" select=".//cit:onlineResource/*/cit:linkage/gco:CharacterString"/>
    <xsl:variable name="email"
                  select="(.//cit:contactInfo/*/cit:address/*/cit:electronicMailAddress/gco:CharacterString)[1]"/>
    <xsl:variable name="phone"
                  select="(./cit:contactInfo/*/cit:phone/*/cit:number[normalize-space(.) != '']/*/text())[1]"/>
    <xsl:variable name="individualName"
                  select="(.//cit:individualName/gco:CharacterString/text())[1]"/>
    <xsl:variable name="positionName"
                  select="(.//cit:positionName/gco:CharacterString/text())[1]"/>
    <xsl:variable name="address" select="string-join(.//cit:contactInfo/*/cit:address/*/(
                                        cit:deliveryPoint|cit:postalCode|cit:city|
                                        cit:administrativeArea|cit:country)/gco:CharacterString/text(), ', ')"/>

    <xsl:if test="normalize-space($organisationName) != ''">
      <xsl:if test="count(preceding-sibling::*[name() = $elementName
                        and .//cit:CI_Organisation/cit:name/gco:CharacterString = $organisationName]) = 0">
        <xsl:element name="Org{$fieldSuffix}">
          <xsl:value-of select="$organisationName"/>
        </xsl:element>
      </xsl:if>

      <xsl:if test="count(preceding-sibling::*[name() = $elementName
                      and .//cit:CI_Organisation/cit:name/gco:CharacterString = $organisationName
                      and .//cit:role/*/@codeListValue = $role]) = 0">
        <xsl:element name="{replace($role, '[^a-zA-Z0-9-]', '')}Org{$fieldSuffix}">
          <xsl:value-of select="$organisationName"/>
        </xsl:element>
      </xsl:if>
    </xsl:if>
    <xsl:element name="contact{$fieldSuffix}">
      <!-- TODO: Can be multilingual -->
      <xsl:attribute name="type" select="'object'"/>{
      "organisation":"<xsl:value-of
      select="gn-fn-index:json-escape($organisationName)"/>",
      "role":"<xsl:value-of select="$role"/>",
      "email":"<xsl:value-of select="gn-fn-index:json-escape($email)"/>",
      "website":"<xsl:value-of select="$website"/>",
      "logo":"<xsl:value-of select="$logo"/>",
      "individual":"<xsl:value-of select="gn-fn-index:json-escape($individualName)"/>",
      "position":"<xsl:value-of select="gn-fn-index:json-escape($positionName)"/>",
      "phone":"<xsl:value-of select="gn-fn-index:json-escape($phone)"/>",
      "address":"<xsl:value-of select="gn-fn-index:json-escape($address)"/>"
      }
    </xsl:element>
  </xsl:template>




  <xsl:template name="index-operatesOn">
    <xsl:for-each
      select="*/srv:SV_ServiceIdentification">
      <xsl:for-each select="srv:operatesOn">
        <xsl:variable name="associationType" select="'operatesOn'"/>
        <xsl:variable name="serviceType"
                      select="../srv:serviceType/gco:LocalName"/>
        <!--<xsl:variable name="relatedTo" select="@uuidref"/>-->
        <xsl:variable name="getRecordByIdId">
          <xsl:if test="@xlink:href != ''">
            <xsl:analyze-string select="@xlink:href"
                                regex=".*[i|I][d|D]=([\w\-\.\{{\}}]*).*">
              <xsl:matching-substring>
                <xsl:value-of select="regex-group(1)"/>
              </xsl:matching-substring>
            </xsl:analyze-string>
          </xsl:if>
        </xsl:variable>

        <xsl:variable name="datasetId">
          <xsl:choose>
            <xsl:when test="$getRecordByIdId != ''">
              <xsl:value-of select="$getRecordByIdId"/>
            </xsl:when>
            <xsl:when test="@uuidref != ''">
              <xsl:value-of select="@uuidref"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>

        <xsl:if test="$datasetId != ''">
          <recordOperateOn><xsl:value-of select="$datasetId"/></recordOperateOn>
          <xsl:variable name="xlink"
                        select="@xlink:href"/>

          <xsl:variable name="resolvedDoc">
            <xsl:if test="$processRemoteDocs
                          and $xlink != ''
                          and not(@xlink:title)
                          and not(starts-with($xlink, $siteUrl))">
              <!-- Process remote docs only if it was not encoded with a title and is not a local one.
              - uses @xlink:href to retrieve the remote metadata and index the relevant information for related service.
              - if the metadata is found in the catalogue, it's used that information.

              The xlink: href attribute can contain a URI to the MD_DataIdentification part of the metadata record of the dataset.
              Example:
                   <srv:operatesOn uuidref="c9c62f4f-a8da-438e-a514-5963fb1b047b"
                       xlink:href="https://server/geonetwork/srv/dut/csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;
                       id=c9c62f4f-a8da-438e-a514-5963fb1b047b#MD_DataIdentification"/>
              Ignore it for indexing.
           -->
              <xsl:variable name="xlinkHref" select="tokenize(@xlink:href, '#')[1]" />

              <!-- remote url: request the document to index data -->
              <xsl:variable name="remoteDoc" select="util:getUrlContent(@xlink:href)" />

              <!-- Remote url that uuid is stored also locally: Use local.
               Remote is supposed to be ISO19139 or 115-3. -->
              <xsl:variable name="datasetUuid"
                            select="$remoteDoc//(*[local-name(.) = 'fileIdentifier']/*/text()|
                                                 *[local-name(.) = 'metadataIdentifier']/*/*[local-name(.) = 'code']/*/text())" />

              <xsl:if test="count($datasetUuid) = 1
                            and string($datasetUuid)">
                <xsl:variable name="existsLocally"
                              select="not(normalize-space(util:getRecord($datasetUuid)) = '')" />

                <xsl:if test="not($existsLocally)">
                  <xsl:variable name="datasetTitle"
                                select="$remoteDoc//*[local-name(.) = 'identificationInfo']/*
                                                    /*[local-name(.) = 'citation']/*
                                                    /*[local-name(.) = 'title']/*/text()" />

                  <xsl:copy-of select="gn-fn-index:build-record-link($datasetUuid, $xlinkHref, $datasetTitle, 'datasets')"/>
                </xsl:if>
              </xsl:if>
            </xsl:if>
          </xsl:variable>

          <xsl:choose>
            <xsl:when test="$resolvedDoc != ''">
              <xsl:copy-of select="$resolvedDoc"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:copy-of select="gn-fn-index:build-record-link($datasetId, $xlink, @xlink:title, 'datasets')"/>
            </xsl:otherwise>
          </xsl:choose>
          <!--
            TODOES - Need more work with routing -->
          <!--          <recordLink type="object">{"name": "dataset", "parent": "<xsl:value-of select="gn-fn-index:json-escape(.)"/>"}</recordLink>-->
        </xsl:if>
      </xsl:for-each>


      <xsl:choose>
        <!-- Default to index the @uuidref value for operatesOn:
             assumes a local metadata with that uuid exists -->
        <xsl:when test="not($processRemoteDocs)">
          <xsl:for-each select="srv:operatesOn/@uuidref">
            <operatesOn><xsl:value-of select="."/></operatesOn>
          </xsl:for-each>
          <xsl:for-each select="srv:operatesOn/@xlink:href">
            <operatesOn><xsl:value-of select="."/></operatesOn>
          </xsl:for-each>
        </xsl:when>

        <!-- Process remote docs:
                - uses @xlink:href to retrieve the remote metadata and index the relevant information for related service.
                - if the metadata is found in the catalogue, it's used that information.
                Index field format (Metadata in local catalogue):  uuid|L|uuid||link
                Index field format (Metadata in remote catalogue): uuid|R|title|abstract|link
        -->
        <xsl:otherwise>
          <xsl:for-each select="srv:operatesOn">
            <!-- The xlink: href attribute can contain a URI to the MD_DataIdentification part of the metadata record of the dataset.
                Example:
                   <srv:operatesOn uuidref="c9c62f4f-a8da-438e-a514-5963fb1b047b"
                       xlink:href="https://server/geonetwork/srv/dut/csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;
                       id=c9c62f4f-a8da-438e-a514-5963fb1b047b#MD_DataIdentification"/>
                Ignore it for indexing.
           -->

            <xsl:variable name="siteUrl" select="util:getSiteUrl()" />
            <xsl:variable name="xlinkHref" select="tokenize(@xlink:href, '#')[1]" />

            <xsl:choose>
              <!-- 1) Is the link referencing an external metadata? -->
              <xsl:when test="string(normalize-space($xlinkHref))
                              and not(starts-with(replace($xlinkHref, 'http://', 'https://'), replace($siteUrl, 'http://', 'https://')))">

                <!-- remote url: request the document to index data -->
                <xsl:variable name="remoteDoc"
                              select="util:getUrlContent(@xlink:href)" />

                <!-- Remote url that uuid is stored also locally: Use local -->
                <xsl:variable name="datasetUuid"
                              select="$remoteDoc//mdb:metadataIdentifier/mcc:MD_Identifier/mcc:code/gco:CharacterString[. != '']" />
                <xsl:choose>
                  <xsl:when test="count($datasetUuid) = 1
                                  and string($datasetUuid)">
                    <xsl:variable name="existsLocally"
                                  select="not(normalize-space(util:getRecord($datasetUuid)) = '')" />

                    <xsl:choose>
                      <xsl:when test="not($existsLocally)">
                        <xsl:variable name="datasetTitle"
                                      select="$remoteDoc//*[mri:MD_DataIdentification or @gco:isoType='mri:MD_DataIdentification']/*/mri:citation/*/cit:title/gco:CharacterString" />

                        <xsl:variable name="datasetAbstract"
                                      select="$remoteDoc//*[mri:MD_DataIdentification or @gco:isoType='gmd:MD_DataIdentification']/*/mri:abstract/gco:CharacterString" />

                        <operatesOn><xsl:value-of select="concat($datasetUuid, '|R|', normalize-space($datasetTitle), '|', normalize-space($datasetAbstract), '|', $xlinkHref)"/></operatesOn>
                      </xsl:when>
                      <!-- Do we need this check? maybe in this case use operatesOn instead of operatesOnRemote to use local info? -->
                      <xsl:otherwise>

                        <xsl:variable name="datasetTitle"
                                      select="$remoteDoc//*[mri:MD_DataIdentification or @gco:isoType='mri:MD_DataIdentification']/*/mri:citation/cit:title/gco:CharacterString" />
                        <xsl:variable name="datasetAbstract" select="$remoteDoc//*[mri:MD_DataIdentification or @gco:isoType='mri:MD_DataIdentification']/*/mri:abstract/gco:CharacterString" />

                        <operatesOn><xsl:value-of
                          select="concat($datasetUuid, '|R|', normalize-space($datasetTitle), '|', normalize-space($datasetAbstract), '|', $xlinkHref)"/></operatesOn>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:when>

                  <xsl:otherwise>
                    <xsl:variable name="uuidFromCsw"
                                  select="tokenize(tokenize(string($xlinkHref),'&amp;id=')[2],'&amp;')[1]" />

                    <xsl:choose>
                      <!-- Assume is a CSW request and extract the uuid from csw request and add as operatesOnRemote -->
                      <xsl:when test="string($uuidFromCsw)">
                        <operatesOn><xsl:value-of select="concat($uuidFromCsw, '|R|', $uuidFromCsw,'|', '|', $xlinkHref)"/></operatesOn>
                      </xsl:when>

                      <!-- If no CSW request, store the link -->
                      <xsl:otherwise>
                        <operatesOn><xsl:value-of select="concat($xlinkHref, '|R|', $xlinkHref, '|', '|', $xlinkHref)"/></operatesOn>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>

              <!-- 2) Is the link referencing to a metadata in the catalogue? -->
              <xsl:otherwise>
                <!-- Extract the uuid from the link, assuming it's a CSW url -->
                <xsl:variable name="uuidFromCsw"  select="tokenize(tokenize(string($xlinkHref),'&amp;id=')[2],'&amp;')[1]" />

                <xsl:choose>
                  <!-- The uuid could be extracted from the url (CSW url)-->
                  <xsl:when test="string($uuidFromCsw)">
                    <operatesOn><xsl:value-of select="concat($uuidFromCsw, '|L|', $uuidFromCsw,'|', '|', $xlinkHref)"/></operatesOn>
                  </xsl:when>

                  <!-- If no CSW url, store the link  with the uuid from uuidref attribute-->
                  <xsl:otherwise>
                    <operatesOn><xsl:value-of select="concat(@uuidref, '|L|', @uuidref, '|', '|', $xlinkHref)"/></operatesOn>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>


  <!-- For each record, the main mode 'index' is called,
  -  then in the document node the mode 'index-extra-fields'
  -  could be used to index more fields. -->
  <xsl:template mode="index-extra-fields" match="mdb:MD_Metadata">

  </xsl:template>
</xsl:stylesheet>
