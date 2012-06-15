<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->

	<xsl:import href="common.xsl"/>

	<!-- ============================================================================================= -->
	<!-- === CGP harvesting node -->
	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="site">
		<url><xsl:value-of select="url/value" /></url>
		<!-- <ogctype><xsl:value-of select="ogctype/value" /></ogctype>    -->
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
					<freeText><xsl:value-of select="children/freeText/value" /></freeText>
					<from><xsl:value-of select="children/from/value" /></from>
					<until><xsl:value-of select="children/until/value" /></until>
					<latNorth><xsl:value-of select="children/latNorth/value" /></latNorth>
					<latSouth><xsl:value-of select="children/latSouth/value" /></latSouth>
					<lonEast><xsl:value-of select="children/lonEast/value" /></lonEast>
					<lonWest><xsl:value-of select="children/lonWest/value" /></lonWest>
				</search>
			</xsl:for-each>
		</searches>
	</xsl:template>

	<!-- ============================================================================================= -->

	<!-- <xsl:template match="*" mode="options">
		<lang><xsl:value-of  select="lang/value" /></lang>
		<topic><xsl:value-of  select="topic/value" /></topic>
		<createThumbnails><xsl:value-of  select="createThumbnails/value" /></createThumbnails>
		<useLayer><xsl:value-of  select="useLayer/value" /></useLayer>
		<useLayerMd><xsl:value-of  select="useLayerMd/value" /></useLayerMd>
		<datasetCategory><xsl:value-of  select="datasetCategory/value" /></datasetCategory>
	</xsl:template>       -->

	<!-- ============================================================================================= -->

	<!-- <xsl:template match="*" mode="searches"/>   -->

	<!-- ============================================================================================= -->

</xsl:stylesheet>
