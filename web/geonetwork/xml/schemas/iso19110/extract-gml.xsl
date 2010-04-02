<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"  xmlns:gml="http://www.opengis.net/gml" >
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/" priority="2">
   		<gml:GeometryCollection/>
    </xsl:template>
</xsl:stylesheet>
