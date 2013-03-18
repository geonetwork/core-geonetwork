<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:include href="metadata.xsl"/>
	<xsl:include href="utils.xsl"/>

	<xsl:template match="/root">
		<response>
			<xsl:apply-templates select="*[geonet:info]" mode="brief"/>
		</response>
	</xsl:template>
	
</xsl:stylesheet>
