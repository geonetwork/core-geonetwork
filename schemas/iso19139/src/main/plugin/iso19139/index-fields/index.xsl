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
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn-fn-index="http://geonetwork-opensource.org/xsl/functions/index"
                xmlns:index="java:org.fao.geonet.kernel.search.EsSearchManager"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:date-util="java:org.fao.geonet.utils.DateUtil"
                xmlns:daobs="http://daobs.org"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:import href="fn.xsl"/>
  <xsl:import href="common/inspire-constant.xsl"/>
  <xsl:import href="common/index-utils.xsl"/>

  <xsl:output name="default-serialize-mode"
              indent="no"
              omit-xml-declaration="yes"
              encoding="utf-8"
              escape-uri-attributes="yes"/>

  <xsl:param name="fastIndexMode" select="false()"/>

  <!-- If identification creation, publication and revision date
    should be indexed as a temporal extent information (eg. in INSPIRE
    metadata implementing rules, those elements are defined as part
    of the description of the temporal extent). -->
  <xsl:variable name="useDateAsTemporalExtent" select="true()"/>

  <!-- Define if operatesOn type should be defined
  by analysis of protocol in all transfers options.
  -->
  <xsl:variable name="operatesOnSetByProtocol" select="false()"/>

  <xsl:variable name="processRemoteDocs" select="false()" />

  <!-- Parent may be encoded using an associatedResource.
  Define which association type should be considered as parent. -->
  <xsl:variable name="parentAssociatedResourceType" select="'partOfSeamlessDatabase'"/>
  <xsl:variable name="childrenAssociatedResourceType" select="'isComposedOf'"/>

  <xsl:template match="/">
    <xsl:apply-templates mode="index"/>
  </xsl:template>

  <xsl:template match="gmi:MI_Metadata|gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']"
                mode="extract-uuid">
    <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/>
  </xsl:template>

  <xsl:variable name="siteUrl" select="util:getSiteUrl()" />

  <xsl:template mode="index-extra-fields" match="*"/>

  <xsl:template mode="index-extra-documents" match="*"/>

  <xsl:template match="gmi:MI_Metadata|gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']"
                mode="index">
    <!-- Main variables for the document

    TODO: GN does not assign UUIDs to template. Maybe it should ?
      XTTE0570: An empty sequence is not allowed as the value of variable $identifier
    -->
    <xsl:variable name="identifier" as="xs:string?"
                  select="gmd:fileIdentifier/gco:CharacterString[. != '']"/>


    <!-- In ISO19139 consider datestamp element the last update date
    even if the standard says creation date. Most of the catalog implementations
    update the datestamp on change. -->
    <xsl:variable name="lastRevisionDate" as="xs:string?"
                  select="gmd:dateStamp[1]/gco:DateTime[gn-fn-index:is-isoDate(.)]"/>

    <xsl:variable name="mainLanguageCode" as="xs:string?"
                  select="gmd:language[1]/gmd:LanguageCode/
                        @codeListValue[normalize-space(.) != '']"/>

    <xsl:variable name="mainLanguage" as="xs:string?"
                  select="if ($mainLanguageCode) then $mainLanguageCode else
                    gmd:language[1]/gco:CharacterString[normalize-space(.) != '']"/>

    <xsl:variable name="otherLanguages" as="attribute()*"
                  select="gmd:locale/gmd:PT_Locale/
                        gmd:languageCode/gmd:LanguageCode/
                          @codeListValue[normalize-space(.) != '']"/>
    <xsl:variable name="allLanguages">
      <lang id="default" value="{$mainLanguage}"/>
      <xsl:for-each select="$otherLanguages">
        <lang id="{../../../@id}" value="{.}"/>
      </xsl:for-each>
    </xsl:variable>

    <!-- Record is dataset if no hierarchyLevel -->
    <xsl:variable name="isDataset" as="xs:boolean"
                  select="
                      count(gmd:hierarchyLevel[gmd:MD_ScopeCode/@codeListValue='dataset']) > 0 or
                      count(gmd:hierarchyLevel) = 0"/>

    <xsl:variable name="isService" as="xs:boolean"
                  select="
                      count(gmd:hierarchyLevel[gmd:MD_ScopeCode/@codeListValue='service']) > 0"/>

    <!-- Create a first document representing the main record. -->
    <doc>

      <xsl:copy-of select="gn-fn-index:add-field('docType', 'metadata')"/>

      <!-- Index the metadata document as XML -->
      <document>
        <!--<xsl:value-of select="saxon:serialize(., 'default-serialize-mode')"/>-->
      </document>

      <xsl:copy-of select="gn-fn-index:add-field('metadataIdentifier', $identifier)"/>

      <xsl:for-each select="gmd:metadataStandardName">
        <xsl:copy-of select="gn-fn-index:add-multilingual-field('standardName', ., $allLanguages)"/>
      </xsl:for-each>

      <xsl:for-each select="gmd:metadataStandardVersion">
        <xsl:copy-of select="gn-fn-index:add-multilingual-field('standardVersion', ., $allLanguages)"/>
      </xsl:for-each>

      <xsl:for-each select="gmd:hierarchyLevelName">
        <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceTypeName', ., $allLanguages)"/>
      </xsl:for-each>

      <!-- Since GN sets the timezone in system/server/timeZone setting as Java system default
        timezone we can rely on XSLT functions to get current date in the right timezone -->
      <indexingDate>
        <xsl:value-of select="format-dateTime(current-dateTime(), $dateFormat)"/>
      </indexingDate>

      <!-- Indexing record information -->
      <!-- # Date -->
      <!-- TODO improve date formatting maybe using Joda parser
      Select first one because some records have 2 dates !
      eg. fr-784237539-bdref20100101-0105

      Remove millisec and timezone until not supported
      eg. 2017-02-08T13:18:03.138+00:02
      -->
      <xsl:for-each select="(gmd:dateStamp/*[gn-fn-index:is-isoDate(.)])[1]">
        <dateStamp><xsl:value-of select="date-util:convertToISOZuluDateTime(normalize-space(.))"/></dateStamp>
      </xsl:for-each>


      <!-- # Languages -->
      <xsl:copy-of select="gn-fn-index:add-field('mainLanguage', $mainLanguage)"/>

      <xsl:for-each select="$otherLanguages">
        <xsl:copy-of select="gn-fn-index:add-field('otherLanguage', .)"/>
        <xsl:copy-of select="gn-fn-index:add-field('otherLanguageId', ../../../@id)"/>
      </xsl:for-each>


      <xsl:for-each select="gmd:characterSet/*[@codeListValue != '']">
        <xsl:copy-of select="gn-fn-index:add-codelist-field(
                                  'cl_characterSet', ., $allLanguages)"/>
      </xsl:for-each>

      <!-- # Resource type -->
      <xsl:choose>
        <xsl:when test="$isDataset">
          <resourceType>dataset</resourceType>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="gmd:hierarchyLevel/*/@codeListValue[normalize-space(.) != '']">
            <resourceType>
              <xsl:value-of select="."/>
            </resourceType>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:variable name="isMapDigital"
                    select="count(gmd:identificationInfo/*/gmd:citation/*/gmd:presentationForm[*/@codeListValue = 'mapDigital']) > 0"/>
      <xsl:variable name="isStatic"
                    select="count(gmd:distributionInfo/*/gmd:distributionFormat/*/gmd:name/*[contains(., 'PDF') or contains(., 'PNG') or contains(., 'JPEG')]) > 0"/>
      <xsl:variable name="isInteractive"
                    select="count(gmd:distributionInfo/*/gmd:distributionFormat/*/gmd:name/*[contains(., 'OGC:WMC') or contains(., 'OGC:OWS-C')]) > 0"/>
      <xsl:variable name="isPublishedWithWMCProtocol"
                    select="count(gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/*/gmd:protocol[starts-with(gco:CharacterString, 'OGC:WMC')]) > 0"/>

      <xsl:choose>
        <xsl:when test="$isDataset and $isMapDigital and
                            ($isStatic or $isInteractive or $isPublishedWithWMCProtocol)">
          <resourceType>map</resourceType>
          <xsl:choose>
            <xsl:when test="$isStatic">
              <resourceType>map/static</resourceType>
            </xsl:when>
            <xsl:when test="$isInteractive or $isPublishedWithWMCProtocol">
              <resourceType>map/interactive</resourceType>
            </xsl:when>
          </xsl:choose>
        </xsl:when>
      </xsl:choose>



      <!-- Indexing metadata contact -->
      <xsl:apply-templates mode="index-contact" select="gmd:contact">
        <xsl:with-param name="fieldSuffix" select="''"/>
        <xsl:with-param name="languages" select="$allLanguages"/>
      </xsl:apply-templates>

      <!-- Indexing all codelists.

        Exclude some codelist like
        Contact role, Date type indexed in other fields.
      -->
      <xsl:for-each-group select=".//*[@codeListValue != '' and
                            name() != 'gmd:CI_RoleCode' and
                            name() != 'gmd:CI_DateTypeCode' and
                            name() != 'gmd:MD_CharacterSetCode' and
                            name() != 'gmd:LanguageCode'
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
      <xsl:for-each select="gmd:identificationInfo[1]/*[1]">
        <xsl:for-each select="gmd:citation/gmd:CI_Citation">

          <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceTitle', gmd:title, $allLanguages)"/>
          <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceAltTitle', gmd:alternateTitle, $allLanguages)"/>

          <xsl:for-each select="gmd:date/gmd:CI_Date[gn-fn-index:is-isoDate(gmd:date/*/text())]">
            <xsl:variable name="dateType"
                          select="gmd:dateType[1]/gmd:CI_DateTypeCode/@codeListValue"
                          as="xs:string?"/>
            <xsl:variable name="date"
                          select="string(gmd:date[1]/gco:Date|gmd:date[1]/gco:DateTime)"/>

            <xsl:variable name="zuluDateTime" as="xs:string?">
              <xsl:if test="gn-fn-index:is-isoDate($date)">
                <xsl:value-of select="date-util:convertToISOZuluDateTime(normalize-space($date))"/>
              </xsl:if>
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


          <xsl:for-each select="gmd:date/gmd:CI_Date[gn-fn-index:is-isoDate(gmd:date/*/text())]">
            <xsl:variable name="dateType"
                          select="gmd:dateType[1]/gmd:CI_DateTypeCode/@codeListValue"
                          as="xs:string?"/>
            <xsl:variable name="date"
                          select="string(gmd:date[1]/gco:Date|gmd:date[1]/gco:DateTime)"/>

            <xsl:variable name="zuluDate"
                          select="date-util:convertToISOZuluDateTime($date)"/>
            <xsl:if test="$zuluDate != ''">
              <resourceDate type="object">
                {"type": "<xsl:value-of select="$dateType"/>", "date": "<xsl:value-of select="$zuluDate"/>"}
              </resourceDate>
            </xsl:if>
          </xsl:for-each>

          <xsl:if test="$useDateAsTemporalExtent">
            <xsl:for-each-group select="gmd:date/gmd:CI_Date[gn-fn-index:is-isoDate(gmd:date/*/text())]/gmd:date/*/text()"
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

          <xsl:for-each select="gmd:identifier/*[string(gmd:code/*)]">
            <resourceIdentifier type="object">{
              "code": "<xsl:value-of select="util:escapeForJson(gmd:code/(gco:CharacterString|gmx:Anchor))"/>",
              "codeSpace": "<xsl:value-of select="gmd:codeSpace/(gco:CharacterString|gmx:Anchor)"/>",
              "link": "<xsl:value-of select="gmd:code/gmx:Anchor/@xlink:href"/>"
              }</resourceIdentifier>
          </xsl:for-each>

          <xsl:for-each select="gmd:edition/*">
            <xsl:copy-of select="gn-fn-index:add-field('resourceEdition', .)"/>
          </xsl:for-each>
        </xsl:for-each>

        <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceAbstract', gmd:abstract, $allLanguages)"/>

        <xsl:for-each select="gmd:characterSet/*[@codeListValue != '']">
          <xsl:copy-of select="gn-fn-index:add-codelist-field(
                                  'cl_resourceCharacterSet', ., $allLanguages)"/>
        </xsl:for-each>

        <!-- Indexing resource contact -->
        <xsl:apply-templates mode="index-contact"
                             select="gmd:pointOfContact">
          <xsl:with-param name="fieldSuffix" select="'ForResource'"/>
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:apply-templates>

        <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceCredit', gmd:credit, $allLanguages)"/>
        <xsl:copy-of select="gn-fn-index:add-multilingual-field('supplementalInformation', gmd:supplementalInformation, $allLanguages)"/>
        <xsl:copy-of select="gn-fn-index:add-multilingual-field('purpose', gmd:purpose, $allLanguages)"/>

        <xsl:variable name="overviews"
                      select="gmd:graphicOverview/gmd:MD_BrowseGraphic/
                              gmd:fileName/gco:CharacterString[. != '']"/>
        <xsl:copy-of select="gn-fn-index:add-field('hasOverview', if (count($overviews) > 0) then 'true' else 'false')"/>


        <xsl:for-each select="$overviews">
          <!-- TODO can be multilingual desc and name -->
          <overview type="object">{
            "url": "<xsl:value-of select="normalize-space(.)"/>"
            <xsl:if test="normalize-space(../../gmd:fileDescription) != ''">,
              "nameObject": <xsl:value-of select="gn-fn-index:add-multilingual-field('name', ../../gmd:fileDescription, $allLanguages, true())"/>
            </xsl:if>
            }</overview>
        </xsl:for-each>

        <xsl:for-each
          select="gmd:language/(gco:CharacterString|gmd:LanguageCode/@codeListValue)">
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
          <!-- TODO: Some MS may be using a translated version of the thesaurus title -->
          <xsl:variable name="inspireKeywords"
                        select="*/gmd:MD_Keywords[
                      contains(lower-case(
                       gmd:thesaurusName[1]/*/gmd:title[1]/*[1]/text()
                       ), 'gemet') and
                       contains(lower-case(
                       gmd:thesaurusName[1]/*/gmd:title[1]/*[1]/text()
                       ), 'inspire')]
                  /gmd:keyword"/>
          <xsl:for-each
            select="$inspireKeywords">
            <xsl:variable name="position" select="position()"/>
            <xsl:for-each select="gco:CharacterString[. != '']|
                                gmx:Anchor[. != '']">
              <xsl:variable name="inspireTheme" as="xs:string"
                            select="index:analyzeField('synInspireThemes', text())"/>

              <inspireTheme_syn>
                <xsl:value-of select="text()"/>
              </inspireTheme_syn>
              <inspireTheme>
                <xsl:value-of select="$inspireTheme"/>
              </inspireTheme>
              <!-- TODOES: Add Acronym -->
              <!--
              WARNING: Here we only index the first keyword in order
              to properly compute one INSPIRE annex.
              -->
              <xsl:if test="$position = 1">
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
                </xsl:if>
              </xsl:if>
              <xsl:if test="$inspireTheme != ''">
                <inspireAnnex>
                  <xsl:value-of
                    select="index:analyzeField('synInspireAnnexes', $inspireTheme)"/>
                </inspireAnnex>
                <xsl:variable name="inspireThemeUri" as="xs:string"
                              select="index:analyzeField('synInspireThemeUris', $inspireTheme)"/>
                <inspireThemeUri>
                  <xsl:value-of select="$inspireThemeUri"/>
                </inspireThemeUri>
              </xsl:if>
            </xsl:for-each>
          </xsl:for-each>

          <!-- For services, the count does not take into account
          dataset's INSPIRE themes which are transfered to the service
          by service-dataset-task. -->
          <inspireThemeNumber>
            <xsl:value-of
              select="count($inspireKeywords)"/>
          </inspireThemeNumber>

          <hasInspireTheme>
            <xsl:value-of
              select="if (count($inspireKeywords) > 0) then 'true' else 'false'"/>
          </hasInspireTheme>
        </xsl:if>

        <xsl:variable name="allKeywords">
          <xsl:for-each-group select="*/gmd:MD_Keywords"
                              group-by="concat(gmd:thesaurusName/*/gmd:title/(gco:CharacterString|gmx:Anchor)/text(), '-', gmd:type/*/@codeListValue[. != ''])">
            <xsl:sort select="current-grouping-key()"/>

            <xsl:variable name="thesaurusType"
                          select="gmd:type/*/@codeListValue[. != '']"/>

            <xsl:variable name="thesaurusTitle"
                          select="if (starts-with(current-grouping-key(), '-'))
                                  then concat('otherKeywords', current-grouping-key())
                                  else gmd:thesaurusName/*/gmd:title/(gco:CharacterString|gmx:Anchor)/text()"/>

            <xsl:variable name="thesaurusRef"
                          select="gmd:thesaurusName/gmd:CI_Citation/
                                        gmd:identifier[position() = 1]/gmd:MD_Identifier/
                                          gmd:code/(gco:CharacterString|gmx:Anchor)"/>

            <xsl:variable name="thesaurusId"
                          select="if ($thesaurusRef != '')
                                  then normalize-space($thesaurusRef/text())
                                  else util:getThesaurusIdByTitle($thesaurusTitle)"/>

            <xsl:variable name="thesaurusUri"
                          select="$thesaurusRef/@xlink:href"/>

            <xsl:variable name="thesaurusFieldName"
                          select="gn-fn-index:build-thesaurus-index-field-name($thesaurusId, $thesaurusTitle)"/>

            <xsl:variable name="keywords"
                          select="gmd:keyword[*/normalize-space() != '']"/>

            <thesaurus>
              <info type="{$thesaurusType}"
                    field="{$thesaurusFieldName}"
                    id="{$thesaurusId}"
                    uri="{$thesaurusUri}"
                    title="{$thesaurusTitle}">
              </info>
              <keywords>
                <xsl:for-each select="$keywords">
                  <keyword>
                    <xsl:variable name="keywordUri"
                                  select="if (gmx:Anchor/@xlink:href[. != ''])
                                          then gmx:Anchor/@xlink:href
                                          else util:getKeywordUri(
                                                (*/text())[1],
                                                $thesaurusId,
                                                $allLanguages/lang[@id = 'default']/@value)"/>
                    <xsl:attribute name="uri"
                                   select="$keywordUri"/>
                    <values>
                      <xsl:copy-of select="gn-fn-index:add-multilingual-field('keyword',
                          ., $allLanguages, false(), true())"/>
                    </values>

                    <!--  If keyword is related to a thesaurus available
                    in current catalogue, checked the keyword exists in the thesaurus.
                    If not, report an error in indexingErrorMsg field.

                    This case may trigger editor warning message when a keyword is not
                     found in the thesaurus. Try to anticipate this and advertise those
                     records in the admin. -->
                    <xsl:if test="$thesaurusId != '' and $keywordUri = ''">
                      <errors>
                        <indexingErrorMsg>Warning / Keyword <xsl:value-of select="(*/text())[1]"/> not found in <xsl:value-of select="$thesaurusId"/>.</indexingErrorMsg>
                      </errors>
                    </xsl:if>

                    <tree>
                      <defaults>
                        <xsl:call-template name="get-keyword-tree-values">
                          <xsl:with-param name="keyword"
                                          select="(*/text())[1]"/>
                          <xsl:with-param name="thesaurus"
                                          select="$thesaurusId"/>
                          <xsl:with-param name="language"
                                          select="$allLanguages/lang[@id = 'default']/@value"/>
                        </xsl:call-template>
                      </defaults>
                      <xsl:if test="$keywordUri != ''">
                        <keys>
                          <xsl:call-template name="get-keyword-tree-values">
                            <xsl:with-param name="keyword"
                                            select="$keywordUri"/>
                            <xsl:with-param name="thesaurus"
                                            select="$thesaurusId"/>
                            <xsl:with-param name="language"
                                            select="$allLanguages/lang[@id = 'default']/@value"/>
                          </xsl:call-template>
                        </keys>
                      </xsl:if>
                    </tree>
                  </keyword>
                </xsl:for-each>
              </keywords>
            </thesaurus>
          </xsl:for-each-group>

          <xsl:variable name="geoDescription"
                        select="//gmd:geographicElement/*/gmd:geographicIdentifier/
                                  */gmd:code[*/normalize-space(.) != '']
                                |//gmd:EX_Extent/gmd:description[*/normalize-space(.) != '']"/>
          <xsl:if test="$geoDescription">
            <thesaurus>
              <info type="place"/>
              <keywords>
                <xsl:for-each select="$geoDescription">
                  <keyword>
                    <values>
                      <xsl:copy-of select="gn-fn-index:add-multilingual-field('keyword',
                          ., $allLanguages, false(), true())"/>
                    </values>
                  </keyword>
                </xsl:for-each>
              </keywords>
            </thesaurus>
          </xsl:if>
        </xsl:variable>

        <xsl:call-template name="build-all-keyword-fields">
          <xsl:with-param name="allKeywords" select="$allKeywords"/>
        </xsl:call-template>


        <xsl:for-each select="gmd:topicCategory/gmd:MD_TopicCategoryCode[string(.)]">
          <xsl:variable name="value" as="node()">
            <xsl:copy>
              <xsl:attribute name="codeListValue" select="."/>
            </xsl:copy>
          </xsl:variable>
          <xsl:copy-of select="gn-fn-index:add-codelist-field(
                                'cl_topic', $value, $allLanguages)"/>
        </xsl:for-each>


        <xsl:for-each select="gmd:spatialResolution/gmd:MD_Resolution">
          <xsl:for-each
            select="gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer[. castable as xs:decimal]">
            <resolutionScaleDenominator>
              <xsl:value-of select="."/>
            </resolutionScaleDenominator>
          </xsl:for-each>

          <xsl:for-each select="gmd:distance/gco:Distance[. != '']">
            <resolutionDistance>
              <xsl:value-of select="if (contains(@uom, '#'))
                                    then concat(., ' ', tokenize(@uom, '#')[2])
                                    else  concat(., ' ', @uom)"/>
            </resolutionDistance>
          </xsl:for-each>
        </xsl:for-each>


        <xsl:for-each select="gmd:resourceConstraints/*">
          <xsl:variable name="fieldPrefix" select="local-name()"/>
          <xsl:for-each select="gmd:otherConstraints">
            <xsl:copy-of select="gn-fn-index:add-multilingual-field(concat($fieldPrefix, 'OtherConstraints'), ., $allLanguages)"/>
          </xsl:for-each>
          <xsl:for-each select="gmd:useLimitation">
            <xsl:copy-of select="gn-fn-index:add-multilingual-field(concat($fieldPrefix, 'UseLimitation'), ., $allLanguages)"/>
          </xsl:for-each>
        </xsl:for-each>

        <xsl:for-each select="gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints">
          <xsl:copy-of select="gn-fn-index:add-multilingual-field('license', ., $allLanguages)"/>
        </xsl:for-each>

        <xsl:if test="*/gmd:EX_Extent/*/gmd:EX_BoundingPolygon/gmd:polygon">
          <hasBoundingPolygon>true</hasBoundingPolygon>
        </xsl:if>

        <xsl:for-each select="*/gmd:EX_Extent/*/gmd:EX_BoundingPolygon/gmd:polygon">
          <xsl:variable name="geojson"
                        select="util:gmlToGeoJson(
                                  saxon:serialize((gml:*|gml320:*), 'default-serialize-mode'),
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

        <xsl:for-each select="*/gmd:EX_Extent">
          <xsl:copy-of select="gn-fn-index:add-multilingual-field('extentDescription', gmd:description, $allLanguages)"/>

          <xsl:for-each select=".//gmd:geographicIdentifier">
            <xsl:copy-of select="gn-fn-index:add-multilingual-field('extentIdentifier', */gmd:code, $allLanguages)"/>
          </xsl:for-each>

          <xsl:variable name="bboxes"
                        select=".//gmd:EX_GeographicBoundingBox[
                                ./gmd:westBoundLongitude/gco:Decimal castable as xs:decimal and
                                ./gmd:eastBoundLongitude/gco:Decimal castable as xs:decimal and
                                ./gmd:northBoundLatitude/gco:Decimal castable as xs:decimal and
                                ./gmd:southBoundLatitude/gco:Decimal castable as xs:decimal
                                ]"/>
          <xsl:for-each select="$bboxes">
            <xsl:variable name="format" select="'#0.000000'"></xsl:variable>

            <xsl:variable name="w"
                          select="format-number(./gmd:westBoundLongitude/gco:Decimal/text(), $format)"/>
            <xsl:variable name="e"
                          select="format-number(./gmd:eastBoundLongitude/gco:Decimal/text(), $format)"/>
            <xsl:variable name="n"
                          select="format-number(./gmd:northBoundLatitude/gco:Decimal/text(), $format)"/>
            <xsl:variable name="s"
                          select="format-number(./gmd:southBoundLatitude/gco:Decimal/text(), $format)"/>

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
                    <geom type="object">
                      <xsl:text>{"type": "Point", "coordinates": </xsl:text>
                      <xsl:value-of select="concat('[', $w, ',', $s, ']}')"/>
                    </geom>
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

          <xsl:for-each select=".//gmd:temporalElement/*/gmd:extent/(gml:TimePeriod|gml320:TimePeriod)">
            <xsl:variable name="start"
                          select="gml:beginPosition
                                  |gml:begin/gml:TimeInstant/gml:timePosition
                                  |gml320:beginPosition
                                  |gml320:begin/gml320:TimeInstant/gml320:timePosition"/>
            <xsl:variable name="end"
                          select="gml:endPosition
                                  |gml:end/gml:TimeInstant/gml:timePosition
                                  |gml320:endPosition
                                  |gml320:end/gml320:TimeInstant/gml320:timePosition"/>

            <xsl:variable name="zuluStartDate"
                          select="date-util:convertToISOZuluDateTime($start)"/>
            <xsl:variable name="zuluEndDate"
                          select="date-util:convertToISOZuluDateTime($end)"/>

            <xsl:choose>
              <xsl:when test="$zuluStartDate castable as xs:dateTime
                              and ($zuluEndDate  castable as xs:dateTime or $end/@indeterminatePosition = 'now')">
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
              </xsl:when>
              <xsl:otherwise>
                <indexingErrorMsg>Warning / Field resourceTemporalDateRange / Lower and upper bounds empty or not valid dates. Date range not indexed.</indexingErrorMsg>
              </xsl:otherwise>
            </xsl:choose>

            <xsl:if test="$zuluStartDate castable as xs:dateTime
                          and $zuluEndDate  castable as xs:dateTime
                          and $start &gt; $end">
              <indexingErrorMsg>Warning / Field resourceTemporalDateRange / Lower range bound '<xsl:value-of select="$start"/>' can not be greater than upper bound '<xsl:value-of select="$end"/>'.</indexingErrorMsg>
            </xsl:if>

            <xsl:call-template name="build-range-details">
              <xsl:with-param name="start" select="$start"/>
              <xsl:with-param name="end" select="$end"/>
            </xsl:call-template>
          </xsl:for-each>

          <xsl:for-each select=".//gmd:verticalElement/*">
            <xsl:variable name="min"
                          select="gmd:minimumValue/*/text()"/>
            <xsl:variable name="max"
                          select="gmd:maximumValue/*/text()"/>

            <xsl:if test="$min castable as xs:double">
              <resourceVerticalRange type="object">{
                "gte": <xsl:value-of select="normalize-space($min)"/>
                <xsl:if test="$max castable as xs:double
                              and xs:double($min) &lt; xs:double($max)">
                  ,"lte": <xsl:value-of select="normalize-space($max)"/>
                </xsl:if>
                }</resourceVerticalRange>
            </xsl:if>
          </xsl:for-each>
        </xsl:for-each>



        <!-- Service information -->
        <xsl:for-each select="srv:serviceType/gco:LocalName[string(text())]">
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
        </xsl:for-each>

        <xsl:for-each select="srv:serviceTypeVersion">
          <serviceTypeVersion><xsl:value-of select="gco:CharacterString/text()"/></serviceTypeVersion>
        </xsl:for-each>
      </xsl:for-each>


      <xsl:for-each select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem">
        <xsl:for-each select="gmd:referenceSystemIdentifier/gmd:RS_Identifier">
          <xsl:variable name="crs" select="(gmd:code/*/text())[1]"/>
          <xsl:variable name="crsLabel"
                        select="if (gmd:code/*/@xlink:title)
                                then gmd:code/*/@xlink:title
                                else $crs"/>

          <xsl:if test="$crs != ''">
            <coordinateSystem>
              <xsl:value-of select="$crs"/>
            </coordinateSystem>
          </xsl:if>

          <crsDetails type="object">{
            "code": "<xsl:value-of select="util:escapeForJson((gmd:code/*/text())[1])"/>",
            "codeSpace": "<xsl:value-of select="util:escapeForJson((gmd:codeSpace/*/text())[1])"/>",
            "name": "<xsl:value-of select="util:escapeForJson($crsLabel)"/>",
            "url": "<xsl:value-of select="util:escapeForJson(gmd:code/*/@xlink:href)"/>"
            }</crsDetails>
        </xsl:for-each>
      </xsl:for-each>


      <!-- INSPIRE Conformity -->
      <xsl:variable name="legalTextList"
                    select="if ($isService) then $eu9762009 else $eu10892010"/>

      <xsl:for-each-group select="gmd:dataQualityInfo/*/gmd:report/*/gmd:result"
                          group-by="*/gmd:specification/gmd:CI_Citation/
    gmd:title/(gco:CharacterString|gmx:Anchor)">
        <xsl:variable name="title" select="current-grouping-key()"/>
        <xsl:variable name="matchingEUText"
                      select="if ($inspireRegulationLaxCheck)
                              then daobs:search-in-contains($legalTextList/*, $title)
                              else daobs:search-in($legalTextList/*, $title)"/>

        <xsl:variable name="pass"
                      select="*/gmd:pass/gco:Boolean"/>

        <xsl:if test="count($matchingEUText) = 1">
          <inspireConformResource>
            <xsl:value-of select="$pass"/>
          </inspireConformResource>
        </xsl:if>

        <xsl:if test="string($title)">
          <specificationConformance type="object">{
            "title": "<xsl:value-of select="util:escapeForJson($title)" />",
            <xsl:if test="gn-fn-index:is-isoDate((*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date)[1])">
              "date": "<xsl:value-of select="(*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date)[1]" />",
            </xsl:if>
            <xsl:if test="*/gmd:specification/*/gmd:title/*/@xlink:href">
              "link": "<xsl:value-of select="*/gmd:specification/*/gmd:title/*/@xlink:href"/>",
            </xsl:if>
            <xsl:if test="*/gmd:explanation/*/text() != ''">
              "explanation": "<xsl:value-of select="util:escapeForJson((*/gmd:explanation/*/text())[1])" />",
            </xsl:if>
            "pass": "<xsl:value-of select="$pass" />"
            }
          </specificationConformance>
        </xsl:if>

        <xsl:element name="conformTo_{replace(normalize-space($title), '[^a-zA-Z0-9]', '')}">
          <xsl:value-of select="$pass"/>
        </xsl:element>
      </xsl:for-each-group>



      <xsl:for-each select="gmd:contentInfo/*/gmd:featureCatalogueCitation[@uuidref != '']">
        <xsl:variable name="xlink"
                      select="@xlink:href"/>
        <recordLink type="object">{
          "type": "fcats",
          "origin": "<xsl:value-of
            select="if ($xlink = '')
                        then 'catalog'
                        else if ($xlink != '' and
                                 not(starts-with($xlink, $siteUrl)))
                          then 'remote'
                        else 'catalog'"/>",
          "to": "<xsl:value-of select="@uuidref"/>",
          "title": "<xsl:value-of select="util:escapeForJson(@xlink:title)"/>",
          "url": "<xsl:value-of select="$xlink"/>"
          }</recordLink>
        <hasfeaturecat><xsl:value-of select="@uuidref"/></hasfeaturecat>
      </xsl:for-each>


      <xsl:for-each select="gmd:dataQualityInfo/*">
        <xsl:for-each select="gmd:lineage//gmd:source[@uuidref != '']">
          <xsl:variable name="xlink"
                        select="@xlink:href"/>

          <hassource><xsl:value-of select="@uuidref"/></hassource>
          <recordLink type="object">{
            "type": "sources",
            "origin": "<xsl:value-of
              select="if ($xlink = '')
                        then 'catalog'
                        else if ($xlink != '' and
                                 not(starts-with($xlink, $siteUrl)))
                          then 'remote'
                        else 'catalog'"/>",
            "to": "<xsl:value-of select="@uuidref"/>",
            "title": "<xsl:value-of select="util:escapeForJson(@xlink:title)"/>",
            "url": "<xsl:value-of select="$xlink"/>"
            }</recordLink>
        </xsl:for-each>

        <xsl:copy-of select="gn-fn-index:add-multilingual-field('lineage', gmd:lineage/gmd:LI_Lineage/
                                gmd:statement, $allLanguages)"/>


        <xsl:for-each select="gmd:report/*[gmd:nameOfMeasure/gco:CharacterString != '']">
          <xsl:variable name="name"
                        select="(gmd:nameOfMeasure/gco:CharacterString)[1]"/>
          <xsl:variable name="value"
                        select="(gmd:result/gmd:DQ_QuantitativeResult/gmd:value)[1]"/>
          <xsl:variable name="unit"
                        select="(gmd:result/gmd:DQ_QuantitativeResult/gmd:valueUnit//gml:identifier)[1]"/>
          <xsl:variable name="description"
                        select="(gmd:measureDescription/gco:CharacterString)[1]"/>
          <measure type="object">{
            "name": "<xsl:value-of select="util:escapeForJson($name)"/>",
            <xsl:if test="$description != ''">
              "description": "<xsl:value-of select="util:escapeForJson($description)"/>",
            </xsl:if>
            <!-- First value only. -->
            "value": "<xsl:value-of select="util:escapeForJson($value/gco:Record[1])"/>",
            <xsl:if test="$unit != ''">
              "unit": "<xsl:value-of select="util:escapeForJson($unit)"/>",
            </xsl:if>
            "type": "<xsl:value-of select="local-name(.)"/>"
            }
          </measure>

          <xsl:for-each select="gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record[. != '']">
            <xsl:element name="measure_{gn-fn-index:build-field-name($name)}">
              <xsl:value-of select="."/>
            </xsl:element>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:variable name="atomProtocol" select="util:getSettingValue('system/inspire/atomProtocol')" />

      <xsl:for-each select="gmd:distributionInfo/*">
        <xsl:for-each
          select="gmd:distributionFormat/*/gmd:name/*[. != '']">
          <xsl:copy-of select="gn-fn-index:add-field('format', .)"/>
        </xsl:for-each>


        <!-- Indexing distributor contact -->
        <xsl:for-each select="gmd:distributor/*[gmd:distributorContact]">
          <xsl:apply-templates mode="index-contact"
                               select="gmd:distributorContact">
            <xsl:with-param name="fieldSuffix" select="'ForDistribution'"/>
            <xsl:with-param name="languages" select="$allLanguages"/>
          </xsl:apply-templates>
        </xsl:for-each>

        <xsl:for-each select="gmd:distributor/*
                                  /gmd:distributionOrderProcess/*/gmd:orderingInstructions">
          <xsl:copy-of select="gn-fn-index:add-multilingual-field('orderingInstructions', ., $allLanguages)"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:transferOptions/*/
                                gmd:onLine/*[gmd:linkage/gmd:URL != '']">

          <xsl:variable name="transferGroup"
                        select="count(ancestor::gmd:transferOptions/preceding-sibling::gmd:transferOptions)"/>
          <xsl:variable name="protocol"
                        select="gmd:protocol/*/text()"/>
          <xsl:variable name="linkName"
                        select="util:escapeForJson((gmd:name/*/text())[1])"/>

          <linkUrl>
            <xsl:value-of select="gmd:linkage/gmd:URL"/>
          </linkUrl>
          <xsl:if test="normalize-space($protocol) != ''">
            <linkProtocol>
              <xsl:value-of select="$protocol"/>
            </linkProtocol>
          </xsl:if>
          <xsl:element name="linkUrlProtocol{replace($protocol[1], '[^a-zA-Z0-9]', '')}">
            <xsl:value-of select="gmd:linkage/gmd:URL"/>
          </xsl:element>
          <xsl:if test="$protocol = $atomProtocol">
            <atomfeed><xsl:value-of select="gmd:linkage/gmd:URL"/></atomfeed>
          </xsl:if>
          <link type="object">{
            "protocol":"<xsl:value-of select="util:escapeForJson((gmd:protocol/*/text())[1])"/>",
            "mimeType":"<xsl:value-of select="if (*/gmx:MimeFileType)
                                              then util:escapeForJson(*/gmx:MimeFileType/@type)
                                              else if (starts-with(gmd:protocol/gco:CharacterString, 'WWW:DOWNLOAD:'))
                                              then util:escapeForJson(replace(gmd:protocol/gco:CharacterString, 'WWW:DOWNLOAD:', ''))
                                              else ''"/>",
            "urlObject":{"default": "<xsl:value-of select="util:escapeForJson(gmd:linkage/gmd:URL)"/>"},
            <xsl:if test="normalize-space(gmd:name) != ''">
              "nameObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                                'name', gmd:name, $allLanguages, true())"/>,
            </xsl:if>
            <xsl:if test="normalize-space(gmd:description) != ''">
              "descriptionObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                                'description', gmd:description, $allLanguages, true())"/>,
            </xsl:if>
            "function":"<xsl:value-of select="gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue"/>",
            "applicationProfile":"<xsl:value-of select="util:escapeForJson(gmd:applicationProfile/(gco:CharacterString|gmx:Anchor)/text())"/>",
            "group": <xsl:value-of select="$transferGroup"/>
            }
            <!--Link object in Angular used to be
            //     name: linkInfos[0],
            //     title: linkInfos[0],
            //     url: linkInfos[2],
            //     desc: linkInfos[1],
            //     protocol: linkInfos[3],
            //     contentType: linkInfos[4],
            //     group: linkInfos[5] ? parseInt(linkInfos[5]) : undefined,
            //     applicationProfile: linkInfos[6]-->
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

      <xsl:variable name="recordLinks"
                    select="gmd:parentIdentifier/*[text() != '']"/>
      <xsl:choose>
        <xsl:when test="count($recordLinks) > 0">
          <xsl:for-each select="$recordLinks">
            <parentUuid><xsl:value-of select="."/></parentUuid>
            <recordGroup><xsl:value-of select="."/></recordGroup>
            <xsl:copy-of select="gn-fn-index:build-record-link(., @xlink:href, @xlink:title, 'parent')"/>
            <!--
            TODOES - Need more work with routing -->
            <!--            <recordJoin type="object">{"name": "children", "parent": "<xsl:value-of select="util:escapeForJson(.)"/>"}</recordLink>-->
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <recordGroup><xsl:value-of select="$identifier"/></recordGroup>
        </xsl:otherwise>
      </xsl:choose>


      <xsl:for-each select=".//gmd:aggregationInfo/*">
        <xsl:variable name="code"
                      select="gmd:aggregateDataSetIdentifier/*/gmd:code/*/text()"/>
        <xsl:if test="$code != ''">
          <xsl:variable name="xlink"
                        select="gmd:aggregateDataSetIdentifier/*/gmd:code/*/@xlink:href"/>
          <xsl:variable name="associationType"
                        select="gmd:associationType/*/@codeListValue"/>
          <xsl:if test="$associationType = $parentAssociatedResourceType">
            <parentUuid><xsl:value-of select="$code"/></parentUuid>
            <xsl:copy-of select="gn-fn-index:build-record-link($code, $xlink, gmd:aggregateDataSetIdentifier/*/gmd:code/*/@xlink:title, 'parent')"/>
          </xsl:if>
          <xsl:if test="$associationType = $childrenAssociatedResourceType">
            <childUuid><xsl:value-of select="$code"/></childUuid>
            <xsl:copy-of select="gn-fn-index:build-record-link(
                                $code, $xlink,
                                gmd:aggregateDataSetIdentifier/*/gmd:code/*/@xlink:title,
                                 'children')"/>
          </xsl:if>

          <xsl:variable name="initiativeType"
                        select="gmd:initiativeType/*/@codeListValue"/>
          <xsl:variable name="properties">
            <properties>
              <p name="associationType" value="{$associationType}"/>
              <p name="initiativeType" value="{$initiativeType}"/>
            </properties>
          </xsl:variable>
          <xsl:copy-of select="gn-fn-index:build-record-link($code, $xlink, gmd:aggregateDataSetIdentifier/*/gmd:code/*/@xlink:title, 'siblings', $properties)"/>
          <agg_associated><xsl:value-of select="$code"/></agg_associated>
          <xsl:element name="{concat('agg_associated_', $associationType)}"><xsl:value-of select="$code"/></xsl:element>
        </xsl:if>
      </xsl:for-each>


      <xsl:variable name="indexingTimeRecordLink"
                    select="util:getSettingValue('system/index/indexingTimeRecordLink')" />
      <xsl:if test="$indexingTimeRecordLink = 'true'">
        <xsl:variable name="recordsLinks"
                      select="util:getTargetAssociatedResourcesAsNode(
                                        $identifier,
                                        gmd:parentIdentifier/*[text() != '']/text())"/>
        <xsl:copy-of select="$recordsLinks//recordLink"/>
      </xsl:if>

      <!-- Index more fields in this element -->
      <xsl:apply-templates mode="index-extra-fields" select="."/>
    </doc>

    <!-- Index more documents for this element -->
    <xsl:apply-templates mode="index-extra-documents" select="."/>
  </xsl:template>


  <xsl:template mode="index-contact" match="*[gmd:CI_ResponsibleParty]">
    <xsl:param name="fieldSuffix" select="''" as="xs:string"/>
    <xsl:param name="languages" as="node()?"/>

    <!-- Select the first child which should be a CI_ResponsibleParty.
    Some records contains more than one CI_ResponsibleParty which is
    not valid and they will be ignored.
     Same for organisationName eg. de:b86a8604-bf78-480f-a5a8-8edff5586679 -->
    <xsl:variable name="organisationName"
                  select="*[1]/gmd:organisationName[1]"
                  as="node()?"/>
    <xsl:variable name="uuid" select="@uuid"/>

    <xsl:variable name="role"
                  select="replace(*[1]/gmd:role/*/@codeListValue, ' ', '')"
                  as="xs:string?"/>
    <xsl:variable name="logo" select=".//gmx:FileName/@src"/>
    <xsl:variable name="website" select=".//gmd:onlineResource/*/gmd:linkage/gmd:URL"/>
    <xsl:variable name="email"
                  select="*[1]/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString"/>
    <xsl:variable name="phone"
                  select="*[1]/gmd:contactInfo/*/gmd:phone/*/gmd:voice[normalize-space(.) != '']/*/text()"/>
    <xsl:variable name="individualName"
                  select="*[1]/gmd:individualName/gco:CharacterString/text()"/>
    <xsl:variable name="positionName"
                  select="*[1]/gmd:positionName/gco:CharacterString/text()"/>
    <xsl:variable name="address" select="string-join(*[1]/gmd:contactInfo/*/gmd:address/*/(
                                        gmd:deliveryPoint|gmd:postalCode|gmd:city|
                                        gmd:administrativeArea|gmd:country)/gco:CharacterString/text(), ', ')"/>

    <xsl:variable name="roleField"
                  select="concat(replace($role, '[^a-zA-Z0-9-]', ''),
                                 'Org', $fieldSuffix)"/>
    <xsl:variable name="orgField"
                  select="concat('Org', $fieldSuffix)"/>


    <xsl:if test="normalize-space($organisationName) != ''">
      <xsl:copy-of select="gn-fn-index:add-multilingual-field(
                            $orgField, $organisationName, $languages)"/>
      <xsl:copy-of select="gn-fn-index:add-multilingual-field(
                            $roleField, $organisationName, $languages)"/>
    </xsl:if>
    <xsl:element name="contact{$fieldSuffix}">
      <xsl:attribute name="type" select="'object'"/>{
      <xsl:if test="$organisationName">
        "organisationObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                              'organisation', $organisationName, $languages, true())"/>,
      </xsl:if>
      "role":"<xsl:value-of select="$role"/>",
      "email":"<xsl:value-of select="util:escapeForJson($email[1])"/>",
      "website":"<xsl:value-of select="util:escapeForJson($website)"/>",
      "logo":"<xsl:value-of select="util:escapeForJson($logo)"/>",
      "individual":"<xsl:value-of select="util:escapeForJson($individualName)"/>",
      "position":"<xsl:value-of select="util:escapeForJson($positionName)"/>",
      "phone":"<xsl:value-of select="util:escapeForJson($phone[1])"/>",
      "address":"<xsl:value-of select="util:escapeForJson($address)"/>"

      }
    </xsl:element>
  </xsl:template>




  <xsl:template name="index-operatesOn">
    <xsl:for-each
      select="gmd:identificationInfo/srv:SV_ServiceIdentification">
      <xsl:for-each select="srv:operatesOn">
        <xsl:variable name="associationType" select="'operatesOn'"/>
        <xsl:variable name="serviceType"
                      select="../srv:serviceType/gco:LocalName"/>
        <!--<xsl:variable name="relatedTo" select="@uuidref"/>-->
        <xsl:variable name="getRecordByIdId">
          <xsl:if test="@xlink:href != ''">
            <xsl:analyze-string select="@xlink:href"
                                regex=".*[i|I][d|D]=([_\w\-\.\{{\}}]*).*">
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
                          and not($fastIndexMode)
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
               Remote is supposed to be ISO19139. -->
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
          <!--          <recordLink type="object">{"name": "dataset", "parent": "<xsl:value-of select="util:escapeForJson(.)"/>"}</recordLink>-->
        </xsl:if>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
