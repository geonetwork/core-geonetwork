<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ================================================================= -->
	
	<xsl:template match="/root">
		 <xsl:apply-templates select="Metadata"/>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="Metadata">
		 <xsl:copy>
		 		<xsl:copy-of select="@*"/>
	 			<mdFileID><xsl:value-of select="/root/env/uuid"/></mdFileID>
			  <xsl:apply-templates select="node()"/>
		 </xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="mdFileID"/>
	
	<!-- ================================================================= -->
	
	<xsl:template match="@*|node()">
		 <xsl:copy>
			  <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
</xsl:stylesheet>

