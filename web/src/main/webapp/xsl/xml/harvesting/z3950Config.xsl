<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->

	<xsl:import href="common.xsl"/>	

	<!-- ============================================================================================= -->
	<!-- === Z3950Config harvesting node -->
	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="site">
		<host><xsl:value-of    select="host/value" /></host>
		<port><xsl:value-of    select="port/value" /></port>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="options">
		<clearConfig><xsl:value-of select="clearConfig/value"/></clearConfig>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="searches">
		<searches>
			<xsl:for-each select="children/search">
				<search>
					<freeText><xsl:value-of select="children/freeText/value" /></freeText>
					<title><xsl:value-of    select="children/title/value" /></title>
					<abstract><xsl:value-of select="children/abstract/value" /></abstract>
					<keywords><xsl:value-of select="children/keywords/value" /></keywords>
					<category><xsl:value-of select="children/category/value" /></category>
				</search>
			</xsl:for-each>
		</searches>
	</xsl:template>
	
	<!-- ============================================================================================= -->

</xsl:stylesheet>
