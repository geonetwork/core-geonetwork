<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:tr="https://geonetwork-opensource.org/xsl-extension/schema"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:xslUtils="https://geonetwork-opensource.org/xsl-extension"
                version="3.0"
                exclude-result-prefixes="#all">

  <xsl:import href="base.xsl"/>
  <xsl:include href="../../../iso19115-3.2018/formatter/citation/common.xsl"/>

  <xsl:variable name="metadata"
                select="/root/simpledc"/>

  <xsl:variable name="configuration"
                select="/empty"/>
  <xsl:variable name="editorConfig"
                select="/empty"/>

  <xsl:template match="/">
    <xsl:variable name="citationInfo">
      <xsl:call-template name="get-dublin-core-citation">
        <xsl:with-param name="metadata" select="$metadata"/>
        <xsl:with-param name="language" select="$language"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:apply-templates mode="citation" select="$citationInfo"/>
  </xsl:template>
</xsl:stylesheet>
