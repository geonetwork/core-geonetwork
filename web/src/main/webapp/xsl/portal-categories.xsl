<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:template match="/root">
		<xsl:variable name="lang" select="/root/gui/language"/>

		<response>
			<xsl:for-each select="response/*">
				<category>
					<xsl:variable name="code" select="name"/>
					<code><xsl:value-of select="$code"/></code>
					<name><xsl:value-of select="label/child::*[name() = $lang]"/></name>
				</category>
			</xsl:for-each>
		</response>
	</xsl:template>
	
</xsl:stylesheet>
