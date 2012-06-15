<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template mode="MetaData" match="GM03Core.Core.MD_Metadata">
    	<xsl:choose>
    		<xsl:when test="string-length($uuid) > 0">
    		<gmd:fileIdentifier>
	            <gco:CharacterString>
	                <xsl:value-of select="$uuid"/>
	            </gco:CharacterString>
	        </gmd:fileIdentifier>
    		</xsl:when>
    		<xsl:otherwise>
    			<xsl:apply-templates mode="MetaData" select="fileIdentifier"/>
    		</xsl:otherwise>
    	</xsl:choose>
        <xsl:choose>
            <xsl:when test="not(language) or normalize-space(language)=''">
                <gmd:language>
                    <gco:CharacterString>deu</gco:CharacterString>
                </gmd:language>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="MetaData" select="language"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates mode="MetaData" select="characterSet"/>
        <xsl:apply-templates mode="MetaData" select="parentIdentifier"/>
        <xsl:apply-templates mode="MetaData" select="hierarchyLevel"/>
        <xsl:apply-templates mode="MetaData" select="hierarchyLevelName"/>
        <xsl:apply-templates mode="MetaData" select="GM03Core.Core.MD_Metadatacontact"/>
        <xsl:apply-templates mode="MetaData" select="dateStamp"/>
        <xsl:apply-templates mode="text" select="metadataStandardName"/>
        <xsl:apply-templates mode="text" select="metadataStandardVersion"/>
        <xsl:apply-templates mode="MetaData" select="dataSetURI"/>
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
        <xsl:apply-templates mode="MetaData" select="GM03Comprehensive.Comprehensive.MD_GridSpatialRepresentation|
                                                     GM03Comprehensive.Comprehensive.MD_VectorSpatialRepresentation|
                                                     GM03Comprehensive.Comprehensive.MD_Georeferenceable|
                                                     GM03Comprehensive.Comprehensive.MD_Georectified"/>
        <xsl:apply-templates mode="MetaData" select="GM03Core.Core.referenceSystemInfoMD_Metadata"/>
        <xsl:apply-templates mode="MetaData" select="metadataExtensionInfo"/>
        <xsl:apply-templates mode="MetaData" select="GM03Comprehensive.Comprehensive.MD_DataIdentification|GM03Core.Core.MD_DataIdentification|GM03Comprehensive.Comprehensive.SV_ServiceIdentification"/>
        <xsl:apply-templates mode="MetaData" select="GM03Comprehensive.Comprehensive.MD_CoverageDescription|GM03Comprehensive.Comprehensive.MD_FeatureCatalogueDescription|GM03Comprehensive.Comprehensive.MD_RangeDimension|GM03Comprehensive.Comprehensive.MD_Band|GM03Comprehensive.Comprehensive.MD_ImageDescription"/>
        <xsl:apply-templates mode="MetaData" select="distributionInfo"/>
        <xsl:apply-templates mode="MetaData" select="GM03Core.Core.DQ_DataQuality"/>
        <xsl:apply-templates mode="MetaData" select="GM03Comprehensive.Comprehensive.portrayalCatalogueInfoMD_Metadata/portrayalCatalogueInfo"/>
        <xsl:apply-templates mode="MetaData" select="GM03Comprehensive.Comprehensive.MD_MetadatametadataConstraints/metadataConstraints"/>
        <xsl:apply-templates mode="MetaData" select="applicationSchemaInfo"/>
        <xsl:apply-templates mode="MetaData" select="metadataMaintenance"/>
        <xsl:apply-templates mode="MetaData" select="GM03Comprehensive.Comprehensive.MD_MaintenanceInformation"/>
        <xsl:apply-templates mode="MetaData" select="series"/>
        <xsl:apply-templates mode="MetaData" select="describes"/>
        <xsl:apply-templates mode="MetaData" select="propertyType"/>
        <xsl:apply-templates mode="MetaData" select="featureType"/>
        <xsl:apply-templates mode="MetaData" select="featureAttribute"/>
        <xsl:apply-templates mode="MetaData" select="GM03Comprehensive.Comprehensive.MD_MetadatalegislationInformation"/>
    </xsl:template>

    <xsl:template mode="MetaData" match="metadataConstraints">
        <gmd:metadataConstraints>
            <xsl:apply-templates mode="ConstsTypes"/>
        </gmd:metadataConstraints>
    </xsl:template>

    <xsl:template mode="MetaData" match="portrayalCatalogueInfo">
        <gmd:portrayalCatalogueInfo>
            <xsl:apply-templates mode="MetaData"/>
        </gmd:portrayalCatalogueInfo>
    </xsl:template>

    <xsl:template mode="MetaData" match="GM03Comprehensive.Comprehensive.MD_PortrayalCatalogueReference">
        <che:CHE_MD_PortrayalCatalogueReference>
            <xsl:for-each select="GM03Comprehensive.Comprehensive.CI_Citation">
                <gmd:portrayalCatalogueCitation>
                    <xsl:apply-templates select="." mode="Citation"/>
                </gmd:portrayalCatalogueCitation>
            </xsl:for-each>
        </che:CHE_MD_PortrayalCatalogueReference>
    </xsl:template>

    <xsl:template mode="MetaData" match="fileIdentifier">
        <gmd:fileIdentifier>
            <gco:CharacterString>
                <xsl:value-of select="."/>
            </gco:CharacterString>
        </gmd:fileIdentifier>
    </xsl:template>

    <xsl:template mode="MetaData" match="language">
        <gmd:language>
            <gco:CharacterString>
                <xsl:apply-templates mode="languageToIso3" select="."/>
            </gco:CharacterString>
        </gmd:language>
    </xsl:template>

    <xsl:template mode="MetaData" match="characterSet">
        <gmd:characterSet>
            <gmd:MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode" codeListValue="{.}"/>
        </gmd:characterSet>
    </xsl:template>

    <xsl:template mode="MetaData" match="parentIdentifier">
        <gmd:parentIdentifier>
            <gco:CharacterString>
                <xsl:value-of select="GM03Core.Core.MD_Metadata/fileIdentifier"/>
            </gco:CharacterString>
        </gmd:parentIdentifier>
    </xsl:template>

    <xsl:template mode="MetaData" match="hierarchyLevel">
        <xsl:for-each select="GM03Core.Core.MD_ScopeCode_">
            <gmd:hierarchyLevel>
                <gmd:MD_ScopeCode codeList="./resources/codeList.xml#MD_ScopeCode" codeListValue="{value}"/>
            </gmd:hierarchyLevel>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="MetaData" match="hierarchyLevelName">
        <xsl:for-each select="GM03Core.Core.CharacterString_">
            <gmd:hierarchyLevelName>
                <gco:CharacterString>
                    <xsl:value-of select="."/>
                </gco:CharacterString>
            </gmd:hierarchyLevelName>
        </xsl:for-each>
    </xsl:template>


    <xsl:template mode="MetaData" match="GM03Core.Core.MD_Metadatacontact">
        <gmd:contact>
            <xsl:apply-templates select="." mode="RespParty"/>
        </gmd:contact>
    </xsl:template>

    <xsl:template mode="MetaData" match="dateStamp">
        <gmd:dateStamp>
            <xsl:apply-templates mode="dateTime" select="."/>
        </gmd:dateStamp>
    </xsl:template>

    <xsl:template mode="MetaData" match="dataSetURI">
        <gmd:dataSetURI>
            <gco:CharacterString>
                <xsl:value-of select="."/>
            </gco:CharacterString>
        </gmd:dataSetURI>
    </xsl:template>

    <xsl:template mode="MetaData" match="metadataMaintenance">
        <gmd:metadataMaintenance>
            <xsl:apply-templates mode="MaintenanceInfo"/>
        </gmd:metadataMaintenance>
    </xsl:template>

    <xsl:template mode="MetaData" match="GM03Comprehensive.Comprehensive.MD_MaintenanceInformation">
        <gmd:metadataMaintenance>
            <xsl:apply-templates mode="MaintenanceInfo" select="."/>
        </gmd:metadataMaintenance>
    </xsl:template>

    <xsl:template mode="MetaData" match="distributionInfo">
        <gmd:distributionInfo>
            <xsl:apply-templates mode="Distribution"/>
		</gmd:distributionInfo>
    </xsl:template>

    <xsl:template mode="MetaData"
                  match="GM03Comprehensive.Comprehensive.MD_GridSpatialRepresentation|
                         GM03Comprehensive.Comprehensive.MD_VectorSpatialRepresentation|
                         GM03Comprehensive.Comprehensive.MD_Georeferenceable|
                         GM03Comprehensive.Comprehensive.MD_Georectified">
        <gmd:spatialRepresentationInfo>
            <xsl:apply-templates select="." mode="SpatialRepr"/>
        </gmd:spatialRepresentationInfo>
    </xsl:template>

    <xsl:template mode="MetaData"
                  match="GM03Comprehensive.Comprehensive.MD_CoverageDescription|GM03Comprehensive.Comprehensive.MD_FeatureCatalogueDescription|GM03Comprehensive.Comprehensive.MD_RangeDimension|GM03Comprehensive.Comprehensive.MD_Band|GM03Comprehensive.Comprehensive.MD_ImageDescription">
        <gmd:contentInfo>
            <xsl:apply-templates select="." mode="Content"/>
        </gmd:contentInfo>
    </xsl:template>

    <xsl:template match="GM03Comprehensive.Comprehensive.MD_DataIdentification|GM03Core.Core.MD_DataIdentification|GM03Comprehensive.Comprehensive.SV_ServiceIdentification" mode="MetaData">
        <gmd:identificationInfo>
            <xsl:apply-templates select="." mode="DataIdentification"/>
        </gmd:identificationInfo>
    </xsl:template>
    <xsl:template match="GM03Comprehensive.Comprehensive.MD_MetadatalegislationInformation" mode="MetaData">
        <xsl:apply-templates mode="LegislationInfo"/>
    </xsl:template>

    <xsl:template match="GM03Core.Core.DQ_DataQuality" mode="MetaData">
        <xsl:apply-templates select="." mode="DataQuality"/>
    </xsl:template>

    <xsl:template match="GM03Core.Core.referenceSystemInfoMD_Metadata" mode="MetaData">
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
