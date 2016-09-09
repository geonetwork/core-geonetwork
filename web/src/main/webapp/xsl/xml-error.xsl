<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                exclude-result-prefixes="xsl">

  <!-- if a portal service throws an exception then return the error id and
             message from the exception -->

  <xsl:template match="root">
    <error>
      <xsl:attribute name="id">
        <xsl:value-of select="error/@id"/>
      </xsl:attribute>
      <xsl:value-of select="error/message"/>
      <xsl:copy-of select="error/object"/>
    </error>
  </xsl:template>

</xsl:stylesheet>
