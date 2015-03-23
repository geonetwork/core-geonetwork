<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
										xmlns:gmd="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:srv="http://www.isotc211.org/2005/srv"
										xmlns:ows="http://www.opengis.net/ows"
										xmlns:geonet="http://www.fao.org/geonetwork">
	
	<xsl:param name="displayInfo"/>
	
	<!-- =================================================================== -->

	<xsl:template match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
		<xsl:variable name="info" select="geonet:info"/>
		<xsl:copy>
			<xsl:apply-templates select="gmd:fileIdentifier"/>
			<xsl:apply-templates select="gmd:hierarchyLevel"/>
			<xsl:apply-templates select="gmd:identificationInfo"/>
			
			<!-- GeoNetwork elements added when resultType is equal to results_with_summary -->
			<xsl:if test="$displayInfo = 'true'">
				<xsl:copy-of select="$info"/>
			</xsl:if>
			
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->

	<xsl:template match="gmd:MD_DataIdentification|
		*[contains(@gco:isoType, 'MD_DataIdentification')]|
		srv:SV_ServiceIdentification|
		*[contains(@gco:isoType, 'SV_ServiceIdentification')]
		">
		<xsl:copy>
			<xsl:apply-templates select="gmd:citation"/>
			<xsl:apply-templates select="gmd:graphicOverview"/>
			<xsl:apply-templates select="gmd:extent[child::gmd:EX_Extent[child::gmd:geographicElement]]|
				srv:extent[child::gmd:EX_Extent[child::gmd:geographicElement]]"/>
			<xsl:apply-templates select="srv:serviceType"/>
			<xsl:apply-templates select="srv:serviceTypeVersion"/>
		</xsl:copy>
	</xsl:template>

	<!-- =================================================================== -->

	<xsl:template match="gmd:MD_BrowseGraphic">
		<xsl:copy>
			<xsl:apply-templates select="gmd:fileName"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- =================================================================== -->
	
	<xsl:template match="gmd:EX_Extent">
        <xsl:copy>
        	<xsl:apply-templates select="gmd:geographicElement[child::gmd:EX_GeographicBoundingBox]"/>
        </xsl:copy>
	</xsl:template>
	
	<xsl:template match="gmd:EX_GeographicBoundingBox">
        <xsl:copy>
            <xsl:apply-templates select="gmd:westBoundLongitude"/>
        	<xsl:apply-templates select="gmd:eastBoundLongitude"/>
        	<xsl:apply-templates select="gmd:southBoundLatitude"/>
        	<xsl:apply-templates select="gmd:northBoundLatitude"/>
        </xsl:copy>
	</xsl:template>
	
	<!-- =================================================================== -->
	
	<xsl:template match="gmd:CI_Citation">
        <xsl:copy>
        	<xsl:apply-templates select="gmd:title"/>
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



