<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
>

  <xsl:output
    omit-xml-declaration="no"
    method="xml"
    indent="yes"
    encoding="UTF-8"/>

  <xsl:template match="/">
    <xsl:copy-of select="/root/gui/startupError/*"/>
  </xsl:template>

  <xsl:template mode="css" match="/"/>

</xsl:stylesheet>
