<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" />

	<xsl:template match="/">
		<!--  debug info 
		<xsl:value-of select="root/gui/graphByDate/dateFrom" />&#151;
    	<xsl:value-of select="root/gui/graphByDate/dateTo"/>&#151;
    	<xsl:value-of select="root/gui/graphByDate/graphicType"/>&#151;
    	<xsl:value-of select="root/gui/graphByDate/chartWidth"/>&#151;
    	<xsl:value-of select="root/gui/graphByDate/chartHeight"/>&#151;
    	<xsl:value-of select="root/gui/graphByDate/message"/>&#151;
    	<xsl:value-of select="root/gui/graphByDate/graphByDateUrl"/>
    	-->
    	<!--  the image map -->
    	<p>
    		<xsl:value-of select="root/gui/graphByDate/tooltipImageMap" disable-output-escaping="yes"/>
    	</p>

    	<!--  the image -->
    	<img Usemap="#graphByDateImageMap">
			<xsl:attribute name="src">
				<xsl:value-of select="root/gui/graphByDate/graphByDateUrl"/>
			</xsl:attribute>
			<xsl:attribute name="width">
				<xsl:value-of select="root/gui/graphByDate/chartWidth"/>
			</xsl:attribute>
			<xsl:attribute name="height">
				<xsl:value-of select="root/gui/graphByDate/chartHeight"/>
			</xsl:attribute>
		</img>
    </xsl:template>
</xsl:stylesheet>
