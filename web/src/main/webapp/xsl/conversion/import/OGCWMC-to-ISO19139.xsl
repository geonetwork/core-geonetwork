<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns="http://www.fao.org/geonetwork" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:include href="../OGCWMCtoISO19139/OGCWMC-to-ISO19139.xsl"/>
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
	<xsl:template match="/">
		<xsl:apply-templates />
	</xsl:template>
</xsl:stylesheet>
