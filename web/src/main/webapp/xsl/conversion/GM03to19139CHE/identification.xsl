<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:util="xalan://org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="util"
                >

    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.MD_DataIdentification|GM03Core.Core.MD_DataIdentification">
        <che:CHE_MD_DataIdentification gco:isoType="gmd:MD_DataIdentification">
            <xsl:call-template name="dataIdentification"/>
        </che:CHE_MD_DataIdentification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.SV_ServiceIdentification">
        <che:CHE_SV_ServiceIdentification gco:isoType="srv:SV_ServiceIdentification">
            <xsl:call-template name="dataIdentification">
                <xsl:with-param name="srv" select="true()"/>
            </xsl:call-template>
            <xsl:apply-templates mode="DataIdentification" select="serviceType"/>
            <xsl:apply-templates mode="DataIdentification" select="serviceTypeVersion"/>
            <xsl:apply-templates mode="DataIdentification" select="restrictions"/>
            <xsl:apply-templates mode="DataIdentification" select="GM03Core.Core.descriptiveKeywordsMD_Identification"/>
            <xsl:apply-templates mode="DataIdentification" select="GM03Comprehensive.Comprehensive.extentSV_ServiceIdentification"/>
            <xsl:apply-templates mode="DataIdentification" select="coupledResource/GM03Comprehensive.Comprehensive.SV_CoupledResource"/>
            <xsl:apply-templates mode="DataIdentification" select="couplingType"/>
            <xsl:apply-templates mode="DataIdentification" select="GM03Comprehensive.Comprehensive.containsOperationsSV_ServiceIdentification"/>
            <xsl:apply-templates mode="DataIdentification" select="operatesOn"/>
        </che:CHE_SV_ServiceIdentification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="serviceType">
        <srv:serviceType>
            <gco:LocalName><xsl:value-of select="GM03Comprehensive.Comprehensive.gml_CodeType/code"/></gco:LocalName>
            <xsl:if test="GM03Comprehensive.Comprehensive.gml_CodeType/codeSpace">
                <gml:codeSpace><xsl:value-of select="GM03Comprehensive.Comprehensive.gml_CodeType/codeSpace"/></gml:codeSpace>
            </xsl:if>
        </srv:serviceType>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.extentSV_ServiceIdentification">
        <srv:extent>
            <gmd:EX_Extent>
            <xsl:apply-templates mode="Extent" select="extent/GM03Core.Core.EX_Extent/*"/>
            </gmd:EX_Extent>
        </srv:extent>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.SV_CoupledResource">
        <srv:coupledResource>
	        <srv:SV_CoupledResource>
	            <xsl:apply-templates mode="text" select="operationName"><xsl:with-param name="prefix">srv</xsl:with-param></xsl:apply-templates>
	            <xsl:apply-templates mode="text" select="identifier"><xsl:with-param name="prefix">srv</xsl:with-param></xsl:apply-templates>
	            <xsl:apply-templates mode="text" select="ScopedName"><xsl:with-param name="prefix">srv</xsl:with-param></xsl:apply-templates>
	        </srv:SV_CoupledResource>
        </srv:coupledResource>
    </xsl:template>
    
    <xsl:template mode="DataIdentification" match="couplingType">
        <srv:couplingType>
            <srv:SV_CouplingType codeList="http://www.isotc211.org/2005/iso19119/resources/Codelist/gmxCodelists.xml#SV_CouplingType" codeListValue="{text()}"/>
        </srv:couplingType>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="operatesOn">
        <srv:operatesOn xmlns:date="http://exslt.org/dates-and-times" 
            xmlns:xlink="http://www.w3.org/1999/xlink" 
            xmlns:gts="http://www.isotc211.org/2005/gts" 
            uuidref="{.//value/text()}"/>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="serviceTypeVersion">
        <srv:serviceTypeVersion>
            <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
        </srv:serviceTypeVersion>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="containsOperations">
        <srv:containsOperations>
            <xsl:apply-templates mode="DataIdentification"/>
        </srv:containsOperations>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="DCP">
        <xsl:for-each select=".//value">
	        <srv:DCP>
	           <srv:DCPList codeList="http://www.isotc211.org/2005/iso19119/resources/Codelist/gmxCodelists.xml#DCPList" codeListValue="{text()}"/>     
	        </srv:DCP>
        </xsl:for-each>
        
    </xsl:template>
    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.SV_OperationMetadata">
        <srv:SV_OperationMetadata>
                <xsl:apply-templates mode="text" select="operationName"><xsl:with-param name="prefix">srv</xsl:with-param></xsl:apply-templates>
                <xsl:apply-templates mode="DataIdentification" select="DCP"/>
                <xsl:apply-templates mode="text" select="operationDescription"><xsl:with-param name="prefix">srv</xsl:with-param></xsl:apply-templates>
                <xsl:apply-templates mode="text" select="invocationName"><xsl:with-param name="prefix">srv</xsl:with-param></xsl:apply-templates>
                <xsl:apply-templates mode="DataIdentification" select="GM03Comprehensive.Comprehensive.SV_OperationMetadataconnectPoint"/>
        </srv:SV_OperationMetadata>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.containsOperationsSV_ServiceIdentification">
        <xsl:apply-templates mode="DataIdentification"/>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.SV_OperationMetadataconnectPoint">
        <xsl:apply-templates mode="DataIdentification"/>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="connectPoint">
        <srv:connectPoint>
	        <xsl:apply-templates mode="OnlineResource"/>
        </srv:connectPoint>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="GM03Core.Core.EX_Extent">
        <xsl:apply-templates mode="Extent" select="."/>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.resourceConstraintsMD_Identification">
        <gmd:resourceConstraints>
            <xsl:apply-templates mode="ConstsTypes"/>
        </gmd:resourceConstraints>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="GM03Core.Core.descriptiveKeywordsMD_Identification">
        <gmd:descriptiveKeywords>
            <xsl:apply-templates mode="DescriptiveKeyword"/>
        </gmd:descriptiveKeywords>
    </xsl:template>

    <xsl:template name="dataIdentification">
        <xsl:param name="srv" select="false()"/>

        <xsl:for-each select="citation">
            <gmd:citation>
                <xsl:apply-templates mode="Citation"/>
            </gmd:citation>
        </xsl:for-each>
        <xsl:apply-templates mode="text" select="abstract"/>
        <xsl:apply-templates mode="text" select="purpose"/>
        <xsl:apply-templates mode="text" select="credit"/>
        <xsl:for-each select="status">
            <gmd:status>
                <gmd:MD_ProgressCode codeList="./resources/codeList.xml#MD_ProgressCode" codeListValue="{GM03Core.Core.MD_ProgressCode_/value}"/>
            </gmd:status>
        </xsl:for-each>
        <xsl:for-each select="GM03Core.Core.MD_IdentificationpointOfContact">
            <gmd:pointOfContact>
                <xsl:apply-templates mode="RespParty" select="."/>
            </gmd:pointOfContact>
        </xsl:for-each>
        <xsl:for-each select="GM03Comprehensive.Comprehensive.MD_MaintenanceInformation">
            <gmd:resourceMaintenance>
                <xsl:apply-templates select="." mode="MaintenanceInfo"/>
            </gmd:resourceMaintenance>
        </xsl:for-each>
        <xsl:for-each select="GM03Comprehensive.Comprehensive.MD_BrowseGraphic">
            <gmd:graphicOverview>
                <xsl:apply-templates  mode="DataIdentification" select="."/>
            </gmd:graphicOverview>
        </xsl:for-each>
        <xsl:for-each select="GM03Comprehensive.Comprehensive.resourceFormatMD_Identification">
            <gmd:resourceFormat>
                <xsl:apply-templates mode="ResourceFormat"/>
            </gmd:resourceFormat>
        </xsl:for-each>
        <xsl:for-each select="GM03Core.Core.descriptiveKeywordsMD_Identification[$srv=false()]">
            <gmd:descriptiveKeywords>
                <xsl:apply-templates mode="DescriptiveKeyword"/>
            </gmd:descriptiveKeywords>
        </xsl:for-each>

        <xsl:for-each select="GM03Comprehensive.Comprehensive.MD_Usage">
                <xsl:apply-templates mode="DataIdentification" select="."/>
        </xsl:for-each>


        <xsl:for-each select="GM03Comprehensive.Comprehensive.resourceConstraintsMD_Identification[$srv=false()]">
            <gmd:resourceConstraints>
                <xsl:apply-templates mode="ConstsTypes"/>
            </gmd:resourceConstraints>
        </xsl:for-each>

        <xsl:apply-templates mode="DataIdentification" select="GM03Comprehensive.Comprehensive.aggregationInfo_MD_Identification"/>

        <xsl:for-each select="spatialRepresentationType">
            <gmd:spatialRepresentationType>
                <gmd:MD_SpatialRepresentationTypeCode codeList="./resources/codeList.xml#MD_SpatialRepresentationTypeCode"
                                                  codeListValue="{GM03Core.Core.MD_SpatialRepresentationTypeCode_/value}"/>
            </gmd:spatialRepresentationType>
        </xsl:for-each>
        <xsl:for-each select="GM03Core.Core.MD_Resolution/distance">
            <gmd:spatialResolution>
                <gmd:MD_Resolution>
                    <xsl:apply-templates mode="DataIdentification" select="."/>
                </gmd:MD_Resolution>
            </gmd:spatialResolution>
        </xsl:for-each>
        <xsl:for-each select="GM03Core.Core.MD_Resolution/equivalentScale">
            <gmd:spatialResolution>
                <gmd:MD_Resolution>
                    <xsl:apply-templates mode="DataIdentification" select="."/>
                </gmd:MD_Resolution>
            </gmd:spatialResolution>
        </xsl:for-each>
        <xsl:apply-templates select="language" mode="language"/>
        <xsl:for-each select="characterSet">
            <gmd:characterSet>
                <gmd:MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode" codeListValue="{GM03Core.Core.MD_CharacterSetCode_/value}"/>
            </gmd:characterSet>
        </xsl:for-each>
        <xsl:for-each select="topicCategory/GM03Core.Core.MD_TopicCategoryCode_">
            <gmd:topicCategory>
                <gmd:MD_TopicCategoryCode>
                    <xsl:variable name="cat" select="value"/>
                    <xsl:choose>
                        <xsl:when test="contains($cat, '.')">
                            <xsl:value-of select="substring-after($cat, '.')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$cat"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </gmd:MD_TopicCategoryCode>
            </gmd:topicCategory>
        </xsl:for-each>
        <xsl:apply-templates mode="text" select="environmentDescription"/>
        <xsl:for-each select="GM03Core.Core.EX_Extent[$srv=false()]">
            <xsl:apply-templates mode="Extent" select="."/>
        </xsl:for-each>
        <xsl:apply-templates mode="text" select="supplementalInformation"/>
        <xsl:apply-templates select="ProjectType" mode="DataIdentification"/>
        <xsl:for-each select="GM03Comprehensive.Comprehensive.revisionMD_Identification/revision">
            <che:revision>
                <xsl:apply-templates mode="Revision"/>
            </che:revision>
        </xsl:for-each>

        <xsl:if test="not($srv) and basicGeodataID">
            <che:basicGeodataID>
                <xsl:apply-templates mode="string" select="basicGeodataID/text()"/>
            </che:basicGeodataID>
            <xsl:apply-templates select="basicGeodataIDType" mode="DataIdentification"/>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.aggregationInfo_MD_Identification">
        <xsl:apply-templates mode="DataIdentification" />
    </xsl:template>

    <xsl:template mode="DataIdentification" match="aggregationInfo">
        <gmd:aggregationInfo>
                <xsl:apply-templates mode="DataIdentification" />
        </gmd:aggregationInfo>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.MD_AggregateInformation">
        <gmd:MD_AggregateInformation>
            <xsl:apply-templates mode="DataIdentification" select="GM03Comprehensive.Comprehensive.CI_Citation"/>
            <xsl:apply-templates mode="DataIdentification" select="aggregateDataSetIdentifier"/>
            <xsl:apply-templates mode="DataIdentification" select="associationType"/>
            <xsl:apply-templates mode="DataIdentification" select="initiativeType"/>
        </gmd:MD_AggregateInformation>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="associationType">
        <gmd:associationType>
            <gmd:DS_AssociationTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#DS_AssociationTypeCode" codeListValue="{text()}"/>
        </gmd:associationType>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="initiativeType">
        <gmd:initiativeType>
            <gmd:DS_InitiativeTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#DS_InitiativeTypeCode" codeListValue="{text()}"/>
        </gmd:initiativeType>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="aggregateDataSetIdentifier">
        <gmd:aggregateDataSetIdentifier>
            <xsl:apply-templates mode="Identifier" select="GM03Core.Core.MD_Identifier"/>
        </gmd:aggregateDataSetIdentifier>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.CI_Citation">
        <gmd:aggregateDataSetName>
            <xsl:apply-templates mode="Citation" select="."/>
        </gmd:aggregateDataSetName>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.MD_Usage">
        <gmd:resourceSpecificUsage>
            <gmd:MD_Usage>
                <xsl:apply-templates mode="text" select="specificUsage"/>
                <xsl:if test="usageDateTime">
                    <gmd:usageDateTime>
                        <xsl:apply-templates mode="dateTime" select="usageDateTime"/>
                    </gmd:usageDateTime>
                </xsl:if>
                <xsl:apply-templates mode="text" select="userDeterminedLimitations"/>
                <xsl:apply-templates mode="DataIdentification" select="GM03Comprehensive.Comprehensive.MD_UsageuserContactInfo"/>
            </gmd:MD_Usage>
        </gmd:resourceSpecificUsage>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.MD_UsageuserContactInfo">
        <gmd:userContactInfo>
            <xsl:apply-templates mode="RespParty" select="."/>
        </gmd:userContactInfo>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.MD_BrowseGraphic">
        <gmd:MD_BrowseGraphic>
            <xsl:apply-templates mode="text" select="fileName"/>
            <xsl:apply-templates mode="text" select="fileDescription"/>
            <xsl:apply-templates mode="text" select="fileType"/>
        </gmd:MD_BrowseGraphic>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="ProjectType">
        <che:projectType>
            <che:CHE_CI_projectTypeCode codeListValue="{.}" codeList="./resources/codeList.xml#CHE_CI_projectTypeCode"/>
        </che:projectType>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="basicGeodataIDType">
        <che:basicGeodataIDType>
            <che:basicGeodataIDTypeCode codeListValue="{.}" codeList="./resources/codeList.xml#basicGeodataIDTypeCode"/>
        </che:basicGeodataIDType>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template mode="DataIdentification" match="distance">
        <gmd:distance>
            <gco:Distance uom="m"><xsl:value-of select="util:expandScientific(string(.))"/></gco:Distance>
        </gmd:distance>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="equivalentScale">
        <gmd:equivalentScale>
            <xsl:apply-templates mode="DataIdentification"/>
        </gmd:equivalentScale>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="GM03Core.Core.MD_RepresentativeFraction">
        <gmd:MD_RepresentativeFraction>
            <xsl:apply-templates mode="DataIdentification"/>
        </gmd:MD_RepresentativeFraction>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="denominator">
        <gmd:denominator>
            <gco:Integer><xsl:value-of select="."/></gco:Integer>
        </gmd:denominator>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.MD_MaintenanceInformation">
        <gmd:resourceMaintenance>
            <xsl:apply-templates select="." mode="MaintenanceInfo"/>
        </gmd:resourceMaintenance>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="GM03Core.Core.MD_IdentificationpointOfContact">
        <gmd:pointOfContact>
            <xsl:apply-templates mode="RespParty" select="."/>
        </gmd:pointOfContact>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="GM03Comprehensive.Comprehensive.resourceFormatMD_Identification">
        <gmd:resourceFormat>
            <xsl:apply-templates mode="ResourceFormat"/>
        </gmd:resourceFormat>
    </xsl:template>

    <xsl:template mode="Revision" match="GM03Comprehensive.Comprehensive.MD_Revision">
        <che:CHE_MD_Revision gco:isoType="gmd:MD_Revision">
            <xsl:for-each select="revisionScope">
                <che:revisionScope>
                    <xsl:apply-templates mode="MaintenanceInfo" select="GM03Core.Core.MD_ScopeCode_"/>
                </che:revisionScope>
            </xsl:for-each>
            <xsl:for-each select="dateOfLastUpdate">
                <che:dateOfLastUpdate>
                    <xsl:apply-templates mode="date" select="."/>
                </che:dateOfLastUpdate>
            </xsl:for-each>
            <xsl:for-each select="revisionNote">
                <che:revisionNote>
                    <xsl:apply-templates mode="string" select="."/>
                </che:revisionNote>
            </xsl:for-each>
            <xsl:for-each select="revisionScopeDescription">
                <che:revisionScopeDescription>
                    <xsl:apply-templates mode="string" select="."/>
                </che:revisionScopeDescription>
            </xsl:for-each>
            <xsl:for-each select="revisionExtent">
                <che:revisionExtent>
                    <xsl:apply-templates mode="Extent" select="GM03Core.Core.EX_Extent"/>
                </che:revisionExtent>
            </xsl:for-each>
            <xsl:for-each select="revisionContact">
                <che:revisionContact>
                    <xsl:apply-templates mode="RespParty" select="."/>
                </che:revisionContact>
            </xsl:for-each>
        </che:CHE_MD_Revision>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">DataIdentification</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template mode="DescriptiveKeyword" match="GM03Core.Core.MD_Keywords">
        <gmd:MD_Keywords>
            <xsl:for-each select="keyword">
                <xsl:for-each select="GM03Core.Core.PT_FreeText">
                    <gmd:keyword>
                        <xsl:apply-templates mode="language" select="."/>
                    </gmd:keyword>
                </xsl:for-each>
            </xsl:for-each>
            <xsl:for-each select="type">
                <gmd:type>
                    <gmd:MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode" codeListValue="{.}" />
                </gmd:type>
            </xsl:for-each>
            <xsl:for-each select="thesaurus">
            	<gmd:thesaurusName>
	                <xsl:for-each select="GM03Core.Core.MD_Thesaurus">
	                	<xsl:for-each select="citation">
	                        <xsl:apply-templates mode="Citation" select="GM03Comprehensive.Comprehensive.CI_Citation"/>
	                	</xsl:for-each>
	                </xsl:for-each>
	            </gmd:thesaurusName>
            </xsl:for-each>
        </gmd:MD_Keywords>
    </xsl:template>

    <xsl:template mode="DescriptiveKeyword" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">DescriptiveKeyword</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template mode="ConstsTypes" match="GM03Comprehensive.Comprehensive.MD_LegalConstraints">
        <che:CHE_MD_LegalConstraints gco:isoType="gmd:MD_LegalConstraints">
            <xsl:apply-templates mode="text" select="useLimitation"/>
            <xsl:apply-templates mode="ConstsTypes" select="accessConstraints"/>
            <xsl:apply-templates mode="ConstsTypes" select="useConstraints"/>
            <xsl:apply-templates mode="text" select="otherConstraints"/>
        </che:CHE_MD_LegalConstraints>
    </xsl:template>

    <xsl:template mode="ConstsTypes" match="useConstraints">
        <gmd:useConstraints>
            <gmd:MD_RestrictionCode codeList="./resources/codeList.xml#MD_RestrictionCode" codeListValue="{GM03Comprehensive.Comprehensive.MD_RestrictionCode_/value}" />
        </gmd:useConstraints>
    </xsl:template>

    <xsl:template mode="ConstsTypes" match="accessConstraints">
        <gmd:accessConstraints>
            <gmd:MD_RestrictionCode codeList="./resources/codeList.xml#MD_RestrictionCode" codeListValue="{GM03Comprehensive.Comprehensive.MD_RestrictionCode_/value}" />
        </gmd:accessConstraints>
    </xsl:template>

    <xsl:template mode="ConstsTypes" match="GM03Comprehensive.Comprehensive.MD_SecurityConstraints">
        <gmd:MD_SecurityConstraints>
            <xsl:apply-templates mode="text" select="useLimitation"/>
            <xsl:apply-templates mode="ConstsTypes" select="classification"/>
            <xsl:apply-templates mode="text" select="userNote"/>
            <xsl:apply-templates mode="text" select="classificationSystem"/>
            <xsl:apply-templates mode="text" select="handlingDescription"/>
        </gmd:MD_SecurityConstraints>
    </xsl:template>

    <xsl:template mode="ConstsTypes" match="classification">
        <gmd:classification>
            <gmd:MD_ClassificationCode codeListValue="{.}" codeList="./resources/codeList.xml#MD_ClassificationCode"/>
        </gmd:classification>
    </xsl:template>

    <xsl:template mode="ConstsTypes" match="GM03Comprehensive.Comprehensive.MD_Constraints">

        <gmd:MD_Constraints >
            <xsl:apply-templates mode="text" select="useLimitation"/>
        </gmd:MD_Constraints>
    </xsl:template>

    <xsl:template mode="ConstsTypes" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">ConstsTypes</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template mode="ResourceFormat" match="GM03Comprehensive.Comprehensive.MD_Format|GM03Core.Core.MD_Format">
        <xsl:param name="loop">0</xsl:param>
        <xsl:variable name="myTID" select="@TID"/>
        <gmd:MD_Format>
            <xsl:apply-templates mode="text" select="name"/>
            <xsl:apply-templates mode="text" select="version"/>
            <xsl:apply-templates mode="text" select="amendmentNumber"/>
            <xsl:apply-templates mode="text" select="specification"/>
            <xsl:apply-templates mode="text" select="fileDecompressionTechnique"/>

            <xsl:if test="$loop!='0'">
                <!-- fetch manually the N-N link with the MD_Distributor entries -->
                <xsl:for-each select="/GM03Comprehensive.Comprehensive/GM03Comprehensive.Comprehensive.formatDistributordistributorFormat[distributorFormat/@REF=$myTID]">
                    <xsl:apply-templates mode="ResourceFormat" select="formatDistributor"/>
                </xsl:for-each>
            </xsl:if>
        </gmd:MD_Format>
    </xsl:template>

    <xsl:template mode="ResourceFormat" match="formatDistributor">
        <gmd:formatDistributor>
            <xsl:apply-templates mode="Distributor"/>
        </gmd:formatDistributor>
    </xsl:template>

    <xsl:template mode="ResourceFormat" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">ResourceFormat</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
