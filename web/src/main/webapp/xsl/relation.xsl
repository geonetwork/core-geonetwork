<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  Create a simple XML tree for relation description.
  <relations>
    <relation type="related|services|children">
      + super-brief representation.
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork" 
  xmlns:exslt="http://exslt.org/common"
  exclude-result-prefixes="geonet exslt">

  <xsl:include href="utils.xsl"/>
  <xsl:include href="metadata.xsl"/>

  <xsl:template match="/">
    <relations>
      <xsl:apply-templates mode="relation" select="/root/relations/*"/>
    </relations>
  </xsl:template>

  <xsl:template mode="relation" match="related|services|datasets|children|parent|sources|fcats">
    <xsl:apply-templates mode="relation" select="response/*">
      <xsl:with-param name="type" select="name(.)"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Bypass summary elements -->
  <xsl:template mode="relation" match="summary" priority="99"/>

  <xsl:template mode="relation" match="*">
    <xsl:param name="type"/>

    <xsl:variable name="md">
      <xsl:apply-templates mode="brief" select="."/>
    </xsl:variable>
    <xsl:variable name="metadata" select="exslt:node-set($md)"/>

    <relation type="{$type}">
      <xsl:copy-of select="$metadata"/>
    </relation>
  </xsl:template>

</xsl:stylesheet>
