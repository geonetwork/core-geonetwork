<?xml version="1.0" encoding="UTF-8"?>
<!--
    Processing steps are :
    * update gmd:language element
    * add gco:nilReason attribute when procotol is DB, FILE
    
    Calling the process using:
    * Log in using a browser
    * Search
    http://localhost:8080/geonetwork/srv/fre/q?
    return: <response from="1" to="3828" selected="0" ...
    * Select all
    http://localhost:8080/geonetwork/srv/fre/metadata.select?id=0&selected=add-all
    return: <response>
        <Selected>3828</Selected>
    </response>
    * Transform 
    http://localhost:8080/geonetwork/srv/fre/metadata.batch.processing?process=migrate-sextantv5
    
    Apply the process to templates
    http://localhost:8080/geonetwork/srv/eng/q?_isTemplate=y
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:geonet="http://www.fao.org/geonetwork" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:exslt="http://exslt.org/common" version="2.0"
    exclude-result-prefixes="exslt">

    <!-- Do a copy of every nodes and attributes -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Remove geonet:* elements. -->
    <xsl:template match="geonet:*" priority="2"/>

    <!-- When protocol is DB, FILE, COPYFILE a gco:nilReason attribute is
    added with value withhelf in order to hide those information to some
    kind of users. -->
    <xsl:template match="gmd:linkage" priority="2">
        <xsl:choose>
            <xsl:when
                test="
                contains(lower-case(string(../gmd:protocol/gco:CharacterString)), 'db') or 
                contains(lower-case(string(../gmd:protocol/gco:CharacterString)), 'copyfile') or 
                contains(lower-case(string(../gmd:protocol/gco:CharacterString)), 'file')">
                <gmd:linkage gco:nilReason="withheld">
                    <xsl:apply-templates select="@*"/>
                    <xsl:copy-of select="./*"/>
                </gmd:linkage>
            </xsl:when>
            <xsl:otherwise>
                <gmd:linkage>
                    <xsl:apply-templates select="@*"/>
                    <xsl:copy-of select="./*"/>
                </gmd:linkage>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Store language in gmd:LanguageCode instead of gco:CharacterString (INSPIRE requirements). -->
    <xsl:template match="gmd:language[gco:CharacterString]" priority="2">
        <xsl:copy>
            <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/"
                codeListValue="{gco:CharacterString}"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
