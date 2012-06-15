<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template mode="LegislationInfo" match="legislationInformation">
        <che:legislationInformation>
            <xsl:apply-templates mode="LegislationInfo"/>
        </che:legislationInformation>
    </xsl:template>

    <xsl:template mode="LegislationInfo" match="GM03Comprehensive.Comprehensive.MD_Legislation">
        <che:CHE_MD_Legislation gco:isoType="gmd:MD_Legislation">
            <xsl:apply-templates mode="LegislationInfo"/>
        </che:CHE_MD_Legislation>
    </xsl:template>

    <xsl:template mode="LegislationInfo" match="country">
        <xsl:apply-templates mode="Country"/>
    </xsl:template>

    <xsl:template mode="LegislationInfo" match="legislationType">
        <che:legislationType>
            <che:CHE_CI_LegislationCode codeList="./resources/codeList.xml#LegislationCode" codeListValue="{.}"/>
        </che:legislationType>
    </xsl:template>

    <xsl:template mode="LegislationInfo" match="internalReference">
        <che:internalReference>
            <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
        </che:internalReference>
    </xsl:template>

    <xsl:template mode="LegislationInfo" match="language/CodeISO.LanguageCodeISO_|language/CodeISO.LanguageCode_">
        <che:language>
            <gmd:LanguageCode codeList="./resources/codeList.xml#LanguageCodeISO">
                <xsl:attribute name="codeListValue">
                    <xsl:apply-templates mode="languageToIso3" select="value"/>
                </xsl:attribute>
            </gmd:LanguageCode>
        </che:language>
    </xsl:template>

    <xsl:template mode="LegislationInfo" match="title">
        <che:title>
            <xsl:apply-templates mode="Citation"/>
        </che:title>
    </xsl:template>

    <xsl:template mode="LegislationInfo" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">LegislationInfo</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- ========================================================================== -->

    <xsl:template mode="Country" match="CodeISO.CountryCodeISO_|CodeISO.Country_">
        <che:country>
            <gmd:Country codeList="./resources/codeList.xml#CountryCodeISO" codeListValue="{value}"/>
        </che:country>
    </xsl:template>

    <xsl:template mode="Country" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">Country</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>
