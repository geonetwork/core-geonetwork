<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"  xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:gml="http://www.opengis.net/gml" >
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/" priority="2">
   		<gml:GeometryCollection>
	      <xsl:variable name="coverage" select="/simpledc/dc:coverage"/>
				<xsl:variable name="n" select="substring-after($coverage,'North ')"/>
				<xsl:variable name="north" select="substring-before($n,',')"/>
				<xsl:variable name="s" select="substring-after($coverage,'South ')"/>
				<xsl:variable name="south" select="substring-before($s,',')"/>
				<xsl:variable name="e" select="substring-after($coverage,'East ')"/>
				<xsl:variable name="east" select="substring-before($e,',')"/>
				<xsl:variable name="w" select="substring-after($coverage,'West ')"/>
				<xsl:variable name="west" select="substring-before($w,'. ')"/>
				<xsl:if test="$w!='' and $e!='' and $n!='' and $s!=''">			
	        <gml:Polygon>
	            <gml:exterior>
	                <gml:LinearRing>
	                    <gml:coordinates><xsl:value-of select="$west"/>,<xsl:value-of select="$north"/>, <xsl:value-of select="$east"/>,<xsl:value-of select="$north"/>, <xsl:value-of select="$east"/>,<xsl:value-of select="$south"/>, <xsl:value-of select="$west"/>,<xsl:value-of select="$south"/>, <xsl:value-of select="$west"/>,<xsl:value-of select="$north"/></gml:coordinates>
	                </gml:LinearRing>
	            </gml:exterior>
	        </gml:Polygon>
				</xsl:if>
			</gml:GeometryCollection>
    </xsl:template>
</xsl:stylesheet>
