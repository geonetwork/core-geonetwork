<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="#all">

  <!-- Convert all keywords encoded using CharacterString
  eg.
  ```xml
      <mri:keyword>
        <gco:CharacterString>Algeria</gco:CharacterString>
     </mri:keyword>
  ```
  to Anchor if found in thesaurus.
  eg.
  ```xml
      <mri:keyword>
        <gcx:Anchor xlink:href="http://www.naturalearthdata.com/ne_admin#Country/DZA">Algeria</gcx:Anchor>
      </mri:keyword>
  ```
  -->
  <xsl:output indent="yes" method="xml"/>


  <xsl:variable name="mainLanguage" as="xs:string?"
                select="mdb:MD_Metadata/mdb:defaultLocale/lan:PT_Locale/
                            lan:language/lan:LanguageCode/
                              @codeListValue[normalize-space(.) != '']"/>

  <xsl:template match="mri:MD_Keywords[count(mri:keyword/gco:CharacterString) > 0]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>

      <xsl:apply-templates mode="characterString-to-anchor" select="mri:keyword"/>

      <xsl:apply-templates select="mri:type|mri:thesaurusName"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template mode="characterString-to-anchor"
                match="mri:keyword">
    <xsl:variable name="keywordLabel"
                  select="gco:CharacterString"/>
    <xsl:variable name="thesaurusId"
                  select="substring-after(../mri:thesaurusName/*/cit:identifier/*/mcc:code/*/text(), 'geonetwork.thesaurus.')"/>
    <xsl:variable name="keywordUri"
                  select="util:getKeywordUri(
                                                $keywordLabel,
                                                $thesaurusId,
                                                $mainLanguage)"/>

    <xsl:choose>
      <xsl:when test="$keywordLabel != '' and $keywordUri">
        <xsl:copy>
          <gcx:Anchor xlink:href="{$keywordUri}">
            <xsl:value-of select="$keywordLabel"/>
          </gcx:Anchor>
          <xsl:copy-of select="lan:PT_FreeText"/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="gn:*" priority="2"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
