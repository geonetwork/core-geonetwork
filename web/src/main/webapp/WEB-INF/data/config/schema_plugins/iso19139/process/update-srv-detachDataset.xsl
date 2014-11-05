<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to update metadata for a service and 
detach a dataset metadata
-->
<xsl:stylesheet version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:date="http://exslt.org/dates-and-times">

	<xsl:param name="uuidref"/>

	<!-- Detach -->
	<xsl:template match="srv:operatesOn[@uuidref=$uuidref]" priority="2"/>
	<xsl:template match="srv:coupledResource[srv:SV_CoupledResource/srv:identifier/gco:CharacterString=$uuidref]" priority="2"/>

	<!-- Do a copy of every nodes and attributes -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- Remove geonet:* elements. -->
	<xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
