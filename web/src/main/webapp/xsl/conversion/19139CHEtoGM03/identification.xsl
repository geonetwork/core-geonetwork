<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:util="xalan://org.fao.geonet.util.XslUtil"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                exclude-result-prefixes="che gco gmd srv util">

    <xsl:template mode="DataIdentification" match="gmd:identificationInfo">
        <xsl:apply-templates mode="DataIdentification"/>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="che:CHE_MD_DataIdentification|gmd:MD_DataIdentification">
        <GM03_2Comprehensive.Comprehensive.MD_DataIdentification TID="x{util:randomId()}">
            <xsl:call-template name="dataIdentification"/>
        </GM03_2Comprehensive.Comprehensive.MD_DataIdentification>
    </xsl:template>

    <xsl:template name="dataIdentification">
            <xsl:apply-templates mode="enum" select="gmd:status"/>
            <xsl:choose>
              <xsl:when test="abstract/node()">
                <xsl:apply-templates mode="text" select="gmd:abstract"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates mode="textGroup" select="gmd:abstract"/>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates mode="text" select="gmd:purpose"/>
            <BACK_REF name="MD_Metadata"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:citation"/>
            <xsl:apply-templates mode="enum" select="gmd:spatialRepresentationType"/>
            <xsl:if test="gmd:language">
                <language>
                    <xsl:apply-templates mode="DataIdentification" select="gmd:language"/>
                </language>
            </xsl:if>
            <xsl:apply-templates mode="enum" select="gmd:characterSet"/>
            <xsl:if test="gmd:topicCategory">
                <topicCategory>
                    <xsl:apply-templates mode="DataIdentification" select="gmd:topicCategory"/>
                </topicCategory>
            </xsl:if>
            <xsl:apply-templates mode="DataIdentification" select="che:projectType"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:credit"/>
            <xsl:apply-templates mode="text" select="che:basicGeodataID"/>
            <xsl:apply-templates mode="DataIdentification" select="che:basicGeodataIDType"/>
            
            <xsl:if test="normalize-space(text())!=''">
                <xsl:apply-templates mode="text" select="gmd:environmentDescription"/>
            </xsl:if>
            <xsl:if test="normalize-space(text())!=''">
                <xsl:apply-templates mode="text" select="gmd:supplementalInformation"/>
            </xsl:if>
            
            <xsl:apply-templates mode="DataIdentification" select="gmd:pointOfContact"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:resourceMaintenance"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:graphicOverview"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:resourceFormat"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:descriptiveKeywords"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:resourceSpecificUsage"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:resourceConstraints|srv:restrictions"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:aggregationInfo"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:spatialResolution/*"/>
            <xsl:apply-templates mode="Extent" select="gmd:extent"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:graphicOverview"/>
            <xsl:apply-templates mode="DataIdentification" select="srv:extent"/>
            <xsl:apply-templates mode="DataIdentification" select="che:revision"/>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:aggregationInfo">
        <GM03_2Comprehensive.Comprehensive.aggregationInfo_MD_Identification TID="x{util:randomId()}">
            <xsl:apply-templates mode="DataIdentification" select="gmd:MD_AggregateInformation"/>
            <BACK_REF name="MD_Identification"/>
        </GM03_2Comprehensive.Comprehensive.aggregationInfo_MD_Identification>
    </xsl:template>
    
    <xsl:template mode="DataIdentification" match="gmd:MD_AggregateInformation">
        <aggregationInfo REF="?">
            <GM03_2Comprehensive.Comprehensive.MD_AggregateInformation TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:associationType"/>
            <xsl:apply-templates mode="text" select="gmd:initiativeType"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:aggregateDataSetIdentifier"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:aggregateDataSetName/CI_Citation"/>
            </GM03_2Comprehensive.Comprehensive.MD_AggregateInformation>
        </aggregationInfo>
    </xsl:template>
    
    <xsl:template mode="DataIdentification" match="gmd:aggregateDataSetIdentifier">
            <aggregateDataSetIdentifier REF="?">
                <xsl:apply-templates mode="DataIdentification"/>
            </aggregateDataSetIdentifier>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:resourceSpecificUsage">
        <GM03_2Comprehensive.Comprehensive.MD_Usage TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:MD_Usage/gmd:usageDateTime"/>
            <xsl:apply-templates mode="text" select="gmd:MD_Usage/gmd:userDeterminedLimitations"/>
            <xsl:apply-templates mode="text" select="gmd:MD_Usage/gmd:specificUsage"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:MD_Usage/gmd:userContactInfo"/>
            <BACK_REF name="MD_Identification"/>
        </GM03_2Comprehensive.Comprehensive.MD_Usage>
    </xsl:template>
    
    <xsl:template mode="DataIdentification" match="gmd:userContactInfo">
        <GM03_2Comprehensive.Comprehensive.MD_UsageuserContactInfo TID="x{util:randomId()}">
            <xsl:apply-templates mode="DataIdentification" select="che:CHE_CI_ResponsibleParty"/>
            <BACK_REF name="MD_Usage"/>
            <xsl:apply-templates mode="enum" select="che:CHE_CI_ResponsibleParty/gmd:role"/>
        </GM03_2Comprehensive.Comprehensive.MD_UsageuserContactInfo>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="che:CHE_CI_ResponsibleParty">
        <userContactInfo REF="?">
            <xsl:apply-templates mode="RespParty" select="."/>
        </userContactInfo>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:graphicOverview">
        <GM03_2Comprehensive.Comprehensive.MD_BrowseGraphic TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:MD_BrowseGraphic/gmd:fileName"/>
            <xsl:apply-templates mode="text" select="gmd:MD_BrowseGraphic/gmd:fileType"/>
            <xsl:apply-templates mode="text" select="gmd:MD_BrowseGraphic/gmd:fileDescription"/>
            <BACK_REF name="MD_Identification"/>
        </GM03_2Comprehensive.Comprehensive.MD_BrowseGraphic>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="che:projectType">
        <ProjectType><xsl:value-of select="che:CHE_CI_projectTypeCode/@codeListValue"/></ProjectType>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:credit">
        <xsl:variable name="credit"><xsl:apply-templates mode="text" select="."/></xsl:variable>
        <credit>
        <GM03_2Core.Core.CharacterString_>
            <value><xsl:value-of select="$credit"/></value>
        </GM03_2Core.Core.CharacterString_>
        </credit>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="che:revision">
        <GM03_2Comprehensive.Comprehensive.revisionMD_Identification TID="x{util:randomId()}">
            <revision REF="?">
                <xsl:apply-templates mode="DataIdentification"/>
            </revision>
            <BACK_REF name="MD_Identification"/>
        </GM03_2Comprehensive.Comprehensive.revisionMD_Identification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="che:CHE_MD_Revision">
        <GM03_2Comprehensive.Comprehensive.MD_Revision TID="x{util:randomId()}">
            <xsl:apply-templates mode="enum" select="che:revisionScope"/>
            <xsl:apply-templates mode="text" select="che:dateOfLastUpdate"/>
            <xsl:apply-templates mode="text" select="che:revisionNote"/>
            <xsl:apply-templates mode="DataIdentification" select="che:revisionExtent"/>
            
            <xsl:apply-templates mode="DataIdentification" select="che:revisionScopeDescription"/>
            <xsl:apply-templates mode="DataIdentification" select="che:revisionContact"/>
        </GM03_2Comprehensive.Comprehensive.MD_Revision>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:resourceConstraints|srv:restrictions">
        <GM03_2Comprehensive.Comprehensive.resourceConstraintsMD_Identification TID="x{util:randomId()}">
            <resourceConstraints REF="?">
                <xsl:apply-templates mode="DataIdentification"/>                
            </resourceConstraints>
            <BACK_REF name="MD_Identification"/>
        </GM03_2Comprehensive.Comprehensive.resourceConstraintsMD_Identification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:MD_SecurityConstraints">
        <GM03_2Comprehensive.Comprehensive.MD_SecurityConstraints TID="x{util:randomId()}">
            <xsl:apply-templates mode="groupText" select=".">
                <xsl:with-param name="element">useLimitation</xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates mode="DataIdentification" select="gmd:classification"/>
            <xsl:apply-templates mode="text" select="gmd:classificationSystem"/>
            <xsl:apply-templates mode="textGroup" select="gmd:userNote"/>
            <xsl:apply-templates mode="text" select="gmd:handlingDescription"/>
        </GM03_2Comprehensive.Comprehensive.MD_SecurityConstraints>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:classification">
        <classification>
            <xsl:value-of select="gmd:MD_ClassificationCode/@codeListValue"/>
        </classification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="che:CHE_MD_LegalConstraints|gmd:MD_LegalConstraints">
        <GM03_2Comprehensive.Comprehensive.MD_LegalConstraints TID="x{util:randomId()}">
            <xsl:apply-templates mode="groupText" select=".">
                <xsl:with-param name="element">useLimitation</xsl:with-param>
            </xsl:apply-templates><!--
            <xsl:apply-templates mode="text" select="gmd:accessConstraints"/>-->
            <xsl:apply-templates mode="groupEnumC" select=".">
                <xsl:with-param name="element">accessConstraints</xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates mode="groupEnumC" select=".">
                <xsl:with-param name="element">useConstraints</xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates mode="groupText" select=".">
                <xsl:with-param name="element">otherConstraints</xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates mode="DataIdentification" select="che:legislationConstraints"/>
        </GM03_2Comprehensive.Comprehensive.MD_LegalConstraints>
    </xsl:template>
    
    <xsl:template mode="DataIdentification" match="gmd:MD_Constraints">
        <GM03_2Comprehensive.Comprehensive.MD_Constraints TID="x{util:randomId()}">
            <xsl:apply-templates mode="groupText" select=".">
                <xsl:with-param name="element">useLimitation</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.MD_Constraints>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:resourceMaintenance">
        <xsl:apply-templates mode="MaintenanceInfo">
            <xsl:with-param name="backRef">MD_Identification</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:MD_Resolution">
        <GM03_2Core.Core.MD_Resolution TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:distance"/>
            <BACK_REF name="MD_DataIdentification"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:equivalentScale"/>
        </GM03_2Core.Core.MD_Resolution>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:equivalentScale">
        <equivalentScale REF="?">
            <xsl:apply-templates mode="DataIdentification" select="*"/>
        </equivalentScale>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:MD_RepresentativeFraction">
        <GM03_2Core.Core.MD_RepresentativeFraction TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:denominator"/>
        </GM03_2Core.Core.MD_RepresentativeFraction>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:descriptiveKeywords">
        <GM03_2Core.Core.descriptiveKeywordsMD_Identification TID="x{util:randomId()}">
            <descriptiveKeywords REF='?'>
                <xsl:apply-templates mode="DataIdentification" select="gmd:MD_Keywords"/>
            </descriptiveKeywords>
            <BACK_REF name="MD_Identification"/>
        </GM03_2Core.Core.descriptiveKeywordsMD_Identification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:MD_Keywords">
        <GM03_2Core.Core.MD_Keywords TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:type"/>
            <!--<xsl:apply-templates mode="text" select="gmd:keyword"/>-->
            <xsl:apply-templates mode="groupText" select=".">
                <xsl:with-param name="element">keyword</xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates mode="DataIdentification" select="gmd:thesaurusName"/>
        </GM03_2Core.Core.MD_Keywords>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:resourceFormat">
        <GM03_2Comprehensive.Comprehensive.resourceFormatMD_Identification TID='x{util:randomId()}'>
            <resourceFormat REF='?'>
                <xsl:apply-templates mode="DataIdentification"/>
            </resourceFormat>
            <BACK_REF name="MD_Identification"/>
      </GM03_2Comprehensive.Comprehensive.resourceFormatMD_Identification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:MD_Format">
        <GM03_2Comprehensive.Comprehensive.MD_Format TID='x{util:randomId()}'>
            <xsl:apply-templates mode="text" select="gmd:name"/>
            <xsl:apply-templates mode="text" select="gmd:version"/>
            <xsl:apply-templates mode="text" select="gmd:amendmentNumber"/>
            <xsl:apply-templates mode="text" select="gmd:specification"/>
            <xsl:apply-templates mode="text" select="gmd:fileDecompressionTechnique"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:formatDistributor"/>
      </GM03_2Comprehensive.Comprehensive.MD_Format>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:pointOfContact">
        <GM03_2Core.Core.MD_IdentificationpointOfContact TID="x{util:randomId()}">
            <pointOfContact REF="?">
                <xsl:apply-templates mode="RespParty"/>
            </pointOfContact>
            <BACK_REF name="MD_Identification"/>
            <xsl:apply-templates mode="RespPartyRole" select="che:CHE_CI_ResponsibleParty|gmd:CI_ResponsibleParty"/>            
        </GM03_2Core.Core.MD_IdentificationpointOfContact>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:topicCategory">
        <GM03_2Core.Core.MD_TopicCategoryCode_>
          <value><xsl:value-of select="normalize-space(gmd:MD_TopicCategoryCode)"/></value>
        </GM03_2Core.Core.MD_TopicCategoryCode_>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:language">
        <xsl:variable name="code">
                <xsl:call-template name="lang3_to_lang2">
                    <xsl:with-param name="lang3" select="gco:CharacterString"/>
                </xsl:call-template>
        </xsl:variable>
        <CodeISO.LanguageCodeISO_>
            <value>
                <xsl:choose>
                    <xsl:when test="normalize-space($code) != ''">
                        <xsl:value-of select="$code"></xsl:value-of>
                    </xsl:when>
                    <xsl:when test="/che:CHE_MD_Metadata/gmd:language/gco:CharacterString">
                        <xsl:call-template name="lang3_to_lang2">
                            <xsl:with-param name="lang3" select="/che:CHE_MD_Metadata/gmd:language/gco:CharacterString"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="/gmd:MD_Metadata/gmd:language/gco:CharacterString">
                        <xsl:call-template name="lang3_to_lang2">
                            <xsl:with-param name="lang3" select="/gmd:MD_Metadata/gmd:language/gco:CharacterString"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <text>en</text>
                    </xsl:otherwise>
                </xsl:choose>
            </value>
        </CodeISO.LanguageCodeISO_>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:citation">
        <citation REF="?">
            <xsl:apply-templates mode="DataIdentification" />
        </citation>
    </xsl:template>

    
    <xsl:template mode="DataIdentification" match="gmd:CI_Citation">
        <GM03_2Comprehensive.Comprehensive.CI_Citation TID="x{util:randomId()}">
            <xsl:apply-templates mode="RefSystem" select=".">
                <xsl:with-param name="showIdentifier" select="false()"/>
            </xsl:apply-templates>
            <xsl:apply-templates mode="DataIdentification" select="gmd:identifier"/>
        </GM03_2Comprehensive.Comprehensive.CI_Citation>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:identifier">
        <GM03_2Comprehensive.Comprehensive.CI_Citationidentifier TID="x{util:randomId()}">
            <identifier REF='?'>
                <xsl:choose>
                <xsl:when test="gmd:RS_Identifier">
                    <xsl:apply-templates mode="RefSystem" select="gmd:RS_Identifier"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="DataIdentification" select="gmd:MD_Identifier"/>
                </xsl:otherwise>
                </xsl:choose>
            </identifier>
            <BACK_REF name="CI_Citation"/>
        </GM03_2Comprehensive.Comprehensive.CI_Citationidentifier>
    </xsl:template>


    <xsl:template mode="DataIdentification" match="gmd:MD_Identifier">
        <GM03_2Core.Core.MD_Identifier TID="x{util:randomId()}">
            <xsl:apply-templates mode="textGroup" select="gmd:code"/>
        </GM03_2Core.Core.MD_Identifier>
    </xsl:template>


    <xsl:template mode="DataIdentification" match="che:CHE_SV_ServiceIdentification">
        <GM03_2Comprehensive.Comprehensive.SV_ServiceIdentification TID="x{util:randomId()}">
            <xsl:call-template name="dataIdentification" />
            <xsl:apply-templates mode="DataIdentification" select="srv:credit"/>
            <xsl:apply-templates mode="DataIdentification" select="srv:inspireServiceType"/>
            <xsl:apply-templates mode="text" select="srv:serviceTypeVersion"/>
            <xsl:apply-templates mode="DataIdentification" select="srv:couplingType"/>
            <xsl:apply-templates mode="DataIdentification" select="srv:operatesOn"/>
            <xsl:if test="srv:coupledResource">
                <coupledResource>
		            <xsl:for-each select="srv:coupledResource">
		               <xsl:apply-templates mode="DataIdentification"/>
		            </xsl:for-each>
	            </coupledResource>
            </xsl:if>
            <xsl:apply-templates mode="DataIdentification" select="srv:serviceType"/>
            <xsl:apply-templates mode="DataIdentification" select="srv:containsOperations"/>
        </GM03_2Comprehensive.Comprehensive.SV_ServiceIdentification>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="srv:couplingType">
            <couplingType><xsl:value-of select="srv:SV_CouplingType/@codeListValue"/></couplingType>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="srv:operatesOn">
        <operatesOn>
          <GM03_2Core.Core.CharacterString_>
            <value><xsl:value-of select="./@uuidref"/></value>
          </GM03_2Core.Core.CharacterString_>
        </operatesOn>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="srv:serviceType">
        <serviceType>
            <GM03_2Comprehensive.Comprehensive.gml_CodeType>
                <code><xsl:value-of select="gco:LocalName"/></code>
                <xsl:apply-templates mode="text" select="codeSpace"/>
            </GM03_2Comprehensive.Comprehensive.gml_CodeType>
        </serviceType>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="srv:SV_CoupledResource">
        <GM03_2Comprehensive.Comprehensive.SV_CoupledResource>
            <xsl:apply-templates mode="text" select="srv:identifier"/>
            <xsl:apply-templates mode="text" select="srv:operationName"/>
            <xsl:apply-templates mode="text" select="gco:ScopedName"/>
        </GM03_2Comprehensive.Comprehensive.SV_CoupledResource>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="srv:containsOperations">
        <GM03_2Comprehensive.Comprehensive.containsOperationsSV_ServiceIdentification TID="x{util:randomId()}">
            <containsOperations REF="?">
                <xsl:apply-templates mode="DataIdentification"/>
            </containsOperations>
            <BACK_REF name="SV_ServiceIdentification"/>
        </GM03_2Comprehensive.Comprehensive.containsOperationsSV_ServiceIdentification>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="srv:SV_OperationMetadata">
        <GM03_2Comprehensive.Comprehensive.SV_OperationMetadata TID="x{util:randomId()}">
	            <xsl:apply-templates mode="text" select="srv:operationName"/>
	            <xsl:apply-templates mode="DataIdentification" select="srv:DCP"/>
	            <xsl:apply-templates mode="text" select="srv:invocationName"/>
                <xsl:apply-templates mode="textGroup" select="srv:operationDescription"/>
                <xsl:apply-templates mode="DataIdentification" select="srv:connectPoint"/>
        </GM03_2Comprehensive.Comprehensive.SV_OperationMetadata>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="srv:connectPoint">
        <GM03_2Comprehensive.Comprehensive.SV_OperationMetadataconnectPoint TID="x{util:randomId()}">
            <BACK_REF name="SV_OperationMetadata"/>
            <connectPoint REF="?">
                <xsl:apply-templates mode="distribution"><xsl:with-param name="backRef">false</xsl:with-param></xsl:apply-templates>
            </connectPoint>
        </GM03_2Comprehensive.Comprehensive.SV_OperationMetadataconnectPoint>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="srv:DCP">
    <DCP>
        <GM03_2Comprehensive.Comprehensive.DCPList_>
	        <xsl:for-each select="*">
	            <value><xsl:value-of select="@codeListValue"/></value>
	        </xsl:for-each>
        </GM03_2Comprehensive.Comprehensive.DCPList_>
    </DCP>
    </xsl:template>
    
    <xsl:template mode="DataIdentification" match="srv:extent">
        <GM03_2Comprehensive.Comprehensive.extentSV_ServiceIdentification TID="x{util:randomId()}">
        <extent REF="?">
            <xsl:apply-templates mode="DataIdentification" select="gmd:EX_Extent"/>
        </extent>
            <BACK_REF name="SV_ServiceIdentification"/>
        </GM03_2Comprehensive.Comprehensive.extentSV_ServiceIdentification>
    </xsl:template>
    
    <xsl:template mode="DataIdentification" match="gmd:EX_Extent">
        <GM03_2Core.Core.EX_Extent TID="x{util:randomId()}">
            <xsl:apply-templates mode="textGroup" select="gmd:description"/>
            <xsl:apply-templates mode="Extent" select="gmd:geographicElement"/>
            <xsl:apply-templates mode="Extent" select="gmd:temporalElement"/>
            <xsl:apply-templates mode="Extent" select="gmd:verticalElement"/>
        </GM03_2Core.Core.EX_Extent>
    </xsl:template>


    <xsl:template mode="DataIdentification" match="che:basicGeodataIDType">
        <basicGeodataIDType><xsl:value-of select="che:basicGeodataIDTypeCode/@codeListValue"/></basicGeodataIDType>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="*" priority="-100">
        <ERROR>Unknown DataIdentification element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>
</xsl:stylesheet>
