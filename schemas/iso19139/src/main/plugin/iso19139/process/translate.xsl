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
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:translation="java:org.fao.geonet.translations.TranslationUtil"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">

    <!-- List of iso3 code languages to translate -->
    <xsl:param name="languages"
               as="xs:string*"/>

    <!-- List of fields (xpaths) to translate -->
    <xsl:param name="fieldsToTranslate"
               as="xs:string*"/>

    <xsl:variable name="translateAll"
                  as="xs:boolean"
                  select="$fieldsToTranslate = ''"/>

    <xsl:variable name="translateOnlyEmptyText"
                  as="xs:boolean"
                  select="false()"/>

    <xsl:variable name="languagesToTranslate"
                  as="node()*">
        <xsl:for-each select="$languages">
            <lang code2="{util:twoCharLangCode(.)}"
                  code3="{.}"/>
        </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="mainLanguage"
                  select="if (//gmd:MD_Metadata/gmd:language/*/@codeListValue) then //gmd:MD_Metadata/gmd:language/*/@codeListValue
                          else substring-before(*/text(),';')"/>

    <xsl:variable name="allRecordLanguages"
                  select="($mainLanguage|//gmd:MD_Metadata/gmd:locale/*/gmd:languageCode/*/@codeListValue)"/>

    <xsl:variable name="mainLanguage2code"
                  select="util:twoCharLangCode($mainLanguage)"/>

    <!-- Do a copy of every nodes and attributes -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Update language list -->
    <xsl:template match="gmd:MD_Metadata | *[contains(@gco:isoType, 'MD_Metadata')]" priority="2">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="
          gmd:fileIdentifier | gmd:language | gmd:characterSet | gmd:parentIdentifier | gmd:hierarchyLevel |
          gmd:hierarchyLevelName | gmd:contact | gmd:dateStamp | gmd:metadataStandardName | gmd:metadataStandardVersion |
          gmd:dataSetURI | gmd:locale"/>

            <xsl:for-each select="$languagesToTranslate[not(@code3 = $allRecordLanguages)]">
                <xsl:variable name="lang2code" select="@code2"/>
                <xsl:variable name="lang3code" select="@code3"/>
                <gmd:locale>
                    <gmd:PT_Locale id="{upper-case($lang2code)}">
                        <gmd:languageCode>
                            <gmd:LanguageCode codeList="" codeListValue="{$lang3code}"/>
                        </gmd:languageCode>
                        <gmd:characterEncoding>
                            <gmd:MD_CharacterSetCode codeListValue="utf8"
                                                     codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_CharacterSetCode"/>
                        </gmd:characterEncoding>
                    </gmd:PT_Locale>
                </gmd:locale>
            </xsl:for-each>


            <xsl:apply-templates select="gmd:spatialRepresentationInfo | gmd:referenceSystemInfo | gmd:metadataExtensionInfo |
        gmd:identificationInfo | gmd:contentInfo | gmd:distributionInfo | gmd:dataQualityInfo | gmd:portrayalCatalogueInfo |
        gmd:metadataConstraints | gmd:applicationSchemaInfo | gmd:metadataMaintenance |
        gmd:series | gmd:describes | gmd:propertyType | gmd:featureType | gmd:featureAttribute"/>
        </xsl:copy>
    </xsl:template>


    <!-- TODO: Maybe translateAll should exclude some fields
    eg. keywords from thesaurus have to be translated from the thesaurus
    eg. add config-editor exclusion ? -->
    <xsl:template
            match="*[(gco:CharacterString or gmx:Anchor) and ($translateAll or concat('/', string-join(current()/ancestor-or-self::*[name() != 'root']/name(), '/')) = $fieldsToTranslate)]"
            priority="2">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:if test="not(@xsi:type)">
                <xsl:attribute name="xsi:type">mdb:PT_FreeText_PropertyType</xsl:attribute>
            </xsl:if>
            <xsl:copy-of select="gco:CharacterString|gmx:Anchor"/>
            <xsl:call-template name="translate"/>
        </xsl:copy>
    </xsl:template>


    <xsl:template name="translate">
        <xsl:variable name="node" select="."/>

        <gmd:PT_FreeText>
            <xsl:copy-of select="*/gmd:textGroup[*/@locale = $mainLanguage2code]"/>
            <xsl:copy-of select="*/gmd:textGroup[not(*/@locale = $languagesToTranslate/concat('#', upper-case(@code2)))]"/>

            <xsl:for-each select="$languagesToTranslate">
                <xsl:variable name="langId" select="concat('#', upper-case(@code2))"/>

                <xsl:variable name="currentTranslation"
                              select="$node/*/gmd:textGroup/*[@locale = $langId]"/>
                <gmd:textGroup>
                    <gmd:LocalisedCharacterString locale="{$langId}">
                        <xsl:value-of select="if(not($translateOnlyEmptyText)
                                                 or ($translateOnlyEmptyText and $currentTranslation = ''))
                                              then translation:translate($node/(gco:CharacterString|gmx:Anchor), lower-case($mainLanguage2code), @code2)
                                              else $currentTranslation"/>
                    </gmd:LocalisedCharacterString>
                </gmd:textGroup>
            </xsl:for-each>
        </gmd:PT_FreeText>
    </xsl:template>
</xsl:stylesheet>
