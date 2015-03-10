<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to update metadata adding a reference to a parent record.
-->
<xsl:stylesheet version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork">

	<!-- Parent metadata record UUID -->
	<xsl:param name="parentUuid"/>

	<xsl:template match="/gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:copy-of
				select="gmd:fileIdentifier|
		    gmd:language|
		    gmd:characterSet"/>
			
			<!-- Only one parent identifier allowed
			- overwriting existing one. -->
			<gmd:parentIdentifier>
				<gco:CharacterString>
					<xsl:value-of select="$parentUuid"/>
				</gco:CharacterString>
			</gmd:parentIdentifier>
			<xsl:copy-of
				select="
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
			gmd:identificationInfo|
			gmd:contentInfo|
		    gmd:distributionInfo|
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
	
	<!-- Remove geonet:* elements. -->
	<xsl:template match="geonet:*" priority="2"/>
</xsl:stylesheet>
