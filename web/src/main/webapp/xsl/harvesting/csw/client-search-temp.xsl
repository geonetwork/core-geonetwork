<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->
	<!-- === Generate a table that represents a search on the remote node -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/search">
		<xsl:apply-templates select="." mode="data"/>
	</xsl:template>

	<xsl:template match="*" mode="data">
		<xsl:copy-of select="."/>
		<xsl:apply-templates/>
	</xsl:template>

</xsl:stylesheet>
