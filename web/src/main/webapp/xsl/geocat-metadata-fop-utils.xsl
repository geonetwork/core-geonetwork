<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
	exclude-result-prefixes="xalan" xmlns:che="http://www.geocat.ch/2008/che"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:geonet="http://www.fao.org/geonetwork" xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<xsl:template mode="localizedURLFop" match="*">
		<xsl:param name="schema" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="name(.)" />
				<xsl:with-param name="schema" select="$schema" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="UPPER">
			ABCDEFGHIJKLMNOPQRSTUVWXYZ
		</xsl:variable>
		<xsl:variable name="LOWER">
			abcdefghijklmnopqrstuvwxyz
		</xsl:variable>
		<xsl:variable name="text">
			<xsl:call-template name="translateURL">
				<xsl:with-param name="schema" select="$schema" />
				<xsl:with-param name="langId"
					select="concat('#',translate(substring(/root/gui/language,1,2),$LOWER,$UPPER))" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:call-template name="info-rows">
			<xsl:with-param name="label" select="$title" />
			<xsl:with-param name="value" select="$text" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="translateURL">
		<xsl:param name="schema" />
		<xsl:param name="edit" />
		<xsl:param name="rows" select="1" />
		<xsl:param name="cols" select="40" />
		<xsl:param name="langId" />
		<xsl:param name="widget" />
		<xsl:param name="validator" />

		<xsl:variable name="defaultLang">
			<xsl:call-template name="getLangId">
				<xsl:with-param name="langGui"
					select="/root/*/gmd:language/gco:CharacterString" />
				<xsl:with-param name="md"
					select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="not($edit=true() and $widget)">
				<xsl:choose>
					<xsl:when test="(.//che:LocalisedURL)[@locale=$langId]">
						<xsl:value-of select="(.//che:LocalisedURL)[@locale=$langId]" />
					</xsl:when>

					<xsl:when test="(.//che:LocalisedURL)[@locale=$defaultLang]">
						<xsl:value-of select=".//che:LocalisedURL[@locale=$defaultLang]" />
					</xsl:when>
					<xsl:when test="che:LocalisedURL">
						<xsl:value-of select=".//che:LocalisedURL[@locale=$defaultLang]" />
					</xsl:when>

					<xsl:otherwise>
						<xsl:value-of select="gmd:URL" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="$widget" />
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
</xsl:stylesheet>
