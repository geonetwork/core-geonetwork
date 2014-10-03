<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:xslutil="java:org.fao.geonet.util.XslUtil"
    xmlns:gnf="http://www.fao.org/geonetwork/functions">

    <!-- Select the root of the metadata element within the full xml provided for creating the output -->
    <xsl:function name="gnf:metadataRoot">
        <xsl:param name="doc"/>
        <xsl:copy-of select="$doc/root/gmd:MD_Metadata | $doc/root/*[@gco:isoType='gmd:MD_Metadata']"/>
    </xsl:function>

    <!-- Retrieve the iso19139 metadata language code from the iso19139 based metadata -->
    <xsl:function name="gnf:metadataLanguage">
        <xsl:param name="doc"/>
        <xsl:value-of select="gnf:metadataRoot($doc)/gmd:language/gco:CharacterString"/>
    </xsl:function>

    <!-- Retrieve the iso19139 metadata language code from the iso19139 based metadata -->
    <xsl:function name="gnf:twoCharLangCode">
        <xsl:param name="doc"/>
        <xsl:value-of select="gnf:metadataRoot($doc)/gmd:language/gco:CharacterString"/>
    </xsl:function>

    <!-- Create a label: value widget where the value is the translated value.  It will attempt to get the desired translation but if that
         value does not exist then it will simply get any translation possible -->
    <xsl:function name="gnf:translatedStringWidget">
        <xsl:param name="label"/>
        <xsl:param name="node"/>
        <xsl:param name="doc"/>
        <xsl:variable name="twoCharLangCode">
            <xsl:choose>
                <xsl:when test="$doc/lang">
                    <xsl:value-of select="xslutil:twoCharLangCode($doc/lang)"/>
                </xsl:when>
                <xsl:otherwise>EN</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="langCode" select="concat('#', upper-case($twoCharLangCode))" />
        <xsl:variable name="value">
            <xsl:choose>
                <xsl:when test="string-length(normalize-space($node/gco:CharacterString)) > 0">
                    <xsl:value-of select="$node/gco:CharacterString" />
                </xsl:when>
                <xsl:when test="string-length(normalize-space($node/gmd:PT_FreeText//gmd:LocalisedCharacterString[@locale=$langCode])) > 0">
                    <xsl:value-of select="$node/gmd:PT_FreeText//gmd:LocalisedCharacterString[@locale=$langCode]"/>
                </xsl:when>
                <xsl:when test="string-length(normalize-space($node/gmd:PT_FreeText//gmd:LocalisedCharacterString[1])) > 0">
                    <xsl:value-of select="$node/gmd:PT_FreeText//gmd:LocalisedCharacterString[1]"/>
                </xsl:when>
                <xsl:otherwise>--</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:copy-of select="gnf:textField($label, $value)" />
    </xsl:function>
</xsl:stylesheet>
