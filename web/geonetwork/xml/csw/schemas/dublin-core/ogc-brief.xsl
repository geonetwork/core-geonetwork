<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
		xmlns:dc ="http://purl.org/dc/elements/1.1/">

	<xsl:template match="simpledc">
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
		</csw:BriefRecord>
	</xsl:template>
</xsl:stylesheet>
