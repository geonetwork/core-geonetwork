<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:include href="../ThreddsCatalogto19119/ThreddsCatalog-to-19119.xsl"/>
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
	<xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>
</xsl:stylesheet>
