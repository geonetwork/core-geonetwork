<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="#all">

  <!--
  Load the schema configuration for the editor.
  Same configuration as ISO19139 here. -->
  <xsl:template name="get-iso19139.sdn-product-configuration">
    <xsl:copy-of select="document('../../iso19139/layout/config-editor.xml')"/>
  </xsl:template>


  <xsl:template name="dispatch-iso19139.sdn-product">
    <xsl:param name="base" as="node()"/>
    <xsl:apply-templates mode="mode-iso19139.sdn-product" select="$base"/>
  </xsl:template>

  <xsl:template name="evaluate-iso19139.sdn-product">
    <xsl:param name="base" as="node()"/>
    <xsl:param name="in"/>

    <xsl:call-template name="evaluate-iso19139">
      <xsl:with-param name="base" select="$base"/>
      <xsl:with-param name="in" select="$in"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="evaluate-iso19139.sdn-product-boolean">
    <xsl:param name="base" as="node()"/>
    <xsl:param name="in"/>

    <xsl:call-template name="evaluate-iso19139-boolean">
      <xsl:with-param name="base" select="$base"/>
      <xsl:with-param name="in" select="$in"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Visit all XML tree recursively -->
  <xsl:template mode="mode-iso19139.sdn-product" match="*|@*">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:apply-templates mode="mode-iso19139" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="labels" select="$labels"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template name="get-iso19139.sdn-product-language">
    <xsl:call-template name="get-iso19139-language"/>
  </xsl:template>

  <xsl:template name="get-iso19139.sdn-product-other-languages">
    <xsl:call-template name="get-iso19139-other-languages"/>
  </xsl:template>

  <xsl:template name="get-iso19139.sdn-product-other-languages-as-json">
    <xsl:call-template name="get-iso19139-other-languages-as-json"/>
  </xsl:template>

  <xsl:template name="get-iso19139.sdn-product-extents-as-json">
    <xsl:call-template name="get-iso19139-extents-as-json"/>
  </xsl:template>

  <xsl:template name="get-iso19139.sdn-product-online-source-config">
    <xsl:param name="pattern"/>
    <xsl:call-template name="get-iso19139-online-source-config">
      <xsl:with-param name="pattern" select="$pattern"/>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>