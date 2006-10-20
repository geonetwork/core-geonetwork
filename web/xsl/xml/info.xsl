<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === This stylesheet is used by the xml.info service -->
	<!-- ============================================================================================= -->

	<xsl:template match="/">
		<response>
			<xsl:apply-templates select="*"/>
		</response>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="system">
		<site>
			<name><xsl:value-of select="children/site/children/name/value"/></name>
			<siteId><xsl:value-of select="children/site/children/siteId/value"/></siteId>
			<icon><xsl:value-of select="/root/env/baseURL"/>/images/logos/<xsl:value-of select="children/site/children/siteId/value"/>.png</icon>
			<platform>
				<name>geonetwork</name>
				<version><xsl:value-of select="children/gnVersion/value"/></version>
			</platform>
		</site>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template match="categories">
		<xsl:copy>
			<xsl:for-each select="record">
				<category>
					<xsl:copy-of select="name"/>
					<xsl:copy-of select="label"/>
				</category>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="groups">
		<xsl:copy>
			<xsl:for-each select="record">
				<group id="{id}">
					<xsl:copy-of select="name"/>
					<xsl:copy-of select="description"/>
					<xsl:copy-of select="email"/>
					<xsl:copy-of select="referrer"/>
					<xsl:copy-of select="label"/>
				</group>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="knownNodes">
		<xsl:copy>
			<xsl:for-each select="record">
				<node>
					<xsl:copy-of select="*"/>
				</node>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
