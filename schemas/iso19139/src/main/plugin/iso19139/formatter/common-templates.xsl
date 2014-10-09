<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:srv="http://www.isotc211.org/2005/srv"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gml="http://www.opengis.net/gml"
    xmlns:xslutil="java:org.fao.geonet.util.XslUtil"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gnf="http://www.fao.org/geonetwork/functions">

    <xsl:template mode="iso19139" match="*[gco:CharacterString or ./gmd:PT_FreeText//gmd:LocalisedCharacterString]" priority="1">
        <xsl:copy-of select="gnf:translatedStringWidget(
            name(), ., $root
        )" />
    </xsl:template>

    <xsl:template mode="iso19139" match="text()" priority="0">
        <xsl:value-of select="." />
    </xsl:template>

</xsl:stylesheet>
