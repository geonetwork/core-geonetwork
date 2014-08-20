<?xml version="1.0" encoding="UTF-8"?>
<!--
    This processing allows changing the url prefix of any
    gmd:URL, gco:CharacterString and xlink:href elements 
    in iso19139 based metadata records.
    
    Parameters:
    * process=url-host-relocator (fixed value)
    * urlPrefix=http://localhost : url prefix to replace
    * newUrlPrefix=http://newhost.org : prefix to be replaced by.
    
    Calling the process using:
    http://localhost:8082/geonetwork/srv/eng/metadata.batch.processing?process=url-host-relocator&urlPrefix=http://localhost&newUrlPrefix=http://newhost.org
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:geonet="http://www.fao.org/geonetwork" 
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gmd="http://www.isotc211.org/2005/gmd" version="1.0">

    <xsl:param name="urlPrefix">http://localhost:8080/</xsl:param>
    <xsl:param name="newUrlPrefix">http://newhost.org/</xsl:param>

    <!-- Do a copy of every nodes and attributes -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Remove geonet:* elements. -->
    <xsl:template match="geonet:*" priority="2"/>

    <!-- Replace in gmd:URL (uploaded document) 
        or gco:CharacterString (eg. resource identifier may be based on host name) -->
    <xsl:template match="gmd:URL[starts-with(text(), $urlPrefix)]|
        gco:CharacterString[starts-with(text(), $urlPrefix)]" priority="2">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:value-of select="concat($newUrlPrefix, substring-after(., $urlPrefix))"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- Replace in XLinks. -->
    <xsl:template match="@xlink:href[starts-with(., $urlPrefix)]" priority="2">
        <xsl:attribute name="href" namespace="http://www.w3.org/1999/xlink">
            <xsl:value-of select="concat($newUrlPrefix, substring-after(., $urlPrefix))"/>
        </xsl:attribute>
    </xsl:template>
</xsl:stylesheet>
