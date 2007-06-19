<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->

	<xsl:import href="common.xsl"/>	

	<!-- ============================================================================================= -->
	<!-- === Geonetwork harvesting node -->
	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="site">
		<host><xsl:value-of    select="host/value" /></host>
		<port><xsl:value-of    select="port/value" /></port>
		<servlet><xsl:value-of select="servlet/value" /></servlet>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="options">
		<createGroups><xsl:value-of select="createGroups/value" /></createGroups>
		<createCateg><xsl:value-of  select="createCateg/value" /></createCateg>
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
					<digital><xsl:value-of  select="children/digital/value" /></digital>
					<hardcopy><xsl:value-of select="children/hardcopy/value" /></hardcopy>
					<siteId><xsl:value-of   select="children/siteId/value" /></siteId>
				</search>
			</xsl:for-each>
		</searches>
	</xsl:template>
	
	<!-- ============================================================================================= -->

</xsl:stylesheet>
