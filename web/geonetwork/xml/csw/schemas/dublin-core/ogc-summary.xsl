<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:dct="http://purl.org/dc/terms/"
		xmlns:geonet="http://www.fao.org/geonetwork">
		
	<xsl:param name="displayInfo"/>
	
	<!-- ================================================================= -->

	<xsl:template match="simpledc">
		<xsl:variable name="info" select="geonet:info"/>
		<csw:SummaryRecord>

			<xsl:for-each select="dc:identifier">
				<dc:identifier><xsl:value-of select="."/></dc:identifier>
			</xsl:for-each>

<!-- Change for CSW 2.0.2 - title is mandatory -->
			<dc:title>
			<xsl:for-each select="dc:title">
				<xsl:value-of select="."/>
			</xsl:for-each>
			</dc:title>

			<xsl:for-each select="dc:type">
				<dc:type><xsl:value-of select="."/></dc:type>
			</xsl:for-each>

			<xsl:for-each select="dc:subject">
				<dc:subject><xsl:value-of select="."/></dc:subject>
			</xsl:for-each>

			<xsl:for-each select="dc:format">
				<dc:format><xsl:value-of select="."/></dc:format>
			</xsl:for-each>

			<xsl:for-each select="dc:relation">
				<dc:relation><xsl:value-of select="."/></dc:relation>
			</xsl:for-each>

			<xsl:for-each select="dct:modified">
				<dct:modified><xsl:value-of select="substring-before(.,'T')"/></dct:modified>
			</xsl:for-each>

<!--
			<xsl:for-each select="dct:modified">
				<dct:modified><xsl:value-of select="."/></dct:modified>
			</xsl:for-each>
-->
			<xsl:for-each select="dct:abstract">
				<dct:abstract><xsl:value-of select="."/></dct:abstract>
			</xsl:for-each>
			
			<xsl:for-each select="dc:creator">
				<dc:creator><xsl:value-of select="."/></dc:creator>
			</xsl:for-each>
			
			<xsl:for-each select="dc:contributor">
				<dc:contributor><xsl:value-of select="."/></dc:contributor>
			</xsl:for-each>
			
			<xsl:for-each select="dc:publisher">
				<dc:publisher><xsl:value-of select="."/></dc:publisher>
            </xsl:for-each>
			
			<xsl:for-each select="dc:source">
				<dc:source><xsl:value-of select="."/></dc:source>
			</xsl:for-each>
			
			<xsl:for-each select="dc:language">
				<dc:language><xsl:value-of select="."/></dc:language>
            </xsl:for-each>

			<xsl:for-each select="dct:spatial">
				<dct:spatial><xsl:value-of select="."/></dct:spatial>
			</xsl:for-each>
			
			<xsl:for-each select="dc:rights">
				<dc:rights><xsl:value-of select="."/></dc:rights>
            </xsl:for-each>
			
			<!-- GeoNetwork elements added when resultType is equal to results_with_summary -->
			<xsl:if test="$displayInfo = 'true'">
				<xsl:copy-of select="$info"/>
			</xsl:if>

		</csw:SummaryRecord>
	</xsl:template>

	<!-- ================================================================= -->

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->

</xsl:stylesheet>
