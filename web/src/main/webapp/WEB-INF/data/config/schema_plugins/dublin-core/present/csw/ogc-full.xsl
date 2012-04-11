<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
		xmlns:dc ="http://purl.org/dc/elements/1.1/"
		xmlns:ows="http://www.opengis.net/ows" 
		xmlns:geonet="http://www.fao.org/geonetwork">
	
	<xsl:param name="displayInfo"/>

	<xsl:template match="simpledc">
		<xsl:variable name="info" select="geonet:info"/>
		<csw:Record>
			<xsl:apply-templates select="*[name(.)!='geonet:info']"/>
			<xsl:apply-templates select="dc:coverage" mode="bbox"/>
			<!-- GeoNetwork elements added when resultType is equal to results_with_summary -->
			<xsl:if test="$displayInfo = 'true'">
				<xsl:copy-of select="$info"/>
			</xsl:if>
		</csw:Record>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- this separate match is needed because the BBOX must be the last field
        (after all dc:xxx and dct:xxx) -->

	<xsl:template match="dc:coverage" mode="bbox">
		<xsl:variable name="coverage" select="."/>
		<xsl:variable name="n" select="substring-after($coverage,'North ')"/>
		<xsl:variable name="north" select="substring-before($n,',')"/>
		<xsl:variable name="s" select="substring-after($coverage,'South ')"/>
		<xsl:variable name="south" select="substring-before($s,',')"/>
		<xsl:variable name="e" select="substring-after($coverage,'East ')"/>
		<xsl:variable name="east" select="substring-before($e,',')"/>
		<xsl:variable name="w" select="substring-after($coverage,'West ')"/>
		<xsl:variable name="west" select="substring-before($w,'. ')"/>

		<ows:BoundingBox crs="urn:x-ogc:def:crs:EPSG:6.11:4326">
			<ows:LowerCorner><xsl:value-of select="concat($south, ' ', $west)"/></ows:LowerCorner>
			<ows:UpperCorner><xsl:value-of select="concat($north, ' ', $east)"/></ows:UpperCorner>
		</ows:BoundingBox>
	</xsl:template>

</xsl:stylesheet>
