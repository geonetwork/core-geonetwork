<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
						xmlns:gco="http://www.isotc211.org/2005/gco"
						xmlns:gmd="http://www.isotc211.org/2005/gmd">

	<!-- ============================================================================================ -->
	
	<xsl:output indent="yes"/>
	
	<!-- ============================================================================================ -->
	
	<xsl:template match="simpledc">
		<oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
						xmlns:dc   ="http://purl.org/dc/elements/1.1/"
						xmlns:xsi  ="http://www.w3.org/2001/XMLSchema-instance"
						xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">

			<xsl:copy-of select="dc:title"/>
			<xsl:copy-of select="dc:creator"/>
			<xsl:copy-of select="dc:subject"/>
			<xsl:copy-of select="dc:description"/>
			<xsl:copy-of select="dc:publisher"/>
			<xsl:copy-of select="dc:contributor"/>
			<xsl:copy-of select="dc:date"/>
			<xsl:copy-of select="dc:type"/>
			<xsl:copy-of select="dc:format"/>
			<xsl:copy-of select="dc:identifier"/>
			<xsl:copy-of select="dc:source"/>
			<xsl:copy-of select="dc:language"/>
			<xsl:copy-of select="dc:relation"/>
			<xsl:copy-of select="dc:coverage"/>
			<xsl:copy-of select="dc:rights"/>
		</oai_dc:dc>
	</xsl:template>

	<!-- ============================================================================================ -->
	
	<xsl:template match="*">
		<xsl:apply-templates select="*"/>
	</xsl:template>
	
	<!-- ============================================================================================ -->
	
</xsl:stylesheet>
