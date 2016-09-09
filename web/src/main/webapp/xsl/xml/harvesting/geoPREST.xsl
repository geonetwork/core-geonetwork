<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- ============================================================================================= -->

  <xsl:import href="common.xsl"/>

  <!-- ============================================================================================= -->
  <!-- === GeoPortal REST harvesting node -->
  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="site">
    <baseUrl>
      <xsl:value-of select="baseUrl/value"/>
    </baseUrl>
    <icon>
      <xsl:value-of select="icon/value"/>
    </icon>
  </xsl:template>

  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="options"/>

  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="searches">
    <searches>
      <xsl:for-each select="children/search">
        <search>
          <freeText>
            <xsl:value-of select="children/freeText/value"/>
          </freeText>
        </search>
      </xsl:for-each>
    </searches>
  </xsl:template>

  <!-- ============================================================================================= -->

</xsl:stylesheet>
