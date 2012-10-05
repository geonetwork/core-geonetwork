<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
										xmlns:dc ="http://purl.org/dc/elements/1.1/"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:gmd="http://www.isotc211.org/2005/gmd"
										xmlns:srv="http://www.isotc211.org/2005/srv"
										xmlns:ows="http://www.opengis.net/ows"
										xmlns:geonet="http://www.fao.org/geonetwork"
										exclude-result-prefixes="gmd srv gco">

	<xsl:param name="displayInfo"/>
	<xsl:param name="lang"/>
	
	<xsl:include href="../metadata-utils.xsl"/>
	
	<!-- ============================================================================= -->

	<xsl:template match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
		
		<xsl:variable name="info" select="geonet:info"/>
		<xsl:variable name="langId">
			<xsl:call-template name="getLangId">
				<xsl:with-param name="langGui" select="$lang"/>
				<xsl:with-param name="md" select="."/>
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:variable name="identification" select="gmd:identificationInfo/gmd:MD_DataIdentification|
			gmd:identificationInfo/*[@gco:isoType='gmd:MD_DataIdentification']|
			gmd:identificationInfo/srv:SV_ServiceIdentification"/>
		
		
		<csw:BriefRecord>

			<xsl:for-each select="gmd:fileIdentifier">
				<dc:identifier><xsl:value-of select="gco:CharacterString"/></dc:identifier>
			</xsl:for-each>
			
			<!-- DataIdentification -->
			<xsl:for-each select="$identification/gmd:citation/gmd:CI_Citation">    
				<xsl:for-each select="gmd:title">
					<dc:title>
						<xsl:apply-templates mode="localised" select=".">
							<xsl:with-param name="langId" select="$langId"/>
						</xsl:apply-templates>
					</dc:title>
				</xsl:for-each>
			</xsl:for-each>

			<xsl:for-each select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue">
				<dc:type><xsl:value-of select="."/></dc:type>
			</xsl:for-each>
			
			<!-- bounding box -->
			<xsl:for-each select="$identification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox|
				$identification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
				<xsl:variable name="rsi"  select="/gmd:MD_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/
					gmd:referenceSystemIdentifier/gmd:RS_Identifier|/gmd:MD_Metadata/gmd:referenceSystemInfo/
					*[@gco:isoType='MD_ReferenceSystem']/gmd:referenceSystemIdentifier/gmd:RS_Identifier"/>
				<xsl:variable name="auth" select="$rsi/gmd:codeSpace/gco:CharacterString"/>
				<xsl:variable name="id"   select="$rsi/gmd:code/gco:CharacterString"/>
				
				<ows:BoundingBox crs="{$auth}::{$id}">
					<ows:LowerCorner>
						<xsl:value-of select="concat(gmd:eastBoundLongitude/gco:Decimal, ' ', gmd:southBoundLatitude/gco:Decimal)"/>
					</ows:LowerCorner>
					
					<ows:UpperCorner>
						<xsl:value-of select="concat(gmd:westBoundLongitude/gco:Decimal, ' ', gmd:northBoundLatitude/gco:Decimal)"/>
					</ows:UpperCorner>
				</ows:BoundingBox>
			</xsl:for-each>
			
			<!-- GeoNetwork elements added when resultType is equal to results_with_summary -->
			<xsl:if test="$displayInfo = 'true'">
				<xsl:copy-of select="$info"/>
			</xsl:if>

		</csw:BriefRecord>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*">
		<xsl:apply-templates select="*"/>
	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
