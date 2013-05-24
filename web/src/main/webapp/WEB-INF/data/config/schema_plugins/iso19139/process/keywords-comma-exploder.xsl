<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork" 
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" version="2.0">

  <xsl:import href="process-utility.xsl"/>
  

  <!-- i18n information -->
  <xsl:variable name="keyword-comma-loc">
    <msg id="a" xml:lang="eng">Keyword field contains "," (ie. </msg>
    <msg id="b" xml:lang="eng">). Run this task to explode that keywords.</msg>
    <msg id="a" xml:lang="fre">Un mot clé contient le caractère "," (ie. </msg>
    <msg id="b" xml:lang="fre">). Exécuter cette action pour le corriger.</msg>
    <msg id="c" xml:lang="eng"> and </msg>
    <msg id="c" xml:lang="fre"> et </msg>
  </xsl:variable>

  <xsl:template name="list-keywords-comma-exploder">
    <suggestion process="keywords-comma-exploder"/>
  </xsl:template>

  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-keywords-comma-exploder">
    <xsl:param name="root"/>
    <xsl:variable name="keywordWithComma"
      select="$root//gmd:keyword[contains(gco:CharacterString, ',')]"/><!-- TODO : PT_FreeText -->
    <xsl:if test="$keywordWithComma">
      <suggestion process="keywords-comma-exploder" id="{generate-id()}" category="keyword" target="keyword">
        <name xml:lang="en">
          <xsl:value-of select="geonet:i18n($keyword-comma-loc, 'a', $guiLang)"/> 
          <xsl:value-of select="string-join($keywordWithComma, geonet:i18n($keyword-comma-loc, 'c', $guiLang))"/>
          <xsl:value-of select="geonet:i18n($keyword-comma-loc, 'b', $guiLang)"/></name>
        <operational>true</operational>
        <form/>
      </suggestion>
    </xsl:if>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
      <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

  <!-- Apply to all keywords having "," in it. -->
  <xsl:template match="gmd:MD_Keywords[contains(gmd:keyword, ',')]" priority="2">
      <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:for-each select="gmd:keyword">
              <xsl:call-template name="explode-keyword">
                  <xsl:with-param name="value" select="gco:CharacterString"/>
                  <xsl:with-param name="separator" select="','"/>
              </xsl:call-template>
          </xsl:for-each>
          <xsl:copy-of select="gmd:type|gmd:thesaurusName"/>
      </xsl:copy>
  </xsl:template>


  <!-- Take each token separated by the separator
  and create as many elements as needed -->
  <xsl:template name="explode-keyword">
      <xsl:param name="value"/>
      <xsl:param name="separator"/>
      
      <gmd:keyword>
          <gco:CharacterString>
              <xsl:choose>
                  <xsl:when test="contains($value, $separator)">
                      <xsl:value-of select="normalize-space(substring-before($value, $separator))"/>
                  </xsl:when>
                  <xsl:otherwise>
                      <xsl:value-of select="$value"/>
                  </xsl:otherwise>
              </xsl:choose>
          </gco:CharacterString>
          <xsl:copy-of select="gmd:PT_FreeText"/>
      </gmd:keyword>
      
      <xsl:if test="contains($value, $separator)">
          <xsl:call-template name="explode-keyword">
              <xsl:with-param name="value" select="substring-after($value, $separator)"/>
              <xsl:with-param name="separator" select="$separator"/>
          </xsl:call-template>
      </xsl:if>
  </xsl:template>
</xsl:stylesheet>
