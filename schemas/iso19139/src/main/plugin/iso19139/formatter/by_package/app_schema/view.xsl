<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:srv="http://www.isotc211.org/2005/srv"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gml="http://www.opengis.net/gml"
    xmlns:xslutil="java:org.fao.geonet.util.XslUtil"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gnf="http://www.fao.org/geonetwork/functions"
    exclude-result-prefixes="#all">

    <xsl:include href="../../common-templates.xsl" />

    <xsl:output omit-xml-declaration="yes" method="xml" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
        doctype-system="http://www.w3.org/TR/html4/loose.dtd" indent="yes" encoding="UTF-8" />

    <xsl:variable name="root" select="/" />
    <xsl:template match="/">
        <div>
            <xsl:apply-templates mode="iso19139" select="gnf:metadataRoot(root())/gmd:applicationSchemaInfo"/>
        </div>
    </xsl:template>

</xsl:stylesheet>
