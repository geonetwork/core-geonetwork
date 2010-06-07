<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- This stylesheet converts ISO19115 and ISO19139 metadata into ISO19139 metadata in XML format -->
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
	<xsl:include href="../19115to19139/19115-to-19139.xsl"/>
	
	<xsl:template match="/root">
		<xsl:choose>
			<!-- Export ISO19115/19139 XML (just a copy)-->
			<xsl:when test="gmd:MD_Metadata">
				<xsl:apply-templates select="gmd:MD_Metadata"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="@*|node()">
			<xsl:copy>
				<xsl:apply-templates select="@*|node()[name(self::*)!='geonet:info']"/>
			</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
