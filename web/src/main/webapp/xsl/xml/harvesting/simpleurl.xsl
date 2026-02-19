<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:import href="common.xsl"/>

  <xsl:template match="*" mode="site">
    <url>
      <xsl:value-of select="url/value"/>
    </url>
    <icon>
      <xsl:value-of select="icon/value"/>
    </icon>
    <loopElement>
      <xsl:value-of select="loopElement/value"/>
    </loopElement>
    <apiKeyHeader>
      <xsl:value-of select="apiKeyHeader/value"/>
    </apiKeyHeader>
    <apiKey>
      <xsl:value-of select="apiKey/value"/>
    </apiKey>
    <numberOfRecordPath>
      <xsl:value-of select="numberOfRecordPath/value"/>
    </numberOfRecordPath>
    <recordIdPath>
      <xsl:value-of select="recordIdPath/value"/>
    </recordIdPath>
    <recordIdPathMode>
      <xsl:choose>
        <xsl:when test="recordIdPathMode/value">
          <xsl:value-of select="recordIdPathMode/value" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>auto</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </recordIdPathMode>
    <pageSizeParam>
      <xsl:value-of select="pageSizeParam/value"/>
    </pageSizeParam>
    <pageFromParam>
      <xsl:value-of select="pageFromParam/value"/>
    </pageFromParam>
    <toISOConversion>
      <xsl:value-of select="toISOConversion/value"/>
    </toISOConversion>
  </xsl:template>

  <xsl:template match="*" mode="options"/>
</xsl:stylesheet>
