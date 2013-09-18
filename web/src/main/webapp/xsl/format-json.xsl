<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" 
	xmlns:gco="http://www.isotc211.org/2005/gco">

	<xsl:output method="text"/>

	<!-- Return an iso19139 representation of a contact 
	stored in the metadata catalogue. 
		
		TODO : return profil specific records.
	-->
	<xsl:template match="/">
{"name":"<xsl:value-of select="root/response/record/name"/>","version": "<xsl:value-of select="root/response/record/version"/>"}
	</xsl:template>
</xsl:stylesheet>
