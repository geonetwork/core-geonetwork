<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:csw="http://www.opengis.net/cat/csw"
										xmlns:dc ="http://purl.org/dc/elements/1.1/">

	<xsl:template match="simpledc">
		<csw:BriefRecord>
			<xsl:for-each select="dc:identifier">
				<dc:identifier><xsl:value-of select="."/></dc:identifier>
			</xsl:for-each>

			<xsl:for-each select="dc:type">
				<dc:type><xsl:value-of select="."/></dc:type>
			</xsl:for-each>
		</csw:BriefRecord>
	</xsl:template>

</xsl:stylesheet>
