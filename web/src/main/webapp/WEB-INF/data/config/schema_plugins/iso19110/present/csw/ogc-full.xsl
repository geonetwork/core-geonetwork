<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gfc="http://www.isotc211.org/2005/gfc" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="gco gfc gmx">

	<xsl:param name="displayInfo"/>

	<!-- ============================================================================= -->

	<xsl:template match="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType|*[@gco:isoType='gfc:FC_FeatureCatalogue']">

		<xsl:variable name="info" select="geonet:info"/>
		<xsl:variable name="identification" select="."/>


		<csw:Record>

			<dc:identifier>
				<xsl:value-of select="@uuid"/>
			</dc:identifier>

			<!-- DataIdentification -->
			<xsl:for-each select="gmx:name/gco:CharacterString|gfc:name/gco:CharacterString|gfc:typeName/gco:LocalName">
				<dc:title>
					<xsl:value-of select="."/>
				</dc:title>
			</xsl:for-each>

			<dc:type>FeatureCatalogue</dc:type>

			<!-- Use feature attribute name for subject -->
			<xsl:for-each select="//gfc:FC_FeatureAttribute/gfc:memberName/gco:LocalName">
				<dc:subject>
					<xsl:value-of select="."/>
				</dc:subject>
			</xsl:for-each>


			<!-- Parent Identifier TODO once relation between iso19139 and iso19110 record available
				set this one here
				<xsl:for-each select="../../gmd:parentIdentifier/gco:CharacterString">
				<dc:relation><xsl:value-of select="."/></dc:relation>
				</xsl:for-each>-->

			<xsl:for-each select="gmx:versionDate/gco:DateTime|gfc:versionDate/gco:DateTime">
				<dct:modified>
					<xsl:value-of select="."/>
				</dct:modified>
			</xsl:for-each>

			<xsl:for-each select="gmx:scope|gfc:scope">
				<dct:abstract>
					<xsl:value-of select="gco:CharacterString"/>
				</dct:abstract>
			</xsl:for-each>
			<xsl:for-each select="gmx:fieldOfApplication|gfc:fieldOfApplication">
				<dct:abstract>
					<xsl:value-of select="gco:CharacterString"/>
				</dct:abstract>
			</xsl:for-each>

			<!-- GeoNetwork elements added when resultType is equal to results_with_summary -->
			<xsl:if test="$displayInfo = 'true'">
				<xsl:copy-of select="$info"/>
			</xsl:if>

		</csw:Record>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*">
		<xsl:apply-templates select="*"/>
	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
