<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="text()" priority="2">
  </xsl:template>
  <xsl:template match="/root/metadata" priority="1">
    <xsl:copy-of select="./*"></xsl:copy-of>
  </xsl:template>

</xsl:stylesheet>
