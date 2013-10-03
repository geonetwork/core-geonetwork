<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  extension-element-prefixes="saxon"
  exclude-result-prefixes="#all">

  <xsl:output omit-xml-declaration="yes" method="html" doctype-public="html" indent="yes"
    encoding="UTF-8"/>

  <xsl:include href="../../common/base-variables-metadata-editor.xsl"/>
  
  <xsl:include href="../../common/functions-metadata.xsl"/>

  <xsl:include href="../../common/profiles-loader.xsl"/>

  <xsl:include href="../../layout-core.xsl"/>

  <xsl:template match="/">
    <article class="gn-metadata-view">
      <xsl:call-template name="scroll-spy-nav-bar"/>
      
      <form id="gn-editor-{$metadataId}" name="gn-editor" accept-charset="UTF-8" method="POST"
          class="form-horizontal" role="form">
        <input type="hidden" id="schema" value="{$schema}"/>
        <input type="hidden" id="template" name="template" value="{$isTemplate}"/>
        <input type="hidden" id="uuid" value="{$metadataUuid}"/>
        <input type="hidden" name="id" value="{$metadataId}"/>
        <input type="hidden" id="version" name="version" value="{$metadata/gn:info/version}"/>
        <input type="hidden" id="currTab" name="currTab" value="{$tab}"/>
        <input type="hidden" id="minor" name="minor" value="{$isMinorEdit}"/>
        <input type="hidden" name="showvalidationerrors" value="{$showValidationErrors}"/>
        
        <!-- Dispatch to profile mode -->
        <xsl:variable name="profileTemplate" select="concat('render-',$schema)"/>
        <saxon:call-template name="{$profileTemplate}">
          <xsl:with-param name="base" select="$metadata"/>
        </saxon:call-template>
      </form>
      
    </article>
  </xsl:template>
  
</xsl:stylesheet>
