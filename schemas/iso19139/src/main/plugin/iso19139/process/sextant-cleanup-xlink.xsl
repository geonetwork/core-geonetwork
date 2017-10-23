<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


  <xsl:variable name="mainLanguage"
                select="//gmd:MD_Metadata/gmd:language/gco:CharacterString/text()|
                        //gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue"/>

  <xsl:variable name="locales"
                select="//gmd:MD_Metadata/gmd:locale/*/gmd:languageCode/*/@codeListValue[. != $mainLanguage]"/>

  <xsl:template match="gmd:descriptiveKeywords/@xlink:href[
                                      contains(., '&amp;amp;') or
                                      contains(., 'xml.keyword.get')]"
                priority="200">
    <xsl:variable name="newXlink" select="replace(replace(.,
                              'local://.*/xml.keyword.get',
                              'local://srv/api/registries/vocabularies/keyword'),
                              '&amp;amp;', '&amp;')"/>
    <xsl:message>Replace <xsl:value-of select="."/> by xlink <xsl:value-of select="$newXlink"/>.</xsl:message>

    <xsl:attribute name="xlink:href"
                   select="concat($newXlink, '&amp;skipdescriptivekeywords=true&amp;lang=', $mainLanguage,
                   if (count($locales) > 0)
                   then concat('&amp;lang=', string-join(
                          $locales,
                          '&amp;lang=')) else '')"/>
  </xsl:template>

  <xsl:template match="*[contains(xlink:href,
      'local://fre/xml.keyword.get?thesaurus=&amp;amp;id=&amp;amp;multiple=false&amp;amp;lang=fre')]"
                priority="200">
    <xsl:message>Remove element with empty xlink <xsl:copy-of select="."/></xsl:message>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>
</xsl:stylesheet>
