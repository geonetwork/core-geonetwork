<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:che="http://www.geocat.ch/2008/che" xmlns:gco="http://www.isotc211.org/2005/gco" 
    xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:srv="http://www.isotc211.org/2005/srv"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml" 
    xmlns:gts="http://www.isotc211.org/2005/gts">

    <xsl:template match="/">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <!-- Remove them -->
    <xsl:template match="gmd:individualName|gmd:deliveryPoint" priority="2"/>

    <!-- Map all dateTime to date type -->
    <xsl:template match="gco:DateTime[name(..)!='dateStamp']">
        <gco:Date>
            <xsl:value-of select="substring-before(., 'T')"/>
        </gco:Date>
    </xsl:template>

    <xsl:template match="gmd:CI_ResponsibleParty">
        <che:CHE_CI_ResponsibleParty gco:isoType="gmd:CI_ResponsibleParty">
            <xsl:apply-templates select="*"/>
            <che:individualFirstName/>
            <che:individualLastName>
                <gco:CharacterString>
                    <xsl:value-of select="gmd:individualName"/>
                </gco:CharacterString>
            </che:individualLastName>
        </che:CHE_CI_ResponsibleParty>
    </xsl:template>

    <xsl:template match="gmd:CI_Address">
        <che:CHE_CI_Address gco:isoType="gmd:CI_Address">
            <xsl:apply-templates select="*"/>
            <che:streetName>
                <gco:CharacterString>
                    <xsl:value-of select="gmd:deliveryPoint"/>
                </gco:CharacterString>
            </che:streetName>
            <che:streetNumber/>
        </che:CHE_CI_Address>
    </xsl:template>

    <xsl:template match="gmd:CI_Telephone">
        <che:CHE_CI_Telephone gco:isoType="gmd:CI_Telephone">
            <xsl:apply-templates select="*"/>
            <che:directNumber/>
            <che:mobile/>
        </che:CHE_CI_Telephone>
    </xsl:template>

    <xsl:template match="gmd:MD_Legislation">
        <che:CHE_MD_Legislation gco:isoType="gmd:MD_Legislation">
            <xsl:apply-templates select="*"/>
        </che:CHE_MD_Legislation>
    </xsl:template>

    <xsl:template match="gmd:MD_LegalConstraints">
        <che:CHE_MD_LegalConstraints gco:isoType="gmd:MD_LegalConstraints">
            <xsl:apply-templates select="*"/>
        </che:CHE_MD_LegalConstraints>
    </xsl:template>

    <xsl:template match="gmd:MD_MaintenanceInformation">
        <che:CHE_MD_MaintenanceInformation gco:isoType="gmd:MD_MaintenanceInformation">
            <xsl:apply-templates select="*"/>
        </che:CHE_MD_MaintenanceInformation>
    </xsl:template>

    <xsl:template match="gmd:MD_PortrayalCatalogueReference">
        <che:CHE_MD_PortrayalCatalogueReference>
            <xsl:apply-templates select="*"/>
        </che:CHE_MD_PortrayalCatalogueReference>
    </xsl:template>

    <xsl:template match="gmd:MD_FeatureCatalogueDescription">
        <che:CHE_MD_FeatureCatalogueDescription>
            <xsl:apply-templates select="*"/>
        </che:CHE_MD_FeatureCatalogueDescription>
    </xsl:template>
    
    <xsl:template match="gmd:MD_CoverageDescription">
        <che:CHE_MD_CoverageDescription>
            <xsl:apply-templates select="*"/>
        </che:CHE_MD_CoverageDescription>
    </xsl:template>
    
    <xsl:template match="gmd:MD_ImageDescription">
        <che:CHE_MD_ImageDescription>
            <xsl:apply-templates select="*"/>
        </che:CHE_MD_ImageDescription>
    </xsl:template>
    
    <xsl:template match="srv:SV_ServiceIdentification">
        <che:CHE_SV_ServiceIdentification gco:isoType="srv:SV_ServiceIdentification">
            <xsl:apply-templates select="*"/>
        </che:CHE_SV_ServiceIdentification>
    </xsl:template>
    
    <xsl:template match="gmd:MD_DataIdentification">
        <che:CHE_MD_DataIdentification gco:isoType="gmd:MD_DataIdentification">
            <xsl:apply-templates select="*"/>
        </che:CHE_MD_DataIdentification>
    </xsl:template>

    <xsl:template match="gmd:MD_Metadata">
        <che:CHE_MD_Metadata gco:isoType="gmd:MD_Metadata">
            <xsl:apply-templates select="*"/>
        </che:CHE_MD_Metadata>
    </xsl:template>

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
