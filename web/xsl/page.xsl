<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/page/title"/>
			<xsl:with-param name="content">
				<table><tr><td>
					<xsl:copy-of select="/root/gui/page/content/*"/>
					<p align="right">siteID: <kbd><xsl:value-of select="/root/gui/env/site/siteId"/></kbd></p>
				</td></tr></table>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
</xsl:stylesheet>
