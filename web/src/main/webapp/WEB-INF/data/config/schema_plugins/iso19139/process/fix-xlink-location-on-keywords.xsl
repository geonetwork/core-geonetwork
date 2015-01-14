<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:gmd="http://www.isotc211.org/2005/gmd"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:geonet="http://www.fao.org/geonetwork"
        exclude-result-prefixes="#all">
    
    <!-- XLink were in some editor configuration located
     on the wrong element gmd:MD_Keywords. In that case,
     the XML document with the XLink was invalid and the same
     document with XLinks resolved was correct.

     This XSLT relocate the XLink on the correct one gmd:descriptiveKeywords.

    -->

    <!-- Match all invalid keywords elements -->
    <xsl:template match="gmd:descriptiveKeywords[gmd:MD_Keywords/@xlink:href]" priority="2">
        <!-- ... and relocate XLink from the child to the current one
        forgetting all children (which are resolved). -->
        <xsl:copy>
            <xsl:copy-of select="gmd:MD_Keywords/@xlink:*"/>
        </xsl:copy>
    </xsl:template>

    <!-- Do a copy of every nodes and attributes -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Always remove geonet:* elements. -->
    <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
