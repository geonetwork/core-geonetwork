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

    <xsl:include href="mapping.xsl"/>

    <!-- Dispatching to the profile mode according to the tab -->
    <xsl:template name="render-iso19139">
        <xsl:param name="base" as="node()"/>

        <!-- Using ENTITY may be more efficient and cache ?
	    <!DOCTYPE document [ 
            <!ENTITY  config SYSTEM 'config-editor.xml'> 
        ]>
	    -->
        <xsl:variable name="theTabConfiguration"
            select="document('config-editor.xml')/config/tab[@id = $tab]/section"/>

        <xsl:choose>
            <xsl:when test="$service != 'md.element.add' and $theTabConfiguration">
                <xsl:apply-templates mode="form-builer" select="$theTabConfiguration">
                    <xsl:with-param name="base" select="$base"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$tab = 'xml'">
                <xsl:apply-templates mode="render-xml" select="$base"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>
                    <xsl:copy-of select="$base"/>
                </xsl:message>
                <xsl:apply-templates mode="mode-iso19139" select="$base"/>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>


    <!-- Create a tab -->
    <xsl:template mode="form-builer" match="section|fieldset">
        <xsl:param name="base" as="node()"/>
        <fieldset>
            <legend><xsl:value-of select="@name"/></legend>
            <xsl:apply-templates mode="form-builer" select="@*|*">
                <xsl:with-param name="base" select="$base"/>
            </xsl:apply-templates>
        </fieldset>
    </xsl:template>
    
    <!-- Element to ignore in that mode -->
    <xsl:template mode="form-builer" match="@name"/>
    
    <xsl:template mode="form-builer" match="@match">
        <xsl:param name="base" as="node()"/>

        <xsl:variable name="matchingElement" select="@match"/>

        <!-- Apply tab mode first and if empty, fallback to default. -->
        <xsl:apply-templates mode="mode-iso19139" select="$base/*[name() = $matchingElement]"/>
    </xsl:template>

    <xsl:template mode="form-builer" match="field">
        <xsl:param name="base" as="node()"/>

        <xsl:if test="@xpath">
            <!-- Match any nodes in the metadata with the XPath -->
            <xsl:variable name="nodes" select="saxon:evaluate(concat('$p1/..', @xpath), $base)"/>

            <!-- Check if any this field is controlled by a condition.
                If @if expression return false, the field is not displayed. -->
            <xsl:variable name="display"
                select="if (@if) then saxon:evaluate(concat('$p1/..', @if), $base) else true()"/>

            <!--                <xsl:message><xsl:copy-of select="$nodes"/></xsl:message>
-->
            <xsl:if test="$display">
                <xsl:for-each select="$nodes">
                    
                    <xsl:apply-templates mode="mode-iso19139" select="."/>
<!--                    <xsl:variable name="nodeRef"
                        select="./gn:element/@ref"/>
                    <xsl:apply-templates mode="mode-iso19139" select="$base/descendant::*[gn:element/@ref = $nodeRef]"/>-->
                </xsl:for-each>

                <!-- TODO: for non existing node -->
                <xsl:if test="not($nodes)">
                    <div class="form-group">
                        <label title="{@xpath}" class="col-lg-2">
                            <xsl:value-of select="@name"/>
                        </label>
                        <div class="col-lg-8">
                            <textarea class="form-control"><xsl:copy-of select="template"/></textarea>
                        </div>
                    </div>
                </xsl:if>
            </xsl:if>

        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
