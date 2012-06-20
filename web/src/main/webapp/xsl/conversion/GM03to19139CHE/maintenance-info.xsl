<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template mode="MaintenanceInfo" match="metadataMaintenance|GM03Comprehensive.Comprehensive.MD_MaintenanceInformation">
        <che:CHE_MD_MaintenanceInformation gco:isoType="gmd:MD_MaintenanceInformation">
            <xsl:apply-templates mode="MaintenanceInfo" select="maintenanceAndUpdateFrequency"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="dateOfNextUpdate"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="userDefinedMaintenanceFrequency"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="updateScope"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="GM03Comprehensive.Comprehensive.MD_ScopeDescription"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="maintenanceNote"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="GM03Comprehensive.Comprehensive.MD_MaintenanceInformationcontact"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="dateOfMonitoringState"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="GM03_2Comprehensive.Comprehensive.MD_HistoryConcept"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="GM03_2Comprehensive.Comprehensive.MD_ArchiveConcept"/>
        </che:CHE_MD_MaintenanceInformation>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="GM03Comprehensive.Comprehensive.MD_MaintenanceInformationcontact">
        <gmd:contact>
            <xsl:apply-templates mode="RespParty" select="."/>
        </gmd:contact>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="GM03Comprehensive.Comprehensive.MD_ScopeDescription">
        <gmd:updateScopeDescription>
          <gmd:MD_ScopeDescription>
            <xsl:choose>
                <xsl:when test="attributes">
                    <xsl:apply-templates mode="objectRef" select="attributes"/>
                </xsl:when>
                <xsl:when test="features">
                    <xsl:apply-templates mode="objectRef" select="features"/>
                </xsl:when>
                <xsl:when test="featureInstances">
                    <xsl:apply-templates mode="objectRef" select="featureInstances"/>
                </xsl:when>
                <xsl:when test="attributeInstances">
                    <xsl:apply-templates mode="objectRef" select="attributeInstances"/>
                </xsl:when>
                <xsl:when test="dataset">
                    <xsl:apply-templates mode="text" select="dataset"/>
                </xsl:when>
                <xsl:when test="other">
                    <xsl:apply-templates mode="text" select="other"/>
                </xsl:when>
            </xsl:choose>
          </gmd:MD_ScopeDescription>
        </gmd:updateScopeDescription>
    </xsl:template>
    <xsl:template mode="objectRef" match="*">
        <xsl:element name="{local-name(.)}"
                     namespace="http://www.isotc211.org/2005/gmd">
            <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
<!--            <xsl:attribute name="uuidref"><xsl:value-of select="."/></xsl:attribute>-->
        </xsl:element>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="maintenanceAndUpdateFrequency">
        <gmd:maintenanceAndUpdateFrequency>
            <gmd:MD_MaintenanceFrequencyCode codeList="./resources/codeList.xml#MD_MaintenanceFrequencyCode"
                                         codeListValue="{.}"/>
        </gmd:maintenanceAndUpdateFrequency>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="userDefinedMaintenanceFrequency">
        <gmd:userDefinedMaintenanceFrequency>
            <gts:TM_PeriodDuration>
                <xsl:value-of select="concat('P', substring-before(.,':'), 'Y', substring-before(substring-after(., ':'), ':'), 'M', substring-before(substring-after(substring-after(., ':'), ':'), ':'), 'DT', substring-before(substring-after(substring-after(substring-after(., ':'), ':'), ':'), ':'), 'H', substring-before(substring-after(substring-after(substring-after(substring-after(., ':'), ':'), ':'), ':'), ':'), 'M', substring-after(substring-after(substring-after(substring-after(substring-after(., ':'), ':'), ':'), ':'), ':'), 'S')"/>
            </gts:TM_PeriodDuration>
        </gmd:userDefinedMaintenanceFrequency>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="dateOfNextUpdate">
        <gmd:dateOfNextUpdate>
            <xsl:apply-templates mode="date" select="."/>
        </gmd:dateOfNextUpdate>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="dateOfMonitoringState">
        <che:dateOfMonitoringState>
            <xsl:apply-templates mode="date" select="."/>
        </che:dateOfMonitoringState>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="updateScope">
        <gmd:updateScope>
            <xsl:apply-templates mode="MaintenanceInfo"/>
        </gmd:updateScope>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="GM03Core.Core.MD_ScopeCode_">
        <gmd:MD_ScopeCode codeList="./resources/codeList.xml#MD_ScopeCode" codeListValue="{.}" />
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="maintenanceNote">
        <xsl:for-each select="GM03Core.Core.PT_FreeText">
            <gmd:maintenanceNote>
                <xsl:apply-templates mode="language" select="."/>
            </gmd:maintenanceNote>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">MaintenanceInfo</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
