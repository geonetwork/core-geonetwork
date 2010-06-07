<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
										xmlns:dc ="http://purl.org/dc/elements/1.1/"
										xmlns:dct="http://purl.org/dc/terms/">

	<!-- ============================================================================= -->

	<xsl:template match="Metadata">
		<csw:SummaryRecord>

			<xsl:for-each select="mdFileID">
				<dc:identifier><xsl:value-of select="."/></dc:identifier>
			</xsl:for-each>

			<!-- DataIdentification -->

			<xsl:for-each select="dataIdInfo">

				<xsl:for-each select="idCitation/resTitle">
					<dc:title><xsl:value-of select="."/></dc:title>
				</xsl:for-each>
	
				<xsl:for-each select="descKeys/keyword">
					<dc:subject><xsl:value-of select="."/></dc:subject>
				</xsl:for-each>

				<xsl:for-each select="idAbs">
					<dct:abstract><xsl:value-of select="."/></dct:abstract>
				</xsl:for-each>

				<xsl:for-each select="idCitation/resEdDate">
					<dct:modified><xsl:value-of select="."/></dct:modified>
				</xsl:for-each>

			</xsl:for-each>
			
			<!-- Type -->
			
			<xsl:for-each select="mdHrLv/ScopeCd/@value">
				<dc:type><xsl:value-of select="."/></dc:type>
			</xsl:for-each>

			<!-- Distribution -->

			<xsl:for-each select="distInfo">
				<xsl:for-each select="distributor/distorFormat/formatName">
					<dc:format><xsl:value-of select="."/></dc:format>
				</xsl:for-each>
			</xsl:for-each>

		</csw:SummaryRecord>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*">
		<xsl:apply-templates select="*"/>
	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>