<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->

	<xsl:import href="common.xsl"/>	

	<!-- ============================================================================================= -->
	<!-- === OAI-PMH harvesting node -->
	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="site">
		<url><xsl:value-of select="url/value" /></url>
		<icon><xsl:value-of select="icon/value" /></icon>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="options">
		<validate><xsl:value-of  select="validate/value" /></validate>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="searches">
		<searches>
			<xsl:for-each select="children/search">
				<search>
					<from><xsl:value-of       select="children/from/value" /></from>
					<until><xsl:value-of      select="children/until/value" /></until>
					<set><xsl:value-of        select="children/set/value" /></set>
					<prefix><xsl:value-of     select="children/prefix/value" /></prefix>
					<stylesheet><xsl:value-of select="children/stylesheet/value" /></stylesheet>
				</search>
			</xsl:for-each>
		</searches>
	</xsl:template>
	
	<!-- ============================================================================================= -->

</xsl:stylesheet>
