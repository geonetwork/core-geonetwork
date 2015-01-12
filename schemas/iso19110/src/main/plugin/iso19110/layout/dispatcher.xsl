<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:gfc="http://www.isotc211.org/2005/gfc" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:gn-fn-iso19110="http://geonetwork-opensource.org/xsl/functions/profiles/iso19110"
  xmlns:saxon="http://saxon.sf.net/" extension-element-prefixes="saxon"
  exclude-result-prefixes="#all">
  

  <!-- ISO 19110 layout delegates most of the work to the ISO19139 mode-->

  <xsl:include href="layout.xsl"/>
  <xsl:include href="evaluate.xsl"/>


  <!-- 
    Load the schema configuration for the editor.
      -->
  <xsl:template name="get-iso19110-configuration">
    <xsl:copy-of select="document('config-editor.xml')"/>
  </xsl:template>



  <!-- Dispatching to the profile mode -->
  <xsl:template name="dispatch-iso19110">
    <xsl:param name="base" as="node()"/>
    <xsl:apply-templates mode="mode-iso19110" select="$base"/>
  </xsl:template>
</xsl:stylesheet>
