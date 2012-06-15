<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                exclude-result-prefixes="che gco gmd">

    <xsl:template mode="RespParty" match="che:CHE_CI_ResponsibleParty|gmd:CI_ResponsibleParty">
        <GM03_2Core.Core.CI_ResponsibleParty TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="che:individualFirstName"/>
            <xsl:apply-templates mode="text" select="che:individualLastName"/>
            <xsl:if test="gmd:contactInfo/gmd:CI_Contact/gmd:address/*/gmd:electronicMailAddress and
                          normalize-space(gmd:contactInfo/gmd:CI_Contact/gmd:address/*/gmd:electronicMailAddress) != ''">
                <electronicalMailAddress>
                    <xsl:apply-templates mode="RespParty" select="gmd:contactInfo/gmd:CI_Contact/gmd:address/*/gmd:electronicMailAddress"/>
                </electronicalMailAddress>
            </xsl:if>
            <xsl:apply-templates mode="text" select="gmd:organisationName"/>
            <xsl:apply-templates mode="text" select="gmd:positionName"/>
            <xsl:apply-templates mode="text" select="che:organisationAcronym"/>
            <xsl:apply-templates mode="RespParty" select="gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource"/>
            <xsl:apply-templates mode="RespParty" select="gmd:contactInfo/gmd:CI_Contact/gmd:address"/>
            <xsl:apply-templates mode="RespParty" select="gmd:contactInfo"/>

            <xsl:apply-templates mode="RespParty" select="che:parentResponsibleParty"/>

            <xsl:apply-templates mode="RespParty" select="gmd:contactInfo/gmd:CI_Contact/gmd:phone/che:CHE_CI_Telephone"/>
        </GM03_2Core.Core.CI_ResponsibleParty>
    </xsl:template>

    <xsl:template mode="RespParty" match="che:parentResponsibleParty">
        <GM03_2Core.Core.CI_ResponsiblePartyparentinfo TID="x{generate-id(.)}">
            <parentResponsibleParty REF="?">
                <xsl:apply-templates mode="RespParty"/>
            </parentResponsibleParty>
            <BACK_REF name="CI_ResponsibleParty"/>
        </GM03_2Core.Core.CI_ResponsiblePartyparentinfo>
    </xsl:template>

    <xsl:template mode="RespParty" match="gmd:electronicMailAddress">
        <GM03_2Core.Core.URL_>
            <value><xsl:value-of select="gco:CharacterString/text()"/></value>
        </GM03_2Core.Core.URL_>
    </xsl:template>

    <xsl:template mode="RespParty" match="gmd:onlineResource">
        <xsl:apply-templates mode="text" select="gmd:CI_OnlineResource/gmd:linkage"/>
    </xsl:template>

    <xsl:template mode="RespParty" match="che:CHE_CI_Telephone">
        <xsl:for-each select="gmd:voice[normalize-space(.) != '']">
            <GM03_2Core.Core.CI_Telephone TID="x{generate-id(.)}">
                <number><xsl:value-of select="gco:CharacterString"/></number>
                <numberType>mainNumber</numberType>
                <BACK_REF name="CI_ResponsibleParty"/>
            </GM03_2Core.Core.CI_Telephone>
        </xsl:for-each>
        <xsl:for-each select="gmd:facsimile[normalize-space(.) != '']">
            <GM03_2Core.Core.CI_Telephone TID="x{generate-id(.)}">
                <number><xsl:value-of select="gco:CharacterString"/></number>
                <numberType>facsimile</numberType>
                <BACK_REF name="CI_ResponsibleParty"/>
            </GM03_2Core.Core.CI_Telephone>
        </xsl:for-each>
        <xsl:for-each select="che:directNumber[normalize-space(.) != '']">
            <GM03_2Core.Core.CI_Telephone TID="x{generate-id(.)}">
                <number><xsl:value-of select="gco:CharacterString"/></number>
                <numberType>directNumber</numberType>
                <BACK_REF name="CI_ResponsibleParty"/>
            </GM03_2Core.Core.CI_Telephone>
        </xsl:for-each>
        <xsl:for-each select="che:mobile[normalize-space(.) != '']">
            <GM03_2Core.Core.CI_Telephone TID="x{generate-id(.)}">
                <number><xsl:value-of select="gco:CharacterString"/></number>
                <numberType>mobile</numberType>
                <BACK_REF name="CI_ResponsibleParty"/>
            </GM03_2Core.Core.CI_Telephone>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="RespParty" match="gmd:address">
        <address REF="?">
            <xsl:apply-templates mode="RespParty"/>
        </address>
    </xsl:template>

    <xsl:template mode="RespParty" match="che:CHE_CI_Address|gmd:CI_Address">
        <GM03_2Core.Core.CI_Address TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="che:streetName"/>
            <xsl:apply-templates mode="text" select="che:streetNumber"/>
            <xsl:apply-templates mode="text" select="che:addressLine"/>
            <xsl:apply-templates mode="text" select="che:postBox"/>
            <xsl:apply-templates mode="text" select="gmd:postalCode"/>
            <xsl:apply-templates mode="text" select="gmd:city"/>
            <xsl:apply-templates mode="text" select="gmd:administrativeArea"/>
            <xsl:apply-templates mode="RespParty" select="gmd:country"/>
        </GM03_2Core.Core.CI_Address>
    </xsl:template>

    <xsl:template mode="RespParty" match="gmd:country">
        <xsl:choose>
        <xsl:when test="@codeListValue">
            <country><xsl:value-of select="@codeListValue"/></country>
        </xsl:when>
        <xsl:when test="normalize-space(.) != ''">
            <country><xsl:value-of select="."/></country>
        </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template mode="RespParty" match="gmd:contactInfo">
        <contactInfo REF="?">
            <xsl:apply-templates mode="RespParty"/>
        </contactInfo>
    </xsl:template>

    <xsl:template mode="RespParty" match="gmd:CI_Contact">
        <GM03_2Core.Core.CI_Contact TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="gmd:hoursOfService"/>
            <xsl:apply-templates mode="textGroup" select="gmd:contactInstructions"/>
        </GM03_2Core.Core.CI_Contact>
    </xsl:template>

    <xsl:template mode="RespParty" match="*" priority="-100">
        <ERROR>Unknown RespParty element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>

    <xsl:template mode="RespPartyRole" match="che:CHE_CI_ResponsibleParty|gmd:CI_ResponsibleParty">
        <xsl:apply-templates mode="enum" select="gmd:role"/>
    </xsl:template>

    <xsl:template mode="RespPartyRole" match="*" priority="-100">
        <ERROR>Unknown RespPartyRole element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>
</xsl:stylesheet>
