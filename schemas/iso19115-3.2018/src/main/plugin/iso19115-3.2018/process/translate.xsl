<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

<!-- Creates a multilingual version of the metadata, translating the content provided in the `fieldsToTranslate`
     parameter to the languages provided in the 'languages' parameter, using the translation provider configured
     in the application settings.
-->
<xsl:stylesheet xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:translation="java:org.fao.geonet.translations.TranslationUtil"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- List of iso3 code languages to translate -->
  <xsl:param name="languages" as="xs:string*" />

  <!-- List of fields (xpaths) to translate -->
  <xsl:param name="fieldsToTranslate" as="xs:string*" />

  <xsl:variable name="langsList">
    <langs>
      <xsl:for-each select="$languages">
        <lang code2="{util:twoCharLangCode(.)}" code3="{.}" />
      </xsl:for-each>
    </langs>
  </xsl:variable>

  <xsl:variable name="fieldsList">
    <fields>
      <xsl:for-each select="$fieldsToTranslate">
        <field xpath="{replace(., '/mdb:MD_Metadata', '')}" />
      </xsl:for-each>
    </fields>
  </xsl:variable>

  <xsl:variable name="mainLanguage"
                select="//mdb:defaultLocale/*/lan:language/*/@codeListValue"/>

  <xsl:variable name="mainLanguage2code" select="util:twoCharLangCode($mainLanguage)" />

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mdb:MD_Metadata | *[contains(@gco:isoType, 'MD_Metadata')]" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:apply-templates select="mdb:metadataIdentifier"/>
      <xsl:apply-templates select="mdb:defaultLocale"/>
      <xsl:apply-templates select="mdb:parentMetadata"/>
      <xsl:apply-templates select="mdb:metadataScope"/>
      <xsl:apply-templates select="mdb:contact"/>
      <xsl:apply-templates select="mdb:dateInfo"/>
      <xsl:apply-templates select="mdb:metadataStandard"/>
      <xsl:apply-templates select="mdb:metadataProfile"/>
      <xsl:apply-templates select="mdb:alternativeMetadataReference"/>
      <xsl:apply-templates select="mdb:otherLocale"/>

      <!-- mdb:otherLocale -->
      <xsl:for-each select="$langsList/langs/lang">
        <xsl:variable name="lang2code" select="@code2" />
        <xsl:variable name="lang3code" select="@code3" />
        <xsl:if test="not(mdb:locale[mdb:PT_Locale/mdb:languageCode/@codeListValue = $lang3code])">
          <mdb:otherLocale>
            <lan:PT_Locale id="{upper-case($lang2code)}">
              <lan:language>
                  <lan:LanguageCode codeList="" codeListValue="{$lang3code}"/>
              </lan:language>
              <lan:characterEncoding>
                <lan:MD_CharacterSetCode codeListValue="utf8"
                                         codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_CharacterSetCode"/>
              </lan:characterEncoding>
            </lan:PT_Locale>
          </mdb:otherLocale>
        </xsl:if>
      </xsl:for-each>

      <xsl:apply-templates select="mdb:metadataLinkage"/>
      <xsl:apply-templates select="mdb:spatialRepresentationInfo"/>
      <xsl:apply-templates select="mdb:referenceSystemInfo"/>
      <xsl:apply-templates select="mdb:metadataExtensionInfo"/>
      <xsl:apply-templates select="mdb:identificationInfo"/>
      <xsl:apply-templates select="mdb:contentInfo"/>
      <xsl:apply-templates select="mdb:distributionInfo"/>
      <xsl:apply-templates select="mdb:dataQualityInfo"/>
      <xsl:apply-templates select="mdb:resourceLineage" />
      <xsl:apply-templates select="mdb:portrayalCatalogueInfo"/>
      <xsl:apply-templates select="mdb:metadataConstraints"/>
      <xsl:apply-templates select="mdb:applicationSchemaInfo"/>
      <xsl:apply-templates select="mdb:metadataMaintenance"/>
      <xsl:apply-templates select="mdb:acquisitionInformation"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="*[gco:CharacterString]" priority="2">
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>

    <xsl:choose>
      <xsl:when test="$fieldsList/fields/field[@xpath = $xpath]">
        <!-- Translate -->
        <xsl:copy>
          <xsl:apply-templates select="@*" />
          <xsl:attribute name="xsi:type">gmd:PT_FreeText_PropertyType</xsl:attribute>

          <xsl:apply-templates select="gco:CharacterString" />

          <xsl:call-template name="multilingual">
            <xsl:with-param name="value" select="gco:CharacterString" />
            <xsl:with-param name="mainLanguage" select="$mainLanguage" />
            <xsl:with-param name="mainLanguage2code" select="$mainLanguage2code" />
          </xsl:call-template>

        </xsl:copy>
      </xsl:when>

      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:function name="gn-fn-metadata:getXPath" as="xs:string">
    <xsl:param name="node" as="node()"/>

    <xsl:value-of select="gn-fn-metadata:getXPath($node, false())"/>
  </xsl:function>

  <xsl:function name="gn-fn-metadata:positionOfType" as="xs:string">
    <xsl:param name="node" as="node()"/>
    <xsl:variable name="nodePosition" select="$node/position()"/>
    <xsl:variable name="allPrecedingSiblings"
                  select="$node/preceding-sibling::*[name() = name($node)]"/>
    <!--<xsl:value-of select="count($node/../*[name = name($node) and position() &lt; $nodePosition]) + 1"/>-->
    <xsl:value-of select="count($allPrecedingSiblings) + 1"/>
  </xsl:function>

  <!--
   Return the xpath of a node.
  -->
  <xsl:function name="gn-fn-metadata:getXPath" as="xs:string">
    <xsl:param name="node" as="node()"/>
    <xsl:param name="withPosition" as="xs:boolean"/>

    <!-- Avoid root element. -->
    <xsl:variable name="untilIndex" select="1"/>
    <xsl:variable name="xpathSeparator">/</xsl:variable>
    <xsl:variable name="elementName" select="name($node)"/>
    <xsl:variable name="isAttribute" select="$node/../attribute::*[name() = $elementName]"/>
    <xsl:variable name="ancestors" select="$node/ancestor::*"/>

    <xsl:variable name="xpath">
      <xsl:for-each select="$ancestors[position() != $untilIndex]">
        <xsl:value-of select="if ($withPosition)
          then concat($xpathSeparator, name(.), '[', gn-fn-metadata:positionOfType(.), ']')
          else concat($xpathSeparator, name(.))"/>
      </xsl:for-each>
    </xsl:variable>

    <xsl:value-of
      select="if ($isAttribute)
      then concat($xpath, $xpathSeparator, '@', $elementName)
      else if ($withPosition)
      then concat($xpath, $xpathSeparator, $elementName, '[', gn-fn-metadata:positionOfType($node), ']')
        else concat($xpath, $xpathSeparator, $elementName)
      "
    />
  </xsl:function>


  <xsl:template name="multilingual">
    <xsl:param name="value" />
    <xsl:param name="mainLanguage" />
    <xsl:param name="mainLanguage2code" />

    <xsl:choose>
      <xsl:when test="lan:PT_FreeText">
        <xsl:copy>
          <xsl:apply-templates select="@*" />

          <xsl:for-each select="$langsList/langs/lang">
            <xsl:variable name="currentLang2code" select="@code2" />
            <xsl:variable name="currentLang3code" select="@code3" />

            <xsl:if test="$currentLang3code != lower-case($mainLanguage)">
              <lan:textGroup>
                <lan:LocalisedCharacterString locale="{concat('#', upper-case($currentLang2code))}"><xsl:value-of select="translation:translate($value, lower-case($mainLanguage2code), $currentLang2code)"/></lan:LocalisedCharacterString>
              </lan:textGroup>
            </xsl:if>

            <xsl:if test="$currentLang3code = lower-case($mainLanguage)">
              <lan:textGroup>
                <lan:LocalisedCharacterString locale="{concat('#', upper-case($currentLang2code))}"><xsl:value-of select="$value"/></lan:LocalisedCharacterString>
              </lan:textGroup>
            </xsl:if>
          </xsl:for-each>
        </xsl:copy>
      </xsl:when>

      <xsl:otherwise>
        <lan:PT_FreeText>
          <xsl:for-each select="$langsList/langs/lang">
            <xsl:variable name="currentLang2code" select="@code2" />
            <xsl:variable name="currentLang3code" select="@code3" />

            <xsl:if test="$currentLang3code != lower-case($mainLanguage)">
              <lan:textGroup>
                <lan:LocalisedCharacterString locale="{concat('#', upper-case($currentLang2code))}"><xsl:value-of select="translation:translate($value, lower-case($mainLanguage2code), $currentLang2code)"/></lan:LocalisedCharacterString>
              </lan:textGroup>
            </xsl:if>

            <xsl:if test="$currentLang3code = lower-case($mainLanguage)">
              <lan:textGroup>
                <lan:LocalisedCharacterString locale="{concat('#', upper-case($currentLang2code))}"><xsl:value-of select="$value"/></lan:LocalisedCharacterString>
              </lan:textGroup>
            </xsl:if>
          </xsl:for-each>
        </lan:PT_FreeText>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
