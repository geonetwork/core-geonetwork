<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
										xmlns:dc ="http://purl.org/dc/elements/1.1/"
										xmlns:dct="http://purl.org/dc/terms/">

	<!-- ============================================================================= -->

	<xsl:template match="metadata">
		<csw:SummaryRecord>

			<xsl:for-each select="idinfo/citation/citeinfo/title/@cat_id">
				<dc:identifier><xsl:value-of select="."/></dc:identifier>
			</xsl:for-each>

			<xsl:for-each select="idinfo/citation/citeinfo/title">
				<dc:title><xsl:value-of select="."/></dc:title>
			</xsl:for-each>

			<xsl:for-each select="idinfo/keywords">
				<xsl:for-each select="theme | place | stratum | temporal">
					<dc:subject><xsl:value-of select="."/></dc:subject>
				</xsl:for-each>
			</xsl:for-each>

			<xsl:for-each select="idinfo/descript/abstract">
				<dct:abstract><xsl:value-of select="."/></dct:abstract>
			</xsl:for-each>

			<xsl:for-each select="metainfo/metd">
				<dct:modified><xsl:value-of select="."/></dct:modified>
			</xsl:for-each>

			<xsl:for-each select="spdoinfo">
				<dc:type><xsl:value-of select="."/></dc:type>
			</xsl:for-each>

			<xsl:for-each select="distinfo/distrib/stdorder/digform/digtinfo">
				<dc:format><xsl:value-of select="."/></dc:format>
			</xsl:for-each>

		</csw:SummaryRecord>
	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
