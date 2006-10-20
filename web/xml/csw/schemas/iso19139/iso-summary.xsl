<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:csw="http://www.opengis.net/cat/csw"
										xmlns:gmd="http://www.isotc211.org/2005/gmd"
										xmlns:ows="http://www.opengis.net/ows">

	<!-- =================================================================== -->

	<xsl:template match="gmd:MD_Metadata">
		<xsl:copy>
			<xsl:apply-templates select="gmd:fileIdentifier"/>
			<xsl:apply-templates select="gmd:language"/>
			<xsl:apply-templates select="gmd:characterSet"/>
			<xsl:apply-templates select="gmd:parentIdentifier"/>
			<xsl:apply-templates select="gmd:hierarchyLevel"/>
			<xsl:apply-templates select="gmd:hierarchyLevelName"/>
			<xsl:apply-templates select="gmd:contact"/>
			<xsl:apply-templates select="gmd:dateStamp"/>
			<xsl:apply-templates select="gmd:metadataStandardName"/>
			<xsl:apply-templates select="gmd:metadataStandardVersion"/>
			<xsl:apply-templates select="gmd:referenceSystemInfo"/>
			<xsl:apply-templates select="gmd:identificationInfo"/>
			<xsl:apply-templates select="gmd:distributionInfo"/>
			<xsl:apply-templates select="gmd:dataQualityInfo"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->

	<xsl:template match="gmd:CI_Citation">
		<xsl:copy>
			<xsl:apply-templates select="gmd:title"/>
			<xsl:apply-templates select="gmd:date"/>
			<xsl:apply-templates select="gmd:identifier"/>
			<xsl:apply-templates select="gmd:citedResponsibleParty"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->

	<xsl:template match="gmd:MD_Distribution">
		<xsl:copy>
			<xsl:apply-templates select="gmd:distributionFormat"/>
			<xsl:apply-templates select="gmd:transferOptions"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->

	<xsl:template match="gmd:MD_DigitalTransferOptions">
		<xsl:copy>
			<xsl:apply-templates select="gmd:onLine"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->

	<xsl:template match="gmd:MD_Format">
		<xsl:copy>
			<xsl:apply-templates select="gmd:name"/>
			<xsl:apply-templates select="gmd:version"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->

	<xsl:template match="gmd:DQ_DataQuality">
		<xsl:copy>
			<xsl:apply-templates select="gmd:lineage"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->

	<xsl:template match="gmd:EX_Extent">
		<xsl:copy>
			<xsl:apply-templates select="gmd:verticalElement"/>
			<xsl:apply-templates select="gmd:temporalElement"/>
			<xsl:apply-templates select="gmd:geographicElement"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->
	<!-- === Data === -->
	<!-- =================================================================== -->

	<xsl:template match="gmd:MD_DataIdentification">
		<xsl:copy>
			<xsl:apply-templates select="gmd:citation"/>
			<xsl:apply-templates select="gmd:abstract"/>
			<xsl:apply-templates select="gmd:pointOfContact"/>
			<xsl:apply-templates select="gmd:resourceConstraints"/>
			<xsl:apply-templates select="gmd:spatialRepresentationType"/>
			<xsl:apply-templates select="gmd:spatialResolution"/>
			<xsl:apply-templates select="gmd:language"/>
			<xsl:apply-templates select="gmd:characterSet"/>
			<xsl:apply-templates select="gmd:topicCategory"/>
			<xsl:apply-templates select="gmd:extent"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->
	<!-- === Services === -->
	<!-- =================================================================== -->

	<xsl:template match="gmd:SV_ServiceIdentification">
		<xsl:copy>
			<xsl:apply-templates select="gmd:citation"/>
			<xsl:apply-templates select="gmd:abstract"/>
			<xsl:apply-templates select="gmd:pointOfContact"/>
			<xsl:apply-templates select="gmd:resourceConstraints"/>
			<xsl:apply-templates select="gmd:serviceType"/>
			<xsl:apply-templates select="gmd:serviceTypeVersion"/>
			<xsl:apply-templates select="gmd:extent"/>
			<xsl:apply-templates select="gmd:couplingType"/>
			<xsl:apply-templates select="gmd:containsOperation"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->

	<xsl:template match="gmd:SV_OperationMetadata">
		<xsl:copy>
			<xsl:apply-templates select="gmd:operationName"/>
			<xsl:apply-templates select="gmd:DCP"/>
			<xsl:apply-templates select="gmd:connectPoint"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->
	<!-- === copy template === -->
	<!-- =================================================================== -->

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->

</xsl:stylesheet>



