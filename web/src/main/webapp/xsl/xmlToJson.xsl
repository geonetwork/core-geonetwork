<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="text" indent="no" media-type="application/json"/>
  <xsl:include href="utils.xsl"/>
  <xsl:template match="/">{"totalCount":"<xsl:value-of select="/root/logentries/totalcount"/>","logentries":[<xsl:for-each select="/root/logentries/entry">{<xsl:for-each select="./*">
          <xsl:variable name="value">
              <xsl:call-template name="replaceString">
                  <xsl:with-param name="expr"        select="."/>
                  <xsl:with-param name="pattern"     select="'&quot;'"/>
                  <xsl:with-param name="replacement" select="'\&quot;'"/>
              </xsl:call-template>
          </xsl:variable>"<xsl:value-of select="name()"/>":"<xsl:value-of select="$value"/>"<xsl:if test="position()!=last()">,</xsl:if></xsl:for-each>}<xsl:if test="position()!=last()">,</xsl:if></xsl:for-each>]
}
  </xsl:template>
</xsl:stylesheet>