<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to remove a reference to a source record.
-->
<xsl:stylesheet version="2.0" xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Source metadata record UUID -->
	<xsl:param name="sourceUuid"/>

	<!-- Do a copy of every nodes and attributes -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- Remove geonet:* elements and the target source. -->
	<xsl:template match="geonet:*|gmd:source[@uuidref=$sourceUuid]" priority="2"/>
</xsl:stylesheet>
