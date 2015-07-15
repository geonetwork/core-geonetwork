<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-dublin-core="http://geonetwork-opensource.org/xsl/functions/profiles/dublin-core"
                exclude-result-prefixes="#all">
  <xsl:import href="../../iso19139/layout/utility-vacuum.xsl"/>

  <!-- Vacuum utility rely on ISO19139 one. -->
  <xsl:function name="gn-fn-dublin-core:vacuum" as="node()">
    <xsl:param name="metadata" as="node()"/>
    <xsl:for-each select="$metadata/*">
      <xsl:apply-templates mode="vacuum-iso19139"
                           select="."/>
    </xsl:for-each>
  </xsl:function>

</xsl:stylesheet>
