<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:int="http://www.interlis.ch/INTERLIS2.3"
                exclude-result-prefixes="int"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template mode="MetaData" match="int:GM03_2Core.Core.MD_Metadata">
        <xsl:apply-templates mode="MetaData" select="int:fileIdentifier"/>
        <xsl:choose>
            <xsl:when test="not(language) or normalize-space(language)=''">
                <gmd:language>
                    <gco:CharacterString>deu</gco:CharacterString>
                </gmd:language>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="MetaData" select="int:language"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates mode="MetaData" select="int:characterSet"/>
        <xsl:apply-templates mode="MetaData" select="int:parentIdentifier"/>
        <xsl:apply-templates mode="MetaData" select="int:hierarchyLevel"/>
        <xsl:apply-templates mode="MetaData" select="int:hierarchyLevelName"/>
        <xsl:apply-templates mode="MetaData" select="int:GM03_2Core.Core.MD_Metadatacontact"/>
        <xsl:apply-templates mode="MetaData" select="int:dateStamp"/>
        <xsl:apply-templates mode="text" select="int:metadataStandardName"/>
        <xsl:apply-templates mode="text" select="int:metadataStandardVersion"/>
        <xsl:apply-templates mode="MetaData" select="int:dataSetURI"/>
        <gmd:locale>
            <gmd:PT_Locale id="DE">
                <gmd:languageCode><gmd:LanguageCode codeList="#LanguageCode" codeListValue="deu">German</gmd:LanguageCode></gmd:languageCode>
                <gmd:characterEncoding>
                    <gmd:MD_CharacterSetCode codeList="#MD_CharacterSetCode" codeListValue="utf8">UTF8</gmd:MD_CharacterSetCode>
                </gmd:characterEncoding>
            </gmd:PT_Locale>
        </gmd:locale>
        <gmd:locale>
            <gmd:PT_Locale id="FR">
                <gmd:languageCode><gmd:LanguageCode codeList="#LanguageCode" codeListValue="fra">French</gmd:LanguageCode></gmd:languageCode>
                <gmd:characterEncoding>
                    <gmd:MD_CharacterSetCode codeList="#MD_CharacterSetCode" codeListValue="utf8">UTF8</gmd:MD_CharacterSetCode>
                </gmd:characterEncoding>
            </gmd:PT_Locale>
        </gmd:locale>
        <gmd:locale>
            <gmd:PT_Locale id="IT">
                <gmd:languageCode><gmd:LanguageCode codeList="#LanguageCode" codeListValue="ita">Italian</gmd:LanguageCode></gmd:languageCode>
                <gmd:characterEncoding>
                    <gmd:MD_CharacterSetCode codeList="#MD_CharacterSetCode" codeListValue="utf8">UTF8</gmd:MD_CharacterSetCode>
                </gmd:characterEncoding>
            </gmd:PT_Locale>
        </gmd:locale>
        <gmd:locale>
            <gmd:PT_Locale id="EN">
                <gmd:languageCode><gmd:LanguageCode codeList="#LanguageCode" codeListValue="eng">English</gmd:LanguageCode></gmd:languageCode>
                <gmd:characterEncoding>
                    <gmd:MD_CharacterSetCode codeList="#MD_CharacterSetCode" codeListValue="utf8">UTF8</gmd:MD_CharacterSetCode>
                </gmd:characterEncoding>
            </gmd:PT_Locale>
        </gmd:locale>
        <xsl:apply-templates mode="MetaData" select="int:GM03_2Comprehensive.Comprehensive.MD_GridSpatialRepresentation|
                                                     int:GM03_2Comprehensive.Comprehensive.MD_VectorSpatialRepresentation|
                                                     int:GM03_2Comprehensive.Comprehensive.MD_Georeferenceable|
                                                     int:GM03_2Comprehensive.Comprehensive.MD_Georectified"/>
        <xsl:apply-templates mode="MetaData" select="int:GM03_2Core.Core.referenceSystemInfoMD_Metadata"/>
        <xsl:apply-templates mode="MetaData" select="int:metadataExtensionInfo"/>
        <xsl:apply-templates mode="MetaData" select="int:GM03_2Comprehensive.Comprehensive.MD_DataIdentification|int:GM03_2Core.Core.MD_DataIdentification|int:GM03_2Comprehensive.Comprehensive.SV_ServiceIdentification"/>
        <xsl:apply-templates mode="MetaData" select="int:GM03_2Comprehensive.Comprehensive.MD_CoverageDescription|int:GM03_2Comprehensive.Comprehensive.MD_FeatureCatalogueDescription|int:GM03_2Comprehensive.Comprehensive.MD_RangeDimension|int:GM03_2Comprehensive.Comprehensive.MD_Band|int:GM03_2Comprehensive.Comprehensive.MD_ImageDescription"/>
        <xsl:apply-templates mode="MetaData" select="int:distributionInfo"/>
        <xsl:apply-templates mode="MetaData" select="int:GM03_2Core.Core.DQ_DataQuality"/>
        <xsl:apply-templates mode="MetaData" select="int:GM03_2Comprehensive.Comprehensive.portrayalCatalogueInfoMD_Metadata/int:portrayalCatalogueInfo"/>
        <xsl:apply-templates mode="MetaData" select="int:GM03_2Comprehensive.Comprehensive.MD_MetadatametadataConstraints/metadataConstraints"/>
        <xsl:apply-templates mode="MetaData" select="int:applicationSchemaInfo"/>
        <xsl:apply-templates mode="MetaData" select="int:metadataMaintenance"/>
        <xsl:apply-templates mode="MetaData" select="int:GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformation"/>
        <xsl:apply-templates mode="MetaData" select="int:series"/>
        <xsl:apply-templates mode="MetaData" select="int:describes"/>
        <xsl:apply-templates mode="MetaData" select="int:propertyType"/>
        <xsl:apply-templates mode="MetaData" select="int:featureType"/>
        <xsl:apply-templates mode="MetaData" select="int:featureAttribute"/>
        <xsl:apply-templates mode="MetaData" select="int:GM03_2Comprehensive.Comprehensive.MD_MetadatalegislationInformation"/>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:metadataConstraints">
        <gmd:metadataConstraints>
            <xsl:apply-templates mode="ConstsTypes"/>
        </gmd:metadataConstraints>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:portrayalCatalogueInfo">
        <gmd:portrayalCatalogueInfo>
            <xsl:apply-templates mode="MetaData"/>
        </gmd:portrayalCatalogueInfo>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:GM03_2Comprehensive.Comprehensive.MD_PortrayalCatalogueReference">
        <che:CHE_MD_PortrayalCatalogueReference>
            <xsl:for-each select="int:GM03_2Comprehensive.Comprehensive.CI_Citation">
                <gmd:portrayalCatalogueCitation>
                    <xsl:apply-templates select="." mode="Citation"/>
                </gmd:portrayalCatalogueCitation>
            </xsl:for-each>
        </che:CHE_MD_PortrayalCatalogueReference>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:fileIdentifier">
        <gmd:fileIdentifier>
            <gco:CharacterString>
                <xsl:value-of select="."/>
            </gco:CharacterString>
        </gmd:fileIdentifier>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:language">
        <gmd:language>
            <gco:CharacterString>
                <xsl:apply-templates mode="languageToIso3" select="."/>
            </gco:CharacterString>
        </gmd:language>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:characterSet">
        <gmd:characterSet>
            <gmd:MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode" codeListValue="{.}"/>
        </gmd:characterSet>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:parentIdentifier">
        <gmd:parentIdentifier>
            <gco:CharacterString>
                <xsl:value-of select="int:GM03_2Core.Core.MD_Metadata/fileIdentifier"/>
            </gco:CharacterString>
        </gmd:parentIdentifier>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:hierarchyLevel">
        <xsl:for-each select="int:GM03_2Core.Core.MD_ScopeCode_">
            <gmd:hierarchyLevel>
                <gmd:MD_ScopeCode codeList="./resources/codeList.xml#MD_ScopeCode" codeListValue="{int:value}"/>
            </gmd:hierarchyLevel>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:hierarchyLevelName">
        <xsl:for-each select="int:GM03_2Core.Core.CharacterString_">
            <gmd:hierarchyLevelName>
                <gco:CharacterString>
                    <xsl:value-of select="."/>
                </gco:CharacterString>
            </gmd:hierarchyLevelName>
        </xsl:for-each>
    </xsl:template>


    <xsl:template mode="MetaData" match="int:GM03_2Core.Core.MD_Metadatacontact">
        <gmd:contact>
            <xsl:apply-templates select="." mode="RespParty"/>
        </gmd:contact>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:dateStamp">
        <gmd:dateStamp>
            <xsl:apply-templates mode="dateTime" select="."/>
        </gmd:dateStamp>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:dataSetURI">
        <gmd:dataSetURI>
            <gco:CharacterString>
                <xsl:value-of select="."/>
            </gco:CharacterString>
        </gmd:dataSetURI>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:metadataMaintenance">
        <gmd:metadataMaintenance>
            <xsl:apply-templates mode="MaintenanceInfo"/>
        </gmd:metadataMaintenance>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformation">
        <gmd:metadataMaintenance>
            <xsl:apply-templates mode="MaintenanceInfo" select="."/>
        </gmd:metadataMaintenance>
    </xsl:template>

    <xsl:template mode="MetaData" match="int:distributionInfo">
        <gmd:distributionInfo>
            <xsl:apply-templates mode="Distribution"/>
		</gmd:distributionInfo>
    </xsl:template>

    <xsl:template mode="MetaData"
                  match="int:GM03_2Comprehensive.Comprehensive.MD_GridSpatialRepresentation|
                         int:GM03_2Comprehensive.Comprehensive.MD_VectorSpatialRepresentation|
                         int:GM03_2Comprehensive.Comprehensive.MD_Georeferenceable|
                         int:GM03_2Comprehensive.Comprehensive.MD_Georectified">
        <gmd:spatialRepresentationInfo>
            <xsl:apply-templates select="." mode="SpatialRepr"/>
        </gmd:spatialRepresentationInfo>
    </xsl:template>

    <xsl:template mode="MetaData"
                  match="int:GM03_2Comprehensive.Comprehensive.MD_CoverageDescription|int:GM03_2Comprehensive.Comprehensive.MD_FeatureCatalogueDescription|int:GM03_2Comprehensive.Comprehensive.MD_RangeDimension|int:GM03_2Comprehensive.Comprehensive.MD_Band|int:GM03_2Comprehensive.Comprehensive.MD_ImageDescription">
        <gmd:contentInfo>
            <xsl:apply-templates select="." mode="Content"/>
        </gmd:contentInfo>
    </xsl:template>

    <xsl:template match="int:GM03_2Comprehensive.Comprehensive.MD_DataIdentification|int:GM03_2Core.Core.MD_DataIdentification|int:GM03_2Comprehensive.Comprehensive.SV_ServiceIdentification" mode="MetaData">
        <gmd:identificationInfo>
            <xsl:apply-templates select="." mode="DataIdentification"/>
        </gmd:identificationInfo>
    </xsl:template>
    <xsl:template match="int:GM03_2Comprehensive.Comprehensive.MD_MetadatalegislationInformation" mode="MetaData">
        <xsl:apply-templates mode="LegislationInfo"/>
    </xsl:template>

    <xsl:template match="int:GM03_2Core.Core.DQ_DataQuality" mode="MetaData">
        <xsl:apply-templates select="." mode="DataQuality"/>
    </xsl:template>

    <xsl:template match="int:GM03_2Core.Core.referenceSystemInfoMD_Metadata" mode="MetaData">
        <gmd:referenceSystemInfo>
            <xsl:apply-templates mode="RefSystem"/>
        </gmd:referenceSystemInfo>
    </xsl:template>

    <xsl:template match="*" mode="MetaData">
        <ERROR mode="MetaData" tag="{name(..)}/{name(.)}"/>
    </xsl:template>

    <xsl:template mode="MetaData" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">MetaData</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
