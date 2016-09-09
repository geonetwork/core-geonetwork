<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:gfc="http://www.isotc211.org/2005/gfc" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <xsl:template match="/root">
    <xsl:if test="gfc:*">
      <xsl:apply-templates select="gfc:*"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()[name(self::*)!='geonet:info']"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
