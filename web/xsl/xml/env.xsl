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
		<xsl:variable name="proxy"    select="children/proxy/children"/>
		<xsl:variable name="feedback" select="children/feedback/children"/>

		<env>
			<site>
				<name><xsl:value-of select="$site/name/value"/></name>
				<organization><xsl:value-of select="$site/organization/value"/></organization>
				<siteId><xsl:value-of select="$site/siteId/value"/></siteId>				
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
		</env>
	</xsl:template>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
