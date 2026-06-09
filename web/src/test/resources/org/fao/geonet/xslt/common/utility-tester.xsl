<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <xsl:include href="../../../../../resources/xslt/common/utility-tpl.xsl"/>

  <xsl:template match="/">
    <results>
      <xsl:for-each select="emails/email">
        <xsl:call-template name="hyperlink-mailaddress">
          <xsl:with-param name="string" select="." />
        </xsl:call-template>
      </xsl:for-each>
    </results>
  </xsl:template>
</xsl:stylesheet>
