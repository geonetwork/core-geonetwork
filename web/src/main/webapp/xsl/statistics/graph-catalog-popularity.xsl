<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" />

	<xsl:template match="/">
    	<!--  the image map -->
    	<p>
    		<xsl:value-of select="root/gui/graphByCatalogPopularity/tooltipImageMap" disable-output-escaping="yes"/>
    	</p>

    	<!--  the image -->
    	<img Usemap="#graphPopuByCatalogImageMap">
			<xsl:attribute name="src">
				<xsl:value-of select="root/gui/graphByCatalogPopularity/popuByCatalogUrl"/>
			</xsl:attribute>
			<xsl:attribute name="width">
				<xsl:value-of select="root/gui/graphByCatalogPopularity/chartWidth"/>
			</xsl:attribute>
			<xsl:attribute name="height">
				<xsl:value-of select="root/gui/graphByCatalogPopularity/chartHeight"/>
			</xsl:attribute>
		</img>
    </xsl:template>
</xsl:stylesheet>
