<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:saxon="http://saxon.sf.net/"
  extension-element-prefixes="saxon">
  <!-- 
    Load the brief template loader if the metadata has to be 
    converted to its brief format. Use the mode="brief" on each
    records to be converted.
    
    
    The profile loader using the oasis-catalog to load
  <schema>/layout/tpl-brief.xsl in each activated schema plugins.
  -->
  <xsl:include href="base-variables.xsl"/>
  <xsl:include href="functions-core.xsl"/>
  
  
  <!-- Initialized variables used in <schema>/utility-tpl.xsl
  which are not used in that mode. To be improved. FIXME -->
  <xsl:variable name="editorConfig"><null/></xsl:variable>
  <xsl:variable name="metadata"><null/></xsl:variable>
  
  <xsl:include href="blanks/metadata-schema01/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema02/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema03/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema04/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema05/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema06/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema07/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema08/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema09/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema10/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema11/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema12/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema13/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema14/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema15/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema16/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema17/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema18/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema19/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema20/layout/tpl-brief.xsl"/>
  
  
  <xsl:template match="*[name() = 'summary']" mode="brief"/>
  
  <xsl:template match="*" mode="brief">
    <xsl:variable name="schema" select="gn:info/schema"/>
    <xsl:variable name="briefSchemaCallBack" select="concat($schema,'Brief')"/>
    <saxon:call-template name="{$briefSchemaCallBack}"/>
  </xsl:template>
  
</xsl:stylesheet>
