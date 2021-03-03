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


  <xsl:variable name="isStoringOverviewInIndex" select="true()"/>


  <!-- A date, dateTime, Year or Year and Month
  Valid with regards to index date supported types:
  date_optional_time||yyyy-MM-dd||yyyy-MM||yyyy||epoch_millis
  -->
  <xsl:function name="gn-fn-index:is-isoDate" as="xs:boolean">
    <xsl:param name="value" as="xs:string?"/>
    <xsl:value-of select="if ($value castable as xs:date
                          or $value castable as xs:dateTime
                          or matches($value, '[0-9]{4}(-[0-9]{2})?'))
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
    <xsl:copy-of select="gn-fn-index:add-multilingual-field($fieldName, $elements, $languages, false())"/>
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
   -->
  <xsl:function name="gn-fn-index:add-multilingual-field" as="node()*">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="elements" as="node()*"/>
    <xsl:param name="languages" as="node()?"/>
    <xsl:param name="asJson" as="xs:boolean?"/>

    <xsl:variable name="mainLanguage"
                  select="$languages/lang[@id='default']/@value"/>

<!--    <xsl:message>gn-fn-index:add-field <xsl:value-of select="$fieldName"/></xsl:message>-->
<!--    <xsl:message>gn-fn-index:add-field languages <xsl:copy-of select="$languages"/></xsl:message>-->

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

            <xsl:for-each select="$element//*:LocalisedCharacterString[. != '']">
              <xsl:variable name="elementLanguage"
                            select="replace(@locale, '#', '')"/>
              <xsl:variable name="elementLanguage3LetterCode"
                            select="$languages/lang[@id = $elementLanguage]/@value"/>

              <xsl:if test="$elementLanguage3LetterCode != ''">
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

      <xsl:if test="$textObject != ''">
        <xsl:choose>
          <xsl:when test="$asJson">
            <xsl:if test="$isArray and position() = 1">[</xsl:if>
            {<xsl:value-of select="string-join($textObject/text(), ', ')"/>}
            <xsl:if test="$isArray and position() != last()">,</xsl:if>
            <xsl:if test="$isArray and position() = last()">]</xsl:if>
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


  <xsl:template name="build-thesaurus-fields">
    <xsl:param name="thesaurus" as="xs:string"/>
    <xsl:param name="thesaurusId" as="xs:string"/>
    <xsl:param name="keywords" as="node()*"/>
    <xsl:param name="mainLanguage" as="xs:string?"/>
    <xsl:param name="allLanguages" as="node()?"/>

    <!-- Index keyword characterString including multilingual ones
     and element like gmx:Anchor including the href attribute
     which may contains keyword identifier. -->
    <xsl:variable name="thesaurusField"
                  select="concat('th_', replace($thesaurus, '[^a-zA-Z0-9-_]', ''))"/>

    <xsl:element name="{$thesaurusField}Number">
      <xsl:value-of select="count($keywords)"/>
    </xsl:element>

    <xsl:if test="count($keywords) > 0">
      <xsl:element name="{$thesaurusField}">
        <xsl:attribute name="type" select="'object'"/>
        [<xsl:for-each select="$keywords">
        <xsl:value-of select="gn-fn-index:add-multilingual-field('keyword', ., $allLanguages)/text()"/>
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>]
      </xsl:element>

      <xsl:variable name="thesaurusTree" as="node()">
        <values>
          <xsl:for-each select="$keywords">
            <xsl:variable name="nodes" as="node()*">
              <xsl:copy-of select="*:CharacterString/text()
                                          |*:Anchor/text()
                                          |*:Anchor/@xlink:href"/>
              <xsl:if test="not(*:Anchor)">
                <xsl:variable name="uri"
                              select="util:getKeywordUri((*/text())[1], $thesaurusId, $mainLanguage)"/>
                <xsl:if test="$uri != ''">
                  <xsl:attribute name="xlink:href" select="$uri"/>
                </xsl:if>
              </xsl:if>
            </xsl:variable>

            <xsl:for-each select="$nodes">
              <xsl:variable name="keywordTree" as="node()*">
                <xsl:call-template name="get-keyword-tree-values">
                  <xsl:with-param name="keyword"
                                  select="."/>
                  <xsl:with-param name="thesaurus"
                                  select="$thesaurusId"/>
                  <xsl:with-param name="language"
                                  select="$mainLanguage"/>
                </xsl:call-template>
              </xsl:variable>

              <xsl:variable name="type"
                            select="if (name() = 'xlink:href') then 'key' else 'default'"/>
              <xsl:for-each select="$keywordTree[. != '']">
                <xsl:element name="{$type}">
                  <xsl:value-of select="concat($doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>
                </xsl:element>
              </xsl:for-each>
            </xsl:for-each>
          </xsl:for-each>
        </values>
      </xsl:variable>

      <xsl:if test="count($thesaurusTree/*) > 0">
        <xsl:element name="{$thesaurusField}_tree">
          <xsl:attribute name="type" select="'object'"/>{
          <xsl:variable name="defaults"
                        select="distinct-values($thesaurusTree/default)"/>
          <xsl:variable name="keys"
                        select="distinct-values($thesaurusTree/key)"/>

          <xsl:if test="count($defaults) > 0">"default": [
            <xsl:for-each select="$defaults">
              <xsl:sort select="."/>
              <xsl:value-of select="."/><xsl:if test="position() != last()">,</xsl:if>
            </xsl:for-each>
            ]<xsl:if test="count($keys) > 0">,</xsl:if>
          </xsl:if>
          <xsl:if test="count($keys) > 0">"key": [
            <xsl:for-each select="$keys">
              <xsl:sort select="."/>
              <xsl:value-of select="."/><xsl:if test="position() != last()">,</xsl:if>
            </xsl:for-each>
            ]
          </xsl:if>
          }</xsl:element>
      </xsl:if>
    </xsl:if>
  </xsl:template>


  <!-- Deprecated -->
  <xsl:template name="build-tree-values">
    <xsl:param name="values"/>
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="thesaurus" as="xs:string"/>
    <xsl:param name="language" as="xs:string?" select="'eng'"/>
    <xsl:param name="allTreeField" as="xs:boolean"/>

    <xsl:variable name="paths">
      <xsl:for-each select="$values">
        <xsl:variable name="keywordsWithHierarchy"
                      select="util:getKeywordHierarchy(normalize-space(.), $thesaurus, $language)"/>

        <xsl:if test="count($keywordsWithHierarchy) > 0">
          <xsl:for-each select="$keywordsWithHierarchy">
            <xsl:variable name="path" select="tokenize(., '\^')"/>
            <xsl:for-each select="$path">
              <xsl:variable name="position"
                            select="position()"/>
              <value><xsl:value-of select="string-join($path[position() &lt;= $position], '^')"/></value>
            </xsl:for-each>
          </xsl:for-each>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:for-each-group select="$paths/*" group-by=".">
      <xsl:sort select="."/>
      <xsl:if test="$fieldName != ''">
        <xsl:element name="{$fieldName}">
          <xsl:value-of select="."/>
        </xsl:element>
      </xsl:if>

      <xsl:if test="$allTreeField">
        <xsl:element name="keywords_tree">
          <xsl:value-of select="."/>
        </xsl:element>
      </xsl:if>
    </xsl:for-each-group>
  </xsl:template>

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
