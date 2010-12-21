<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" />

	<xsl:template match="/">
    	<!--  the image map -->
    	<p>
    		<xsl:value-of select="root/gui/graphByGroupPopularity/tooltipImageMap" disable-output-escaping="yes"/>
    	</p>

    	<!--  the image -->
    	<img Usemap="#graphPopuByGroupImageMap">
			<xsl:attribute name="src">
				<xsl:value-of select="root/gui/graphByGroupPopularity/popuByGroupUrl"/>
			</xsl:attribute>
			<xsl:attribute name="width">
				<xsl:value-of select="root/gui/graphByGroupPopularity/chartWidth"/>
			</xsl:attribute>
			<xsl:attribute name="height">
				<xsl:value-of select="root/gui/graphByGroupPopularity/chartHeight"/>
			</xsl:attribute>
		</img>
    </xsl:template>
</xsl:stylesheet>
