<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to remove a reference to a online resource.
-->
<xsl:stylesheet version="2.0" xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:param name="url"/>
	<xsl:param name="name" />
	
	<!-- Do a copy of every nodes and attributes -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- Remove geonet:* elements. -->
	<xsl:template match="geonet:*|gmd:onLine[normalize-space(gmd:CI_OnlineResource/gmd:linkage/gmd:URL) = $url and normalize-space(gmd:CI_OnlineResource/gmd:name/gco:CharacterString) = $name]" priority="2"/>
	<xsl:template match="geonet:*|gmd:onLine[normalize-space(gmd:CI_OnlineResource/gmd:linkage/gmd:URL) = $url and normalize-space(gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString) = 'WWW:DOWNLOAD-1.0-http--download']" priority="2"/>
</xsl:stylesheet>
