<?xml version="1.0" encoding="UTF-8"?>

<!-- COGS - This file was created as part of a project for NIWA to allow metadata harvested from a OGC WxS service to populate a template metadata record. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
    xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="gmd srv">


    <!-- Parameters -->
    <xsl:param name="template"></xsl:param>


    <xsl:variable name="templateMD" select="document($template)"/>

    <xsl:param name="updateMode" select="'replace'"/>
    

    <!-- ================================================================= -->

    <xsl:template match="/">
        <xsl:apply-templates select="/gmd:MD_Metadata"/>
    </xsl:template>

    <!-- ================================================================= -->

    <xsl:template match="/gmd:MD_Metadata">
        <xsl:copy>
            <xsl:copy-of select="gmd:fileIdentifier"/>
            <xsl:copy-of select="gmd:language|gmd:characterSet"/>
            <xsl:copy-of select="gmd:parentIdentifier"/>
            <xsl:copy-of select="gmd:hierarchyLevel"/>
            <xsl:copy-of select="gmd:hierarchyLevelName"/>

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:contact/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:contact/."/>
                <xsl:with-param name="mode" select="''"/>
            </xsl:call-template>

            <xsl:copy-of select="gmd:dateStamp"/>
            <xsl:copy-of select="gmd:metadataStandardName"/>
            <xsl:copy-of select="gmd:metadataStandardVersion"/>
            
            <xsl:copy-of select="gmd:dataSetURI"/>
            <xsl:copy-of select="gmd:locale"/>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:spatialRepresentationInfo/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:spatialRepresentationInfo/."/>
                <xsl:with-param name="mode" select="$updateMode"/>
            </xsl:call-template>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:referenceSystemInfo/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:referenceSystemInfo/."/>
                <xsl:with-param name="mode" select="$updateMode"/>
            </xsl:call-template>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:metadataExtensionInfo/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:metadataExtensionInfo/."/>
                <xsl:with-param name="mode" select="$updateMode"/>
            </xsl:call-template>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
            <!-- Identification -->
            <gmd:identificationInfo>
                <xsl:for-each select="/gmd:MD_Metadata/gmd:identificationInfo/*">
	            	<xsl:copy>

                        <xsl:copy-of select="@*"/>

                        <xsl:call-template name="process">
                            <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:citation/."/>
                            <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:identificationInfo//gmd:citation/."/>
                            <xsl:with-param name="mode" select="$updateMode"/>
                        </xsl:call-template>

                        <xsl:call-template name="process">
                            <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:abstract/."/>
                            <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:identificationInfo//gmd:abstract/."/>
                            <xsl:with-param name="mode" select="$updateMode"/>
                        </xsl:call-template>

                        <xsl:call-template name="process">
                            <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:purpose/."/>
                            <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:identificationInfo//gmd:purpose/."/>
                            <xsl:with-param name="mode" select="$updateMode"/>
                        </xsl:call-template>

                        <xsl:call-template name="process">
                            <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:credit/."/>
                            <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:identificationInfo//gmd:credit/."/>
                            <xsl:with-param name="mode" select="$updateMode"/>
                        </xsl:call-template>

                        <xsl:call-template name="process">
                            <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:status/."/>
                            <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:identificationInfo//gmd:status/."/>
                            <xsl:with-param name="mode" select="$updateMode"/>
                        </xsl:call-template>
	
	                    <xsl:call-template name="process">
                            <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:pointOfContact/."/>
	                        <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:identificationInfo//gmd:pointOfContact/."/>
	                        <xsl:with-param name="mode" select="$updateMode"/>
	                    </xsl:call-template>

                        <xsl:call-template name="process">
                            <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:descriptiveKeywords/."/>
                            <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:identificationInfo//gmd:descriptiveKeywords/."/>
                            <xsl:with-param name="mode" select="'add'"/>
                        </xsl:call-template>
	                    
	                    <xsl:copy-of select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:resourceMaintenance"/>
	                    <xsl:copy-of select="/gmd:MD_Metadata/gmd:identificationInfo//gmd:graphicOverview"/>
	                    <xsl:copy-of select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:resourceFormat"/>
	                    

	                    
	                    <!-- FIXME / TO BE DISCUSS following sections are replaced. -->
	                    <xsl:copy-of select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:resourceSpecificUsage"/>
	                    <xsl:copy-of select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:resourceConstraints"/>
	                    <xsl:copy-of select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:aggregationInfo"/>
	                    <xsl:copy-of select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:spatialRepresentationType"/>
	                    <xsl:copy-of select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:spatialResolution"/>
	                    <xsl:copy-of select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:langage"/>
	                    <xsl:copy-of select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:characterSet"/>
	                    <xsl:copy-of select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:topicCategory"/>
	                    <xsl:copy-of select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo//gmd:environmentDescription"/>
	                    
	                    <xsl:call-template name="process">
                            <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:extent/."/>
	                        <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:identificationInfo//gmd:extent/."/>
	                        <xsl:with-param name="mode" select="$updateMode"/>
	                    </xsl:call-template>
	                    
	                    <!-- FIXME / TO BE DISCUSS following sections are replaced/preserved  -->
	                    <xsl:copy-of select="$templateMD//gmd:MD_Metadata/gmd:identificationInfo//gmd:supplementalInformation"/>
		            	<xsl:copy-of select="srv:*"/>
		            	
		            	<!-- Note: When applying this stylesheet
			                to an ISO profil having a new substitute for
			                MD_Identification, profil specific element copy.
			            -->
			            <xsl:for-each select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd' and namespace-uri()!='http://www.isotc211.org/2005/srv']">
			                <xsl:copy-of select="."/>
			            </xsl:for-each>
	            	</xsl:copy>
	            </xsl:for-each>
	        </gmd:identificationInfo>
               
            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:contentInfo/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:contentInfo/."/>
                <xsl:with-param name="mode" select="$updateMode"/>
            </xsl:call-template>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
            <!-- Distribution -->
            
            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:distributionInfo/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:distributionInfo/."/>
                <!-- Force mode to replace element due to schema cardinality -->
                <xsl:with-param name="mode" select="'replace'"/>
            </xsl:call-template>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
            <!-- Quality -->
            
            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:dataQualityInfo/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:dataQualityInfo/."/>
                <xsl:with-param name="mode" select="$updateMode"/>
            </xsl:call-template>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:portrayalCatalogueInfo/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:portrayalCatalogueInfo/."/>
                <xsl:with-param name="mode" select="$updateMode"/>
            </xsl:call-template>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:metadataConstraints/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:metadataConstraints/."/>
                <xsl:with-param name="mode" select="$updateMode"/>
            </xsl:call-template>
            
            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:applicationSchemaInfo/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:applicationSchemaInfo/."/>
                <xsl:with-param name="mode" select="$updateMode"/>
            </xsl:call-template>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:metadataMaintenance/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:metadataMaintenance/."/>

                <xsl:with-param name="mode" select="'replace'"/>
            </xsl:call-template>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:series/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:series/."/>

                <xsl:with-param name="mode" select="'replace'"/>
            </xsl:call-template>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:describes/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:describes/."/>

                <xsl:with-param name="mode" select="'replace'"/>
            </xsl:call-template>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:propertyType/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:propertyType/."/>

                <xsl:with-param name="mode" select="'replace'"/>
            </xsl:call-template>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:featureType/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:featureType/."/>

                <xsl:with-param name="mode" select="'replace'"/>
            </xsl:call-template>


            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:call-template name="process">
                <xsl:with-param name="templateElement" select="$templateMD/gmd:MD_Metadata/gmd:featureAttribute/."/>
                <xsl:with-param name="name" select="/gmd:MD_Metadata/gmd:featureAttribute/."/>

                <xsl:with-param name="mode" select="'replace'"/>
            </xsl:call-template>


        </xsl:copy>
    </xsl:template>
    
    
    <!-- Generic template for template record update -->
    <!-- Depending on the choosen strategy to be applied on each main sections (mode) -->
    <xsl:template name="process">
        <!--<xsl:param name="update" select="false()"/>   -->
        <xsl:param name="templateElement"/>
        <xsl:param name="name"/>
        <xsl:param name="mode"/>
        <xsl:variable name="update">
            <xsl:value-of select="$name/*" />
        </xsl:variable>

<!--        <xsl:variable name="templateElement">
            <xsl:choose>
                <xsl:when test="$subLevel=true()">
                    <xsl:copy-of select="$templateMD/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/*[name(.)=name($name)]"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="$templateMD/gmd:MD_Metadata/*[name(.)=name($name)]"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>             -->
		
		<xsl:choose>
            <!-- Replacing elements from parent -->
            <xsl:when test="$mode='replace' and $update != ''">
                <xsl:copy-of select="$name"/>
            </xsl:when>
            <!-- Adding elements -->
            <xsl:when test="$mode='add' and $update != ''">
                <xsl:copy-of select="$templateElement"/>
                <xsl:copy-of select="$name"/>
            </xsl:when>
            <!-- Elements preserved from template-->
            <xsl:otherwise>
                <xsl:copy-of select="$templateElement"/>
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>


</xsl:stylesheet>

