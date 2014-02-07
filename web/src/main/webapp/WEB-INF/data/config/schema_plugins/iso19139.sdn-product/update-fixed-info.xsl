<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:sdn-product="http://seadatanet.org/geonetwork" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes="#all">


	<xsl:template match="/root">
		<xsl:apply-templates select="gmd:MD_Metadata"/>
	</xsl:template>

	<xsl:template match="gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/gmd:MD_Identifier/gmd:code" priority="99">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<gco:CharacterString>
				<xsl:value-of select="/root/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString/text()" />
			</gco:CharacterString>
		</xsl:copy>
	</xsl:template>



	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
