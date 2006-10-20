<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:csw="http://www.opengis.net/cat/csw"
										xmlns:dc ="http://purl.org/dc/elements/1.1/"
										xmlns:dct="http://purl.org/dc/terms/"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:gmd="http://www.isotc211.org/2005/gmd">

	<!-- ============================================================================= -->

	<xsl:template match="gmd:MD_Metadata">
		<csw:SummaryRecord>

			<!-- DataIdentification -->

			<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification">

				<xsl:for-each select="gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
					<dc:identifier><xsl:value-of select="."/></dc:identifier>
				</xsl:for-each>
	
				<xsl:for-each select="gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
					<dc:title><xsl:value-of select="."/></dc:title>
				</xsl:for-each>
	
				<xsl:for-each select="gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString">
					<dc:subject><xsl:value-of select="."/></dc:subject>
				</xsl:for-each>

				<xsl:for-each select="gmd:abstract/gco:CharacterString">
					<dct:abstract><xsl:value-of select="."/></dct:abstract>
				</xsl:for-each>

				<xsl:for-each select="gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date">
					<dct:modified><xsl:value-of select="."/></dct:modified>
				</xsl:for-each>

			</xsl:for-each>

			<!-- Type -->

			<xsl:for-each select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue">
				<dc:type><xsl:value-of select="."/></dc:type>
			</xsl:for-each>

			<!-- Distribution -->

			<xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution">
				<xsl:for-each select="gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString">
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
