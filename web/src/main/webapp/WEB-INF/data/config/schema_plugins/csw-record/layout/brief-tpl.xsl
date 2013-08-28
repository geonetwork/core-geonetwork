<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/"
  xmlns:gn="http://www.fao.org/geonetwork" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="xs" version="2.0">
  
  <!-- Brief template - csw-record just calls Brief from 
       dublin-core -->
  <xsl:template name="csw-recordBrief">
    <xsl:call-template name="dublin-coreBrief"/>
  </xsl:template>
</xsl:stylesheet>
