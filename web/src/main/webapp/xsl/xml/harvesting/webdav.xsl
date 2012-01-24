<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->

	<xsl:import href="common.xsl"/>	

	<!-- ============================================================================================= -->
	<!-- === Web DAV harvesting node -->
	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="site">
		<url><xsl:value-of select="url/value" /></url>
		<icon><xsl:value-of select="icon/value" /></icon>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="options">
		<validate><xsl:value-of  select="validate/value" /></validate>
		<recurse><xsl:value-of select="recurse/value" /></recurse>
		<subtype><xsl:value-of select="subtype/value" /></subtype> 
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="searches"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
