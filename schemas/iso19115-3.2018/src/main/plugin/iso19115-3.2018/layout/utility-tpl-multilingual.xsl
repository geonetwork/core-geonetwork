<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
  xmlns:xslutil="java:org.fao.geonet.util.XslUtil"
  exclude-result-prefixes="#all">

  <!-- Get the main metadata languages -->
  <xsl:template name="get-iso19115-3.2018-language">
    <xsl:value-of select="$metadata/mdb:defaultLocale/
    lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue"/>
  </xsl:template>

  <!-- Get the list of other languages in JSON -->
  <xsl:template name="get-iso19115-3.2018-other-languages-as-json">
    <xsl:variable name="langs">
      <xsl:choose>
        <xsl:when test="$metadata/gn:info[position() = last()]/isTemplate = 's'">

          <xsl:for-each select="distinct-values($metadata//lan:LocalisedCharacterString/@locale)">
            <xsl:variable name="locale" select="string(.)" />
            <xsl:variable name="langId" select="xslutil:threeCharLangCode(substring($locale,2,2))" />
            <lang><xsl:value-of select="concat('&quot;', $langId, '&quot;:&quot;', ., '&quot;')"/></lang>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="mainLanguage">
            <xsl:call-template name="get-iso19115-3.2018-language"/>
          </xsl:variable>
          <xsl:if test="$mainLanguage">
            <xsl:variable name="mainLanguageId"
                          select="$metadata/*/lan:PT_Locale[
                                  lan:language/lan:LanguageCode/@codeListValue = $mainLanguage]/@id"/>

            <lang><xsl:value-of select="concat('&quot;', $mainLanguage, '&quot;:&quot;#', $mainLanguageId[1], '&quot;')"/></lang>
          </xsl:if>

          <xsl:for-each select="$metadata/mdb:otherLocale/lan:PT_Locale[
                                  lan:language/lan:LanguageCode/@codeListValue != $mainLanguage]">
            <lang><xsl:value-of select="concat('&quot;', lan:language/lan:LanguageCode/@codeListValue, '&quot;:&quot;#', @id, '&quot;')"/></lang>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:text>{</xsl:text><xsl:value-of select="string-join($langs/lang, ',')"/><xsl:text>}</xsl:text>
  </xsl:template>

  <!-- Get the list of other languages -->
  <xsl:template name="get-iso19115-3.2018-other-languages">
    <xsl:variable name="mainLanguage">
      <xsl:call-template name="get-iso19115-3.2018-language"/>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$metadata/gn:info[position() = last()]/isTemplate = 's'">
        <xsl:for-each select="distinct-values($metadata//lan:LocalisedCharacterString/@locale)">
          <xsl:variable name="locale" select="string(.)" />
          <xsl:variable name="langId" select="xslutil:threeCharLangCode(substring($locale, 2, 2))" />
          <lang id="{substring(., 2, 2)}" code="{$langId}"/>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="$metadata/mdb:otherLocale/lan:PT_Locale">
          <lang id="{@id}" code="{lan:language/lan:LanguageCode/@codeListValue}"/>
        </xsl:for-each>
        <xsl:for-each select="$metadata/mdb:defaultLocale/lan:PT_Locale">
          <lang id="{@id}" code="{lan:language/lan:LanguageCode/@codeListValue}" default=""/>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Template used to return a translation if one found,
       or the text in default metadata language
       or the first non empty text element.
       If language id used is "#ALL", then all translations
       are reported with an xml:lang attribute indicating
       the language of the text.
    -->
  <xsl:template name="get-iso19115-3.2018-localised"
                mode="localised"
                match="*[lan:PT_FreeText or gco:CharacterString or gcx:Anchor]">
    <xsl:param name="langId"/>

    <xsl:variable name="mainValue"
                  select="(gco:CharacterString|gcx:Anchor)[1]"/>

    <xsl:choose>
      <xsl:when test="$langId = '#ALL' and lan:PT_FreeText">
        <xsl:choose>
          <xsl:when test="lan:PT_FreeText">
            <xsl:for-each select="lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString[. != '']">
              <xsl:variable name="id"
                            select="replace(@locale, '#', '')"/>
              <div xml:lang="{$metadata//lan:PT_Locale[@id = $id]/lan:language/*/@codeListValue}"><xsl:value-of select="."/></div>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <div xml:lang="{$metadata//mdb:defaultLocale/*/lan:language/*/@codeListValue}"><xsl:value-of select="$mainValue"/></div>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="translation"
                      select="lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString[@locale= $langId]"/>

        <xsl:variable name="firstNonEmptyValue"
                      select="((gco:CharacterString|gcx:Anchor|lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString)[. != ''])[1]"/>

        <xsl:value-of select="if($translation != '')
                          then $translation
                          else (if($mainValue != '')
                                then $mainValue
                                else $firstNonEmptyValue)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Map GUI language to iso3code -->
  <xsl:template name="getLangId19115-3.2018">
    <xsl:param name="langGui"/>
    <xsl:param name="md"/>

    <xsl:call-template name="getLangIdFromMetadata19115-3.2018">
      <xsl:with-param name="lang" select="$langGui"/>
      <xsl:with-param name="md" select="$md"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Get lang #id in metadata PT_Locale section,  deprecated: if not return the 2 first letters
      of the lang iso3code in uper case.

       if not return the lang iso3code in uper case.
      -->
  <xsl:template name="getLangIdFromMetadata19115-3.2018">
    <xsl:param name="md"/>
    <xsl:param name="lang"/>

    <xsl:choose>
      <xsl:when
        test="$md/mdb:defaultLocale/lan:PT_Locale[lan:language/lan:LanguageCode/@codeListValue = $lang]/@id"
      >#<xsl:value-of
        select="$md/mdb:defaultLocale/lan:PT_Locale[lan:language/lan:LanguageCode/@codeListValue = $lang]/@id"
      />
      </xsl:when>
      <xsl:otherwise>#<xsl:value-of select="upper-case($lang)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template mode="localised" match="*">
    <!-- Nothing to do, is not a text content field. -->
  </xsl:template>
</xsl:stylesheet>
