<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/"
  xmlns:gn="http://www.fao.org/geonetwork" xmlns:saxon="http://saxon.sf.net/"
  extension-element-prefixes="saxon" exclude-result-prefixes="#all">

  <!-- Evaluate an expression. This is schema dependant in order to properly 
        set namespaces required for evaluate.
        
    "The static context for the expression includes all the in-scope namespaces, 
    types, and functions from the calling stylesheet or query"
    http://saxonica.com/documentation9.4-demo/html/extensions/functions/evaluate.html
    -->
  <xsl:template name="evaluate-dublin-core">
    <xsl:param name="base" as="node()"/>
    <xsl:param name="in"/>
    <xsl:copy-of select="saxon:evaluate(concat('$p1', $in), $base)"/>
  </xsl:template>
</xsl:stylesheet>
