<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === This stylesheet is used by the 'env' gui service -->
	<!-- ============================================================================================= -->

	<xsl:template match="/system">
		<xsl:variable name="site"     select="children/site/children"/>
		<xsl:variable name="server"   select="children/server/children"/>
		<xsl:variable name="intranet" select="children/intranet/children"/>
		<xsl:variable name="z3950"    select="children/z3950/children"/>
		<xsl:variable name="oai"    	select="children/oai/children"/>
		<xsl:variable name="xlinkResolver" select="children/xlinkResolver/children"/>
		<xsl:variable name="userSelfRegistration" select="children/userSelfRegistration/children"/>
		<xsl:variable name="clickablehyperlinks" select="children/clickablehyperlinks/children"/>		
		<xsl:variable name="localrating" select="children/localrating/children"/>		
		<xsl:variable name="csw"        select="children/csw/children"/>
		<xsl:variable name="proxy"    select="children/proxy/children"/>
		<xsl:variable name="feedback" select="children/feedback/children"/>
		<xsl:variable name="platform" select="children/platform/children"/>
		<xsl:variable name="shib"       select="children/shib/children"/>
		<xsl:variable name="shibAttrib" select="$shib/attrib/children"/>
        <xsl:variable name="inspire" select="children/inspire/children"/>

		<env>
			<site>
				<name><xsl:value-of select="$site/name/value"/></name>
				<organization><xsl:value-of select="$site/organization/value"/></organization>
				<siteId><xsl:value-of select="$site/siteId/value"/></siteId>				
				<platform>
					<name>geonetwork</name>
					<version><xsl:value-of select="$platform/version/value"/></version>
					<subVersion><xsl:value-of select="$platform/subVersion/value"/></subVersion>
				</platform>
			</site>

			<server>
				<host><xsl:value-of select="$server/host/value"/></host>
				<port><xsl:value-of select="$server/port/value"/></port>
			</server>

			<intranet>
				<network><xsl:value-of select="$intranet/network/value"/></network>
				<netmask><xsl:value-of select="$intranet/netmask/value"/></netmask>
			</intranet>

			<z3950>
				<enable><xsl:value-of select="$z3950/enable/value"/></enable>
				<port><xsl:value-of select="$z3950/port/value"/></port>
			</z3950>

			<oai>
				<mdmode><xsl:value-of select="$oai/mdmode/value"/></mdmode>
				<tokentimeout><xsl:value-of select="$oai/tokentimeout/value"/></tokentimeout>
				<cachesize><xsl:value-of select="$oai/cachesize/value"/></cachesize>
			</oai>

			<xlinkResolver>
				<enable><xsl:value-of select="$xlinkResolver/enable/value"/></enable>
			</xlinkResolver>
			
			<userSelfRegistration>
				<enable><xsl:value-of select="$userSelfRegistration/enable/value"/></enable>
			</userSelfRegistration>
			
			<csw>
				<contactId><xsl:value-of select="$csw/contactId/value"/></contactId>
			</csw>

			<clickablehyperlinks>
				<enable><xsl:value-of select="$clickablehyperlinks/enable/value"/></enable>
			</clickablehyperlinks>

			<localrating>
				<enable><xsl:value-of select="$localrating/enable/value"/></enable>
			</localrating>

            <inspire>
				<enable><xsl:value-of select="$inspire/enable/value"/></enable>
			</inspire>

			<proxy>
				<use><xsl:value-of select="$proxy/use/value"/></use>
				<host><xsl:value-of select="$proxy/host/value"/></host>
				<port><xsl:value-of select="$proxy/port/value"/></port>
			</proxy>

			<feedback>
				<email><xsl:value-of select="$feedback/email/value"/></email>
				<mailServer>
					<host><xsl:value-of select="$feedback/mailServer/children/host/value"/></host>
					<port><xsl:value-of select="$feedback/mailServer/children/port/value"/></port>
				</mailServer>
			</feedback>
			
			<shib>
				<use><xsl:value-of select="$shib/use"/></use>
				<path><xsl:value-of select="$shib/path"/></path>
				<attrib>
					<username><xsl:value-of select="$shibAttrib/username"/></username>
					<surname><xsl:value-of select="$shibAttrib/surname"/></surname>
					<firstname><xsl:value-of select="$shibAttrib/firstname"/></firstname>
					<profile><xsl:value-of select="$shibAttrib/profile"/></profile>
				</attrib>
			</shib>			
		</env>
	</xsl:template>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
