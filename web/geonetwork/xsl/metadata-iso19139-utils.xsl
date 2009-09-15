<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd"
    version="2.0">


    <!-- Template use to return a gco:CharacterString element
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
                    select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId]"
                />
            </xsl:when>
            <xsl:when test="not(gco:CharacterString)">
                <!-- If no CharacterString, try to use the first textGroup available -->
                <xsl:value-of
                    select="gmd:PT_FreeText/gmd:textGroup[position()=1]/gmd:LocalisedCharacterString"
                />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="gco:CharacterString"/>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!-- Map GUI language to iso3code -->
    <xsl:template name="getLangId">
        <xsl:param name="langGui"/>
        <xsl:param name="md"/>
        
        <!-- Mapping gui language to iso3code -->
        <xsl:variable name="lang">
            <xsl:choose>
                <xsl:when test="$langGui='ar'">ara</xsl:when>
                <xsl:when test="$langGui='cn'">chi</xsl:when>
                <xsl:when test="$langGui='de'">ger</xsl:when>
                <xsl:when test="$langGui='es'">spa</xsl:when>
                <xsl:when test="$langGui='fr'">fre</xsl:when><!-- TODO : sometimes fra is used in metadata record -->
                <xsl:when test="$langGui='nl'">dut</xsl:when>
                <xsl:when test="$langGui='ru'">rus</xsl:when>
                <xsl:otherwise>eng</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="getLangIdFromMetadata">
            <xsl:with-param name="lang" select="$lang"/>
            <xsl:with-param name="md" select="$md"/>
        </xsl:call-template>
    </xsl:template>        

    <!-- Get lang #id in metadata PT_Locale section,  if not return the 2 first letters 
        of the lang iso3code in uper case. -->
    <xsl:template name="getLangIdFromMetadata">
        <xsl:param name="md"/>
        <xsl:param name="lang"/>
    
        <xsl:choose>
            <xsl:when
                test="$md/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue = $lang]/@id"
                    >#<xsl:value-of
                        select="$md/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue = $lang]/@id"
                    />
            </xsl:when>
            <xsl:otherwise>#<xsl:value-of select="upper-case(substring($lang, 1, 2))"/></xsl:otherwise>            
        </xsl:choose>
    </xsl:template>
    
    <!-- Get lang codeListValue in metadata PT_Locale section,  if not return eng by default -->
    <xsl:template name="getLangCode">
        <xsl:param name="md"/>
        <xsl:param name="langId"/>

          <xsl:choose>
            <xsl:when
                test="$md/gmd:locale/gmd:PT_Locale[@id=$langId]/gmd:languageCode/gmd:LanguageCode/@codeListValue"
                    ><xsl:value-of
                        select="$md/gmd:locale/gmd:PT_Locale[@id=$langId]/gmd:languageCode/gmd:LanguageCode/@codeListValue"
                /></xsl:when>
            <xsl:otherwise>eng</xsl:otherwise>            
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
