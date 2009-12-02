<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
										xmlns:dc ="http://purl.org/dc/elements/1.1/"
										xmlns:dct="http://purl.org/dc/terms/"
										xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:param name="displayInfo"/>
	
	<!-- ============================================================================= -->

	<xsl:template match="metadata">
		<xsl:variable name="info" select="geonet:info"/>
		
		<csw:BriefRecord>

			<xsl:choose>
				<xsl:when test="idinfo/citation/citeinfo/title/@cat_id">
					<xsl:for-each select="idinfo/citation/citeinfo/title/@cat_id">
						<dc:identifier><xsl:value-of select="."/></dc:identifier>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<dc:identifier><xsl:value-of select="geonet:info/uuid"/></dc:identifier>
				</xsl:otherwise>
			</xsl:choose>
			
			<xsl:for-each select="idinfo/citation/citeinfo/title">
				<dc:title><xsl:value-of select="."/></dc:title>
			</xsl:for-each>
			
			<xsl:for-each select="spdoinfo">
				<dc:type><xsl:value-of select="."/></dc:type>
			</xsl:for-each>
			
			<!-- GeoNetwork elements added when resultType is equal to results_with_summary -->
			<xsl:if test="$displayInfo = 'true'">
				<xsl:copy-of select="$info"/>
			</xsl:if>

		</csw:BriefRecord>
	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
