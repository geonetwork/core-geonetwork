<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl ="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmx="http://www.isotc211.org/2005/gmx"
	xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:gml="http://www.opengis.net/gml"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:gn="http://www.fao.org/geonetwork"
	xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
	xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
	xmlns:exslt="http://exslt.org/common"
	exclude-result-prefixes="#all">
	
	<xsl:include href="utility-fn.xsl"/>
	<xsl:include href="utility-tpl.xsl"/>
	
	<xsl:template mode="mode-iso19139" match="gn:*"/>
	
	<!-- Visit all tree -->
	<xsl:template mode="mode-iso19139"
		match="gmd:*|gmx:*|gml:*|srv:*|gts:*">
		<xsl:apply-templates mode="mode-iso19139" select="*|@*"/>
	</xsl:template>
	
	
	<!-- Boxed element -->
	<xsl:template mode="mode-iso19139" priority="200"
		match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']|
		gmd:identificationInfo|
		gmd:distributionInfo|
		gmd:portrayalCatalogueInfo|
		gmd:portrayalCatalogueCitation|
		gmd:descriptiveKeywords|
		gmd:thesaurusName|
		*[name(..)='gmd:resourceConstraints']|
		gmd:spatialRepresentationInfo|
		gmd:pointOfContact|
		gmd:dataQualityInfo|
		gmd:contentInfo|
		gmd:distributionFormat|
		gmd:referenceSystemInfo|
		gmd:spatialResolution|
		gmd:offLine|
		gmd:projection|
		gmd:ellipsoid|
		gmd:extent[name(..)!='gmd:EX_TemporalExtent']|
		gmd:attributes|
		gmd:verticalCRS|
		gmd:geographicBox|
		gmd:EX_TemporalExtent|
		gmd:MD_Distributor|
		srv:containsOperations|
		srv:SV_CoupledResource|
		gmd:metadataConstraints|
		gmd:aggregationInfo|
		gmd:report/*|
		gmd:result/*|
		gmd:processStep|
		gmd:lineage">
		
		<xsl:call-template name="render-boxed-element">
			<xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, name(.), $labels)"/>
			<xsl:with-param name="cls" select="local-name()"/>
			<xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
			<xsl:with-param name="subTreeSnippet">
				<xsl:apply-templates mode="mode-iso19139" select="*"/>
			</xsl:with-param>
		</xsl:call-template>
		
	</xsl:template>
	
	
	
	<!-- Label -->
	<xsl:template mode="mode-iso19139" priority="100"
		match="*[gco:CharacterString|gco:Date|gco:DateTime|gco:Integer|gco:Decimal|
		gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|
		gco:Scale|gco:RecordType|gmx:MimeFileType|gmd:URL]">
		
<!--		<xsl:message>###<xsl:copy-of select="."/></xsl:message>
-->		<xsl:call-template name="render-element">
			<xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, name(), $labels)"/>
			<xsl:with-param name="value" select="*"/>
			<xsl:with-param name="cls" select="local-name()"/>
			<!--<xsl:with-param name="widget"/>
			<xsl:with-param name="widgetParams"/>-->
			<xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
			<!--<xsl:with-param name="attributesSnippet" as="node()"/>-->
			<xsl:with-param name="type" select="gn-fn-iso19139:getFieldType(name())"/>
			<xsl:with-param name="name" select="if ($isEditing) then */gn:element/@ref else ''"/>
			<xsl:with-param name="editInfo" select="*/gn:element"/>
			<xsl:with-param name="parentEditInfo" select="gn:element"/>
		</xsl:call-template>
		
	</xsl:template>
	
	
	
	<xsl:template mode="mode-iso19139" priority="100"
		match="gmd:*[*/@codeList]|srv:*[*/@codeList]">
		
		
		<xsl:call-template name="render-element">
			<xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, name(), $labels)"/>
			<xsl:with-param name="value" select="*/@codeListValue"/>
			<xsl:with-param name="cls" select="local-name()"/>
			<!--<xsl:with-param name="widget"/>
            <xsl:with-param name="widgetParams"/>-->
			<xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
			<!--<xsl:with-param name="attributesSnippet" as="node()"/>-->
			<xsl:with-param name="type" select="gn-fn-iso19139:getCodeListType(name())"/>
			<xsl:with-param name="editInfo" select="*/gn:element"/>
		</xsl:call-template>
		
	</xsl:template>
	
	
	
	
</xsl:stylesheet>
