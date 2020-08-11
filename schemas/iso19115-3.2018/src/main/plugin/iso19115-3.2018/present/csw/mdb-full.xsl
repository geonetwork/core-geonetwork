<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gn="http://www.fao.org/geonetwork"
  exclude-result-prefixes="#all">

  <xsl:template match="@*|node()[name(.)!='gn:info']">
    <xsl:variable name="info" select="gn:info"/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()[name(.)!='gn:info']"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
