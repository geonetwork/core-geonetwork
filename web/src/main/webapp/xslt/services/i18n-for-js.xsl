<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- 
    Copy all strings from i18n having a js attribute to be used by the client side.
  -->
  <xsl:template match="/">
    <lang>
      <xsl:apply-templates select="/root/gui/i18n/*[@js]"/>
    </lang>
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:copy><xsl:value-of select="text()"/></xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
