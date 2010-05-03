<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->

	<xsl:import href="common.xsl"/>	

	<!-- ============================================================================================= -->
	<!-- === Z3950 harvesting node -->
	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="site">
		<query><xsl:value-of select="query/value" /></query>
		<icon><xsl:value-of select="icon/value" /></icon>

		<xsl:apply-templates select="repositories/children" mode="repositories"/>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="options"/>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="repositories">
		<repositories>
			<xsl:for-each select="repository">
				<repository id="{value}"/>
			</xsl:for-each>
		</repositories>
	</xsl:template>

	<!-- ============================================================================================= -->


</xsl:stylesheet>
