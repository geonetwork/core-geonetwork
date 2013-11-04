<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:output method="text"/>
    
    
    <xsl:template match="/">
        {
        "id": "<xsl:value-of select="root/error/@id"/>",
        "class": "<xsl:value-of select="root/error/class"/>",
        "service": "<xsl:value-of select="root/error/request/service"/>",
        "message": "<xsl:value-of select="normalize-space(translate(root/error/message, '&quot;', ''))"/>",
        "stack": "<xsl:apply-templates select="root/error/stack"/>"
        }
	</xsl:template>
    
    <xsl:template match="stack">
        <xsl:apply-templates select="*"/>
    </xsl:template>
    <xsl:template match="at">
        <xsl:value-of select="concat(@class, ' ', @file, '#', @line, ' ', @method)"/>
    </xsl:template>
    <xsl:template match="skip">
        <xsl:text>...</xsl:text>
    </xsl:template>
    
</xsl:stylesheet>