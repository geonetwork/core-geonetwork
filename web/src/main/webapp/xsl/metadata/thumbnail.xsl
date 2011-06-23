<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:sch="http://www.ascc.net/xml/schematron" xmlns:gml="http://www.opengis.net/gml"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:srv="http://www.isotc211.org/2005/srv"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
  xmlns:exslt="http://exslt.org/common" 
  exclude-result-prefixes="geonet srv gco gmd xlink gml sch svrl exslt">

  <xsl:include href="common.xsl"/>
  
  <xsl:template match="/">
    <thumbnails>
      <xsl:apply-templates mode="thumbnail" select="/root/*"/>
    </thumbnails>
  </xsl:template>
  
  <xsl:template mode="thumbnail" match="gui|request" priority="99"/>
    
  <xsl:template mode="thumbnail" match="*">
    <xsl:param name="type"/>
    
    <xsl:variable name="md">
      <xsl:apply-templates mode="get-thumbnail" select="."/>
    </xsl:variable>
    <xsl:variable name="metadata" select="exslt:node-set($md)"/>
    
    <xsl:copy-of select="$metadata"/>  
  </xsl:template>
  
</xsl:stylesheet>
