<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:csw="http://www.opengis.net/cat/csw"
										xmlns:dc ="http://purl.org/dc/elements/1.1/"> 

	<xsl:template match="simpledc">
		<csw:SummaryRecord>
			<xsl:for-each select="dc:identifier">
				<dc:identifier><xsl:value-of select="."/></dc:identifier>
			</xsl:for-each>

			<xsl:for-each select="dc:type">
				<dc:type><xsl:value-of select="."/></dc:type>
			</xsl:for-each>

			<xsl:for-each select="dc:title">
				<dc:title><xsl:value-of select="."/></dc:title>
			</xsl:for-each>

			<xsl:for-each select="dc:subject">
				<dc:subject><xsl:value-of select="."/></dc:subject>
			</xsl:for-each>

			<xsl:for-each select="dc:format">
				<dc:format><xsl:value-of select="."/></dc:format>
			</xsl:for-each>

			<xsl:for-each select="dc:relation">
				<dc:relation><xsl:value-of select="."/></dc:relation>
			</xsl:for-each>

			<!-- We should add dct:modified, dct:abstract and dct:spatial 
				  but we handle only the simple DC schema which doesn't have them -->

		</csw:SummaryRecord>
	</xsl:template>

</xsl:stylesheet>
