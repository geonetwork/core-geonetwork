<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  extension-element-prefixes="saxon"
  exclude-result-prefixes="#all">

  <xsl:import href="../../common/base-variables-metadata.xsl"/>
  
  <xsl:import href="../../common/functions-metadata.xsl"/>
<!-- Add template utility-->
  
  <xsl:import href="../../common/profiles-loader.xsl"/>

  <xsl:import href="../form-builder.xsl"/>
  
  <xsl:output omit-xml-declaration="yes" method="html" doctype-public="html" indent="yes"
    encoding="UTF-8"/>
  
  <xsl:template match="/">
    <article class="gn-metadata-view">
      <xsl:call-template name="scroll-spy-nav-bar"/>
      
      <!-- Dispatch to profile mode -->
      <xsl:variable name="profileTemplate" select="concat('render-',$schema)"/>
      <saxon:call-template name="{$profileTemplate}">
        <xsl:with-param name="base" select="$metadata"/>
      </saxon:call-template>
      
    </article>
  </xsl:template>
  
</xsl:stylesheet>
