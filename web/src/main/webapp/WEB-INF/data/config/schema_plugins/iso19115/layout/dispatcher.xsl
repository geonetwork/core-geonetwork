<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gn="http://www.fao.org/geonetwork"
    xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
    xmlns:gn-fn-iso19115="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115"
    xmlns:saxon="http://saxon.sf.net/" extension-element-prefixes="saxon"
    exclude-result-prefixes="#all">

    <xsl:variable name="iso19115EditorConfiguration" select="document('config-editor.xml')"/>


    <xsl:include href="layout.xsl"/>


    <!-- Dispatching to the profile mode according to the tab -->
    <xsl:template name="render-iso19115">
        <xsl:param name="base" as="node()"/>

        <xsl:variable name="tabConfiguration"
            select="$iso19115EditorConfiguration/editor/views/view/tab[@id = $tab]/section"/>
        <xsl:if test="$service != 'md.element.add'">
            <xsl:call-template name="menu-builder">
                <xsl:with-param name="config" select="$iso19115EditorConfiguration"/>
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
                <xsl:apply-templates mode="mode-iso19115" select="$base"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>



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
