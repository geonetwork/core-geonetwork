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
	
	<xsl:template match="results">
		<sources>
			<xsl:for-each select="record">
				<xsl:sort select="name" order="ascending"/>
				<source id="{id}">
					<uuid><xsl:value-of select="id"/></uuid>
					<xsl:copy-of select="name"/>
					<xsl:copy-of select="label"/>
				</source>
			</xsl:for-each>
		</sources>
	</xsl:template>
	
    <xsl:template match="isolanguage">
        <xsl:copy>
            <xsl:for-each select="record">
                <isolanguage id="{id}">
                    <xsl:copy-of select="code"/>
                    <xsl:copy-of select="label"/>
                </isolanguage>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
	
    <xsl:template match="language">
        <defaultlanguage><xsl:value-of select="record[defaultlanguage = 'true']/id" /></defaultlanguage>
        <xsl:copy>
            <xsl:for-each select="record">
                <xsl:sort select="name" order="ascending"/>
                <language>
                    <xsl:copy-of select="id"/>
                    <xsl:copy-of select="inspire"/>
                    <xsl:copy-of select="name"/>
                </language>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="metadatacategory">
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

	<xsl:template match="group">
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

	<xsl:template match="operation">
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
	
	<xsl:template match="statusvalue">
		<xsl:copy>
			<xsl:for-each select="record">
				<xsl:sort select="name" order="ascending"/>
				<status id="{id}">
					<xsl:copy-of select="name"/>
					<xsl:copy-of select="reserved"/>
					<xsl:copy-of select="displayorder"/>
					<xsl:copy-of select="label"/>
				</status>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

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
	
	<xsl:template match="users|me|auth|readonly|index|env|regions|sources|schemas">
        <xsl:copy-of select="."/>
    </xsl:template>
	
	<xsl:template match="settings">
		<xsl:choose>
			<xsl:when test="setting[@name='system/site/name']">
				<site>
					<name><xsl:value-of select="setting[@name='system/site/name']/@value"/></name>
					<organization><xsl:value-of select="setting[@name='system/site/organization']/@value"/></organization>
					<siteId><xsl:value-of select="setting[@name='system/site/siteId']/@value"/></siteId>
					<platform>
						<name>geonetwork</name>
						<version><xsl:value-of select="setting[@name='system/platform/version']/@value"/></version>
						<subVersion><xsl:value-of select="setting[@name='system/platform/subVersion']/@value"/></subVersion>
					</platform>
				</site>
			</xsl:when>
			<!-- Only INSPIRE -->
			<xsl:when test="not(setting[@name='system/site/name']) and setting[@name='system/inspire/enable']">
				<inspire>
					<enable><xsl:value-of select="setting[@name='system/inspire/enable']/@value"/></enable>
					<enableSearchPanel><xsl:value-of select="setting[@name='system/inspire/enableSearchPanel']/@value"/></enableSearchPanel>
				</inspire>
			</xsl:when>
			<xsl:when test="not(setting[@name='system/site/name']) and setting[@name='system/harvester/enableEditing']">
				<harvester>
					<enable><xsl:value-of select="setting[@name='system/harvester/enableEditing']/@value"/></enable>
				</harvester>
			</xsl:when>
		    <xsl:when test="not(setting[@name='system/site/name']) and setting[@name='system/metadataprivs/usergrouponly']">
		        <metadataprivs>
		            <userGroupOnly><xsl:value-of select="setting[@name='system/metadataprivs/usergrouponly']/@value"/></userGroupOnly>
		        </metadataprivs>
		    </xsl:when>
			<xsl:otherwise>
				
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="config">
		<xsl:for-each select="settings/setting">
			<xsl:element name="{replace(@name, '/', '.')}"><xsl:value-of select="@value"/></xsl:element>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="systemInfo">
		<xsl:copy-of select="." />
	</xsl:template>

</xsl:stylesheet>
