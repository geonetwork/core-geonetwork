<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to update metadata for a service and 
attached it to the metadata for data.
-->
<xsl:stylesheet version="2.0" 			xmlns:gmd="http://www.isotc211.org/2005/gmd"	
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:gts="http://www.isotc211.org/2005/gts"
										xmlns:gml="http://www.opengis.net/gml"
										xmlns:srv="http://www.isotc211.org/2005/srv"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:xlink="http://www.w3.org/1999/xlink"
										xmlns:date="http://exslt.org/dates-and-times">

	<!-- ============================================================================= -->
	
	<xsl:param name="uuidref"></xsl:param>
	<xsl:param name="siteUrl"></xsl:param>

	<!-- ============================================================================= -->
	
	<xsl:template match="/gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
	<xsl:copy>
		<xsl:copy-of select="@*"/>
		<xsl:copy-of
		    select="gmd:fileIdentifier|
		    gmd:language|
		    gmd:characterSet|
		    gmd:parentIdentifier|
		    gmd:hierarchyLevel|
		    gmd:hierarchyLevelName|
		    gmd:contact|
		    gmd:dateStamp|
		    gmd:metadataStandardName|
		    gmd:metadataStandardVersion|
		    gmd:dataSetURI|
		    gmd:locale|
		    gmd:spatialRepresentationInfo|
		    gmd:referenceSystemInfo|
		    gmd:metadataExtensionInfo|
		    gmd:identificationInfo"/>
		    

		    <xsl:choose>
                <!-- Check if featureCatalogueCitation for uuidref -->
			    <xsl:when test="gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation[@uuidref = $uuidref]">
					<gmd:contentInfo>
						<gmd:MD_FeatureCatalogueDescription>
                            <xsl:copy-of select="gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation[@uuidref = $uuidref]/../gmd:complianceCode|
                                gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation[@uuidref = $uuidref]/../gmd:language|
                                gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation[@uuidref = $uuidref]/../gmd:includedWithDataset|
                                gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation[@uuidref = $uuidref]/../gmd:featureTypes"/>

                            <!-- Add xlink:href featureCatalogueCitation -->
                            <gmd:featureCatalogueCitation uuidref="{$uuidref}" xlink:href="{$siteUrl}/csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;id={$uuidref}">
                                <xsl:copy-of select="gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation[@uuidref = $uuidref]/gmd:CI_Citation"/>
                            </gmd:featureCatalogueCitation>

                        </gmd:MD_FeatureCatalogueDescription>
                    </gmd:contentInfo>
			    </xsl:when>

			    <xsl:otherwise>
					<xsl:copy-of select="gmd:contentInfo"/>
                    <gmd:contentInfo>
                        <gmd:MD_FeatureCatalogueDescription>
                            <gmd:includedWithDataset />
                            <gmd:featureCatalogueCitation uuidref="{$uuidref}" xlink:href="{$siteUrl}/csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;id={$uuidref}" />
                        </gmd:MD_FeatureCatalogueDescription>
                    </gmd:contentInfo>
			    </xsl:otherwise>
		    </xsl:choose>
			
		<xsl:copy-of select="gmd:distributionInfo|
		    gmd:dataQualityInfo|
		    gmd:portrayalCatalogueInfo|
		    gmd:metadataConstraints|
		    gmd:applicationSchemaInfo|
		    gmd:metadataMaintenance|
		    gmd:series|
		    gmd:describes|
		    gmd:propertyType|
		    gmd:featureType|
		    gmd:featureAttribute"/>
		
	</xsl:copy>
	</xsl:template>


</xsl:stylesheet>
