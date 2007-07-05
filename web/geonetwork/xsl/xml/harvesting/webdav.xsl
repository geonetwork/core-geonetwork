<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->

	<xsl:import href="common.xsl"/>	

	<!-- ============================================================================================= -->
	<!-- === Web DAV harvesting node -->
	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="site">
		<url><xsl:value-of select="url/value" /></url>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="options">
		<structure><xsl:value-of select="structure/value" /></structure>
		<validate><xsl:value-of  select="validate/value" /></validate>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="searches"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
