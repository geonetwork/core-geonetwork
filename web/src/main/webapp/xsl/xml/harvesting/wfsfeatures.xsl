<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- ==================================================================== -->

  <xsl:import href="common.xsl"/>

  <!-- ==================================================================== -->
  <!-- === Metadata fragments harvesting node -->
  <!-- ==================================================================== -->

  <xsl:template match="*" mode="site">
    <url>
      <xsl:value-of select="url/value"/>
    </url>
    <icon>
      <xsl:value-of select="icon/value"/>
    </icon>
  </xsl:template>

  <!-- ==================================================================== -->

  <xsl:template match="*" mode="options">
    <lang>
      <xsl:value-of select="lang/value"/>
    </lang>
    <query>
      <xsl:value-of select="query/value"/>
    </query>
    <outputSchema>
      <xsl:value-of select="outputSchema/value"/>
    </outputSchema>
    <stylesheet>
      <xsl:value-of select="stylesheet/value"/>
    </stylesheet>
    <streamFeatures>
      <xsl:value-of select="streamFeatures"/>
    </streamFeatures>
    <createSubtemplates>
      <xsl:value-of select="createSubtemplates"/>
    </createSubtemplates>
    <templateId>
      <xsl:value-of select="templateId/value"/>
    </templateId>
    <recordsCategory>
      <xsl:value-of select="recordsCategory/value"/>
    </recordsCategory>
  </xsl:template>

  <!-- ==================================================================== -->

  <xsl:template match="*" mode="searches"/>

  <!-- ==================================================================== -->

</xsl:stylesheet>
