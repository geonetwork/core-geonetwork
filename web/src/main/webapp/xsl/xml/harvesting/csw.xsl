<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->

	<xsl:import href="common.xsl"/>	

	<!-- ============================================================================================= -->
	<!-- === CSW harvesting node -->
	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="site">
		<capabilitiesUrl><xsl:value-of select="capabUrl/value" /></capabilitiesUrl>
		<icon><xsl:value-of select="icon/value" /></icon>
		<rejectDuplicateResource><xsl:value-of select="rejectDuplicateResource/value"/></rejectDuplicateResource>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="options"/>

	<!-- ============================================================================================= -->


		
	<xsl:template match="*" mode="searches">
		
		<searches>
		<search>
			<xsl:apply-templates select="children" />
		</search>
		</searches>
	
	</xsl:template>
		
	<xsl:template match="children">
		 <xsl:copy-of select="search/children/child::*"/>
	</xsl:template>	
	
	
	<!-- ============================================================================================= -->

</xsl:stylesheet>
