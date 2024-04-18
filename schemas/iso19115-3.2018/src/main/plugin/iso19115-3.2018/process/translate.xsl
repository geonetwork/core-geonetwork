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
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:translation="java:org.fao.geonet.translations.TranslationUtil"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">

    <xsl:import href="process-utility.xsl"/>

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
                  as="xs:string?"
                  select="//mdb:MD_Metadata/mdb:defaultLocale/*/lan:language/*/@codeListValue"/>

    <xsl:variable name="allRecordLanguages"
                  as="xs:string*"
                  select="//mdb:MD_Metadata/(mdb:defaultLocale|mdb:otherLocale)/*/lan:language/*/@codeListValue"/>

    <xsl:variable name="mainLanguage2code"
                  select="util:twoCharLangCode($mainLanguage)"/>



    <xsl:variable name="translate-info-loc">
        <msg id="a" xml:lang="eng">Translate record</msg>
        <msg id="a" xml:lang="fre">Traduire la fiche</msg>
    </xsl:variable>

    <xsl:template name="list-translate">
        <suggestion process="translate"/>
    </xsl:template>

    <xsl:template name="analyze-translate">
        <xsl:param name="root"/>

        <!-- TODO: If translation provider available-->
        <suggestion process="translate" id="{generate-id()}"
                    category="metadata" target="MD_Metadata">
            <name>
                <xsl:value-of select="geonet:i18n($translate-info-loc, 'a', $guiLang)"/>
            </name>
            <operational>true</operational>
            <params>{
                "languages":{"type":"string", "defaultValue":""},
                "fieldsToTranslate":{"type":"textarea", "defaultValue":"/mdb:MD_Metadata/mdb:identificationInfo/mri:MD_DataIdentification/mri:citation/cit:CI_Citation/cit:title"}
                }</params>
        </suggestion>
    </xsl:template>




    <!-- Do a copy of every nodes and attributes -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Update language list -->
    <xsl:template match="mdb:MD_Metadata | *[contains(@gco:isoType, 'MD_Metadata')]"
                  priority="2">
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

            <xsl:for-each select="$languagesToTranslate[not(@code3 = $allRecordLanguages)]">
                <xsl:variable name="lang2code" select="@code2"/>
                <xsl:variable name="lang3code" select="@code3"/>
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
            </xsl:for-each>

            <xsl:apply-templates select="mdb:metadataLinkage"/>
            <xsl:apply-templates select="mdb:spatialRepresentationInfo"/>
            <xsl:apply-templates select="mdb:referenceSystemInfo"/>
            <xsl:apply-templates select="mdb:metadataExtensionInfo"/>
            <xsl:apply-templates select="mdb:identificationInfo"/>
            <xsl:apply-templates select="mdb:contentInfo"/>
            <xsl:apply-templates select="mdb:distributionInfo"/>
            <xsl:apply-templates select="mdb:dataQualityInfo"/>
            <xsl:apply-templates select="mdb:resourceLineage"/>
            <xsl:apply-templates select="mdb:portrayalCatalogueInfo"/>
            <xsl:apply-templates select="mdb:metadataConstraints"/>
            <xsl:apply-templates select="mdb:applicationSchemaInfo"/>
            <xsl:apply-templates select="mdb:metadataMaintenance"/>
            <xsl:apply-templates select="mdb:acquisitionInformation"/>
        </xsl:copy>
    </xsl:template>


    <!-- TODO: Maybe translateAll should exclude some fields
    eg. keywords from thesaurus have to be translated from the thesaurus
    eg. add config-editor exclusion ? -->
    <xsl:template
            match="*[(gco:CharacterString or gcx:Anchor) and ($translateAll or concat('/', string-join(current()/ancestor-or-self::*[name() != 'root']/name(), '/')) = $fieldsToTranslate)]"
            priority="2">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:if test="not(@xsi:type)">
                <xsl:attribute name="xsi:type">mdb:PT_FreeText_PropertyType</xsl:attribute>
            </xsl:if>
            <xsl:copy-of select="gco:CharacterString|gcx:Anchor"/>
            <xsl:call-template name="translate"/>
        </xsl:copy>
    </xsl:template>


    <xsl:template name="translate">
        <xsl:variable name="node" select="."/>

        <lan:PT_FreeText>
            <xsl:copy-of select="*/lan:textGroup[*/@locale = $mainLanguage2code]"/>
            <xsl:copy-of select="*/lan:textGroup[not(*/@locale = $languagesToTranslate/concat('#', upper-case(@code2)))]"/>

            <xsl:for-each select="$languagesToTranslate">
                <xsl:variable name="langId" select="concat('#', upper-case(@code2))"/>

                <xsl:variable name="currentTranslation"
                              select="$node/*/lan:textGroup/*[@locale = $langId]"/>
                <lan:textGroup>
                    <lan:LocalisedCharacterString locale="{$langId}">
                        <xsl:value-of select="if(not($translateOnlyEmptyText)
                                                 or ($translateOnlyEmptyText and $currentTranslation = ''))
                                              then translation:translate($node/(gco:CharacterString|gcx:Anchor), lower-case($mainLanguage2code), @code2)
                                              else $currentTranslation"/>
                    </lan:LocalisedCharacterString>
                </lan:textGroup>
            </xsl:for-each>
        </lan:PT_FreeText>
    </xsl:template>
</xsl:stylesheet>