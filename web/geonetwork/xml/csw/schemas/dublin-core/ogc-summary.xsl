<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:dct="http://purl.org/dc/terms/">

	<!-- ================================================================= -->

	<xsl:template match="simpledc">
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

			<xsl:for-each select="dct:spatial">
				<dct:spatial><xsl:value-of select="."/></dct:spatial>
			</xsl:for-each>

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
