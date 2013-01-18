<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gfc="http://www.isotc211.org/2005/gfc" xmlns:gmx="http://www.isotc211.org/2005/gmx">

	<!-- ============================================================================================ -->

	<xsl:output indent="yes"/>

	<!-- ============================================================================================ -->

	<xsl:template match="gfc:FC_FeatureCatalogue|*[@gco:isoType='gfc:FC_FeatureCatalogue']">
		<oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">

			<dc:identifier>
				<xsl:value-of select="@uuid"/>
			</dc:identifier>

			<dc:date>
				<xsl:value-of select="/root/env/changeDate"/>
			</dc:date>


			<xsl:for-each select="gmx:name/gco:CharacterString|gfc:name/gco:CharacterString">
				<dc:title>
					<xsl:value-of select="."/>
				</dc:title>
			</xsl:for-each>

			<!-- subject -->

			<xsl:for-each select="//gfc:FC_FeatureAttribute/gfc:memberName/gco:LocalName">
				<dc:subject>
					<xsl:value-of select="."/>
				</dc:subject>
			</xsl:for-each>

			<!-- description -->

			<xsl:for-each select="gmx:scope/gco:CharacterString|gfc:scope/gco:CharacterString">
				<dc:description>
					<xsl:value-of select="."/>
				</dc:description>
			</xsl:for-each>
		</oai_dc:dc>
	</xsl:template>

	<!-- ============================================================================================ -->

	<xsl:template match="*">
		<xsl:apply-templates select="*"/>
	</xsl:template>

	<!-- ============================================================================================ -->

</xsl:stylesheet>
