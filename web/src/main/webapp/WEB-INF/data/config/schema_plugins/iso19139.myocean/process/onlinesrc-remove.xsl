<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to remove a reference to a online resource.
-->
<xsl:stylesheet version="2.0" xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:param name="url"/>
	<xsl:param name="name" />
	<xsl:param name="protocol" />
	
	<!-- Do a copy of every nodes and attributes -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- Remove geonet:* elements. -->
	<xsl:template match="geonet:*|gmd:distributor[gmd:MD_Distributor//gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL = $url 
						and gmd:MD_Distributor//gmd:onLine/gmd:CI_OnlineResource/gmd:name/gco:CharacterString = $name
						and gmd:MD_Distributor//gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString = $protocol]" priority="2"/>
						
	<xsl:template match="geonet:*|gmd:distributor[gmd:MD_Distributor//gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL = $url 
						and gmd:MD_Distributor//gmd:onLine/gmd:CI_OnlineResource/gmd:name/@gco:nilReason = 'missing'
						and gmd:MD_Distributor//gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString = $protocol]" priority="2"/>
						
	<xsl:template match="geonet:*|gmd:distributionInfo[count(//gmd:distributor) = 1]" priority="2"/>
</xsl:stylesheet>
