<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd" version="1.0" xmlns:gml="http://www.opengis.net/gml" xmlns:gco="http://www.isotc211.org/2005/gco" >
    <xsl:output method="xml" indent="no"/>
    <xsl:template match="/" priority="2">
   		<gml:GeometryCollection>
        	<xsl:apply-templates/>
       	</gml:GeometryCollection>
    </xsl:template>
    <xsl:template match="*">
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="text()"/>
    
    <xsl:template match="gmd:EX_BoundingPolygon" priority="2">
        <xsl:copy-of select="./gml:Polygon"/>
	</xsl:template>
    <xsl:template match="geoBox" priority="2">
      <xsl:variable name="w" select="./westBL/text()"/>
      <xsl:variable name="e" select="./eastBL/text()"/>
      <xsl:variable name="n" select="./northBL/text()"/>
      <xsl:variable name="s" select="./southBL/text()"/>
			<xsl:if test="$w!='' and $e!='' and $n!='' and $s!=''">			
        <gml:Polygon>
            <gml:exterior>
                <gml:LinearRing>
                    <gml:coordinates><xsl:value-of select="$w"/>,<xsl:value-of select="$n"/>, <xsl:value-of select="$e"/>,<xsl:value-of select="$n"/>, <xsl:value-of select="$e"/>,<xsl:value-of select="$s"/>, <xsl:value-of select="$w"/>,<xsl:value-of select="$s"/>, <xsl:value-of select="$w"/>,<xsl:value-of select="$n"/></gml:coordinates>
                </gml:LinearRing>
            </gml:exterior>
        </gml:Polygon>
			</xsl:if>
    </xsl:template>
</xsl:stylesheet>
