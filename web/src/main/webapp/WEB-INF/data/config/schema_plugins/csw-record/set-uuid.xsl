<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
					 xmlns:dc ="http://purl.org/dc/elements/1.1/"
  					 xmlns:dct="http://purl.org/dc/terms/"
  					 xmlns:ows="http://www.opengis.net/ows"
  					 xmlns:csw="http://www.opengis.net/cat/csw/2.0.2">

	<!-- ================================================================= -->
	
	<xsl:template match="/root">
		 <xsl:apply-templates select="csw:Record"/>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="csw:Record">
		<xsl:copy>
			<xsl:apply-templates select="dc:title"/>
			<xsl:apply-templates select="dc:creator"/>
			<xsl:apply-templates select="dc:subject"/>
			<xsl:apply-templates select="dc:description"/>
			<xsl:apply-templates select="dc:publisher"/>
			<xsl:apply-templates select="dc:contributor"/>
			<xsl:apply-templates select="dc:date"/>
			<xsl:apply-templates select="dc:type"/>
			<xsl:apply-templates select="dc:format"/>
			
			<dc:identifier><xsl:value-of select="/root/env/uuid"/></dc:identifier>
			
			<xsl:apply-templates select="dc:source"/>
			<xsl:apply-templates select="dc:language"/>
			<xsl:apply-templates select="dc:relation"/>
			<xsl:apply-templates select="dc:coverage"/>
			<xsl:apply-templates select="dc:rights"/>

			<xsl:apply-templates select="dct:abstract"/>
			<xsl:apply-templates select="dct:modified"/>
			<xsl:apply-templates select="dct:spatial"/>
			<xsl:apply-templates select="dct:audience"/>
			<xsl:apply-templates select="dct:accrualMethod"/>
			<xsl:apply-templates select="dct:accrualPeriodicity"/>
			<xsl:apply-templates select="dct:accrualPolicy"/>
			<xsl:apply-templates select="dct:instructionalMethod"/>
			<xsl:apply-templates select="dct:provenance"/>
			<xsl:apply-templates select="dct:rightsHolder"/>
			<xsl:apply-templates select="ows:BoundingBox"/>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->

	<xsl:template match="@*|node()">
		 <xsl:copy>
			  <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->

</xsl:stylesheet>
