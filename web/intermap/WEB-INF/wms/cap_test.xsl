<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
	<xsl:apply-templates select="//Layer[Name=/root/serviceName]"/>
</xsl:template>

<xsl:template match="Layer">
	<xsl:copy>
		<xsl:apply-templates mode="copy"/>
		<xsl:apply-templates select="ancestor-or-self::Layer/LatLonBoundingBox[1]" mode="copy" />
		<xsl:apply-templates select="ancestor::Layer/Style" mode="copy" />
		<xsl:apply-templates select="ancestor::Layer/Extent" mode="copy" />
		<xsl:apply-templates select="ancestor::Layer/Dimension" mode="copy" /> 
	</xsl:copy>
</xsl:template>

<xsl:template match="@*|node()" mode="copy">
	<xsl:copy>
		<xsl:apply-templates select="@*|node()" mode="copy" />
	</xsl:copy>
</xsl:template>

</xsl:stylesheet>
