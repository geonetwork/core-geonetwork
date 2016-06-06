<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
>
  <xsl:output method="text" indent="no" media-type="application/json"/>

  <xsl:template match="/response/feedbackreceptorsuccess">{success:true}</xsl:template>

  <xsl:template match="@*|node()">
    <xsl:apply-templates select="@*|node()"/>
  </xsl:template>

</xsl:stylesheet>
