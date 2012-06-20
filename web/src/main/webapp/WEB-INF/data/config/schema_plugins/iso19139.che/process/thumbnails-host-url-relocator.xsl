<?xml version="1.0" encoding="UTF-8"?>
<!--
    This processing allows changing the url prefix of any
    gmd:URL elements in iso19139 based metadata records.
    
    Parameters:
    * process=thumbnails-host-url-relocator (fixed value)
    * urlPrefix=http://localhost : url prefix to replace
    * newUrlPrefix=http://newhost.org : prefix to be replaced by.
    
    Calling the process using:
    http://localhost:8082/geonetwork/srv/eng/metadata.batch.processing?process=thumbnails-host-url-relocator&urlPrefix=http://localhost&newUrlPrefix=http://newhost.org
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" version="1.0">

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

    <!-- Replace url prefix. -->
    <xsl:template match="gmd:URL" priority="2">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:choose>
                <xsl:when test="starts-with(., $urlPrefix)">
                    <xsl:value-of select="$newUrlPrefix"/><xsl:value-of select="substring-after(., $urlPrefix)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="."/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
