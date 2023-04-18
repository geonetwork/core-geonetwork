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
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
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
  <xsl:import href="link-utility.xsl"/>

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
  <xsl:variable name="childrenAssociatedResourceType" select="'isComposedOf'"/>

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



      <!-- ISO19115-3 records can be only a feature catalogue description.
       In this case,
       * add the resourceType=featureCatalog to enable search when linking records
       * (TODO: Check which scopeCode is more appropriate eg. featureType ?)
       * Index feature catalogue name as title, scope as abstract.
       -->
      <xsl:variable name="isOnlyFeatureCatalog"
                    select="not(mdb:identificationInfo)
                            and exists(mdb:contentInfo/*/mrc:featureCatalogue)"
                    as="xs:boolean"/>

      <xsl:if test="$isOnlyFeatureCatalog">
        <resourceType>featureCatalog</resourceType>
      </xsl:if>

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
      <xsl:for-each select="mdb:metadataScope/*/mdb:name">
        <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceTypeName', ., $allLanguages)"/>
      </xsl:for-each>

      <xsl:if test="not($isOnlyFeatureCatalog)
                    and exists(mdb:contentInfo/*/mrc:featureCatalogue//gfc:FC_FeatureCatalogue/gfc:featureType)">
        <resourceType>featureCatalog</resourceType>
      </xsl:if>


      <!-- Indexing metadata contact -->
      <xsl:apply-templates mode="index-contact" select="mdb:contact">
        <xsl:with-param name="fieldSuffix" select="''"/>
        <xsl:with-param name="languages" select="$allLanguages"/>
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

        <xsl:for-each-group select="mri:defaultLocale/*/lan:characterEncoding/*[@codeListValue != '']"
                            group-by="@codeListValue">
          <xsl:copy-of select="gn-fn-index:add-codelist-field(
                                'cl_resourceCharacterSet', ., $allLanguages)"/>
        </xsl:for-each-group>

        <!-- Indexing resource contact -->
        <xsl:apply-templates mode="index-contact"
                             select="mri:pointOfContact">
          <xsl:with-param name="fieldSuffix" select="'ForResource'"/>
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:apply-templates>


        <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceCredit', mri:credit[* != ''], $allLanguages)"/>
        <xsl:copy-of select="gn-fn-index:add-multilingual-field('supplementalInformation', mri:supplementalInformation[* != ''], $allLanguages)"/>
        <xsl:copy-of select="gn-fn-index:add-multilingual-field('purpose', mri:purpose[* != ''], $allLanguages)"/>

        <xsl:variable name="overviews"
                      select="mri:graphicOverview/mcc:MD_BrowseGraphic/
                                mcc:fileName/(gco:CharacterString[. != '']|gcx:FileName[@src != ''])"/>
        <xsl:copy-of select="gn-fn-index:add-field('hasOverview', if (count($overviews) > 0) then 'true' else 'false')"/>


        <xsl:for-each select="$overviews">
          <overview type="object">{
            "url": "<xsl:value-of select="if (local-name() = 'FileName') then @src else normalize-space(.)"/>"
            <xsl:if test="normalize-space(../../mcc:fileDescription) != ''">,
              "nameObject":
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



        <xsl:variable name="allKeywords">
          <xsl:for-each-group select="*/mri:MD_Keywords"
                              group-by="concat(mri:thesaurusName/*/cit:title/(gco:CharacterString|gcx:Anchor)/text(), '-', mri:type/*/@codeListValue[. != ''])">
            <xsl:sort select="current-grouping-key()"/>

            <xsl:variable name="thesaurusType"
                          select="mri:type/*/@codeListValue[. != '']"/>

            <xsl:variable name="thesaurusTitle"
                          select="if (starts-with(current-grouping-key(), '-'))
                                  then concat('otherKeywords', current-grouping-key())
                                  else mri:thesaurusName/*/cit:title/(gco:CharacterString|gcx:Anchor)/text()"/>

            <xsl:variable name="thesaurusRef"
                          select="mri:thesaurusName/cit:CI_Citation/
                                        cit:identifier[position() = 1]/*/
                                          mcc:code/(gco:CharacterString|gcx:Anchor)"/>

            <xsl:variable name="thesaurusId"
                          select="if ($thesaurusRef != '')
                                  then normalize-space($thesaurusRef/text())
                                  else util:getThesaurusIdByTitle($thesaurusTitle)"/>

            <xsl:variable name="thesaurusUri"
                          select="$thesaurusRef/@xlink:href"/>

            <xsl:variable name="thesaurusFieldName"
                          select="gn-fn-index:build-thesaurus-index-field-name($thesaurusId, $thesaurusTitle)"/>

            <xsl:variable name="keywords"
                          select="mri:keyword[*/normalize-space() != '']"/>

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
                                  select="if (gcx:Anchor/@xlink:href[. != ''])
                                          then gcx:Anchor/@xlink:href
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
                        select="//gex:geographicElement/*/gex:geographicIdentifier/
                                  */mcc:code[*/normalize-space(.) != '']
                                |//gex:EX_Extent/gex:description[*/normalize-space(.) != '']"/>
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


        <xsl:for-each select="mri:topicCategory/mri:MD_TopicCategoryCode[string(.)]">
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

            <xsl:choose>
              <xsl:when test="$zuluStartDate != ''
                                and ($zuluEndDate != '' or $end/@indeterminatePosition = 'now')">
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
                <indexingErrorMsg>Warning / Field resourceTemporalDateRange / Lower and upper bounds empty. Date range not indexed.</indexingErrorMsg>
              </xsl:otherwise>
            </xsl:choose>

            <xsl:if test="$zuluStartDate != ''
                          and $zuluEndDate != ''
                          and $start &gt; $end">
              <indexingErrorMsg>Warning / Field resourceTemporalDateRange / Lower range bound '<xsl:value-of select="$start"/>' can not be greater than upper bound '<xsl:value-of select="$end"/>'.</indexingErrorMsg>
            </xsl:if>


            <xsl:call-template name="build-range-details">
              <xsl:with-param name="start" select="$start"/>
              <xsl:with-param name="end" select="$end"/>
            </xsl:call-template>
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
        <xsl:for-each select="srv:serviceType/gco:ScopedName[string(text())]">
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
          <xsl:variable name="crs" select="mcc:code/*[1]/text()"/>
          <xsl:variable name="crsLabel"
                        select="if (mcc:description/*[1])
                                then mcc:description/*[1]/text()
                                else if (mcc:code/*/@xlink:title)
                                then mcc:code/*/@xlink:title
                                else $crs"/>
          <xsl:if test="$crs != ''">
            <coordinateSystem>
              <xsl:value-of select="$crs"/>
            </coordinateSystem>
          </xsl:if>

          <crsDetails type="object">{
            "code": "<xsl:value-of select="gn-fn-index:json-escape($crs)"/>",
            "codeSpace": "<xsl:value-of select="gn-fn-index:json-escape(mcc:codeSpace/*/text())"/>",
            "name": "<xsl:value-of select="gn-fn-index:json-escape($crsLabel)"/>",
            "url": "<xsl:value-of select="gn-fn-index:json-escape(mcc:code/*/@xlink:href)"/>"
            }</crsDetails>
        </xsl:for-each>
      </xsl:for-each>


      <!-- INSPIRE Conformity -->
      <xsl:variable name="legalTextList"
                    select="if ($isService) then $eu9762009 else $eu10892010"/>

      <xsl:for-each-group select="mdb:dataQualityInfo/*/mdq:report/*/mdq:result"
                          group-by="*/mdq:specification/cit:CI_Citation/
                                        cit:title/(gco:CharacterString|gcx:Anchor)">

        <xsl:variable name="title" select="current-grouping-key()"/>
        <xsl:variable name="matchingEUText"
                      select="if ($inspireRegulationLaxCheck)
                              then daobs:search-in-contains($legalTextList/*, $title)
                              else daobs:search-in($legalTextList/*, $title)"/>

        <xsl:variable name="pass"
                      select="*/mdq:pass/gco:Boolean"/>

        <xsl:if test="count($matchingEUText) = 1">
          <inspireConformResource>
            <xsl:value-of select="$pass"/>
          </inspireConformResource>
        </xsl:if>

        <xsl:if test="string($title)">
          <specificationConformance type="object">{
            "title": "<xsl:value-of select="gn-fn-index:json-escape($title)" />",
            <xsl:if test="gn-fn-index:is-isoDate((*/mdq:specification/cit:CI_Citation/cit:date/cit:CI_Date/cit:date/gco:Date)[1])">
              "date": "<xsl:value-of select="(*/mdq:specification/cit:CI_Citation/cit:date/cit:CI_Date/cit:date/gco:Date)[1]" />",
            </xsl:if>
            <xsl:if test="*/mdq:specification/*/cit:title/*/@xlink:href">
              "link": "<xsl:value-of select="*/mdq:specification/*/cit:title/*/@xlink:href"/>",
            </xsl:if>
            <xsl:if test="*/mdq:explanation/*/text() != ''">
              "explanation": "<xsl:value-of select="gn-fn-index:json-escape((*/mdq:explanation/*/text())[1])" />",
            </xsl:if>
            "pass": "<xsl:value-of select="$pass" />"
            }
          </specificationConformance>
        </xsl:if>

        <xsl:element name="conformTo_{gn-fn-index:build-field-name($title)}">
          <xsl:value-of select="$pass"/>
        </xsl:element>
      </xsl:for-each-group>

      <xsl:if test="$isOnlyFeatureCatalog">
        <resourceType>featureCatalog</resourceType>

        <xsl:for-each select="mdb:contentInfo/*/mrc:featureCatalogue/*">
          <xsl:for-each select="(cat:name[*/text() != '']
                        |gfc:featureType/*/gfc:typeName[text() != ''])[1]">
            <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceTitle',
                               ., $allLanguages)"/>
          </xsl:for-each>

          <xsl:for-each select="cat:versionNumber/*">
            <xsl:copy-of select="gn-fn-index:add-field('resourceEdition', .)"/>
          </xsl:for-each>

          <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceAbstract',
                                cat:scope, $allLanguages)"/>
        </xsl:for-each>


        <xsl:apply-templates mode="index-contact"
                             select="mdb:contentInfo//gfc:producer">
          <xsl:with-param name="fieldSuffix" select="'ForResource'"/>
        </xsl:apply-templates>
      </xsl:if>

      <xsl:variable name="jsonFeatureTypes">[
        <xsl:for-each select="mdb:contentInfo//gfc:FC_FeatureCatalogue/gfc:featureType">{

          "typeName" : "<xsl:value-of select="gn-fn-index:json-escape(gfc:FC_FeatureType/gfc:typeName/text())"/>",
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
              {"name": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:memberName/text())"/>",
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
        <hasfeaturecat><xsl:value-of select="@uuidref"/></hasfeaturecat>
      </xsl:for-each>


      <xsl:variable name="additionalDocuments" as="node()*">
        <xsl:call-template name="collect-documents">
          <xsl:with-param name="forIndexing" select="true()"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:for-each select="$additionalDocuments">
        <link type="object">{
          "protocol": "<xsl:value-of select="gn-fn-index:json-escape(
                                        protocol/text())"/>",
          "function": "<xsl:value-of select="gn-fn-index:json-escape(
                                        function/text())"/>",
          <xsl:if test="normalize-space(url) != ''">
            "urlObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                                'url', url/*, $allLanguages)"/>,
          </xsl:if>
          <xsl:if test="normalize-space(title) != ''">
            "nameObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                                'name', title/*, $allLanguages)"/>,
          </xsl:if>
          <xsl:if test="normalize-space(description) != ''">
            "descriptionObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                                'description', description/*, $allLanguages)"/>,
          </xsl:if>
          "applicationProfile": "<xsl:value-of select="gn-fn-index:json-escape(
                                        applicationProfile/text())"/>"
          }
        </link>
      </xsl:for-each>


      <xsl:for-each select="mdb:resourceLineage/*">
        <xsl:copy-of select="gn-fn-index:add-multilingual-field('lineage',
                                mrl:statement, $allLanguages)"/>

        <xsl:for-each select=".//mrl:source[@uuidref != '']">
          <xsl:variable name="xlink"
                        select="@xlink:href"/>
          <hassource><xsl:value-of select="@uuidref"/></hassource>
          <xsl:copy-of select="gn-fn-index:build-record-link(@uuidref, $xlink, @xlink:title, 'sources')"/>
        </xsl:for-each>

        <xsl:for-each select=".//mrl:source/*/mrl:description[gco:CharacterString != '']">
          <xsl:copy-of select="gn-fn-index:add-multilingual-field('sourceDescription', ., $allLanguages)"/>
        </xsl:for-each>
      </xsl:for-each>


      <xsl:for-each select="mdb:dataQualityInfo/*">
        <xsl:for-each select="mdq:report/*[
                normalize-space(mdq:measure/*/mdq:nameOfMeasure/gco:CharacterString) != '']">

          <xsl:variable name="name"
                        select="(mdq:measure/*/mdq:nameOfMeasure/gco:CharacterString)[1]"/>
          <xsl:variable name="value"
                        select="(mdq:result/mdq:DQ_QuantitativeResult/mdq:value)[1]"/>
          <xsl:variable name="unit"
                        select="(mdq:result/mdq:DQ_QuantitativeResult/mdq:valueUnit//gml:identifier)[1]"/>
          <xsl:variable name="description"
                        select="(mdq:measure/*/mdq:measureDescription/gco:CharacterString)[1]"/>
          <measure type="object">{
            "name": "<xsl:value-of select="gn-fn-index:json-escape($name)"/>",
            <xsl:if test="$description != ''">
              "description": "<xsl:value-of select="gn-fn-index:json-escape($description)"/>",
            </xsl:if>
            <!-- First value only. -->
            "value": "<xsl:value-of select="gn-fn-index:json-escape($value/gco:Record[1])"/>",
            <xsl:if test="$unit != ''">
              "unit": "<xsl:value-of select="gn-fn-index:json-escape($unit)"/>",
            </xsl:if>
            "type": "<xsl:value-of select="local-name(.)"/>"
            }
          </measure>

          <xsl:for-each select="mdq:result/mdq:DQ_QuantitativeResult/mdq:value/gco:Record[. != '']">
            <xsl:element name="measure_{gn-fn-index:build-field-name($name)}">
              <xsl:value-of select="."/>
            </xsl:element>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:for-each select="mdb:distributionInfo/*">
        <xsl:for-each select="mrd:distributionFormat/*/
                                mrd:formatSpecificationCitation/*/cit:title/*/text()[. != '']">
          <xsl:copy-of select="gn-fn-index:add-field('format', .)"/>
        </xsl:for-each>

        <xsl:for-each select="mrd:distributor/mrd:MD_Distributor[mrd:distributorContact]">
          <!-- Indexing resource contact -->
          <xsl:apply-templates mode="index-contact"
                               select="mrd:distributorContact">
            <xsl:with-param name="fieldSuffix" select="'ForDistribution'"/>
            <xsl:with-param name="languages" select="$allLanguages"/>
          </xsl:apply-templates>
        </xsl:for-each>

        <xsl:for-each select="mrd:distributor/mrd:MD_Distributor
                                  /mrd:distributionOrderProcess/*/mrd:orderingInstructions">
          <xsl:copy-of select="gn-fn-index:add-multilingual-field('orderingInstructions', ., $allLanguages)"/>
        </xsl:for-each>

        <xsl:for-each select=".//mrd:onLine/*[cit:linkage/gco:CharacterString != '']">
          <xsl:variable name="transferGroup"
                        select="count(ancestor::mrd:transferOptions/preceding-sibling::mrd:transferOptions)"/>

          <xsl:variable name="protocol" select="cit:protocol/*/text()"/>

          <linkUrl>
            <xsl:value-of select="cit:linkage/gco:CharacterString"/>
          </linkUrl>
          <xsl:if test="normalize-space($protocol) != ''">
            <linkProtocol>
              <xsl:value-of select="$protocol"/>
            </linkProtocol>
          </xsl:if>
          <xsl:element name="linkUrlProtocol{replace($protocol, '[^a-zA-Z0-9]', '')}">
            <xsl:value-of select="cit:linkage/*/text()"/>
          </xsl:element>
          <link type="object">{
            "protocol":"<xsl:value-of select="gn-fn-index:json-escape(cit:protocol/*/text())"/>",
            "mimeType":"<xsl:value-of select="if (*/gcx:MimeFileType)
                                              then gn-fn-index:json-escape(*/gcx:MimeFileType/@type)
                                              else if (starts-with(cit:protocol/gco:CharacterString, 'WWW:DOWNLOAD:'))
                                              then gn-fn-index:json-escape(replace(cit:protocol/gco:CharacterString, 'WWW:DOWNLOAD:', ''))
                                              else ''"/>",
            <xsl:if test="normalize-space(cit:linkage) != ''">
              "urlObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                                'url', cit:linkage, $allLanguages)"/>,
            </xsl:if>
            <xsl:if test="normalize-space(cit:name) != ''">
              "nameObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                                'name', cit:name, $allLanguages)"/>,
            </xsl:if>
            <xsl:if test="normalize-space(cit:description) != ''">
              "descriptionObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                                'description', cit:description, $allLanguages)"/>,
            </xsl:if>
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
                              else if (mri:metadataReference/@xlink:href != '')
                              then mri:metadataReference/@xlink:href
                              else mri:metadataReference/cit:CI_Citation/cit:identifier/mcc:MD_Identifier/mcc:code/*/text()"/>
        <xsl:if test="$code != ''">
          <xsl:variable name="xlink"
                        select="mri:metadataReference/@xlink:href"/>
          <xsl:variable name="associationType"
                        select="mri:associationType/*/@codeListValue"/>
          <xsl:if test="$associationType = $parentAssociatedResourceType">
            <parentUuid><xsl:value-of select="$code"/></parentUuid>
            <xsl:copy-of select="gn-fn-index:build-record-link(
                                $code, $xlink, mri:metadataReference/@xlink:title, 'parent')"/>
          </xsl:if>
          <xsl:if test="$associationType = $childrenAssociatedResourceType">
            <childUuid><xsl:value-of select="$code"/></childUuid>
            <xsl:copy-of select="gn-fn-index:build-record-link(
                                $code, $xlink, mri:metadataReference/@xlink:title, 'children')"/>
          </xsl:if>

          <xsl:variable name="initiativeType"
                        select="mri:initiativeType/*/@codeListValue"/>
          <xsl:variable name="properties">
            <properties>
              <p name="associationType" value="{$associationType}"/>
              <p name="initiativeType" value="{$initiativeType}"/>
            </properties>
          </xsl:variable>
          <xsl:copy-of select="gn-fn-index:build-record-link(
                                $code, $xlink, mri:metadataReference/@xlink:title,
                                'siblings', $properties)"/>
          <agg_associated><xsl:value-of select="$code"/></agg_associated>
          <xsl:element name="{concat('agg_associated_', $associationType)}"><xsl:value-of select="$code"/></xsl:element>
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


  <!-- TODO: here is a limited vision of 1 org with first individual.
  ISO19115-3 allows more combinations. -->
  <xsl:template mode="index-contact" match="*[cit:CI_Responsibility]">
    <xsl:param name="fieldSuffix" select="''" as="xs:string"/>
    <xsl:param name="languages" as="node()?"/>

    <xsl:variable name="organisationName"
                  select="(.//cit:CI_Organisation/cit:name)[1]"
                  as="node()?"/>
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
                  select="(.//cit:CI_Individual/cit:name/gco:CharacterString/text())[1]"/>
    <xsl:variable name="positionName"
                  select="(.//cit:positionName/gco:CharacterString/text())[1]"/>
    <xsl:variable name="address" select="string-join(.//cit:contactInfo/*/cit:address/*/(
                                        cit:deliveryPoint|cit:postalCode|cit:city|
                                        cit:administrativeArea|cit:country)/gco:CharacterString/text(), ', ')"/>

    <xsl:variable name="roleField"
                  select="concat(replace($role, '[^a-zA-Z0-9-]', ''), 'Org', $fieldSuffix)"/>
    <xsl:variable name="orgField"
                  select="concat('Org', $fieldSuffix)"/>

    <xsl:if test="normalize-space($organisationName) != ''">
      <xsl:if test="count(preceding-sibling::*[name() = $elementName
                        and .//cit:CI_Organisation/cit:name/gco:CharacterString = $organisationName/gco:CharacterString]) = 0">
        <xsl:copy-of select="gn-fn-index:add-multilingual-field(
                              $orgField, $organisationName, $languages)"/>
      </xsl:if>

      <xsl:if test="count(preceding-sibling::*[name() = $elementName
                      and .//cit:CI_Organisation/cit:name/gco:CharacterString = $organisationName/gco:CharacterString
                      and .//cit:role/*/@codeListValue = $role]) = 0">
        <xsl:copy-of select="gn-fn-index:add-multilingual-field(
                              $roleField, $organisationName, $languages)"/>
      </xsl:if>
    </xsl:if>

    <xsl:variable name="identifiers"
                  select=".//cit:partyIdentifier/*"/>
    <xsl:element name="contact{$fieldSuffix}">
      <!-- TODO: Can be multilingual -->
      <xsl:attribute name="type" select="'object'"/>{
      <xsl:if test="$organisationName">
        "organisationObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                                'organisation', $organisationName, $languages)"/>,
      </xsl:if>
      "role":"<xsl:value-of select="$role"/>",
      "email":"<xsl:value-of select="gn-fn-index:json-escape($email)"/>",
      "website":"<xsl:value-of select="$website"/>",
      "logo":"<xsl:value-of select="$logo"/>",
      "individual":"<xsl:value-of select="gn-fn-index:json-escape($individualName)"/>",
      "position":"<xsl:value-of select="gn-fn-index:json-escape($positionName)"/>",
      "phone":"<xsl:value-of select="gn-fn-index:json-escape($phone)"/>",
      "address":"<xsl:value-of select="gn-fn-index:json-escape($address)"/>"
      <xsl:if test="count($identifiers) > 0">
        ,"identifiers":[
        <xsl:for-each select="$identifiers">
          {
            "code": "<xsl:value-of select="gn-fn-index:json-escape(mcc:code/(gco:CharacterString|gcx:Anchor))"/>",
            "codeSpace": "<xsl:value-of select="(mcc:codeSpace/(gco:CharacterString|gcx:Anchor))[1]/normalize-space()"/>",
            "link": "<xsl:value-of select="mcc:code/gcx:Anchor/@xlink:href"/>"
          }
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>
        ]
      </xsl:if>
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
              <xsl:when test="not($fastIndexMode)
                              and string(normalize-space($xlinkHref))
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
