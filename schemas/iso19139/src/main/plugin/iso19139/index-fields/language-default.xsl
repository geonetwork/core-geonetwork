<?xml version="1.0" encoding="UTF-8" ?>

<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                exclude-result-prefixes="#all">

  <!--This file defines what parts of the metadata are indexed by Lucene
      Searches can be conducted on indexes defined here.
      The Field@name attribute defines the name of the search variable.
      If a variable has to be maintained in the user session, it needs to be
      added to the GeoNetwork constants in the Java source code.
      Please keep indexes consistent among metadata standards if they should
      work accross different metadata resources -->
  <!-- ========================================================================================= -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no"/>
  <xsl:include href="../convert/functions.xsl"/>
  <xsl:include href="../../../xsl/utils-fn.xsl"/>
  <xsl:include href="inspire-util.xsl" />

  <xsl:param name="thesauriDir"/>
  <xsl:param name="inspire">false</xsl:param>

  <xsl:variable name="inspire-thesaurus" select="if ($inspire!='false') then document(concat('file:///', $thesauriDir, '/external/thesauri/theme/httpinspireeceuropaeutheme-theme.rdf')) else ''"/>
  <xsl:variable name="inspire-theme" select="if ($inspire!='false') then $inspire-thesaurus//skos:Concept else ''"/>

  <!-- ========================================================================================= -->
  <xsl:variable name="isoDocLangId">
    <xsl:call-template name="langId19139"/>
  </xsl:variable>

  <xsl:template match="/">

    <Documents>
      <xsl:for-each
        select="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:locale/gmd:PT_Locale">
        <xsl:call-template name="document">
          <xsl:with-param name="isoLangId"
                          select="util:threeCharLangCode(normalize-space(string(gmd:languageCode/gmd:LanguageCode/@codeListValue)))"/>
          <xsl:with-param name="langId" select="@id"></xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
      <!--
              Create a language document only if PT_Locale defined (ie. is a multilingual document)
              and gmd:locale contains the main metadata language. -->
      <xsl:if test="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:locale/gmd:PT_Locale
                     and count(/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/
                       gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode[@codeListValue = $isoDocLangId]) = 0">
        <xsl:call-template name="document">
          <xsl:with-param name="isoLangId" select="$isoDocLangId"></xsl:with-param>
          <xsl:with-param name="langId"
                          select="util:twoCharLangCode(normalize-space(string($isoDocLangId)))"></xsl:with-param>
        </xsl:call-template>
      </xsl:if>
    </Documents>
  </xsl:template>

  <!-- ========================================================================================= -->
  <xsl:template name="document">
    <xsl:param name="isoLangId"/>
    <xsl:param name="langId"/>

    <Document locale="{$isoLangId}">
      <Field name="_locale" string="{$isoLangId}" store="true" index="true"/>
      <Field name="_docLocale" string="{$isoDocLangId}" store="true" index="true"/>

      <xsl:variable name="poundLangId" select="concat('#',$langId)"/>
      <xsl:variable name="_defaultTitle">
        <xsl:call-template name="defaultTitle">
          <xsl:with-param name="isoDocLangId" select="$isoLangId"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:if test="$isoLangId!=$isoDocLangId">
        <!-- not tokenized title for sorting -->
        <Field name="_defaultTitle" string="{string($_defaultTitle)}" store="true" index="true"/>
      </xsl:if>
      <xsl:variable name="title"
                    select="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo//gmd:citation//gmd:title//gmd:LocalisedCharacterString[@locale=$poundLangId]"/>

      <!-- not tokenized title for sorting -->
      <xsl:choose>
        <xsl:when test="normalize-space($title) = ''">
          <Field name="_title" string="{string($_defaultTitle)}" store="true" index="true"/>
        </xsl:when>
        <xsl:otherwise>
          <Field name="_title" string="{string($title)}" store="true" index="true"/>
        </xsl:otherwise>
      </xsl:choose>


      <xsl:variable name="_defaultAbstract">
        <xsl:call-template name="defaultAbstract">
          <xsl:with-param name="isoDocLangId" select="$isoDocLangId"/>
        </xsl:call-template>
      </xsl:variable>

      <Field name="_defaultAbstract"
             string="{string($_defaultAbstract)}"
             store="true"
             index="true"/>

      <xsl:apply-templates select="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']"
                           mode="metadata">
        <xsl:with-param name="langId" select="$poundLangId"/>
        <xsl:with-param name="isoLangId" select="$isoLangId"/>
      </xsl:apply-templates>

      <xsl:apply-templates mode="index"
                           select="*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']">
        <xsl:with-param name="langId" select="$poundLangId"/>
      </xsl:apply-templates>
    </Document>
  </xsl:template>

  <xsl:template mode="index" match="*|@*">
    <xsl:param name="langId"/>

    <xsl:apply-templates mode="index" select="*|@*">
      <xsl:with-param name="langId" select="$langId"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- ========================================================================================= -->

  <xsl:template match="*" mode="metadata">
    <xsl:param name="langId"/>
    <xsl:param name="isoLangId"/>

    <xsl:for-each select="gmd:dateStamp/*">
      <Field name="changeDate" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>

    <!-- === Data or Service Identification === -->

    <!-- the double // here seems needed to index MD_DataIdentification when
        it is nested in a SV_ServiceIdentification class -->

    <xsl:for-each select="gmd:contact/*/gmd:organisationName//gmd:LocalisedCharacterString[@locale=$langId]">
        <Field name="metadataPOC" string="{string(.)}" store="true" index="true"/>

        <xsl:variable name="role" select="../../../../gmd:role/*/@codeListValue"/>
        <xsl:variable name="roleTranslation" select="util:getCodelistTranslation('gmd:CI_RoleCode', string($role), string($isoLangId))"/>
        <xsl:variable name="logo" select="../../../..//gmx:FileName/@src"/>
        <xsl:variable name="email" select="../../../../gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString"/>
        <xsl:variable name="phone" select="../../../../gmd:contactInfo/*/gmd:phone/*/gmd:voice[normalize-space(.) != '']/*/text()"/>
        <xsl:variable name="individualName" select="../../../../gmd:individualName/gco:CharacterString/text()"/>
        <xsl:variable name="positionName" select="../../../../gmd:positionName/gco:CharacterString/text()"/>
        <xsl:variable name="address" select="string-join(../../../../gmd:contactInfo/*/gmd:address/*/(
                                      gmd:deliveryPoint|gmd:postalCode|gmd:city|
                                      gmd:administrativeArea|gmd:country)/gco:CharacterString/text(), ', ')"/>

        <Field name="responsibleParty"
               string="{concat($roleTranslation, '|metadata|', ., '|', $logo, '|', string-join($email, ','), '|', $individualName, '|', $positionName, '|', $address, '|', string-join($phone, ','))}"
               store="true" index="false"/>
    </xsl:for-each>


    <xsl:for-each select="gmd:identificationInfo/*">

      <xsl:for-each select="gmd:citation/gmd:CI_Citation">

        <xsl:for-each
          select="gmd:identifier/gmd:MD_Identifier/gmd:code//gmd:LocalisedCharacterString[@locale=$langId]">
          <Field name="identifier" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <!-- not tokenized title for sorting -->
        <Field name="_defaultTitle" string="{string(gmd:title/gco:CharacterString)}" store="true"
               index="true"/>
        <!-- not tokenized title for sorting -->
        <Field name="_title"
               string="{string(gmd:title//gmd:LocalisedCharacterString[@locale=$langId])}"
               store="true" index="true"/>

        <xsl:for-each select="gmd:title//gmd:LocalisedCharacterString[@locale=$langId]">
          <Field name="title" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:alternateTitle//gmd:LocalisedCharacterString[@locale=$langId]">
          <Field name="altTitle" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each
          select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date">
          <Field name="revisionDate" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each
          select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date">
          <Field name="createDate" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each
          select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']/gmd:date/gco:Date">
          <Field name="publicationDate" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <!-- fields used to search for metadata in paper or digital format -->

        <xsl:for-each select="gmd:presentationForm">
          <xsl:if test="contains(gmd:CI_PresentationFormCode/@codeListValue, 'Digital')">
            <Field name="digital" string="true" store="true" index="true"/>
          </xsl:if>

          <xsl:if test="contains(gmd:CI_PresentationFormCode/@codeListValue, 'Hardcopy')">
            <Field name="paper" string="true" store="true" index="true"/>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="gmd:abstract//gmd:LocalisedCharacterString[@locale=$langId]">
        <Field name="abstract" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>
      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="*/gmd:EX_Extent">
        <xsl:apply-templates select="gmd:geographicElement/gmd:EX_GeographicBoundingBox"
                             mode="latLon"/>

        <xsl:for-each
          select="gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code//gmd:LocalisedCharacterString[@locale=$langId]">
          <Field name="geoDescCode" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:description//gmd:LocalisedCharacterString[@locale=$langId]">
          <Field name="extentDesc" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent|
          gmd:temporalElement/gmd:EX_SpatialTemporalExtent/gmd:extent">
          <xsl:for-each select="gml:TimePeriod/gml:beginPosition|gml320:TimePeriod/gml320:beginPosition">
            <Field name="tempExtentBegin" string="{string(.)}" store="true" index="true"/>
          </xsl:for-each>

          <xsl:for-each select="gml:TimePeriod/gml:endPosition|gml320:TimePeriod/gml320:endPosition">
            <Field name="tempExtentEnd" string="{string(.)}" store="true" index="true"/>
          </xsl:for-each>

          <xsl:for-each select="gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition|gml320:TimePeriod/gml320:begin/gml320:TimeInstant/gml320:timePosition">
            <Field name="tempExtentBegin" string="{string(.)}" store="true" index="true"/>
          </xsl:for-each>

          <xsl:for-each select="gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition|gml320:TimePeriod/gml320:end/gml320:TimeInstant/gml320:timePosition">
            <Field name="tempExtentEnd" string="{string(.)}" store="true" index="true"/>
          </xsl:for-each>

          <xsl:for-each select="gml:TimeInstant/gml:timePosition|gml320:TimeInstant/gml320:timePosition">
            <Field name="tempExtentBegin" string="{string(.)}" store="true" index="true"/>
            <Field name="tempExtentEnd" string="{string(.)}" store="true" index="true"/>
          </xsl:for-each>

        </xsl:for-each>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="*/gmd:MD_Keywords">
        <xsl:for-each select="gmd:keyword//gmd:LocalisedCharacterString[@locale=$langId]">
          <xsl:variable name="keyword" select="string(.)"/>

          <Field name="keyword" string="{$keyword}" store="true" index="true"/>

          <!-- If INSPIRE is enabled, check if the keyword is one of the 34 themes
                 and index annex, theme and theme in english. -->
          <xsl:if test="$inspire='true'">
            <xsl:if test="string-length(.) &gt; 0">

              <xsl:variable name="inspireannex">
                <xsl:call-template name="determineInspireAnnex">
                  <xsl:with-param name="keyword" select="$keyword"/>
                  <xsl:with-param name="inspireThemes" select="$inspire-theme"/>
                </xsl:call-template>
              </xsl:variable>

              <xsl:variable name="inspireThemeAcronym">
                <xsl:call-template name="getInspireThemeAcronym">
                  <xsl:with-param name="keyword" select="$keyword"/>
                </xsl:call-template>
              </xsl:variable>

              <!-- Add the inspire field if it's one of the 34 themes -->
              <xsl:if test="normalize-space($inspireannex)!=''">
                <Field name="inspiretheme" string="{$keyword}" store="true" index="true"/>
                <Field name="inspirethemewithac"
                       string="{concat($inspireThemeAcronym, '|', $keyword)}"
                       store="true" index="true"/>

                <!--<Field name="inspirethemeacronym" string="{$inspireThemeAcronym}" store="true" index="true"/>-->
                <xsl:variable name="inspireThemeURI"  select="$inspire-theme[skos:prefLabel = $keyword]/@rdf:about"/>
                <Field name="inspirethemeuri" string="{$inspireThemeURI}" store="true" index="true"/>

                <xsl:variable name="englishInspireTheme">
                  <xsl:call-template name="translateInspireThemeToEnglish">
                    <xsl:with-param name="keyword" select="$keyword"/>
                    <xsl:with-param name="inspireThemes" select="$inspire-theme"/>
                  </xsl:call-template>
                </xsl:variable>

                <Field name="inspiretheme_en" string="{$englishInspireTheme}" store="true" index="true"/>
                <Field name="inspireannex" string="{$inspireannex}" store="true" index="true"/>
                <!-- FIXME : inspirecat field will be set multiple time if one record has many themes -->
                <Field name="inspirecat" string="true" store="false" index="true"/>
              </xsl:if>
            </xsl:if>
          </xsl:if>
        </xsl:for-each>

        <xsl:for-each select="gmd:type/gmd:MD_KeywordTypeCode/@codeListValue">
          <Field name="keywordType" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:for-each>


      <xsl:if test="count(//gmd:keyword//gmd:LocalisedCharacterString[@locale = $langId and text() != '']) > 0">
        <xsl:variable name="listOfKeywords">{
          <xsl:variable name="keywordWithNoThesaurus"
                        select="//gmd:MD_Keywords[
                                  not(gmd:thesaurusName) or gmd:thesaurusName/*/gmd:title/*/text() = '']/
                                    gmd:keyword//gmd:LocalisedCharacterString[@locale=$langId][*/text() != '']"/>
          <xsl:if test="count($keywordWithNoThesaurus) > 0">
            'keywords': [
            <xsl:for-each select="$keywordWithNoThesaurus/(gco:CharacterString|gmx:Anchor)">
              {'value': <xsl:value-of select="concat('''', replace(., '''', '\\'''), '''')"/>,
              'link': '<xsl:value-of select="@xlink:href"/>'}
              <xsl:if test="position() != last()">,</xsl:if>
            </xsl:for-each>
            ]
            <xsl:if test="//gmd:MD_Keywords[gmd:thesaurusName]">,</xsl:if>
          </xsl:if>
          <xsl:for-each-group select="//gmd:MD_Keywords[
                                        gmd:thesaurusName/*/gmd:title/*/text() != '' and
                                        count(gmd:keyword//gmd:LocalisedCharacterString[@locale = $langId and text() != '']) > 0]"
                              group-by="gmd:thesaurusName/*/gmd:title/*/text()">

            '<xsl:value-of select="replace(current-grouping-key(), '''', '\\''')"/>' :[
            <xsl:for-each select="current-group()/gmd:keyword//gmd:LocalisedCharacterString[@locale = $langId and text() != '']">
              {'value': <xsl:value-of select="concat('''', replace(., '''', '\\'''), '''')"/>,
              'link': '<xsl:value-of select="@xlink:href"/>'}
              <xsl:if test="position() != last()">,</xsl:if>
            </xsl:for-each>
            ]
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each-group>
          }
        </xsl:variable>

        <Field name="keywordGroup"
               string="{normalize-space($listOfKeywords)}"
               store="true"
               index="false"/>
      </xsl:if>
      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName//gmd:LocalisedCharacterString[@locale=$langId]">
          <Field name="orgName" string="{string(.)}" store="true" index="true"/>
          <Field name="_orgName" string="{string(.)}" store="true" index="true"/>

          <xsl:variable name="role"    select="../../../../gmd:role/*/@codeListValue"/>
          <xsl:variable name="roleTranslation" select="util:getCodelistTranslation('gmd:CI_RoleCode', string($role), string($isoLangId))"/>
          <xsl:variable name="logo"    select="../../../..//gmx:FileName/@src"/>
          <xsl:variable name="email"   select="../../../../gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString"/>
          <xsl:variable name="phone"   select="../../../../gmd:contactInfo/*/gmd:phone/*/gmd:voice[normalize-space(.) != '']/*/text()"/>
          <xsl:variable name="individualName" select="../../../../gmd:individualName/gco:CharacterString/text()"/>
          <xsl:variable name="positionName"   select="../../../../gmd:positionName/gco:CharacterString/text()"/>
          <xsl:variable name="address" select="string-join(../../../../gmd:contactInfo/*/gmd:address/*/(
                                    gmd:deliveryPoint|gmd:postalCode|gmd:city|
                                    gmd:administrativeArea|gmd:country)/gco:CharacterString/text(), ', ')"/>

          <Field name="responsibleParty"
                 string="{concat($roleTranslation, '|resource|', ., '|', $logo, '|',  string-join($email, ','), '|', $individualName, '|', $positionName, '|', $address, '|', string-join($phone, ','))}"
                 store="true" index="false"/>
      </xsl:for-each>

      <xsl:for-each select="gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString|
  gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualFirstName/gco:CharacterString|
  gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualLastName/gco:CharacterString">
          <Field name="creator" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:choose>
        <xsl:when test="gmd:resourceConstraints/gmd:MD_SecurityConstraints">
          <Field name="secConstr" string="true" store="true" index="true"/>
        </xsl:when>
        <xsl:otherwise>
          <Field name="secConstr" string="false" store="true" index="true"/>
        </xsl:otherwise>
      </xsl:choose>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="gmd:topicCategory/gmd:MD_TopicCategoryCode">
        <Field name="topicCat" string="{string(.)}" store="true" index="true"/>
        <Field name="keyword"
               string="{util:getCodelistTranslation('gmd:MD_TopicCategoryCode', string(.), string($isoLangId))}"
               store="true"
               index="true"/>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="gmd:language/gco:CharacterString">
        <Field name="datasetLang" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="gmd:spatialResolution/gmd:MD_Resolution">
        <xsl:for-each
          select="gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer">
          <Field name="denominator" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:distance/gco:Distance">
          <Field name="distanceVal" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:distance/gco:Distance/@uom">
          <Field name="distanceUom" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:for-each
        select="gmd:graphicOverview/gmd:MD_BrowseGraphic[normalize-space(gmd:fileName/gco:CharacterString) != '']">
        <xsl:variable name="fileName" select="gmd:fileName/gco:CharacterString"/>
        <xsl:variable name="fileDescr" select="gmd:fileDescription/gco:CharacterString"/>
        <xsl:variable name="thumbnailType"
                      select="if (position() = 1) then 'thumbnail' else 'overview'"/>
        <!-- First thumbnail is flagged as thumbnail and could be considered the main one -->
        <Field name="image"
               string="{concat($thumbnailType, '|', $fileName, '|', $fileDescr)}"
               store="true" index="false"/>
      </xsl:for-each>

      <!-- Index aggregation info and provides option to query by type of association
        and type of initiative

      Aggregation info is indexed by adding the following fields to the index:
       * agg_use: boolean
       * agg_with_association: {$associationType}
       * agg_{$associationType}: {$code}
       * agg_{$associationType}_with_initiative: {$initiativeType}
       * agg_{$associationType}_{$initiativeType}: {$code}

      Sample queries:
       * Search for records with siblings: http://localhost:8080/geonetwork/srv/fre/q?agg_use=true
       * Search for records having a crossReference with another record:
       http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference=23f0478a-14ba-4a24-b365-8be88d5e9e8c
       * Search for records having a crossReference with another record:
       http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference=23f0478a-14ba-4a24-b365-8be88d5e9e8c
       * Search for records having a crossReference of type "study" with another record:
       http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference_study=23f0478a-14ba-4a24-b365-8be88d5e9e8c
       * Search for records having a crossReference of type "study":
       http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference_with_initiative=study
       * Search for records having a "crossReference" :
       http://localhost:8080/geonetwork/srv/fre/q?agg_with_association=crossReference
      -->
      <xsl:for-each select="gmd:aggregationInfo/gmd:MD_AggregateInformation">
        <xsl:variable name="code" select="gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString|
                        gmd:aggregateDataSetIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString"/>
        <xsl:if test="$code != ''">
          <xsl:variable name="associationType"
                        select="gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue"/>
          <xsl:variable name="initiativeType"
                        select="gmd:initiativeType/gmd:DS_InitiativeTypeCode/@codeListValue"/>
          <Field name="agg_{$associationType}_{$initiativeType}" string="{$code}" store="false"
                 index="true"/>
          <Field name="agg_{$associationType}_with_initiative" string="{$initiativeType}"
                 store="false" index="true"/>
          <Field name="agg_{$associationType}" string="{$code}" store="true" index="true"/>
          <Field name="agg_associated" string="{$code}" store="false" index="true"/>
          <Field name="agg_with_association" string="{$associationType}" store="false"
                 index="true"/>
          <Field name="agg_use" string="true" store="false" index="true"/>
        </xsl:if>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!--  Fields use to search on Service -->

      <xsl:for-each select="srv:serviceType/gco:LocalName">
        <Field name="serviceType" string="{string(.)}" store="true" index="true"/>
        <Field name="type" string="service-{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="srv:serviceTypeVersion/gco:CharacterString">
        <Field name="serviceTypeVersion" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="//srv:SV_OperationMetadata/srv:operationName/gco:CharacterString">
        <Field name="operation" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="srv:operatesOn/@uuidref">
        <Field name="operatesOn" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="srv:coupledResource">
        <xsl:for-each select="srv:SV_CoupledResource/srv:identifier/gco:CharacterString">
          <Field name="operatesOnIdentifier" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="srv:SV_CoupledResource/srv:operationName/gco:CharacterString">
          <Field name="operatesOnName" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:for-each>

    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- === Content info === -->
    <xsl:for-each select="gmd:contentInfo/*/gmd:featureCatalogueCitation[@uuidref]">
      <Field name="hasfeaturecat" string="{string(@uuidref)}" store="false" index="true"/>
    </xsl:for-each>

    <!-- === Data Quality  === -->
    <xsl:for-each select="gmd:dataQualityInfo/*/gmd:lineage//gmd:source[@uuidref]">
      <Field name="hassource" string="{string(@uuidref)}" store="false" index="true"/>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- === Distribution === -->


    <xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution">
      <xsl:for-each select="gmd:distributionFormat/gmd:MD_Format/gmd:name//gmd:LocalisedCharacterString[@locale=$langId]">
        <Field name="format" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <!-- For local atom feed services -->
      <xsl:if test="count(gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='download'])>0">
        <Field name="has_atom" string="true" store="false" index="true"/>
      </xsl:if>

      <xsl:for-each select="gmd:transferOptions/gmd:MD_DigitalTransferOptions">
        <xsl:variable name="tPosition" select="position()"></xsl:variable>

        <xsl:for-each select="gmd:onLine/gmd:CI_OnlineResource">
          <xsl:variable name="download_check"><xsl:text>&amp;fname=&amp;access</xsl:text></xsl:variable>
          <xsl:variable name="linkage" select="gmd:linkage/gmd:URL" />
          <xsl:variable name="title"
                        select="if (gmd:name/*/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId] != '')
                                then normalize-space(gmd:name/*/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId])
                                else normalize-space(gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType)"/>
          <xsl:variable name="desc"
                        select="if (gmd:description/*/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId] != '')
                                then normalize-space(gmd:description/*/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId])
                                else normalize-space(gmd:description/gco:CharacterString)"/>
          <xsl:variable name="protocol" select="normalize-space(gmd:protocol/gco:CharacterString)"/>
          <xsl:variable name="applicationProfile" select="normalize-space(gmd:applicationProfile/*/text())"/>
          <xsl:variable name="mimetype" select="if ($linkage != '') then geonet:protocolMimeType($linkage, $protocol, gmd:name/gmx:MimeFileType/@type) else ''"/>

          <!-- If the linkage points to WMS service and no protocol specified, manage as protocol OGC:WMS -->
          <xsl:variable name="wmsLinkNoProtocol" select="contains(lower-case($linkage), 'service=wms') and not(string($protocol))" />

          <!-- ignore empty downloads -->
          <xsl:if test="string($linkage)!='' and not(contains($linkage,$download_check))">
            <Field name="protocol" string="{string($protocol)}" store="true" index="true"/>
          </xsl:if>

          <xsl:if test="string($title)!='' and string($desc)!='' and not(contains($linkage,$download_check))">
            <Field name="linkage_name_des" string="{string(concat($title, ':::', $desc))}" store="true" index="true"/>
          </xsl:if>

          <xsl:if test="normalize-space($mimetype)!=''">
            <Field name="mimetype" string="{$mimetype}" store="true" index="true"/>
          </xsl:if>

          <xsl:if test="contains($protocol, 'WWW:DOWNLOAD') or contains($protocol, 'DB')
           or contains($protocol, 'FILE')  or contains($protocol, 'WFS')  or contains($protocol, 'WCS')  or contains($protocol, 'COPYFILE')">
            <Field name="download" string="true" store="false" index="true"/>
            <Field name="_mdActions" string="mdActions-download" store="false" index="true"/>
          </xsl:if>

          <xsl:if test="contains($protocol, 'OGC:WMS') or contains($protocol, 'OGC:WMC') or contains($protocol, 'OGC:OWS')
                    or contains($protocol, 'OGC:OWS-C') or $wmsLinkNoProtocol">
            <Field name="dynamic" string="true" store="false" index="true"/>
            <Field name="_mdActions" string="mdActions-view" store="false" index="true"/>
          </xsl:if>

          <xsl:if test="contains($protocol, 'OGC:WPS')">
            <Field name="_mdActions" string="mdActions-process" store="false" index="true"/>
          </xsl:if>

          <!-- ignore WMS links without protocol (are indexed below with mimetype application/vnd.ogc.wms_xml) -->
          <xsl:if test="not($wmsLinkNoProtocol)">
            <Field name="link" string="{concat($title, '|', $desc, '|', $linkage, '|', $protocol, '|', $mimetype, '|', $tPosition, '|', $applicationProfile)}" store="true" index="false"/>
          </xsl:if>

          <!-- Add KML link if WMS -->
          <xsl:if test="starts-with($protocol,'OGC:WMS') and
                        string($linkage)!='' and string($title)!=''">
            <!-- FIXME : relative path -->
            <Field name="link" string="{concat($title, '|', $desc, '|',
              '../../srv/en/google.kml?uuid=', /gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString, '&amp;layers=', $title,
              '|application/vnd.google-earth.kml+xml|application/vnd.google-earth.kml+xml', '|', $tPosition, '|', $applicationProfile)}" store="true" index="false"/>
          </xsl:if>

          <!-- Try to detect Web Map Context by checking protocol or file extension -->
          <xsl:if test="starts-with($protocol,'OGC:WMC') or contains($linkage,'.wmc')">
            <Field name="link" string="{concat($title, '|', $desc, '|',
              $linkage, '|application/vnd.ogc.wmc|application/vnd.ogc.wmc', '|', $tPosition, '|', $applicationProfile)}" store="true" index="false"/>
          </xsl:if>
          <!-- Try to detect OWS Context by checking protocol or file extension -->
          <xsl:if test="starts-with($protocol,'OGC:OWS-C') or contains($linkage,'.ows')">
            <Field name="link" string="{concat($title, '|', $desc, '|',
            $linkage, '|application/vnd.ogc.ows|application/vnd.ogc.ows', '|', $tPosition, '|', $applicationProfile)}" store="true" index="false"/>
          </xsl:if>

          <xsl:if test="$wmsLinkNoProtocol">
            <Field name="link" string="{concat($title, '|', $desc, '|',
            $linkage, '|OGC:WMS|application/vnd.ogc.wms_xml', '|', $tPosition, '|', $applicationProfile)}" store="true" index="false"/>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:for-each>


    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- === Service stuff ===  -->
    <!-- Service type           -->
    <xsl:for-each select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName|
      gmd:identificationInfo/*[contains(@gco:isoType, 'SV_ServiceIdentification')]/srv:serviceType/gco:LocalName">
      <Field name="serviceType" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>

    <!-- Service version        -->
    <xsl:for-each select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceTypeVersion/gco:CharacterString|
      gmd:identificationInfo/*[contains(@gco:isoType, 'SV_ServiceIdentification')]/srv:serviceTypeVersion/gco:CharacterString">
      <Field name="serviceTypeVersion" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>


    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- === General stuff === -->

    <xsl:for-each select="gmd:metadataStandardName/gco:CharacterString">
        <Field name="standardName" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>

    <xsl:choose>
      <xsl:when test="gmd:hierarchyLevel">
        <xsl:for-each select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue">
          <Field name="type" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <Field name="type" string="dataset" store="true" index="true"/>
      </xsl:otherwise>
    </xsl:choose>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="gmd:hierarchyLevelName//gmd:LocalisedCharacterString[@locale=$langId]">
      <Field name="levelName" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="gmd:fileIdentifier/gco:CharacterString">
      <Field name="fileId" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="gmd:parentIdentifier/gco:CharacterString">
      <Field name="parentUuid" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>
    <Field name="isChild" string="{exists(gmd:parentIdentifier)}" store="true" index="true"/>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- === Reference system info === -->

    <xsl:for-each select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem">
        <xsl:for-each select="gmd:referenceSystemIdentifier/gmd:RS_Identifier">
            <xsl:variable name="crs">
                <xsl:for-each select="gmd:codeSpace/*/text() | gmd:code/*/text()">
                    <xsl:value-of select="."/>
                    <xsl:if test="not(position() = last())">::</xsl:if>
                </xsl:for-each>
            </xsl:variable>

            <xsl:if test="$crs != ''">
                <Field name="crs" string="{$crs}" store="true" index="true"/>
            </xsl:if>

            <xsl:variable name="crsDetails">
            {
              "code": "<xsl:value-of select="gmd:code/*/text()"/>",
              "codeSpace": "<xsl:value-of select="gmd:codeSpace/*/text()"/>",
              "name": "<xsl:value-of select="gmd:code/*/@xlink:title"/>",
              "url": "<xsl:value-of select="gmd:code/*/@xlink:href"/>"
            }
            </xsl:variable>

            <Field name="crsDetails"
                   string="{normalize-space($crsDetails)}"
                   store="true"
                   index="false"/>
        </xsl:for-each>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- === Free text search === -->
    <Field name="any" store="false" index="true">
        <xsl:attribute name="string">
          <xsl:for-each select="//node()[@locale=$langId]">
            <xsl:value-of select="concat(normalize-space(.), ' ')"/>
          </xsl:for-each>
            <xsl:text> </xsl:text>
            <xsl:for-each select="//@codeListValue">
                <xsl:value-of select="concat(., ' ')"/>
            </xsl:for-each>
        </xsl:attribute>
    </Field>

    <xsl:variable name="identification" select="gmd:identificationInfo//gmd:MD_DataIdentification|
                    gmd:identificationInfo//*[contains(@gco:isoType, 'MD_DataIdentification')]|
                    gmd:identificationInfo/srv:SV_ServiceIdentification"/>

    <Field name="anylight" store="false" index="true">
        <xsl:attribute name="string">
            <xsl:for-each
                    select="$identification/gmd:citation/gmd:CI_Citation/gmd:title//gmd:LocalisedCharacterString[@locale=$langId]|
                $identification/gmd:citation/gmd:CI_Citation/gmd:alternateTitle//gmd:LocalisedCharacterString[@locale=$langId]|
                $identification/gmd:abstract//gmd:LocalisedCharacterString[@locale=$langId]|
                $identification/gmd:credit//gmd:LocalisedCharacterString[@locale=$langId]|
                $identification//gmd:organisationName//gmd:LocalisedCharacterString[@locale=$langId]|
                $identification/gmd:supplementalInformation//gmd:LocalisedCharacterString[@locale=$langId]|
                $identification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword//gmd:LocalisedCharacterString[@locale=$langId]">
                <xsl:value-of select="concat(., ' ')"/>
            </xsl:for-each>
        </xsl:attribute>
    </Field>

    <!-- Index all codelist -->
    <xsl:for-each select=".//*[*/@codeListValue != '']">
        <Field name="cl_{local-name()}"
               string="{*/@codeListValue}"
               store="true" index="true"/>
        <Field name="cl_{concat(local-name(), '_text')}"
               string="{util:getCodelistTranslation(name(*), string(*/@codeListValue), string($isoLangId))}"
               store="true" index="true"/>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
