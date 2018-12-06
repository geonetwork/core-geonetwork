
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



  <!-- Add a multilingual field to the index.
   A multilingual field is composed of one root field
   with the default language value. Then one field per language
   is added with a suffix "_lang{{iso3letterLangCode}}".
   Function can produce an XML document or a JSON format properties
   when translations have to be added to an object field.
   -->
  <xsl:function name="gn-fn-index:add-multilingual-field" as="node()*">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="element" as="node()*"/>
    <xsl:param name="languages" as="node()?"/>
    <xsl:copy-of select="gn-fn-index:add-multilingual-field($fieldName, $element, $languages, false())"/>
  </xsl:function>

  <xsl:function name="gn-fn-index:add-multilingual-field" as="node()*">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="element" as="node()*"/>
    <xsl:param name="languages" as="node()?"/>
    <xsl:param name="asJson" as="xs:boolean?"/>

    <!--
    TODO:
    * escape JSON char
    * handle Anchor
    -->
    <!--<xsl:message>gn-fn-index:add-field <xsl:value-of select="$fieldName"/></xsl:message>-->
    <!--<xsl:message>gn-fn-index:add-field languages <xsl:copy-of select="$languages"/></xsl:message>-->
    <xsl:variable name="field">
      <xsl:choose>
        <xsl:when test="$languages">

          <!--
          <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
            <gco:CharacterString>Template for Vector data in ISO19139 (multilingual)</gco:CharacterString>
            <gmd:PT_FreeText>
              <gmd:textGroup>
                <gmd:LocalisedCharacterString locale="#FRE">Modèle de données vectorielles en
                  ISO19139 (multilingue)
                </gmd:LocalisedCharacterString>
              </gmd:textGroup>
              -->
          <xsl:for-each select="$element//gmd:LocalisedCharacterString[. != '']">
            <xsl:variable name="elementLanguage"
                          select="replace(@locale, '#', '')"/>
            <xsl:variable name="elementLanguage3LetterCode"
                          select="$languages/lang[@id = $elementLanguage]/@value"/>
            <xsl:variable name="field"
                          select="if ($elementLanguage3LetterCode = '') then $fieldName else concat($fieldName, '_lang', $elementLanguage3LetterCode)"/>

            <xsl:choose>
              <xsl:when test="$asJson">
                <xsl:value-of select="concat($doubleQuote, $field, $doubleQuote, ':',
                                             $doubleQuote, normalize-space(.), $doubleQuote,
                                             if ($element//gco:CharacterString) then ',' else '')"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:element name="{$field}">
                  <xsl:value-of select="normalize-space(.)"/>
                </xsl:element>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>

          <!-- The default language -->
          <xsl:for-each select="$element//gco:CharacterString[. != '']">
            <xsl:choose>
              <xsl:when test="$asJson">
                <xsl:value-of select="concat($doubleQuote, $fieldName, $doubleQuote, ':',
                                             $doubleQuote, normalize-space(.), $doubleQuote)"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:element name="{$fieldName}">
                  <xsl:value-of select="normalize-space(.)"/>
                </xsl:element>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <!-- Index each values in a field. -->
          <xsl:for-each select="distinct-values($element[. != ''])">
            <xsl:choose>
              <xsl:when test="$asJson">
                <xsl:value-of select="concat($doubleQuote, $fieldName, $doubleQuote, ':',
                                             $doubleQuote, normalize-space(.), $doubleQuote)"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:element name="{$fieldName}">
                  <xsl:value-of select="normalize-space(.)"/>
                </xsl:element>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!--<xsl:message>gn-fn-index:add-field <xsl:copy-of select="$field"/></xsl:message>-->
    <xsl:copy-of select="$field"/>
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
