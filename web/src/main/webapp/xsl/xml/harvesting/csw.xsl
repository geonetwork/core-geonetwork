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
			<xsl:for-each select="children/search">
				<search>
					<freeText><xsl:value-of select="children/freeText/value" /></freeText>
					<title><xsl:value-of    select="children/title/value" /></title>
					<abstract><xsl:value-of select="children/abstract/value" /></abstract>
					<subject><xsl:value-of  select="children/subject/value" /></subject>
					<minscale><xsl:value-of select="children/minscale/value" /></minscale>
					<maxscale><xsl:value-of select="children/maxscale/value" /></maxscale>
				</search>
			</xsl:for-each>
		</searches>
	</xsl:template>
	
	<!-- ============================================================================================= -->

</xsl:stylesheet>
