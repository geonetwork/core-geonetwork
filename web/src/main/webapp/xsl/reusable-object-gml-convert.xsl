<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"  xmlns:gml="http://www.opengis.net/gml" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="gml:Ring" priority="2">
      <gml:LinearRing>
            <xsl:copy-of select=".//gml:coordinates"/>
      </gml:LinearRing>
    </xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
	
</xsl:stylesheet>