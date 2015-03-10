<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template name="replaceString">
    <xsl:param name="expr"/>
    <xsl:param name="pattern"/>
    <xsl:param name="replacement"/>

    <xsl:variable name="first" select="substring-before($expr,$pattern)"/>
    <xsl:choose>
      <xsl:when test="$first or starts-with($expr, $pattern)">
        <xsl:value-of select="$first"/>
        <xsl:value-of select="$replacement"/>
        <xsl:call-template name="replaceString">
          <xsl:with-param name="expr"        select="substring-after($expr,$pattern)"/>
          <xsl:with-param name="pattern"     select="$pattern"/>
          <xsl:with-param name="replacement" select="$replacement"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$expr"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>