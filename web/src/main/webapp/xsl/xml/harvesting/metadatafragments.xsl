<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ==================================================================== -->

	<xsl:import href="common.xsl"/>	

	<!-- ==================================================================== -->
	<!-- === Metadata fragments harvesting node -->
	<!-- ==================================================================== -->

	<xsl:template match="*" mode="site">
		<url><xsl:value-of select="url/value" /></url>
	</xsl:template>

	<!-- ==================================================================== -->

	<xsl:template match="*" mode="options">
		<lang><xsl:value-of  select="lang/value" /></lang>
		<query><xsl:value-of  select="query/value" /></query>
		<stylesheet><xsl:value-of  select="stylesheet/value" /></stylesheet>
		<templateId><xsl:value-of  select="templateId/value" /></templateId>
		<recordsCategory><xsl:value-of  select="recordsCategory/value" /></recordsCategory>
	</xsl:template>

	<!-- ==================================================================== -->

	<xsl:template match="*" mode="searches"/>

	<!-- ==================================================================== -->

</xsl:stylesheet>
