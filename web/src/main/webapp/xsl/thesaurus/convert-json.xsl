<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:saxon="http://saxon.sf.net/" xmlns:gmx="http://www.isotc211.org/2005/gmx"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:xlink="http://www.w3.org/1999/xlink" extension-element-prefixes="saxon">

	<xsl:output method="text"/>

	<!-- Default template to use (ISO19139 keyword by default). -->
	<xsl:variable name="defaultTpl" select="'to-iso19139-keyword'"/>

	<!-- TODO : use a global function -->

    <xsl:template match="/">
        <xsl:apply-templates select="root/descKeys/keyword"/>
    </xsl:template>
    <xsl:template match="keyword">
    {
        <xsl:apply-templates mode="loc" select="values/value"/>
    }
    </xsl:template>

    <xsl:template mode="loc" match="*">
        <xsl:variable name="lang" select="@language"/>
       "<xsl:value-of select="$lang"/>": {
            "label": "<xsl:value-of select="."/>",
            "definition": "<xsl:value-of select="../definitions/definition[@language = $lang]"/>"
        }<xsl:if test="position()!=last()">,</xsl:if>
    </xsl:template>
</xsl:stylesheet>
