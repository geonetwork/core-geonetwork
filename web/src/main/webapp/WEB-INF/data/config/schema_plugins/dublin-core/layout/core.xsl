<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl ="http://www.w3.org/1999/XSL/Transform"
	xmlns:dc = "http://purl.org/dc/elements/1.1/"
	xmlns:dct = "http://purl.org/dc/terms/"
	xmlns:gn="http://www.fao.org/geonetwork"
	xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
	xmlns:gn-fn-dublin-core="http://geonetwork-opensource.org/xsl/functions/profiles/dublin-core"
	exclude-result-prefixes="#all">
	
	<xsl:include href="utility-fn.xsl"/>
    
	<!-- Dispatching to the profile mode according to the tab -->
	<xsl:template name="render-dublin-core">
		<xsl:param name="base" as="node()"/>
		
		<xsl:choose>
			<xsl:when test="$tab = 'xml'">
				<xsl:apply-templates mode="render-xml" select="$base"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="mode-dublin-core" select="$base"/>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
	<!-- Visit all tree -->
	<xsl:template mode="mode-dublin-core"
		match="dc:*|dct:*">
		<xsl:apply-templates mode="mode-dublin-core" select="*|@*"/>
	</xsl:template>
	
	<xsl:template mode="mode-dublin-core" match="gn:*"/>
	

	<xsl:template mode="mode-dublin-core" match="dc:*[
		starts-with(name(), 'dc:elementContainer') or
		starts-with(name(), 'dc:any')
		]" priority="300">
		<xsl:apply-templates mode="mode-dublin-core" select="*|@*"/>
	</xsl:template>
	
	
	<!-- Boxed element -->
	<xsl:template mode="mode-dublin-core" priority="200"
		match="simpledc">
		
		<xsl:call-template name="render-boxed-element">
			<xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, name(.), $labels)"/>
			<xsl:with-param name="cls" select="local-name()"/>
			<xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
			<xsl:with-param name="subTreeSnippet">
				<xsl:apply-templates mode="mode-dublin-core" select="*"/>
			</xsl:with-param>
		</xsl:call-template>
		
	</xsl:template>
	
	
	
	<!-- Label -->
	<xsl:template mode="mode-dublin-core" priority="100"
		match="dc:*|dct:*">
<!-- Add view and edit template-->
		<xsl:call-template name="render-element">
			<xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, name(.), $labels)"/>
			<xsl:with-param name="value" select="."/>
			<xsl:with-param name="cls" select="local-name()"/>
			<!--<xsl:with-param name="widget"/>
            <xsl:with-param name="widgetParams"/>-->
			<xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
			<!--<xsl:with-param name="attributesSnippet" as="node()"/>-->
			<xsl:with-param name="type" select="gn-fn-dublin-core:getFieldType(name())"/>
			<xsl:with-param name="name" select="if ($isEditing) then gn:element/@ref else ''"/>
			<xsl:with-param name="editInfo" select="gn:element"/>
			<xsl:with-param name="widget" select="gn-fn-dublin-core:getFieldWidget(name())"/>
		</xsl:call-template>
	</xsl:template>
	
	
</xsl:stylesheet>
