<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === This stylesheet is used by the xml.info service -->
	<!-- ============================================================================================= -->

	<xsl:template match="/">
		<info>
			<xsl:apply-templates select="*"/>
		</info>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="system">
		<site>
			<name><xsl:value-of select="children/site/children/name/value"/></name>
			<organization><xsl:value-of select="children/site/children/organization/value"/></organization>
			<siteId><xsl:value-of select="children/site/children/siteId/value"/></siteId>
			<platform>
				<name>geonetwork</name>
				<version><xsl:value-of select="children/platform/children/version/value"/></version>
				<subVersion><xsl:value-of select="children/platform/children/subVersion/value"/></subVersion>
			</platform>
		</site>
	</xsl:template>

    <xsl:template match="inspire">
        <inspire>
            <enable><xsl:value-of select="children/enable/value"/></enable>
            <enableSearchPanel><xsl:value-of select="children/enableSearchPanel/value"/></enableSearchPanel>
        </inspire>
    </xsl:template>
	
	<!-- ============================================================================================= -->

    <xsl:template match="isolanguages">
        <xsl:copy>
            <xsl:for-each select="record">
                <xsl:sort select="name" order="ascending"/>
                <isolanguage id="{id}">
                    <xsl:copy-of select="code"/>
                    <xsl:copy-of select="label"/>
                </isolanguage>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template match="categories">
		<xsl:copy>
			<xsl:for-each select="record">
				<xsl:sort select="name" order="ascending"/>
				<category id="{id}">
					<xsl:copy-of select="name"/>
					<xsl:copy-of select="label"/>
				</category>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="z3950repositories">
		<xsl:copy>
			<xsl:for-each select="record">
				<xsl:sort select="name" order="ascending"/>
				<repository id="{id}">
					<xsl:copy-of select="id"/>
					<label>
						<xsl:value-of select="name"/>
					</label>
				</repository>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="groups">
		<xsl:copy>
			<xsl:for-each select="record">
				<xsl:sort select="name" order="ascending"/>
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

	<xsl:template match="operations">
		<xsl:copy>
			<xsl:for-each select="record">
				<operation id="{id}">
					<xsl:copy-of select="name"/>
					<xsl:copy-of select="reserved"/>
					<xsl:copy-of select="label"/>
				</operation>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="regions">
		<xsl:copy>
			<xsl:for-each select="record">
				<region id="{id}">
					<xsl:copy-of select="north"/>
					<xsl:copy-of select="south"/>
					<xsl:copy-of select="west"/>
					<xsl:copy-of select="east"/>
					<xsl:copy-of select="label"/>
				</region>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="sources">
		<xsl:copy-of select="."/>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="templates">
		<xsl:copy>
			<xsl:for-each select="record">
				<xsl:sort select="name" order="ascending"/>
				<template id="{id}">
					<xsl:copy-of select="id"/>
					<title>
						<xsl:value-of select="name"/>
					</title>
					<schema>
						<xsl:value-of select="id/@code"/>
					</schema>
				</template>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="users">
		<xsl:copy-of select="."/>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="me">
		<xsl:copy-of select="."/>
	</xsl:template>
	<!-- ============================================================================================= -->

	<xsl:template match="auth">
		<xsl:copy-of select="."/>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
