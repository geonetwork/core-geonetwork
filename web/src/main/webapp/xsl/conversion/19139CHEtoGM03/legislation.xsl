<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="che gco gmd">

    <xsl:template mode="Legislation" match="che:CHE_MD_Legislation">
        <GM03_2Comprehensive.Comprehensive.MD_Legislation TID='x{generate-id(.)}'>
            <xsl:apply-templates mode="enumISO" select=".">
                <xsl:with-param name="name">CodeISO.CountryCodeISO_</xsl:with-param>
                <xsl:with-param name="element">country</xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates mode="enumISO" select=".">
                <xsl:with-param name="name">CodeISO.LanguageCodeISO_</xsl:with-param>
                <xsl:with-param name="element">language</xsl:with-param>
                <xsl:with-param name="lowercase">1</xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates mode="text" select="che:legislationType"/>
            <xsl:apply-templates mode="Legislation" select="che:internalReference"/>
            <xsl:apply-templates mode="Legislation" select="che:title"/>
        </GM03_2Comprehensive.Comprehensive.MD_Legislation>
    </xsl:template>

    <xsl:template mode="Legislation" match="che:title">
        <title REF="?">
            <GM03_2Comprehensive.Comprehensive.CI_Citation TID="x{generate-id(.)}">
                <xsl:apply-templates mode="RefSystem"/>
            </GM03_2Comprehensive.Comprehensive.CI_Citation>
        </title>
    </xsl:template>

    <xsl:template mode="Legislation" match="che:internalReference">
        <internalReference>
            <xsl:for-each select="gco:CharacterString">
                <GM03_2Core.Core.CharacterString_>
                    <value><xsl:value-of select="."/></value>
                </GM03_2Core.Core.CharacterString_>
            </xsl:for-each>
        </internalReference>
    </xsl:template>

    <xsl:template mode="Legislation" match="*" priority="-100">
        <ERROR>Unknown Legislation element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>
</xsl:stylesheet>