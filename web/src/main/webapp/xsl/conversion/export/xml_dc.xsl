<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0">

  <xsl:template match="/root">
    <xsl:apply-templates select="simpledc"/>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()[name(self::*)!='geonet:info']"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
