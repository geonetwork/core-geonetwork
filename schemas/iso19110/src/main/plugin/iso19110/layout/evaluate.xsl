<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:gfc="http://www.isotc211.org/2005/gfc" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:saxon="http://saxon.sf.net/"
  extension-element-prefixes="saxon"
  exclude-result-prefixes="#all">
  

  <!-- Evaluate an expression. This is schema dependant in order to properly 
        set namespaces required for evaluate.
        
    "The static context for the expression includes all the in-scope namespaces, 
    types, and functions from the calling stylesheet or query"
    http://saxonica.com/documentation9.4-demo/html/extensions/functions/evaluate.html
    -->
  <xsl:template name="evaluate-iso19110">
    <xsl:param name="base" as="node()"/>
    <xsl:param name="in"/>
    <xsl:copy-of select="saxon:evaluate(concat('$p1', $in), $base)"/>
  </xsl:template>
</xsl:stylesheet>
