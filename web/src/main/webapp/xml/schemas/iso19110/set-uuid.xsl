<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gfc="http://www.isotc211.org/2005/gfc">

	<!-- ================================================================= -->
	
	<xsl:template match="/root">
		<xsl:apply-templates select="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType"/>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType">
		 <xsl:copy>
	 			<xsl:attribute name="uuid"><xsl:value-of select="/root/env/uuid"/></xsl:attribute>
			    <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>
	
	<!-- ================================================================= -->
	
	<xsl:template match="@*|node()">
		 <xsl:copy>
			  <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
</xsl:stylesheet>

