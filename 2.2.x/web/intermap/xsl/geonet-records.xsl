<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<response>
			<summary>
				<xsl:attribute name="count"><xsl:value-of select="/root/response/summary/@count" /></xsl:attribute>
				<xsl:attribute name="from"><xsl:value-of select="/root/response/@from"/></xsl:attribute>
				<xsl:attribute name="to"><xsl:value-of select="/root/response/@to"/></xsl:attribute>
			</summary>
			<xsl:apply-templates select="//metadata" />
		</response>
	</xsl:template>
	
	<xsl:template match="/root/response/metadata">
		<metadata>
			<xsl:attribute name="title"><xsl:value-of select="title" /></xsl:attribute>
			<xsl:apply-templates select="service[@type='wms']" />
		</metadata>
	</xsl:template>
	
	<xsl:template match="service">
		<xsl:copy-of select="." />
	</xsl:template>
	
</xsl:stylesheet>
