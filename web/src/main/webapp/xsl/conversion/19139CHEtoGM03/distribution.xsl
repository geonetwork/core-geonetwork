<xsl:stylesheet version="1.0"
                xmlns="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="che gco gmd">

    <xsl:template mode="distribution" match="gmd:distributionInfo">
        <distributionInfo REF="?">
            <GM03_2Core.Core.MD_Distribution TID="x{generate-id(.)}">
                <xsl:apply-templates mode="distribution"/>
            </GM03_2Core.Core.MD_Distribution>
        </distributionInfo>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:transferOptions">
        <xsl:apply-templates mode="distribution"/>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:distributor">
    <GM03_2Comprehensive.Comprehensive.MD_Distributiondistributor TID="x{generate-id(.)}">
        <distributor REF="?">
              <xsl:apply-templates mode="distribution" select="./*"/>
        </distributor>
        <BACK_REF name="MD_Distribution"/>
    </GM03_2Comprehensive.Comprehensive.MD_Distributiondistributor>
        
    </xsl:template>
    
    <xsl:template mode="distribution" match="gmd:distributionOrderProcess">
    <GM03_2Comprehensive.Comprehensive.distributionOrderProcessMD_Distributor TID="x{generate-id(.)}">
          <distributionOrderProcess REF="?">
            <GM03_2Comprehensive.Comprehensive.MD_StandardOrderProcess TID="x2{generate-id(.)}">
                <xsl:apply-templates mode="text" select="gmd:MD_StandardOrderProcess/gmd:fees"/>
                <xsl:apply-templates mode="text" select="gmd:MD_StandardOrderProcess/gmd:plannedAvailableDateTime"/>
                <xsl:apply-templates mode="text" select="gmd:MD_StandardOrderProcess/gmd:turnaround"/>
                <xsl:apply-templates mode="textGroup" select="gmd:MD_StandardOrderProcess/gmd:orderingInstructions"/>
            </GM03_2Comprehensive.Comprehensive.MD_StandardOrderProcess>
          </distributionOrderProcess>
          <BACK_REF name="MD_Distributor"/>
        </GM03_2Comprehensive.Comprehensive.distributionOrderProcessMD_Distributor>
    </xsl:template>
    
    <xsl:template mode="distribution" match="gmd:MD_DigitalTransferOptions">
        <xsl:param name="showBackRef" select="true()"/>
        <GM03_2Comprehensive.Comprehensive.MD_DigitalTransferOptions TID="x{generate-id(.)}">
            <xsl:if test="$showBackRef = true()">
                <BACK_REF name="MD_Distribution"/>
            </xsl:if>
            <xsl:apply-templates mode="distribution" select="gmd:MD_Distribution"/>
            <xsl:apply-templates mode="text" select="gmd:unitsOfDistribution"/>
            <xsl:apply-templates mode="text" select="gmd:transferSize"/>
            <xsl:apply-templates mode="distribution" select="gmd:onLine/gmd:CI_OnlineResource"/>
            <xsl:apply-templates mode="distribution" select="gmd:offLine"/>
        </GM03_2Comprehensive.Comprehensive.MD_DigitalTransferOptions>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:CI_OnlineResource">
        <xsl:param name="backRef">true</xsl:param>
        <GM03_2Core.Core.CI_OnlineResource TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="gmd:protocol"/>
            <xsl:apply-templates mode="text" select="gmd:applicationProfile"/>
            <xsl:apply-templates mode="text" select="gmd:function"/>
            <xsl:apply-templates mode="textGroup" select="gmd:description"/>
            <xsl:apply-templates mode="textGroup" select="gmd:name"/>
            <xsl:apply-templates mode="text" select="gmd:linkage"/>
            <xsl:if test="$backRef = 'true'">
                <BACK_REF name="MD_DigitalTransferOptions"/>
            </xsl:if>
        </GM03_2Core.Core.CI_OnlineResource>
    </xsl:template>
    <xsl:template mode="distribution" match="gmd:offLine">
        <offLine REF="?">
            <xsl:apply-templates mode="distribution"/>
        </offLine>
    </xsl:template>    
    <xsl:template mode="distribution" match="gmd:MD_Medium">
        <GM03_2Comprehensive.Comprehensive.MD_Medium TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="gmd:name"/>
            <xsl:apply-templates mode="text" select="gmd:density"/>
            <xsl:apply-templates mode="text" select="gmd:densityUnits"/>
            <xsl:apply-templates mode="text" select="gmd:volumes"/>
            <xsl:apply-templates mode="text" select="gmd:mediumFormat"/>
        </GM03_2Comprehensive.Comprehensive.MD_Medium>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:MD_Distribution">
        <xsl:apply-templates mode="distribution"/>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:distributionFormat">
        <GM03_2Core.Core.MD_DistributiondistributionFormat TID="x{generate-id(.)}">
            <BACK_REF name="MD_Distribution"/>
            <distributionFormat REF="?">
                <xsl:apply-templates mode="distribution"/>
            </distributionFormat>
        </GM03_2Core.Core.MD_DistributiondistributionFormat>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:MD_Format">
        <xsl:param name="showDistributor" select="true()"/>
        <GM03_2Comprehensive.Comprehensive.MD_Format TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="gmd:name"/>
            <xsl:apply-templates mode="text" select="gmd:version"/>
            <xsl:apply-templates mode="text" select="gmd:amendmentNumber"/>
            <xsl:apply-templates mode="text" select="gmd:specification"/>
            <xsl:apply-templates mode="text" select="gmd:fileDecompressionTechnique"/>
            <xsl:if test="$showDistributor = true()">
                <xsl:apply-templates mode="distribution" select="gmd:formatDistributor"/>
            </xsl:if>
        </GM03_2Comprehensive.Comprehensive.MD_Format>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:formatDistributor">
        <GM03_2Comprehensive.Comprehensive.formatDistributordistributorFormat TID="x{generate-id(.)}">
            <formatDistributor REF="?">
                <xsl:apply-templates mode="distribution"/>    
            </formatDistributor>
            <BACK_REF name="distributorFormat"/>
        </GM03_2Comprehensive.Comprehensive.formatDistributordistributorFormat>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:MD_Distributor">
        <GM03_2Comprehensive.Comprehensive.MD_Distributor TID="x{generate-id(.)}">
            <xsl:apply-templates mode="distribution" select="gmd:distributorContact"/>
            <xsl:apply-templates mode="distribution" select="gmd:distributionOrderProcess"/>
            <xsl:apply-templates mode="distribution" select="gmd:distributorTransferOptions"/>    
        </GM03_2Comprehensive.Comprehensive.MD_Distributor>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:distributorContact">
        <distributorContact REF="?">
            <xsl:apply-templates mode="RespParty"/>  <!-- the node taken by the REF, what follows will stay in place -->
            <GM03_2Comprehensive.Comprehensive.MD_DistributordistributorContact>
                <xsl:apply-templates mode="RespPartyRole"/>
            </GM03_2Comprehensive.Comprehensive.MD_DistributordistributorContact>
        </distributorContact>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:distributorTransferOptions">
        <GM03_2Comprehensive.Comprehensive.distributorTransferOptionsMD_Distributor TID="x{generate-id(.)}">
            <distributorTransferOptions REF="?">
                <xsl:apply-templates mode="distribution">
                    <xsl:with-param name="showBackRef" select="false()"/>
                </xsl:apply-templates>
            </distributorTransferOptions>
            <BACK_REF name="MD_Distributor"/>
        </GM03_2Comprehensive.Comprehensive.distributorTransferOptionsMD_Distributor>
    </xsl:template>

    <xsl:template mode="distribution" match="*">
        <ERROR>Unknown distribution element <xsl:value-of select="local-name(.)"/> child of <xsl:value-of select="local-name(..)"/> </ERROR>
    </xsl:template>
</xsl:stylesheet>