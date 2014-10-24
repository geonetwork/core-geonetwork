<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<!-- ============================================================================================ -->
	
	<xsl:output indent="yes"/>
	
	<!-- ============================================================================================ -->

	<xsl:template match="metadata">
		<oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
						xmlns:dc   ="http://purl.org/dc/elements/1.1/"
						xmlns:xsi  ="http://www.w3.org/2001/XMLSchema-instance"
						xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">

			<dc:identifier><xsl:value-of select="/root/env/uuid"/></dc:identifier>
			<dc:date><xsl:value-of select="/root/env/changeDate"/></dc:date>
			
			<xsl:for-each select="idinfo/citation/citeinfo/title">
				<dc:title><xsl:value-of select="."/></dc:title>
			</xsl:for-each>

			<!-- subject -->

			<xsl:for-each select="idinfo/keywords/theme/themekey">
				<dc:subject><xsl:value-of select="."/></dc:subject>
			</xsl:for-each>

			<xsl:for-each select="idinfo/keywords/place/placekey">
				<dc:subject><xsl:value-of select="."/></dc:subject>
			</xsl:for-each>

			<xsl:for-each select="idinfo/keywords/stratum/stratkey">
				<dc:subject><xsl:value-of select="."/></dc:subject>
			</xsl:for-each>

			<xsl:for-each select="idinfo/keywords/temporal/tempkey">
				<dc:subject><xsl:value-of select="."/></dc:subject>
			</xsl:for-each>

			<!-- description -->

			<xsl:for-each select="idinfo/descript/abstract">
				<dc:description><xsl:value-of select="."/></dc:description>
			</xsl:for-each>

			<!-- bounding box -->

			<xsl:for-each select="idinfo/spdom/bounding">	
				<dc:coverage>
					<xsl:value-of select="concat('North ', northbc, ', ')"/>
					<xsl:value-of select="concat('South ', southbc, ', ')"/>
					<xsl:value-of select="concat('East ' , eastbc,  ', ')"/>
					<xsl:value-of select="concat('West ' , westbc,  '.')"/>
				</dc:coverage>
			</xsl:for-each>
		</oai_dc:dc>
	</xsl:template>

	<!-- ============================================================================================ -->
	
	<xsl:template match="*">
		<xsl:apply-templates select="*"/>
	</xsl:template>
	
	<!-- ============================================================================================ -->

</xsl:stylesheet>
