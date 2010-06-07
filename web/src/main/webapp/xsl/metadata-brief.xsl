<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="utils.xsl"/>
	<xsl:import href="metadata.xsl"/>

	<xsl:template match="/">
		<xsl:apply-templates mode="brief"/>
	</xsl:template>

</xsl:stylesheet>
