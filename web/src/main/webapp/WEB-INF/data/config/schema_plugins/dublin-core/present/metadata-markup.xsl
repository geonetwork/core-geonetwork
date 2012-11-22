<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:dc = "http://purl.org/dc/elements/1.1/"
	xmlns:dct = "http://purl.org/dc/terms/"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:ows="http://www.opengis.net/ows"
	xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
	
	<xsl:template mode="permitMarkup" match="dc:description">
		<xsl:value-of select="true()" />
	</xsl:template>
</xsl:stylesheet>