<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:int="http://www.interlis.ch/INTERLIS2.3"
                exclude-result-prefixes="int"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template mode="LegislationInfo" match="int:legislationInformation">
        <che:legislationInformation>
            <xsl:apply-templates mode="LegislationInfo"/>
        </che:legislationInformation>
    </xsl:template>

    <xsl:template mode="LegislationInfo" match="int:GM03_2Comprehensive.Comprehensive.MD_Legislation">
        <che:CHE_MD_Legislation gco:isoType="gmd:MD_Legislation">
            <xsl:apply-templates mode="LegislationInfo"/>
        </che:CHE_MD_Legislation>
    </xsl:template>

    <xsl:template mode="LegislationInfo" match="int:country">
        <xsl:apply-templates mode="Country"/>
    </xsl:template>

    <xsl:template mode="LegislationInfo" match="int:legislationType">
        <che:legislationType>
            <che:CHE_CI_LegislationCode codeList="./resources/codeList.xml#LegislationCode" codeListValue="{.}"/>
        </che:legislationType>
    </xsl:template>

    <xsl:template mode="LegislationInfo" match="int:internalReference">
        <che:internalReference>
            <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
        </che:internalReference>
    </xsl:template>

    <xsl:template mode="LegislationInfo" match="int:language/int:CodeISO.LanguageCodeISO_|int:language/int:CodeISO.LanguageCode_">
        <che:language>
            <gmd:LanguageCode codeList="./resources/codeList.xml#LanguageCodeISO">
                <xsl:attribute name="codeListValue">
                    <xsl:apply-templates mode="languageToIso3" select="int:value"/>
                </xsl:attribute>
            </gmd:LanguageCode>
        </che:language>
    </xsl:template>

    <xsl:template mode="LegislationInfo" match="int:title">
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

    <xsl:template mode="Country" match="int:CodeISO.CountryCodeISO_|int:CodeISO.Country_">
        <che:country>
            <gmd:Country codeList="./resources/codeList.xml#CountryCodeISO" codeListValue="{int:value}"/>
        </che:country>
    </xsl:template>

    <xsl:template mode="Country" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">Country</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>
