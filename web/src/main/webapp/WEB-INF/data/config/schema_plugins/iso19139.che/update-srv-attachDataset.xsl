<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to update metadata for a service and 
detach a dataset metadata
-->
<xsl:stylesheet version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:che="http://www.geocat.ch/2008/che"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:date="http://exslt.org/dates-and-times">

	<xsl:param name="uuid"/>
	<xsl:param name="layerName"/>

	<!-- Templates to add dataset metadata
	record links. -->
	<xsl:template name="add-operates-on">
		<xsl:param name="uuid"/>

		<xsl:if test="normalize-space($uuid)!=''">
			<srv:operatesOn uuidref="{$uuid}"/>
		</xsl:if>
	</xsl:template>

	<xsl:template name="add-coupled-resource">
		<xsl:param name="uuid"/>
		<xsl:param name="scoped"/>

		<xsl:if test="$scoped!='' and $uuid!=''">
			<srv:coupledResource>
				<srv:SV_CoupledResource>
					<srv:operationName>
						<gco:CharacterString>GetCapabilities</gco:CharacterString>
					</srv:operationName>
					<srv:identifier>
						<gco:CharacterString>
							<xsl:value-of select="$uuid"/>
						</gco:CharacterString>
					</srv:identifier>
					<gco:ScopedName>
						<xsl:value-of select="$scoped"/>
					</gco:ScopedName>
				</srv:SV_CoupledResource>
			</srv:coupledResource>
		</xsl:if>
	</xsl:template>


	<xsl:template match="/">
		<xsl:apply-templates select="@*|node()" mode="copy"/>
	</xsl:template>

	<!-- Copy all elements and add coupled resource
	and operates on. Do not create duplicates on operates on and
	refresh existing content if new scoped name provided. -->
	<xsl:template match="che:CHE_SV_ServiceIdentification|srv:SV_ServiceIdentification|*[gco:isoType='srv:SV_ServiceIdentification']" priority="200" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:copy-of select="gmd:*"/>
			<xsl:copy-of select="srv:serviceType"/>
			<xsl:copy-of select="srv:serviceTypeVersion"/>
			<xsl:copy-of select="srv:accessProperties"/>
			<xsl:copy-of select="srv:restrictions"/>
			<xsl:copy-of select="srv:keywords"/>
			<xsl:copy-of select="srv:extent"/>
			<xsl:copy-of select="srv:coupledResource[srv:SV_CoupledResource/srv:identifier/gco:CharacterString!=$uuid]"/>
			<xsl:copy-of select="srv:coupledResource[srv:SV_CoupledResource/srv:identifier/gco:CharacterString=$uuid and srv:SV_CoupledResource/gco:ScopedName!=$layerName]"/>
			<xsl:call-template name="add-coupled-resource">
				<xsl:with-param name="uuid" select="$uuid"/>
				<xsl:with-param name="scoped" select="$layerName"/>
			</xsl:call-template>
			<xsl:copy-of select="srv:couplingType"/>
			<xsl:copy-of select="srv:containsOperations"/>
			<xsl:copy-of select="srv:operatesOn[@uuidref!=$uuid]"/>
			<xsl:call-template name="add-operates-on">
				<xsl:with-param name="uuid" select="$uuid"/>
			</xsl:call-template>
			<xsl:copy-of select="che:*"/>
		</xsl:copy>
	</xsl:template>


	<!-- Do a copy of every nodes and attributes -->
	<xsl:template match="@*|node()" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="copy"/>
		</xsl:copy>
	</xsl:template>

	<!-- Remove geonet:* elements. -->
	<xsl:template match="geonet:*" priority="200" mode="copy"/>

</xsl:stylesheet>
