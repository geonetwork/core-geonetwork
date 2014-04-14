<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gn="http://www.fao.org/geonetwork"
    xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
    xmlns:gn-fn-iso19115="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115"
    xmlns:saxon="http://saxon.sf.net/" extension-element-prefixes="saxon"
    exclude-result-prefixes="#all">

    <xsl:include href="layout.xsl"/>

    <!-- 
    Load the schema configuration for the editor.
      -->
    <xsl:template name="get-iso19115-configuration">
        <xsl:copy-of select="document('config-editor.xml')"/>
    </xsl:template>

    <!-- Dispatching to the profile mode -->
    <xsl:template name="dispatch-iso19115">
        <xsl:param name="base" as="node()"/>
        <xsl:apply-templates mode="mode-iso19115" select="$base"/>
    </xsl:template>



    <!-- Evaluate an expression. This is schema dependant in order to properly 
        set namespaces required for evaluate.
        
    "The static context for the expression includes all the in-scope namespaces, 
    types, and functions from the calling stylesheet or query"
    http://saxonica.com/documentation9.4-demo/html/extensions/functions/evaluate.html
    -->
    <xsl:template name="evaluate-iso19115">
        <xsl:param name="base" as="node()"/>
        <xsl:param name="in"/>
        <xsl:copy-of select="saxon:evaluate(concat('$p1', $in), $base)"/>
    </xsl:template>


</xsl:stylesheet>
