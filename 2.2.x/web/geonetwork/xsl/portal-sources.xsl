<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:template match="/root">
		<response>
			<xsl:for-each select="gui/sources/*">
				<source>
					<xsl:copy-of select="*"/>
				</source>
			</xsl:for-each>
		</response>
	</xsl:template>
	
</xsl:stylesheet>
