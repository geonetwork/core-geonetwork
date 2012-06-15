<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:che="http://www.geocat.ch/2008/che" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gfc="http://www.isotc211.org/2005/gfc"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gml="http://www.opengis.net/gml" xmlns:gmi="http://www.isotc211.org/2005/gmi" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="che" xmlns:srv="http://www.isotc211.org/2005/srv" >
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

	<!-- Some default values -->
	<xsl:template match="gmd:metadataStandardName">
		<gmd:metadataStandardName>
			<gco:CharacterString>ISO 19115/19119</gco:CharacterString>
		</gmd:metadataStandardName>
	</xsl:template>

	<xsl:template match="gmd:metadataStandardVersion">
		<gmd:metadataStandardVersion>
			<gco:CharacterString/>
		</gmd:metadataStandardVersion>
	</xsl:template>

	<!-- All profil specific elements should be bypassed -->
	<xsl:template match="che:*[not(@gco:isoType)]" priority="2"/>

	<!-- All gco:isoType should be mapped to iso19139 elements -->
	<xsl:template match="*[@gco:isoType]">
		<xsl:element name="{@gco:isoType}">
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>

	<!--Copy -->
	<xsl:template match="*|@*">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- Remove isoType and xsi:type attribute in iso19139 -->
	<xsl:template match="@gco:isoType|@xsi:type[.='che:PT_FreeURL_PropertyType']" priority="2"/>
	
</xsl:stylesheet>
