<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:txsl="http://www.w3.org/1999/XSL/Transform/target"
  xmlns:ctl="http://www.occamlab.com/ctl"
  xmlns:conf="java:com.occamlab.te.web.Config"
  version="2.0">

  <xsl:strip-space elements="*"/>
  <xsl:output indent="yes"/>
  <xsl:namespace-alias stylesheet-prefix="txsl" result-prefix="xsl"/>

  <xsl:template match="ctl:get-home">
    <txsl:value-of select="conf:getHome()"/>
  </xsl:template>

  <xsl:template match="ctl:get-users-dir">
    <txsl:value-of select="conf:getUsersDir()"/>
  </xsl:template>

  <xsl:template match="ctl:get-available-suites">
    <txsl:copy-of select="conf:getAvailableSuites()"/>
  </xsl:template>

</xsl:transform>

