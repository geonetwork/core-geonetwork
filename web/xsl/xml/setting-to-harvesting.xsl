<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === This stylesheet transforms an harvesting node from settings XML to output XML -->
	<!-- ============================================================================================= -->

	<xsl:template match="harvesting">
		<nodes>
			<xsl:apply-templates select="*"/>
		</nodes>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template match="node[value = 'geonetwork']">
		<xsl:variable name="site" select="children/site/children"/>
		<xsl:variable name="opt"  select="children/options/children"/>
		<xsl:variable name="info" select="children/info/children"/>
		
		<node id="{@id}" name="{$site/name/value}" type="{value}">
			<site>
				<host><xsl:value-of select="$site/host/value" /></host>
				<port><xsl:value-of select="$site/port/value" /></port>
				<servlet><xsl:value-of select="$site/servlet/value" /></servlet>
				<account>
					<use><xsl:value-of select="$site/useAccount/value" /></use>
					<username><xsl:value-of select="$site/useAccount/children/username/value" /></username>
					<password><xsl:value-of select="$site/useAccount/children/password/value" /></password>
				</account>
			</site>
		
			<searches>
				<xsl:for-each select="children/search">
					<search>
						<freeText><xsl:value-of select="children/freeText/value" /></freeText>
						<title><xsl:value-of select="children/title/value" /></title>
						<abstract><xsl:value-of select="children/abstract/value" /></abstract>
						<keywords><xsl:value-of select="children/keywords/value" /></keywords>
						<digital><xsl:value-of select="children/digital/value" /></digital>
						<hardcopy><xsl:value-of select="children/hardcopy/value" /></hardcopy>
						<siteId><xsl:value-of select="children/siteId/value" /></siteId>
					</search>
				</xsl:for-each>
			</searches>

			<options>
				<every><xsl:value-of select="$opt/every/value" /></every>
				<createGroups><xsl:value-of select="$opt/createGroups/value" /></createGroups>
				<oneRunOnly><xsl:value-of select="$opt/oneRunOnly/value" /></oneRunOnly>
				<status><xsl:value-of select="$opt/status/value"/></status>
			</options>
			
			<info>
				<lastRun><xsl:value-of select="$info/lastRun/value" /></lastRun>
			</info>
		</node>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="node[value = 'webFolder']">
		<xsl:variable name="site"   select="children/site/children"/>
		<xsl:variable name="opt"    select="children/options/children"/>
		<xsl:variable name="info"   select="children/info/children"/>
		<xsl:variable name="privil" select="children/privileges/children"/>
		
		<node id="{@id}" name="{$site/name/value}" type="{value}"> 
			<site>
				<url><xsl:value-of select="$site/url/value" /></url>
				<account>
					<use><xsl:value-of select="$site/useAccount/value" /></use>
					<username><xsl:value-of select="$site/useAccount/children/username/value" /></username>
					<password><xsl:value-of select="$site/useAccount/children/password/value" /></password>
				</account>
			</site>
			
			<options>
				<every><xsl:value-of select="$opt/every/value" /></every>
				<oneRunOnly><xsl:value-of select="$opt/oneRunOnly/value" /></oneRunOnly>				
				<structure><xsl:value-of select="$opt/structure/value" /></structure>
				<validate><xsl:value-of select="$opt/validate/value" /></validate>
				<status><xsl:value-of select="$opt/status/value"/></status>
			</options>
		
			<privileges>
				<xsl:for-each select="$privil/group">
					<group id="{value}">
						<xsl:for-each select="children/operation">
							<xsl:choose>
								<xsl:when test="value = '0'"><operation name="view"/></xsl:when>
								<xsl:when test="value = '1'"><operation name="download"/></xsl:when>
								<xsl:when test="value = '3'"><operation name="notify"/></xsl:when>
								<xsl:when test="value = '5'"><operation name="dynamic"/></xsl:when>
								<xsl:when test="value = '6'"><operation name="featured"/></xsl:when>
							</xsl:choose>
						</xsl:for-each>
					</group>
				</xsl:for-each>
			</privileges>
			
			<info>
				<lastRun><xsl:value-of select="$info/lastRun/value" /></lastRun>
			</info>
		</node>
	</xsl:template>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
