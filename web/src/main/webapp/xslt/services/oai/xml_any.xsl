<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gfc="http://www.isotc211.org/2005/gfc"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/root">
		<xsl:apply-templates select="simpledc|gfc:*|gmd:*"/>
	</xsl:template>

	<xsl:template match="@*|node()">
			<xsl:copy>
				<xsl:apply-templates select="@*|node()[name(self::*)!='geonet:info']"/>
			</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
