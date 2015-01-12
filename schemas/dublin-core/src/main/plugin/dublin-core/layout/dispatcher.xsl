<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:gn="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all">

  <xsl:include href="layout.xsl"/>
  <xsl:include href="evaluate.xsl"/>

  <!-- 
    Load the schema configuration for the editor.
      -->
  <xsl:template name="get-dublin-core-configuration">
    <xsl:copy-of select="document('config-editor.xml')"/>
  </xsl:template>


  <!-- Dispatching to the profile mode  -->
  <xsl:template name="dispatch-dublin-core">
    <xsl:param name="base" as="node()"/>
    <xsl:apply-templates mode="mode-dublin-core" select="$base"/>
  </xsl:template>

</xsl:stylesheet>
