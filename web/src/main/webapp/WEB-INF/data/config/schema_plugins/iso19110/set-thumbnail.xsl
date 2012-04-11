<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gfc="http://www.isotc211.org/2005/gfc"
    xmlns:gco="http://www.isotc211.org/2005/gco">

	<!-- ================================================================= -->
	
	<xsl:template match="/root">
		 <xsl:apply-templates/>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="@*|node()">
		 <xsl:copy>
			  <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
</xsl:stylesheet>
