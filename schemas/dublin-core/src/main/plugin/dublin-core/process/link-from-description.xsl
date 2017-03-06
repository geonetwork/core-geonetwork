<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to copy links from description to reference tag
-->
<xsl:stylesheet version="2.0"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:dct="http://purl.org/dc/terms/"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork">
	
	<!-- Parent metadata record UUID -->
	<xsl:param name="parentUuid"/>
	
	<xsl:template match="/simpledc">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:copy-of
				select="dc:*|dct:*"/>
			<xsl:if test="starts-with(dc:identifier, 'http')">				
			<dct:references>
				<xsl:value-of select="dc:identifier"/>
			</dct:references>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	
	<!-- Remove geonet:* elements. -->
	<xsl:template match="geonet:*" priority="2"/>
</xsl:stylesheet>