<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output  indent="yes" method="text" encoding="UTF-8" media-type="application/json"/>

  <xsl:include href="base-variables.xsl"/>

  <xsl:template match="/">{"error": {
    <xsl:apply-templates mode="error" select="root/error/*"/>
    }
  }</xsl:template>

  <xsl:template mode="error" match="*"/>
  <xsl:template mode="error" match="at">
    "<xsl:value-of select="name(.)"/>": "<xsl:value-of select="concat(@class, ' ', @file, ' line ', @line, ' #', @method)"/>"
    <xsl:if test="following-sibling::at">,</xsl:if>
  </xsl:template>
  <xsl:template mode="error" match="message|class|stack|request">
    "<xsl:value-of select="name(.)"/>": 
    <xsl:choose>
      <xsl:when test="*">
        {<xsl:apply-templates mode="error" select="*"/>}
      </xsl:when>
      <xsl:otherwise>
        "<xsl:value-of select="."/>"
      </xsl:otherwise>
    </xsl:choose>
    <!-- Last element of interest for error is request. Next one is the record. -->
    <xsl:if test="name() != 'request'">,</xsl:if>
  </xsl:template>
</xsl:stylesheet>
