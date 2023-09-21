<?xml version="1.0" encoding="UTF-8" ?>
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
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:gn-fn-index="http://geonetwork-opensource.org/xsl/functions/index"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:variable name="dateFormat" as="xs:string"
                select="'[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01][ZN]'"/>

  <xsl:variable name="separator" as="xs:string"
                select="'|'"/>

  <xsl:variable name="doubleQuote">"</xsl:variable>

  <xsl:variable name="escapedDoubleQuote">\\"</xsl:variable>

  <!-- To avoid Document contains at least one immense term
  in field="resourceAbstract" (whose UTF8 encoding is longer
  than the max length 32766. -->
  <xsl:variable name="maxFieldLength" select="32000" as="xs:integer"/>

  <!-- List of keywords to search for to flag a record as opendata.
   Do not put accents or upper case letters here as comparison will not
   take them in account. -->
  <xsl:variable name="openDataKeywords"
                select="'opendata|open data|donnees ouvertes'"/>

  <xsl:variable name="isStoringOverviewInIndex" select="false()"/>


  <!-- A date, dateTime, Year or Year and Month
  Valid with regards to index date supported types:
  date_optional_time||yyyy-MM-dd||yyyy-MM||yyyy||epoch_millis
  -->
  <xsl:function name="gn-fn-index:is-isoDate" as="xs:boolean">
    <xsl:param name="value" as="xs:string?"/>
    <xsl:value-of select="if ($value castable as xs:date
                          or $value castable as xs:dateTime
                          or matches($value, '^[0-9]{4}$|^[0-9]{4}-(0[1-9]|1[012])$'))
                          then true() else false()"/>
  </xsl:function>

  <!-- 2020-12-12 -->
  <xsl:function name="gn-fn-index:is-date" as="xs:boolean">
    <xsl:param name="value" as="xs:string?"/>
    <xsl:value-of select="if ($value castable as xs:date)
                          then true() else false()"/>
  </xsl:function>


  <!-- 2020-12-12T12:00:00 -->
  <xsl:function name="gn-fn-index:is-dateTime" as="xs:boolean">
    <xsl:param name="value" as="xs:string?"/>
    <xsl:value-of select="if ($value castable as xs:dateTime)
                          then true() else false()"/>
  </xsl:function>


  <xsl:function name="gn-fn-index:add-field" as="node()*">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="fieldValue" as="xs:string?"/>

    <xsl:element name="{$fieldName}">
      <xsl:value-of select="$fieldValue"/>
    </xsl:element>
  </xsl:function>

  <!-- Add a JSON object field in the index.
  A JSON object field is inserted here with JSON properly formatted
  as a String. The java part then parse the JSON as an object
  before sending it to the index. -->
  <xsl:function name="gn-fn-index:add-object-field" as="node()*">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="jsonObjectAsString" as="xs:string"/>

    <xsl:element name="{$fieldName}">
      <xsl:attribute name="type" select="'object'"/>
      <xsl:value-of select="$jsonObjectAsString"/>
    </xsl:element>
  </xsl:function>

  <xsl:function name="gn-fn-index:build-record-link" as="node()?">
    <xsl:param name="uuid" as="xs:string"/>
    <xsl:param name="url" as="xs:string?"/>
    <xsl:param name="title" as="xs:string?"/>
    <xsl:param name="type" as="xs:string"/>
    <xsl:variable name="properties" as="node()">
      <properties/>
    </xsl:variable>
    <xsl:copy-of select="gn-fn-index:build-record-link($uuid, $url, $title, $type, $properties)"/>
  </xsl:function>

  <xsl:function name="gn-fn-index:build-record-link" as="node()?">
    <xsl:param name="uuid" as="xs:string"/>
    <xsl:param name="url" as="xs:string?"/>
    <xsl:param name="title" as="xs:string?"/>
    <xsl:param name="type" as="xs:string"/>
    <xsl:param name="otherProperties" as="node()?"/>

    <xsl:variable name="siteUrl" select="util:getSiteUrl()" />

    <xsl:variable name="origin"
                  select="if ($url = '')
                          then 'catalog'
                          else if ($url != '' and
                                   not(starts-with($url, $siteUrl)))
                            then 'remote'
                          else 'catalog'"/>

    <xsl:variable name="recordTitle"
                  select="if ($title != '' ) then $title
                          else util:getIndexField(
                                '',
                                $uuid,
                                'resourceTitleObject',
                                '')"/>
    <recordLink type="object">{
      "type": "<xsl:value-of select="normalize-space($type)"/>",
      <xsl:for-each select="$otherProperties//p[@name != '']">
        "<xsl:value-of select="@name"/>": "<xsl:value-of select="@value"/>",
      </xsl:for-each>
      "to": "<xsl:value-of select="normalize-space($uuid)"/>",
      "url": "<xsl:value-of select="normalize-space($url)"/>",
      "title": "<xsl:value-of select="gn-fn-index:json-escape($recordTitle)"/>",
      "origin": "<xsl:value-of select="normalize-space($origin)"/>"
      }</recordLink>
  </xsl:function>

  <!-- Add a multilingual field to the index.

   Function can produce an XML document or a JSON format properties
   when translations have to be added to an object field.
   -->
  <xsl:function name="gn-fn-index:add-multilingual-field" as="node()*">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="elements" as="node()*"/>
    <xsl:param name="languages" as="node()?"/>
    <xsl:copy-of select="gn-fn-index:add-multilingual-field($fieldName, $elements, $languages, false(), false())"/>
  </xsl:function>

  <xsl:function name="gn-fn-index:add-multilingual-field" as="node()*">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="elements" as="node()*"/>
    <xsl:param name="languages" as="node()?"/>
    <xsl:param name="asJson" as="xs:boolean?"/>
    <xsl:copy-of select="gn-fn-index:add-multilingual-field($fieldName, $elements, $languages, $asJson, false())"/>
  </xsl:function>

  <!--
   Multilingual fields are stored as an object.
   ```json
   {
    default: "Français", -> The default language
    langfre: "Français", -> The default language is the first property
    langeng: "English",
    ...
    (optional) link: "http://" -> Anchor xlink:href attribute
   }
   ```


    A multilingual field in ISO19139 looks like:
    ```xml
    <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
      <gco:CharacterString|gmx:Anchor xlink:href="http">Template for Vector data in ISO19139 (multilingual)</gco:CharacterString>
      <gmd:PT_FreeText>
        <gmd:textGroup>
          <gmd:LocalisedCharacterString locale="#FRE">Modèle de données vectorielles en
            ISO19139 (multilingue)
          </gmd:LocalisedCharacterString>
        </gmd:textGroup>
      ```

      A multilingual record in Dublin core
      ```xml
      <dc:title xml:lang="en">...
      <dc:title xml:lang="fr">...
      ```

      Use this function in 2 modes:
      * Adding a new field (using copy-of because output is an XML element.
      ```xsl
      <xsl:copy-of select="gn-fn-index:add-multilingual-field(
                            $roleField, $organisationName, $languages)"/>
      ```

      * Populating a JSON property in an existing object (using value-of, output is text)
      If the element is empty, `{}` is returned.
      ```xsl
      "organisationObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                              'organisation', $organisationName, $languages, true())"/>,
      ```
   -->
  <xsl:function name="gn-fn-index:add-multilingual-field" as="node()*">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="elements" as="node()*"/>
    <xsl:param name="languages" as="node()?"/>
    <!-- Return the JSON object directly if true, wrap it in an element if false. -->
    <xsl:param name="asJson" as="xs:boolean?"/>
    <xsl:param name="asXml" as="xs:boolean?"/>

    <xsl:variable name="mainLanguage"
                  select="$languages/lang[@id='default']/@value"/>

    <!--<xsl:message>gn-fn-index:add-field <xsl:value-of select="$fieldName"/></xsl:message>
    <xsl:message>gn-fn-index:add-field elements <xsl:copy-of select="$elements"/></xsl:message>
    <xsl:message>gn-fn-index:add-field languages <xsl:copy-of select="$languages"/></xsl:message>-->

    <xsl:variable name="isArray"
                  select="count($elements[not(@xml:lang)]) > 1"/>
    <xsl:for-each select="$elements">
      <xsl:variable name="element" select="."/>
      <xsl:variable name="textObject" as="node()*">
        <xsl:choose>
          <!-- Not ISO but multilingual eg. DC or DCAT -->
          <xsl:when test="$languages and count($element//(*:CharacterString|*:Anchor|*:LocalisedCharacterString)) = 0">
            <xsl:if test="position() = 1">
              <value><xsl:value-of select="concat($doubleQuote, 'default', $doubleQuote, ':',
                                             $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/></value>
              <xsl:for-each select="$elements">
                <value><xsl:value-of select="concat($doubleQuote, 'lang', @xml:lang, $doubleQuote, ':',
                                             $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/></value>
              </xsl:for-each>
            </xsl:if>
          </xsl:when>
          <xsl:when test="$languages">
            <!-- The default language -->
            <xsl:for-each select="$element//(*:CharacterString|*:Anchor)[. != '']">
              <value><xsl:value-of select="concat($doubleQuote, 'default', $doubleQuote, ':',
                                           $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/></value>
              <value><xsl:value-of select="concat($doubleQuote, 'lang', $mainLanguage, $doubleQuote, ':',
                                           $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/></value>
            </xsl:for-each>

            <xsl:variable name="translations"
                          select="$element//*:LocalisedCharacterString[. != '']"/>

            <xsl:if test="count($element//(*:CharacterString|*:Anchor)[. != '']) = 0
                          and count($translations) > 0">

              <xsl:variable name="mainLanguageId"
                            select="concat('#', $languages/lang[@id != 'default' and @value = $mainLanguage]/@id)"/>

              <value><xsl:value-of select="concat($doubleQuote, 'default', $doubleQuote, ':',
                                           $doubleQuote, gn-fn-index:json-escape(
                                           if ($translations[@local = $mainLanguageId])
                                           then $translations[@local = $mainLanguageId]
                                           else $translations[1]), $doubleQuote)"/></value>
            </xsl:if>

            <xsl:for-each select="$translations">
              <xsl:variable name="elementLanguage"
                            select="replace(@locale, '#', '')"/>
              <xsl:variable name="elementLanguage3LetterCode"
                            select="$languages/lang[@id = $elementLanguage]/@value"/>
              <xsl:if test="$elementLanguage3LetterCode != '' and ($elementLanguage3LetterCode !=$mainLanguage or count($element//(*:CharacterString|*:Anchor)[. != ''])=0) ">
                <xsl:variable name="field"
                              select="concat('lang', $elementLanguage3LetterCode)"/>
                <value><xsl:value-of select="concat(
                                        $doubleQuote, $field, $doubleQuote, ':',
                                        $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/></value>
              </xsl:if>
            </xsl:for-each>

          </xsl:when>
          <xsl:otherwise>
            <!-- Index each values in a field. -->
            <xsl:for-each select="distinct-values($element[. != ''])">
              <value><xsl:value-of select="concat($doubleQuote, 'default', $doubleQuote, ':',
                                           $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/></value>
              <value><xsl:value-of select="concat($doubleQuote, 'lang', $mainLanguage, $doubleQuote, ':',
                                           $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/></value>
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:for-each select="$element//*:Anchor/@xlink:href">
          <value><xsl:value-of select="concat($doubleQuote, 'link', $doubleQuote, ':',
                                           $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/></value>
        </xsl:for-each>
      </xsl:variable>

      <xsl:if test="count($textObject[. != '']) > 0 or $asJson">
        <xsl:choose>
          <xsl:when test="$asJson">
            <xsl:if test="$isArray and position() = 1">[</xsl:if>
            {<xsl:value-of select="string-join($textObject/text(), ', ')"/>}
            <xsl:if test="$isArray and position() != last()">,</xsl:if>
            <xsl:if test="$isArray and position() = last()">]</xsl:if>
          </xsl:when>
          <xsl:when test="$asXml">
            <xsl:copy-of select="$textObject"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:element name="{$fieldName}Object">
              <xsl:attribute name="type" select="'object'"/>
              {<xsl:value-of select="string-join($textObject/text(), ', ')"/>}
            </xsl:element>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:for-each>
  </xsl:function>


  <!-- Convert to ASCII,
       Replace . by -,
       Keep only letters, numbers and _ and -. -->
  <xsl:function name="gn-fn-index:build-field-name">
    <xsl:param name="value"/>

    <xsl:value-of select="replace(
                            replace(
                              replace(
                                normalize-unicode($value, 'NFKD'),
                                '\P{IsBasicLatin}', '')
                              , '\.', '-'),
                            '[^a-zA-Z0-9_-]', '')"/>
  </xsl:function>


  <!-- Template to build the following index fields for the metadata keywords:
          - tag: contains all the keywords.
          - tagNumber: total number of keywords.
          - isOpenData: checks if any keyword is defined in openDataKeywords to flag it as open data.
          - keywordType-{TYPE}: Index field per keyword type (examples: keywordType-theme, keywordType-place).
          - th_{THESAURUSID}: Field with keywords of a thesaurus, eg. th_regions
          - th_{THESAURUSID}Number: Field with keywords of a thesaurus, eg. th_regionsNumber
          - allKeywords: Object field with all thesaurus and all keywords.
          - {THESAURUSID}_tree: Object with keywords tree per thesaurus.
  -->
  <xsl:template name="build-all-keyword-fields" as="node()*">
    <xsl:param name="allKeywords" as="node()?"/>

    <!-- Build global tag field -->
    <tag type="object">
      [<xsl:for-each select="$allKeywords//keyword">
      {
      <xsl:value-of select="string-join(values/value, ', ')"/>
      <xsl:if test="@uri != ''">, "key": "<xsl:value-of select="@uri"/>"</xsl:if>
      }
      <xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>]
    </tag>

    <!-- Total number of keywords -->
    <tagNumber>
      <xsl:value-of select="count($allKeywords//keyword)"/>
    </tagNumber>

    <!-- Checks if any keyword is defined in openDataKeywords to flag it as open data -->
    <isOpenData>
      <xsl:value-of select="count(
                        $allKeywords//keyword/values/value[matches(
                          normalize-unicode(
                            replace(
                              normalize-unicode(
                                lower-case(normalize-space(text())),
                                'NFKD'),
                            '\p{Mn}', ''),
                          'NFKC'),
                        $openDataKeywords)]) > 0"/></isOpenData>


    <!-- Build index field for type
    keywordType-place: [{default: France}]-->
    <xsl:for-each-group select="$allKeywords"
                        group-by="thesaurus/info/@type">
      <xsl:if test="matches(current-grouping-key(), '^[A-Za-z\-_]+$')">
        <xsl:element name="keywordType-{current-grouping-key()}">
          <xsl:attribute name="type" select="'object'"/>
          [<xsl:for-each select="$allKeywords/thesaurus[info/@type = current-grouping-key()]/keywords/keyword">
          {
          <xsl:value-of select="string-join(values/value, ', ')"/>
          <xsl:if test="@uri != ''">, "link": "<xsl:value-of select="@uri"/>"</xsl:if>
          }
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>]
        </xsl:element>
      </xsl:if>
    </xsl:for-each-group>

    <!-- Fields with keywords and keyword count of a thesaurus, eg. th_regions, th_regionsNumber -->
    <xsl:for-each select="$allKeywords/thesaurus[info/@field]">
      <!-- Keyword count of a thesaurus -->
      <xsl:element name="{info/@field}Number">
        <xsl:value-of select="count(keywords/keyword)"/>
      </xsl:element>

      <!-- Keywords of a thesaurus -->
      <xsl:element name="{info/@field}">
        <xsl:attribute name="type" select="'object'"/>
        [<xsl:for-each select="keywords/keyword">
        {
        <xsl:value-of select="string-join(values/value, ', ')"/>
        <xsl:if test="@uri != ''">, "link": "<xsl:value-of select="@uri"/>"</xsl:if>
        }
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>]
      </xsl:element>
    </xsl:for-each>

    <!-- Object field with all thesaurus and all keywords. -->
    <allKeywords type="object">{
      <xsl:for-each select="$allKeywords/thesaurus[info/@field]">
        "<xsl:value-of select="if (info/@field != '') then info/@field else 'otherKeywords'"/>": {
        <xsl:if test="info/@id != ''">
          "id": "<xsl:value-of select="gn-fn-index:json-escape(info/@id)"/>",
        </xsl:if>
        "title": "<xsl:value-of select="gn-fn-index:json-escape(info/@title)"/>",
        "theme": "<xsl:value-of select="gn-fn-index:json-escape(info/@type)"/>",
        <xsl:if test="info/@uri != ''">
          "link": "<xsl:value-of select="gn-fn-index:json-escape(info/@uri)"/>",
        </xsl:if>
        "keywords": [
        <xsl:for-each select="keywords/keyword">
          {
          <xsl:value-of select="string-join(values/value, ', ')"/>
          <xsl:if test="@uri != ''">, "link": "<xsl:value-of select="@uri"/>"</xsl:if>
          }
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>
        ]}
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>
      }
    </allKeywords>

    <!-- Object with keywords tree per thesaurus -->
    <xsl:for-each select="$allKeywords/thesaurus[keywords/keyword/tree/*/value]">
      <xsl:element name="{info/@field}_tree">
        <xsl:attribute name="type" select="'object'"/>{
        <xsl:variable name="defaults"
                      select="distinct-values(keywords/keyword/tree/defaults/value)"/>
        <xsl:variable name="keys"
                      select="distinct-values(keywords/keyword/tree/keys/value)"/>

        <xsl:if test="count($defaults) > 0">"default": [
          <xsl:for-each select="$defaults">
            <xsl:sort select="."/>
            <xsl:value-of select="concat($doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/><xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          ]<xsl:if test="count($keys) > 0">,</xsl:if>
        </xsl:if>
        <xsl:if test="count($keys) > 0">"key": [
          <xsl:for-each select="$keys">
            <xsl:sort select="."/>
            <xsl:value-of select="concat($doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/><xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          ]
        </xsl:if>
        }</xsl:element>
    </xsl:for-each>

    <xsl:for-each select="$allKeywords//indexingErrorMsg">
      <indexingErrorMsg><xsl:value-of select="."/></indexingErrorMsg>
    </xsl:for-each>
  </xsl:template>

  <!--

      <spatialRepresentationType>
        <MD_SpatialRepresentationTypeCode codeListValue="vector"
                                          codeList="./resources/codeList.xml#MD_SpatialRepresentationTypeCode"/>


  "cl_spatialRepresentationType" : {
    "key": "grid",
    "default": "Grid",
    "langeng": "Grid",
    "langfre": "Grid",
    "text": "", > inner text of the element,
    "link": "./resources/codeList.xml#MD_SpatialRepresentationTypeCode",
  }
  -->
  <xsl:function name="gn-fn-index:add-codelist-field" as="node()*">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="value" as="node()"/>
    <xsl:param name="languages" as="node()?"/>
    <xsl:copy-of select="gn-fn-index:add-codelist-field($fieldName, $value, $languages, false())"/>
  </xsl:function>

  <xsl:function name="gn-fn-index:add-codelist-field" as="node()*">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="value" as="node()"/>
    <xsl:param name="languages" as="node()?"/>
    <xsl:param name="asJson" as="xs:boolean?"/>

    <xsl:variable name="mainLanguage"
                  select="$languages/lang[@id = 'default']/@value"/>
    <xsl:variable name="codelistType"
                  select="$value/name()"/>

<!--    <xsl:message>gn-fn-index:add-codelist <xsl:value-of select="$fieldName"/></xsl:message>-->
<!--    <xsl:message>gn-fn-index:add-codelist value <xsl:copy-of select="$value"/></xsl:message>-->
<!--    <xsl:message>gn-fn-index:add-codelist languages <xsl:copy-of select="$languages"/></xsl:message>-->
<!--    <xsl:message>gn-fn-index:add-codelist type <xsl:copy-of select="$codelistType"/></xsl:message>-->

    <xsl:variable name="textObject">
      <!-- The codelist key -->
      <xsl:value-of select="concat($doubleQuote, 'key', $doubleQuote, ':',
                                       $doubleQuote, gn-fn-index:json-escape($value/@codeListValue), $doubleQuote)"/>

      <xsl:variable name="translation"
                    select="util:getCodelistTranslation(
                          string($codelistType), string($value/@codeListValue), string($mainLanguage))"/>

      <xsl:value-of select="concat(',', $doubleQuote, 'default', $doubleQuote, ':',
                                       $doubleQuote, gn-fn-index:json-escape($translation), $doubleQuote)"/>
      <xsl:value-of select="concat(',', $doubleQuote, 'lang', $mainLanguage, $doubleQuote, ':',
                                     $doubleQuote, gn-fn-index:json-escape($translation), $doubleQuote)"/>


      <xsl:for-each select="$languages/lang[@id != 'default']/@value">
        <xsl:variable name="translation"
                      select="util:getCodelistTranslation(
                        string($codelistType), string($value/@codeListValue), string(.))"/>
        <xsl:value-of select="concat(',', $doubleQuote, 'lang', ., $doubleQuote, ':',
                                   $doubleQuote, gn-fn-index:json-escape($translation), $doubleQuote)"/>
      </xsl:for-each>

      <xsl:for-each select="$value/@codeList">
        <xsl:value-of select="concat(',', $doubleQuote, 'link', $doubleQuote, ':',
                                         $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>
      </xsl:for-each>
      <xsl:for-each select="$value/text()">
        <xsl:value-of select="concat(',', $doubleQuote, 'text', $doubleQuote, ':',
                                         $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:if test="$textObject != ''">
      <xsl:choose>
        <xsl:when test="$asJson">
          {<xsl:value-of select="$textObject"/>}
        </xsl:when>
        <xsl:otherwise>
          <xsl:element name="{$fieldName}">
            <xsl:attribute name="type" select="'object'"/>
            {<xsl:value-of select="$textObject"/>}
          </xsl:element>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:function>


  <!-- Return all paths based on broader/narrower relation up to top.
  If keyword is a label, returns labels, if URI, returns URIs
  -->
  <xsl:template name="get-keyword-tree-values" as="node()*">
    <xsl:param name="keyword" as="xs:string"/>
    <xsl:param name="thesaurus" as="xs:string"/>
    <xsl:param name="language" as="xs:string?" select="'eng'"/>

    <xsl:variable name="paths" as="node()*">
      <xsl:variable name="keywordsWithHierarchy"
                    select="util:getKeywordHierarchy(
                              normalize-space($keyword), $thesaurus, $language)"/>

      <xsl:for-each select="$keywordsWithHierarchy">
        <xsl:variable name="path" select="tokenize(., '\^')"/>
        <xsl:for-each select="$path">
          <xsl:variable name="position"
                        select="position()"/>
          <value><xsl:value-of select="string-join($path[position() &lt;= $position], '^')"/></value>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:variable>

    <xsl:copy-of select="$paths"/>
  </xsl:template>


  <xsl:template name="build-range-details">
    <xsl:param name="start" as="node()?"/>
    <xsl:param name="end" as="node()?"/>

    <xsl:variable name="rangeStartDetails">
      <xsl:if test="$start/text() castable as xs:date
                    or $start/text() castable as xs:dateTime
                    or $start/text() castable as xs:gYearMonth
                    or $start/text() castable as xs:gYear">
        <value><xsl:value-of select="concat('&quot;date&quot;: &quot;', $start/text(), '&quot;')"/></value>
      </xsl:if>
      <xsl:for-each select="$start/@*[. != '']">
        <value><xsl:value-of select="concat('&quot;', name(.), '&quot;: &quot;', gn-fn-index:json-escape(.), '&quot;')"/></value>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="rangeEndDetails">
      <xsl:if test="$end/text() castable as xs:date
                    or $end/text() castable as xs:dateTime
                    or $end/text() castable as xs:gYearMonth
                    or $end/text() castable as xs:gYear">
        <value><xsl:value-of select="concat('&quot;date&quot;: &quot;', $end/text(), '&quot;')"/></value>
      </xsl:if>
      <xsl:for-each select="$end/@*[. != '']">
        <value><xsl:value-of select="concat('&quot;', name(.), '&quot;: &quot;', gn-fn-index:json-escape(.), '&quot;')"/></value>
      </xsl:for-each>
    </xsl:variable>

    <xsl:if test="count($rangeStartDetails/value) > 0 or count($rangeEndDetails/value) > 0">
      <resourceTemporalExtentDetails type="object">{
        "start": {
        <xsl:value-of select="string-join($rangeStartDetails/value, ',')"/>
        },
        "end": {
        <xsl:value-of select="string-join($rangeEndDetails/value, ',')"/>
        }
        }</resourceTemporalExtentDetails>
    </xsl:if>
  </xsl:template>



  <!-- Produce a thesaurus field name valid in an XML document
  and as an Elasticsearch field name. -->
  <xsl:function name="gn-fn-index:build-thesaurus-index-field-name">
    <xsl:param name="thesaurusId" as="xs:string?"/>
    <xsl:param name="thesaurusName" as="xs:string?"/>

    <xsl:variable name="oldFieldNameMapping" as="node()*">
      <!-- INSPIRE themes are loaded from INSPIRE registry. The thesaurus key changed. -->
      <thesaurus old="th_inspire-theme"
                 new="th_httpinspireeceuropaeutheme-theme"/>
      <thesaurus old="th_SpatialScope"
                 new="th_httpinspireeceuropaeumetadatacodelistSpatialScope-SpatialScope"/>
    </xsl:variable>

    <xsl:variable name="key">
      <xsl:choose>
        <xsl:when test="starts-with($thesaurusId, 'geonetwork.thesaurus')">
          <!-- eg. geonetwork.thesaurus.local.theme.dcsmm.area = dcsmm.area-->
          <xsl:value-of select="string-join(
                                  tokenize($thesaurusId, '\.')[position() > 4], '.')"/>
        </xsl:when>
        <xsl:when test="normalize-space($thesaurusId) != ''">
          <xsl:value-of select="normalize-space($thesaurusId)"/>
        </xsl:when>
        <xsl:when test="normalize-space($thesaurusName) != ''">
          <xsl:value-of select="replace(normalize-unicode($thesaurusName, 'NFKD'), '\P{IsBasicLatin}', '')"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="keyWithoutDot"
                  select="replace($key, '\.', '-')"/>

    <xsl:variable name="fieldName"
                  select="concat('th_', replace($keyWithoutDot, '[^a-zA-Z0-9_-]', ''))"/>

    <xsl:value-of select="if($oldFieldNameMapping[@old = $fieldName])
                          then $oldFieldNameMapping[@old = $fieldName]/@new
                          else $fieldName"/>
  </xsl:function>


  <xsl:function name="gn-fn-index:json-escape" as="xs:string?">
    <xsl:param name="v" as="xs:string?"/>
    <xsl:choose>
      <xsl:when test="normalize-space($v) = ''"></xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="replace(replace(replace(replace(replace($v,
                  '\\','\\\\'),
                  $doubleQuote, $escapedDoubleQuote),
                  '&#09;', '\\t'),
                  '&#10;', '\\n'),
                  '&#13;', '\\r')"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
</xsl:stylesheet>
