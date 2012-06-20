<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:int="http://www.interlis.ch/INTERLIS2.3"
                exclude-result-prefixes="int"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template mode="MaintenanceInfo" match="int:metadataMaintenance|int:GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformation">
        <che:CHE_MD_MaintenanceInformation gco:isoType="gmd:MD_MaintenanceInformation">
            <xsl:apply-templates mode="MaintenanceInfo" select="int:maintenanceAndUpdateFrequency"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="int:dateOfNextUpdate"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="int:userDefinedMaintenanceFrequency"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="int:updateScope"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="int:GM03_2Comprehensive.Comprehensive.MD_ScopeDescription"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="int:maintenanceNote"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="int:GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationcontact"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="int:dateOfMonitoringState"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="int:GM03_2_2Comprehensive.Comprehensive.MD_HistoryConcept"/>
            <xsl:apply-templates mode="MaintenanceInfo" select="int:GM03_2_2Comprehensive.Comprehensive.MD_ArchiveConcept"/>
        </che:CHE_MD_MaintenanceInformation>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="int:GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationcontact">
        <gmd:contact>
            <xsl:apply-templates mode="RespParty" select="."/>
        </gmd:contact>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="int:GM03_2Comprehensive.Comprehensive.MD_ScopeDescription">
        <gmd:updateScopeDescription>
          <gmd:MD_ScopeDescription>
            <xsl:choose>
                <xsl:when test="int:attributes">
                    <xsl:apply-templates mode="objectRef" select="int:attributes"/>
                </xsl:when>
                <xsl:when test="int:features">
                    <xsl:apply-templates mode="objectRef" select="int:features"/>
                </xsl:when>
                <xsl:when test="int:featureInstances">
                    <xsl:apply-templates mode="objectRef" select="int:featureInstances"/>
                </xsl:when>
                <xsl:when test="int:attributeInstances">
                    <xsl:apply-templates mode="objectRef" select="int:attributeInstances"/>
                </xsl:when>
                <xsl:when test="int:dataset">
                    <xsl:apply-templates mode="text" select="int:dataset"/>
                </xsl:when>
                <xsl:when test="int:other">
                    <xsl:apply-templates mode="text" select="int:other"/>
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

    <xsl:template mode="MaintenanceInfo" match="int:maintenanceAndUpdateFrequency">
        <gmd:maintenanceAndUpdateFrequency>
            <gmd:MD_MaintenanceFrequencyCode codeList="./resources/codeList.xml#MD_MaintenanceFrequencyCode"
                                         codeListValue="{.}"/>
        </gmd:maintenanceAndUpdateFrequency>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="int:userDefinedMaintenanceFrequency">
        <gmd:userDefinedMaintenanceFrequency>
            <gts:TM_PeriodDuration>
                <xsl:value-of select="concat('P', substring-before(.,':'), 'Y', substring-before(substring-after(., ':'), ':'), 'M', substring-before(substring-after(substring-after(., ':'), ':'), ':'), 'DT', substring-before(substring-after(substring-after(substring-after(., ':'), ':'), ':'), ':'), 'H', substring-before(substring-after(substring-after(substring-after(substring-after(., ':'), ':'), ':'), ':'), ':'), 'M', substring-after(substring-after(substring-after(substring-after(substring-after(., ':'), ':'), ':'), ':'), ':'), 'S')"/>
            </gts:TM_PeriodDuration>
        </gmd:userDefinedMaintenanceFrequency>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="int:dateOfNextUpdate">
        <gmd:dateOfNextUpdate>
            <xsl:apply-templates mode="date" select="."/>
        </gmd:dateOfNextUpdate>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="int:dateOfMonitoringState">
        <che:dateOfMonitoringState>
            <xsl:apply-templates mode="date" select="."/>
        </che:dateOfMonitoringState>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="int:updateScope">
        <gmd:updateScope>
            <xsl:apply-templates mode="MaintenanceInfo"/>
        </gmd:updateScope>
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="int:GM03_2Core.Core.MD_ScopeCode_">
        <gmd:MD_ScopeCode codeList="./resources/codeList.xml#MD_ScopeCode" codeListValue="{.}" />
    </xsl:template>

    <xsl:template mode="MaintenanceInfo" match="int:maintenanceNote">
        <xsl:for-each select="int:GM03_2Core.Core.PT_FreeText">
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
