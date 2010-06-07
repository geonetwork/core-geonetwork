<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dc="http://purl.org/dc/elements/1.1/">

	<xsl:template match="simpledc">
		 <uuid><xsl:value-of select="dc:identifier"/></uuid>
	</xsl:template>

</xsl:stylesheet>
