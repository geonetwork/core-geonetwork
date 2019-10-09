<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- ==================================================================== -->

  <xsl:import href="common.xsl"/>

  <!-- ==================================================================== -->
  <!-- === Thredds catalog harvesting node -->
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
    <topic>
      <xsl:value-of select="topic/value"/>
    </topic>
    <createServiceMd>
      <xsl:value-of select="createServiceMd/value"/>
    </createServiceMd>
    <outputSchema>
      <xsl:value-of select="outputSchema/value"/>
    </outputSchema>
    <datasetTitle>
      <xsl:value-of select="datasetTitle/value"/>
    </datasetTitle>
    <datasetAbstract>
      <xsl:value-of select="datasetAbstract/value"/>
    </datasetAbstract>
    <serviceCategory>
      <xsl:value-of select="serviceCategory/value"/>
    </serviceCategory>
    <datasetCategory>
      <xsl:value-of select="datasetCategory/value"/>
    </datasetCategory>
  </xsl:template>

  <!-- ==================================================================== -->

  <xsl:template match="*" mode="searches"/>

  <!-- ==================================================================== -->

</xsl:stylesheet>
