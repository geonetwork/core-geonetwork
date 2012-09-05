<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <xsl:template name="metadata-fop-dublin-core">
    <xsl:param name="schema"/>
    
    <xsl:for-each select="*">
      <xsl:call-template name="blockElementFop">
        <xsl:with-param name="block">
          <xsl:apply-templates mode="elementFop" select=".">
            <xsl:with-param name="schema" select="$schema"/>
          </xsl:apply-templates>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>