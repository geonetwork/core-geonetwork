<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
  xmlns:xs="http://www.w3.org/2001/XMLSchema">


  <!-- Template used to return a gco:CharacterString element
        in default metadata language or in a specific locale
        if exist. 
        FIXME : gmd:PT_FreeText should not be in the match clause as gco:CharacterString 
        is mandatory and PT_FreeText optional. Added for testing GM03 import.
    -->
  <xsl:template name="localised" mode="localised" match="*[gco:CharacterString or gmd:PT_FreeText]">
    <xsl:param name="langId"/>

    <xsl:choose>
      <xsl:when
        test="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId] and
        gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId] != ''">
        <xsl:value-of
          select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId]"/>
      </xsl:when>
      <xsl:when test="not(gco:CharacterString)">
        <!-- If no CharacterString, try to use the first textGroup available -->
        <xsl:value-of
          select="gmd:PT_FreeText/gmd:textGroup[position()=1]/gmd:LocalisedCharacterString"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="gco:CharacterString"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>
</xsl:stylesheet>
