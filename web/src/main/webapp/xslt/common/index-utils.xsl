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
                select="'[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]'"/>

  <xsl:variable name="separator" as="xs:string"
                select="'|'"/>

  <xsl:variable name="doubleQuote">"</xsl:variable>

  <xsl:variable name="escapedDoubleQuote">\\"</xsl:variable>

  <!-- To avoid Document contains at least one immense term
  in field="resourceAbstract" (whose UTF8 encoding is longer
  than the max length 32766. -->
  <xsl:variable name="maxFieldLength" select="32000" as="xs:integer"/>


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
   A multilingual field is composed of one root field
   with the default language value. Then one field per language
   is added with a suffix "_lang{{iso3letterLangCode}}".

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
   -->
  <xsl:function name="gn-fn-index:add-multilingual-field" as="node()*">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="elements" as="node()*"/>
    <xsl:param name="languages" as="node()?"/>
    <xsl:param name="asJson" as="xs:boolean?"/>

    <xsl:variable name="mainLanguage"
                  select="$languages/lang[@id='default']/@value"/>
    <!--
          <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
            <gco:CharacterString|gmx:Anchor xlink:href="http">Template for Vector data in ISO19139 (multilingual)</gco:CharacterString>
            <gmd:PT_FreeText>
              <gmd:textGroup>
                <gmd:LocalisedCharacterString locale="#FRE">Modèle de données vectorielles en
                  ISO19139 (multilingue)
                </gmd:LocalisedCharacterString>
              </gmd:textGroup>
    -->
    <!--<xsl:message>gn-fn-index:add-field <xsl:value-of select="$fieldName"/></xsl:message>-->
    <!--<xsl:message>gn-fn-index:add-field languages <xsl:copy-of select="$languages"/></xsl:message>-->
   <!--
   Multilingual mode, one field per language.

   <xsl:variable name="isArray"
                  select="count($elements) > 1"/>
    <xsl:for-each select="$elements">
      <xsl:variable name="element"
                    select="."/>

      <xsl:variable name="field">
        <xsl:choose>
          <xsl:when test="$languages">

            <xsl:for-each select="$element//*:LocalisedCharacterString[. != '']">
              <xsl:variable name="elementLanguage"
                            select="replace(@locale, '#', '')"/>
              <xsl:variable name="elementLanguage3LetterCode"
                            select="$languages/lang[@id = $elementLanguage]/@value"/>
              <xsl:variable name="field"
                            select="if ($elementLanguage3LetterCode = '') then $fieldName else concat($fieldName, '_lang', $elementLanguage3LetterCode)"/>

              <xsl:choose>
                <xsl:when test="$asJson">
                  <xsl:value-of select="concat($doubleQuote, $field, $doubleQuote, ':',
                                               $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote,
                                               if ($element//(*:CharacterString|*:Anchor)) then ',' else '')"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:element name="{$field}">
                    <xsl:value-of select="gn-fn-index:json-escape(.)"/>
                  </xsl:element>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>

            &lt;!&ndash; The default language &ndash;&gt;
            <xsl:for-each select="$element//(*:CharacterString|*:Anchor)[. != '']">
              <xsl:choose>
                <xsl:when test="$asJson">
                  <xsl:value-of select="concat($doubleQuote, $fieldName, $doubleQuote, ':',
                                               $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:element name="{$fieldName}">
                    <xsl:value-of select="."/>
                  </xsl:element>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            &lt;!&ndash; Index each values in a field. &ndash;&gt;
            <xsl:for-each select="distinct-values($element[. != ''])">
              <xsl:choose>
                <xsl:when test="$asJson">
                  <xsl:value-of select="concat($doubleQuote, $fieldName, $doubleQuote, ':',
                                               $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:element name="{$fieldName}">
                    <xsl:value-of select="gn-fn-index:json-escape(.)"/>
                  </xsl:element>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:for-each select="$element//*:Anchor/@xlink:href">
          <xsl:element name="{$fieldName}_link">
            <xsl:value-of select="gn-fn-index:json-escape(.)"/>
          </xsl:element>
        </xsl:for-each>
      </xsl:variable>

  &lt;!&ndash;    <xsl:message>gn-fn-index:add-field <xsl:copy-of select="$field"/></xsl:message>&ndash;&gt;
      <xsl:choose>
        <xsl:when test="$asJson">
          <xsl:if test="$isArray and position() = 1">[</xsl:if>
          {<xsl:value-of select="$field"/>}
          <xsl:if test="$isArray and position() != last()">,</xsl:if>
          <xsl:if test="$isArray and position() = last()">]</xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$field"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>-->

    <!--
    Experimental for multilingual field
    Multilingual mode : Object
   Add a multilingual field as an object having:
   {
    default: "Français", -> The default property should be removed at some point
    (temporary as long as multilingual support is not available).
    langfre: "Français", -> The default language is the first property
    langeng: "English",
    ...
    (optional) link: "http://" -> Anchor xlink:href attribute
   }
    -->
    <xsl:variable name="isArray"
                  select="count($elements[not(@xml:lang)]) > 1"/>
    <xsl:for-each select="$elements">
      <xsl:variable name="element" select="."/>
      <xsl:variable name="textObject">
        <xsl:choose>
          <!-- Not ISO but multilingual eg. DC or DCAT -->
          <xsl:when test="$languages and count($element//(*:CharacterString|*:Anchor|*:LocalisedCharacterString)) = 0">

            <xsl:if test="position() = 1">
              <xsl:value-of select="concat($doubleQuote, 'default', $doubleQuote, ':',
                                             $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>
              <xsl:for-each select="$elements">
                <xsl:value-of select="concat(',', $doubleQuote, 'lang', @xml:lang, $doubleQuote, ':',
                                             $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>
              </xsl:for-each>
            </xsl:if>
          </xsl:when>
          <xsl:when test="$languages">
            <!-- The default language -->
            <xsl:for-each select="$element//(*:CharacterString|*:Anchor)[. != '']">
              <xsl:value-of select="concat($doubleQuote, 'default', $doubleQuote, ':',
                                           $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote, ', ')"/>
              <xsl:value-of select="concat($doubleQuote, 'lang', $mainLanguage, $doubleQuote, ':',
                                           $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>
            </xsl:for-each>

            <xsl:for-each select="$element//*:LocalisedCharacterString[. != '']">
              <xsl:variable name="elementLanguage"
                            select="replace(@locale, '#', '')"/>
              <xsl:variable name="elementLanguage3LetterCode"
                            select="$languages/lang[@id = $elementLanguage]/@value"/>
              <xsl:variable name="field"
                            select="concat('lang', if ($elementLanguage3LetterCode = '') then $mainLanguage else $elementLanguage3LetterCode)"/>
              <xsl:value-of select="concat(
                                      ',',
                                      $doubleQuote, $field, $doubleQuote, ':',
                                      $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <!-- Index each values in a field. -->
            <xsl:for-each select="distinct-values($element[. != ''])">
              <xsl:value-of select="concat($doubleQuote, 'default', $doubleQuote, ':',
                                           $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote, ', ')"/>
              <xsl:value-of select="concat($doubleQuote, 'lang', $mainLanguage, $doubleQuote, ':',
                                           $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:for-each select="$element//*:Anchor/@xlink:href">
          <xsl:value-of select="concat(',', $doubleQuote, 'link', $doubleQuote, ':',
                                           $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>
        </xsl:for-each>
      </xsl:variable>
      <xsl:if test="$textObject != ''">
        <xsl:choose>
          <xsl:when test="$asJson">
            <xsl:if test="$isArray and position() = 1">[</xsl:if>
            {<xsl:value-of select="$textObject"/>}
            <xsl:if test="$isArray and position() != last()">,</xsl:if>
            <xsl:if test="$isArray and position() = last()">]</xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:element name="{$fieldName}Object">
              <xsl:attribute name="type" select="'object'"/>
              {<xsl:value-of select="$textObject"/>}
            </xsl:element>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:for-each>
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
