<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<keywords>
		<xsl:for-each select="/root/response/summary/keywords/keyword[string-length(normalize-space(@name)) > 0]">
			<xsl:sort select="lower-case(@name)" />
			<keyword name="{@name}"/>		
		</xsl:for-each>
		</keywords>
	</xsl:template>
</xsl:stylesheet>