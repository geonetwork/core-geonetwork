<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
		xmlns:dc ="http://purl.org/dc/elements/1.1/"
		xmlns:geonet="http://www.fao.org/geonetwork"		
		xmlns:ows="http://www.opengis.net/ows" >

	<xsl:param name="displayInfo"/>
	
	<xsl:template match="csw:Record">		
		<csw:Record>
			<xsl:apply-templates select="*[name(.)!='geonet:info']"/>			
			
			<xsl:if test="$displayInfo = 'true'">				
				<xsl:copy-of select="geonet:info"/>
			</xsl:if>
		</csw:Record>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
