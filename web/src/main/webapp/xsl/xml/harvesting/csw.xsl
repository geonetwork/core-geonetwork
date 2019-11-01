<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:import href="common.xsl"/>

  <xsl:template match="*" mode="site">
    <capabilitiesUrl>
      <xsl:value-of select="capabUrl/value"/>
    </capabilitiesUrl>
    <icon>
      <xsl:value-of select="icon/value"/>
    </icon>
    <rejectDuplicateResource>
      <xsl:value-of select="rejectDuplicateResource/value"/>
    </rejectDuplicateResource>
    <hopCount>
      <xsl:value-of select="hopCount/value"/>
    </hopCount>
    <xpathFilter>
      <xsl:value-of select="xpathFilter/value"/>
    </xpathFilter>
    <xslfilter>
      <xsl:value-of select="xslfilter/value"/>
    </xslfilter>
    <queryScope>
      <xsl:value-of select="queryScope/value"/>
    </queryScope>
    <outputSchema>
      <xsl:value-of select="outputSchema/value"/>
    </outputSchema>
  </xsl:template>


  <xsl:template match="*" mode="options"/>


  <xsl:template match="*" mode="searches">
    <searches>
      <search>
        <xsl:apply-templates select="children"/>
      </search>
    </searches>
  </xsl:template>

  <xsl:template match="children">
    <xsl:copy-of select="search/children/child::*"/>
  </xsl:template>
</xsl:stylesheet>
