<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
  xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:java="java:org.fao.geonet.util.XslUtil"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gn="http://www.fao.org/geonetwork"
  exclude-result-prefixes="#all">

  <!-- eg. fre -->
  <xsl:param name="defaultLanguage"
             select="/mdb:MD_Metadata/mdb:defaultLocale/*/lan:language/*/@codeListValue"
             as="xs:string"/>

  <!-- A CSV list of ISO3 letter code or "none" to drop them. -->
  <!-- eg. fre,spa,ger -->
  <xsl:param name="others"
             select="string-join(/mdb:MD_Metadata/mdb:otherLocale/*/lan:language/*/@codeListValue[. != ''], ',')"
             as="xs:string?"/>

  <xsl:param name="characterSet" select="'utf8'" as="xs:string?"/>

  <xsl:param name="copyPreviousDefaultIfEmpty" select="'false'" as="xs:string?"/>


  <!-- eg. eng
  Here is a hack to also handle ISO19139 records
  See ISO19139-to-ISO19115-3-2018-with-languages-refactor.xsl
  It would be better maybe to be able to chain processing? -->
  <xsl:variable name="previousDefaultLanguage"
                select="/mdb:MD_Metadata/mdb:defaultLocale/*/lan:language/*/@codeListValue
                        |/gmd:MD_Metadata/gmd:language/*/@codeListValue"
                as="xs:string"/>

  <!-- eg. true -->
  <xsl:variable name="isChangingDefault"
                select="$previousDefaultLanguage != $defaultLanguage"
                as="xs:boolean"/>

  <xsl:variable name="defaultLanguageId"
                select="upper-case(java:twoCharLangCode($defaultLanguage))"
                as="xs:string?"/>

  <xsl:variable name="defaultLanguageIdRef"
                select="concat('#', $defaultLanguageId)"
                as="xs:string?"/>

  <xsl:variable name="previousDefaultLanguageId"
                select="upper-case(java:twoCharLangCode($previousDefaultLanguage))"
                as="xs:string?"/>

  <xsl:variable name="previousDefaultLanguageIdRef"
                select="concat('#', $previousDefaultLanguageId)"
                as="xs:string?"/>

  <!-- eg. eng,spa,ger -->
  <xsl:variable name="otherLanguageIds"
                select="tokenize(
                          replace($others, $defaultLanguage, $previousDefaultLanguage),
                          ',')"
                as="xs:string*"/>

  <!-- eg. #FR,#EN,#SP,#GE -->
  <xsl:variable name="otherLanguageIdsRef" as="node()*">
    <lang><xsl:value-of select="$defaultLanguageIdRef"/></lang>
    <xsl:for-each select="$otherLanguageIds[. != 'none']">
      <lang>
        <xsl:value-of select="concat('#',
                                    upper-case(java:twoCharLangCode(.)))"/>
      </lang>
    </xsl:for-each>
  </xsl:variable>


  <xsl:template match="/mdb:MD_Metadata|*[contains(@gco:isoType, 'mdb:MD_Metadata')]">
    <xsl:apply-templates mode="language-add" select="."/>
  </xsl:template>


  <xsl:template match="/mdb:MD_Metadata|*[contains(@gco:isoType, 'mdb:MD_Metadata')]"
                mode="language-add">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="mdb:metadataIdentifier" mode="language-add"/>
      <xsl:choose>
        <xsl:when test="$isChangingDefault">
          <mdb:defaultLocale>
            <xsl:call-template name="create-locale">
              <xsl:with-param name="language" select="$defaultLanguage"/>
            </xsl:call-template>
          </mdb:defaultLocale>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="mdb:defaultLocale" mode="language-add"/>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates select="mdb:parentMetadata" mode="language-add"/>
      <xsl:apply-templates select="mdb:metadataScope" mode="language-add"/>
      <xsl:apply-templates select="mdb:contact" mode="language-add"/>
      <xsl:apply-templates select="mdb:dateInfo" mode="language-add"/>
      <xsl:apply-templates select="mdb:metadataStandard" mode="language-add"/>
      <xsl:apply-templates select="mdb:metadataProfile" mode="language-add"/>
      <xsl:apply-templates select="mdb:alternativeMetadataReference" mode="language-add"/>

      <xsl:choose>
        <xsl:when test="$others = 'none'"/>
        <xsl:otherwise>
          <xsl:for-each select="$otherLanguageIds">
            <mdb:otherLocale>
              <xsl:call-template name="create-locale">
                <xsl:with-param name="language" select="."/>
              </xsl:call-template>
            </mdb:otherLocale>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates select="mdb:metadataLinkage" mode="language-add"/>
      <xsl:apply-templates select="mdb:spatialRepresentationInfo" mode="language-add"/>
      <xsl:apply-templates select="mdb:referenceSystemInfo" mode="language-add"/>
      <xsl:apply-templates select="mdb:metadataExtensionInfo" mode="language-add"/>
      <xsl:apply-templates select="mdb:identificationInfo" mode="language-add"/>
      <xsl:apply-templates select="mdb:contentInfo" mode="language-add"/>
      <xsl:apply-templates select="mdb:distributionInfo" mode="language-add"/>
      <xsl:apply-templates select="mdb:dataQualityInfo" mode="language-add"/>
      <xsl:apply-templates select="mdb:resourceLineage" mode="language-add"/>
      <xsl:apply-templates select="mdb:portrayalCatalogueInfo" mode="language-add"/>
      <xsl:apply-templates select="mdb:metadataConstraints" mode="language-add"/>
      <xsl:apply-templates select="mdb:applicationSchemaInfo" mode="language-add"/>
      <xsl:apply-templates select="mdb:metadataMaintenance" mode="language-add"/>
      <xsl:apply-templates select="mdb:acquisitionInformation" mode="language-add"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template name="create-locale">
    <xsl:param name="language" as="xs:string"/>

    <xsl:variable name="id"
                  select="upper-case(java:twoCharLangCode($language))"/>
    <lan:PT_Locale id="{$id}">
      <lan:language>
        <lan:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="{$language}"/>
      </lan:language>
      <lan:characterEncoding>
        <lan:MD_CharacterSetCode
          codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_CharacterSetCode"
          codeListValue="{$characterSet}"/>
      </lan:characterEncoding>
    </lan:PT_Locale>
  </xsl:template>


  <xsl:template match="*[(gco:CharacterString|gcx:Anchor)
                         and lan:PT_FreeText]"
                mode="language-add"
                priority="3">
    <xsl:copy>
      <xsl:apply-templates select="@*" mode="language-add"/>

      <xsl:variable name="hasPtFreeText"
                    select="count(lan:PT_FreeText) > 0"
                    as="xs:boolean"/>
      <xsl:variable name="valueInPtFreeTextForDefault"
                    select="lan:PT_FreeText/*/lan:LocalisedCharacterString[
                              @locale = $defaultLanguageIdRef]/text()"/>

      <xsl:variable name="valueForPreviousDefaultLanguageInPtFreeText"
                    select="lan:PT_FreeText/*/lan:LocalisedCharacterString[
                              @locale = $previousDefaultLanguageIdRef]/text()"/>

      <xsl:element name="{if (gcx:Anchor)
                          then 'gcx:Anchor' else 'gco:CharacterString'}">
        <xsl:apply-templates select="gco:CharacterString/@*|gcx:Anchor/@*" mode="language-add"/>
        <xsl:value-of select="if ($hasPtFreeText
                                  and $valueInPtFreeTextForDefault != '')
                              then $valueInPtFreeTextForDefault
                              else if (xs:boolean($copyPreviousDefaultIfEmpty) = true()
                                       and $valueForPreviousDefaultLanguageInPtFreeText != '')
                              then $valueForPreviousDefaultLanguageInPtFreeText
                              else (gco:CharacterString/text()|gcx:Anchor/text())"/>
      </xsl:element>


      <xsl:if test="$hasPtFreeText">
        <xsl:choose>
          <xsl:when test="$others = 'none'"/>
          <xsl:otherwise>
            <xsl:variable name="translations"
                          select="lan:PT_FreeText/lan:textGroup"
                          as="node()*"/>
            <lan:PT_FreeText>
              <xsl:for-each select="$otherLanguageIdsRef">
                <xsl:variable name="langRef" select="." as="xs:string?"/>
                <xsl:variable name="translation"
                              select="$translations[
                                            lan:LocalisedCharacterString/
                                              @locale = $langRef]"/>
                <xsl:if test="$translation != ''">
                  <lan:textGroup>
                    <lan:LocalisedCharacterString locale="{.}">
                      <xsl:value-of select="$translation"/>
                    </lan:LocalisedCharacterString>
                  </lan:textGroup>
                </xsl:if>
              </xsl:for-each>
            </lan:PT_FreeText>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:copy>
  </xsl:template>


  <!-- Remove geonet:* elements. -->
  <xsl:template match="gn:*"
                mode="language-add"
                priority="2"/>

  <!-- Copy everything. -->
  <xsl:template match="@*|node()"
                mode="language-add">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="language-add"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
