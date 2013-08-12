<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to remove a reference to a online resource.
-->
<xsl:stylesheet version="2.0" xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:param name="uuidref"/>
	
	<!-- Do a copy of every nodes and attributes -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="geonet:*|srv:coupledResource[normalize-space(srv:SV_CoupledResource/srv:identifier/gco:CharacterString) = $uuidref]|srv:operatesOn[@uuidref = $uuidref]" priority="2"/>
	
</xsl:stylesheet>
