<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gn="http://www.fao.org/geonetwork"
  >
  <!-- 
    Global XSL variables for metadata editing. 
  -->
  <xsl:include href="base-variables-metadata.xsl"/>
  
  <xsl:variable name="isMinorEdit" select="/root/request/minor"/>
  
  <!-- 
  In flat mode, only existing element are displayed. This means that
  all geonet:child element from the metadocument are ignored.
  -->
  <xsl:variable name="isFlatMode" select="/root/request/flat"/>
  
  <xsl:variable name="showValidationErrors" select="/root/request/showvalidationerrors"/>
  
</xsl:stylesheet>
