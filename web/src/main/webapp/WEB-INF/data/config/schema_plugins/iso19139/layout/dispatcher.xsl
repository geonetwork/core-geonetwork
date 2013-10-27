<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
    xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
    xmlns:srv="http://www.isotc211.org/2005/srv"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml"
    xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gn="http://www.fao.org/geonetwork"
    xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
    xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
    xmlns:saxon="http://saxon.sf.net/" extension-element-prefixes="saxon"
    exclude-result-prefixes="#all">

    <xsl:include href="layout.xsl"/>

    <!-- Dispatching to the profile mode according to the tab -->
    <xsl:template name="render-iso19139">
        <xsl:param name="base" as="node()"/>

        <!-- Using ENTITY may be more efficient and cache ?
	    <!DOCTYPE document [ 
            <!ENTITY  config SYSTEM 'config-editor.xml'> 
        ]>
	    -->
        <xsl:variable name="editorConfiguration"
            select="document('config-editor.xml')"/>
        <xsl:variable name="tabConfiguration"
          select="$editorConfiguration/editor/view/tab[@id = $tab]/section"/>
<!--        <xsl:message>=========== <xsl:copy-of select="$theTabConfiguration"/></xsl:message>
-->      
      <xsl:if test="$service != 'md.element.add'">
        <xsl:call-template name="menu-builder">
          <xsl:with-param name="config" select="$editorConfiguration"/>
        </xsl:call-template>
      </xsl:if>
      
        <xsl:choose>
            <xsl:when test="$service != 'md.element.add' and $tabConfiguration">
                <xsl:apply-templates mode="form-builer" select="$tabConfiguration">
                    <xsl:with-param name="base" select="$base"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$tab = 'xml'">
                <xsl:apply-templates mode="render-xml" select="$base"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="mode-iso19139" select="$base"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    
    <xsl:template name="dispatch-iso19139">
        <xsl:param name="base" as="node()"/>
        <xsl:apply-templates mode="mode-iso19139" select="$base"/>
    </xsl:template>
    
    
    
    <!-- Evaluate an expression. This is schema dependant in order to properly 
        set namespaces required for evaluate.
        
    "The static context for the expression includes all the in-scope namespaces, 
    types, and functions from the calling stylesheet or query"
    http://saxonica.com/documentation9.4-demo/html/extensions/functions/evaluate.html
    -->
    <xsl:template name="evaluate-iso19139">
        <xsl:param name="base" as="node()"/>
        <xsl:param name="in"/>
        <xsl:copy-of select="saxon:evaluate(concat('$p1/..', $in), $base)"/>
    </xsl:template>


</xsl:stylesheet>
