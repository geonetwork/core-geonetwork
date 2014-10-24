<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
	xmlns:ows="http://www.opengis.net/ows"
	xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
	xmlns:dc="http://purl.org/dc/elements/1.1/" 
	xmlns:gml="http://www.opengis.net/gml" >
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/" priority="2">
   		<gml:GeometryCollection>
   			<!-- csw:Record contains ows:BoundingBox element.
   				Example:
   				<ows:BoundingBox crs="urn:x-ogc:def:crs:EPSG:6.11:4326">
   				  <ows:LowerCorner>47.595 -4.097</ows:LowerCorner>
   				  <ows:UpperCorner>51.217 0.889</ows:UpperCorner>
   				</ows:BoundingBox>
   				
   				TODO : handle CRS
   			-->
   			<xsl:variable name="lc" select="/csw:Record/ows:BoundingBox/ows:LowerCorner"/>
   			<xsl:variable name="uc" select="/csw:Record/ows:BoundingBox/ows:UpperCorner"/>
				<xsl:variable name="n" select="substring-after($uc,' ')"/>
				<xsl:variable name="s" select="substring-after($lc,' ')"/>
   			<xsl:variable name="e" select="substring-before($uc,' ')"/>
   			<xsl:variable name="w" select="substring-before($lc,' ')"/>
				<xsl:if test="$w!='' and $e!='' and $n!='' and $s!=''">			
	        <gml:Polygon>
	            <gml:exterior>
	                <gml:LinearRing>
	                    <gml:coordinates><xsl:value-of select="$w"/>,<xsl:value-of select="$n"/>, <xsl:value-of select="$e"/>,<xsl:value-of select="$n"/>, <xsl:value-of select="$e"/>,<xsl:value-of select="$s"/>, <xsl:value-of select="$w"/>,<xsl:value-of select="$s"/>, <xsl:value-of select="$w"/>,<xsl:value-of select="$n"/></gml:coordinates>
	                </gml:LinearRing>
	            </gml:exterior>
	        </gml:Polygon>
				</xsl:if>
      </gml:GeometryCollection>
    </xsl:template>
</xsl:stylesheet>
