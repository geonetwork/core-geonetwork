<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/system">
		<config>
			<xsl:apply-templates/>
		</config>
	</xsl:template>

	<xsl:template match="children">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="value">
		<xsl:value-of select="."/>
	</xsl:template>

	<xsl:template match="node()">
		<xsl:element name="{name(.)}">
			<xsl:apply-templates select="*"/>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>
