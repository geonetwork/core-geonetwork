<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->

	<xsl:import href="common.xsl"/>	

	<!-- ============================================================================================= -->
	<!-- === OGC WxS harvesting node -->
	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="site">
		<url><xsl:value-of select="url/value" /></url>
		<ogctype><xsl:value-of select="ogctype/value" /></ogctype>
		<icon><xsl:value-of select="icon/value" /></icon>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="options">
		<lang><xsl:value-of  select="lang/value" /></lang>
		<topic><xsl:value-of  select="topic/value" /></topic>
		<createThumbnails><xsl:value-of  select="createThumbnails/value" /></createThumbnails>
		<useLayer><xsl:value-of  select="useLayer/value" /></useLayer>
		<useLayerMd><xsl:value-of  select="useLayerMd/value" /></useLayerMd>
		<datasetCategory><xsl:value-of  select="datasetCategory/value" /></datasetCategory>
		<outputSchema><xsl:value-of  select="outputSchema/value" /></outputSchema>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="searches"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
