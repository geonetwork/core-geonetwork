<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="che gco gmd">

    <xsl:template mode="RefSystem" match="gmd:referenceSystemInfo">
        <GM03_2Core.Core.referenceSystemInfoMD_Metadata TID='x{generate-id(.)}'>
            <referenceSystemInfo REF="?">
                <xsl:apply-templates mode="RefSystem"/>
            </referenceSystemInfo>
            <BACK_REF name="MD_Metadata"/>
        </GM03_2Core.Core.referenceSystemInfoMD_Metadata>
    </xsl:template>

    <xsl:template mode="RefSystem" match="gmd:MD_ReferenceSystem">
        <GM03_2Core.Core.MD_ReferenceSystem TID='x{generate-id(.)}'>
            <xsl:apply-templates mode="RefSystem"/>
        </GM03_2Core.Core.MD_ReferenceSystem>
    </xsl:template>

    <xsl:template mode="RefSystem" match="gmd:referenceSystemIdentifier">
        <referenceSystemIdentifier REF='?'>
            <xsl:apply-templates mode="RefSystem"/>
        </referenceSystemIdentifier>
    </xsl:template>

    <xsl:template mode="RefSystem" match="gmd:RS_Identifier">
        <GM03_2Comprehensive.Comprehensive.RS_Identifier TID='x{generate-id(.)}'>
            <xsl:apply-templates mode="textGroup" select="gmd:code"/>
            <xsl:apply-templates mode="RefSystem" select="gmd:authority"/>
            <xsl:apply-templates mode="text" select="gmd:codeSpace"/>
            <xsl:apply-templates mode="text" select="gmd:version"/>
        </GM03_2Comprehensive.Comprehensive.RS_Identifier>
    </xsl:template>

    <xsl:template mode="RefSystem" match="gmd:authority">
        <MD_Authority REF="?">
            <GM03_2Core.Core.MD_Authority TID='x{generate-id(.)}'>
                <xsl:apply-templates mode="RefSystem"/>
            </GM03_2Core.Core.MD_Authority>
        </MD_Authority>
    </xsl:template>

    <xsl:template mode="RefSystem" match="gmd:CI_Citation">
        <xsl:param name="showIdentifier" select="true()"/>
        <xsl:param name="backRef"/>
        
        <xsl:apply-templates mode="textGroup" select="gmd:title"/>
        <xsl:apply-templates mode="text" select="gmd:edition"/>
        <xsl:apply-templates mode="text" select="gmd:editionDate"/>
        <xsl:apply-templates mode="groupEnum" select=".">
        	<xsl:with-param name="element">presentationForm</xsl:with-param>
        	<xsl:with-param name="newName">GM03_2Comprehensive.Comprehensive.CI_PresentationFormCode_</xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates mode="text" select="gmd:ISBN"/>
        <xsl:apply-templates mode="text" select="gmd:ISSN"/>
        <xsl:apply-templates mode="groupText" select=".">
            <xsl:with-param name="element">alternateTitle</xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates mode="text" select="gmd:collectiveTitle"/>
        <xsl:apply-templates mode="text" select="gmd:otherCitationDetails"/>
        <xsl:if test="$backRef">
            <BACK_REF name="{$backRef}"/>
        </xsl:if>
        <xsl:apply-templates mode="RefSystem" select="gmd:series/gmd:CI_Series"/>

        <xsl:apply-templates mode="RefSystem" select="gmd:date"/>
        <xsl:if test="$showIdentifier = true()">
            <xsl:apply-templates mode="RefSystem" select="gmd:identifier/*"/>
        </xsl:if>
        <!-- not mapped -->
        <xsl:apply-templates mode="RefSystem" select="gmd:citedResponsibleParty"/>
    </xsl:template>

    <xsl:template mode="RefSystem" match="gmd:citedResponsibleParty">
        <GM03_2Comprehensive.Comprehensive.CI_CitationcitedResponsibleParty TID="x{generate-id(.)}">
            <citedResponsibleParty REF="?">
                <xsl:apply-templates mode="RespParty"/>
            </citedResponsibleParty>
            <BACK_REF name="CI_Citation"/>
            <xsl:apply-templates mode="RespPartyRole"/>
        </GM03_2Comprehensive.Comprehensive.CI_CitationcitedResponsibleParty>
    </xsl:template>

    <xsl:template mode="RefSystem" match="gmd:date">
        <xsl:apply-templates mode="RefSystem"/>
    </xsl:template>

    <xsl:template mode="RefSystem" match="gmd:CI_Series">
        <series REF="?">
	        <GM03_2Comprehensive.Comprehensive.CI_Series TID="x{generate-id(.)}">
		        <xsl:apply-templates mode="text" select="gmd:page"/>
		        <xsl:apply-templates mode="text" select="gmd:issueIdentification"/>
		        <xsl:apply-templates mode="text" select="gmd:name"/>
	        </GM03_2Comprehensive.Comprehensive.CI_Series>
        </series>
    </xsl:template>

    <xsl:template mode="RefSystem" match="gmd:CI_Date">
        <GM03_2Core.Core.CI_Date TID='x{generate-id(.)}'>
            <xsl:apply-templates mode="text" select="gmd:date"/>
            <xsl:apply-templates mode="text" select="gmd:dateType"/>
            <BACK_REF name="CI_Citation"/>
        </GM03_2Core.Core.CI_Date>
    </xsl:template>

    <xsl:template mode="RefSystem" match="*" priority="-100">
        <ERROR>Unknown RefSystem element <xsl:value-of select="local-name(.)"/> child of <xsl:value-of select="local-name(..)"/> </ERROR>
    </xsl:template>
</xsl:stylesheet>