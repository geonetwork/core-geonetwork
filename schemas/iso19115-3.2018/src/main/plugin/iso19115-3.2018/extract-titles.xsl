<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
        xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
        xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
        xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
        xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
        xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
        xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
        xmlns:java="java:org.fao.geonet.util.XslUtil">


  <xsl:variable name="mainLanguage"
                select="/*/mdb:defaultLocale/*/lan:language/*/@codeListValue"/>

  <xsl:variable name="mainLanguageId"
                select="upper-case(java:twoCharLangCode($mainLanguage))"/>

  <xsl:variable name="locales"
                select="/*/*/lan:PT_Locale"/>

  <xsl:template match="mdb:MD_Metadata|*[contains(@gco:isoType, 'MD_Metadata')]">
    <titles>
      <xsl:for-each
              select="mdb:identificationInfo[1]/*[1]/mri:citation/cit:CI_Citation/cit:title">
        <title>
          <xsl:attribute name="lang"><xsl:value-of select="$mainLanguage"/></xsl:attribute>
          <xsl:value-of select="gco:CharacterString"/>
        </title>
        <xsl:for-each select="lan:PT_FreeText/*/lan:LocalisedCharacterString[@locale != concat('#', $mainLanguageId)]">
          <title>
            <xsl:variable name="localId" select="substring-after(@locale, '#')"/>
            <xsl:attribute name="lang"><xsl:value-of select="$locales[@id=$localId]/lan:language/lan:LanguageCode/@codeListValue"/></xsl:attribute>
            <xsl:value-of select="."/>
          </title>

        </xsl:for-each>
      </xsl:for-each>
    </titles>
  </xsl:template>

</xsl:stylesheet>
