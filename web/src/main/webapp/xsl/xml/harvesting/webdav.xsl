<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- ============================================================================================= -->

  <xsl:import href="common.xsl"/>

  <!-- ============================================================================================= -->
  <!-- === Web DAV harvesting node -->
  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="site">
    <url>
      <xsl:value-of select="url/value"/>
    </url>
    <icon>
      <xsl:value-of select="icon/value"/>
    </icon>
    <xslfilter>
      <xsl:value-of select="xslfilter"/>
    </xslfilter>
  </xsl:template>

  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="options">
    <validate>
      <xsl:value-of select="validate/value"/>
    </validate>
    <recurse>
      <xsl:value-of select="recurse/value"/>
    </recurse>
    <subtype>
      <xsl:value-of select="subtype/value"/>
    </subtype>
  </xsl:template>

  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="searches"/>

  <!-- ============================================================================================= -->

</xsl:stylesheet>
