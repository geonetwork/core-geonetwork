<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:csw="http://www.opengis.net/cat/csw"
										xmlns:gmd="http://www.isotc211.org/2005/gmd"
										xmlns:ows="http://www.opengis.net/ows">

	<!-- =================================================================== -->

	<xsl:template match="gmd:MD_Metadata">
		<xsl:copy>
			<xsl:apply-templates select="gmd:fileIdentifier"/>
			<xsl:apply-templates select="gmd:hierarchyLevel"/>
			<xsl:apply-templates select="gmd:contact"/>
			<xsl:apply-templates select="gmd:identificationInfo"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->

	<xsl:template match="gmd:MD_DataIdentification">
		<xsl:copy>
			<xsl:apply-templates select="gmd:title"/>
			<xsl:apply-templates select="gmd:graphicOverview"/>
			<xsl:apply-templates select="gmd:topicCategory"/>
			<xsl:apply-templates select="gmd:extent"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->

	<xsl:template match="gmd:SV_ServiceIdentification">
		<xsl:copy>
			<xsl:apply-templates select="gmd:title"/>
			<xsl:apply-templates select="gmd:graphicOverview"/>
			<xsl:apply-templates select="gmd:serviceType"/>
			<xsl:apply-templates select="gmd:serviceTypeVersion"/>
			<xsl:apply-templates select="gmd:extent"/>
			<xsl:apply-templates select="gmd:couplingType"/>
		</xsl:copy>
	</xsl:template>

	<!-- === copy template ================================================= -->

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->

</xsl:stylesheet>



