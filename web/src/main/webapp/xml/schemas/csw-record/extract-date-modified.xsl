<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dc="http://purl.org/dc/elements/1.1/"   
	xmlns:dct="http://purl.org/dc/terms/"
	xmlns:ows="http://www.opengis.net/ows"
	xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
	>

	<xsl:template match="csw:Record|csw:SummaryRecord">
		 <dateStamp><xsl:value-of select="dct:modified"/></dateStamp>
	</xsl:template>

</xsl:stylesheet>
