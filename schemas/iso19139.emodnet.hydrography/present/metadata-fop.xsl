<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <!-- Redirect to iso19139 default layout -->
  <xsl:template name="metadata-fop-iso19139.emodnet.hydrography">
    <xsl:param name="schema"/>
    
    <xsl:call-template name="metadata-fop-iso19139">
      <xsl:with-param name="schema" select="'iso19139'"/>
    </xsl:call-template>
  </xsl:template>
  
</xsl:stylesheet>