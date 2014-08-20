<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
		xmlns:dc ="http://purl.org/dc/elements/1.1/"
		xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:param name="displayInfo"/>
	
	<xsl:template match="simpledc">
		<xsl:variable name="info" select="geonet:info"/>
		<csw:BriefRecord>
			<xsl:for-each select="dc:identifier">
				<dc:identifier><xsl:value-of select="."/></dc:identifier>
			</xsl:for-each>

<!-- Change for CSW 2.0.2 - title is mandatory -->
			<dc:title>
			<xsl:for-each select="dc:title">
				<xsl:value-of select="."/>
			</xsl:for-each>
			</dc:title>

			<xsl:for-each select="dc:type">
				<dc:type><xsl:value-of select="."/></dc:type>
			</xsl:for-each>
			
			<!-- GeoNetwork elements added when resultType is equal to results_with_summary -->
			<xsl:if test="$displayInfo = 'true'">
				<xsl:copy-of select="$info"/>
			</xsl:if>
			
		</csw:BriefRecord>
	</xsl:template>
</xsl:stylesheet>
