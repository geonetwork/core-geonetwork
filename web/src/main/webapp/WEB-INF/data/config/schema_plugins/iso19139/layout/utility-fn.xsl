<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  exclude-result-prefixes="#all">


  <!-- Get lang #id in metadata PT_Locale section,  deprecated: if not return the 2 first letters
        of the lang iso3code in uper case.
        
         if not return the lang iso3code in uper case.
        -->
  <xsl:function name="gn-fn-iso19139:getLangId" as="xs:string">
    <xsl:param name="md"/>
    <xsl:param name="lang"/>

    <xsl:choose>
      <xsl:when
        test="$md/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue = $lang]/@id">
        <xsl:value-of
          select="concat('#', $md/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue = $lang]/@id)"
        />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat('#', upper-case($lang))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>






  <xsl:function name="gn-fn-iso19139:getCodeListType" as="xs:string">
    <xsl:param name="name" as="xs:string"/>
    
    <xsl:variable name="configType" select="$editorConfig/editor/fields/for[@name = $name]/@use"/>
    
    <xsl:value-of select="if ($configType) then $configType else 'select'"/>
  </xsl:function>




</xsl:stylesheet>
