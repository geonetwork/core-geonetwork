<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--
	<xsl:include href="banner.xsl"/>
-->
	<xsl:template match="/">
		<html>
			<head>
				<title>
					<xsl:value-of select="/root/gui/strings/title" />
				</title>
				<link rel="stylesheet" type="text/css" href="../../intermap.css" />
			</head>
			<body class="addmargin">
				<span class="bold">
					<p>
						<xsl:value-of select="/root/gui/strings/choosepredefinedmap"/>
						<xsl:text> </xsl:text>
						<a href="{/root/gui/locService}/mapServers.listServers"><xsl:value-of select="/root/gui/strings/manually"/></a>
					</p>
				</span>
				<xsl:for-each select="/root/response/mapContexts/context">
					<a href="{/root/gui/locService}/map.setContext?id={@id}"><xsl:value-of select="@name" /></a>
					<br />
				</xsl:for-each>
				<br />
<!--				<p><xsl:value-of select="/root/gui/strings/findMapsGeoNetwork"/>: <a href="{/root/gui/url}/../" target="GeoNetwork"><xsl:value-of select="/root/gui/strings/gotoGeoNetwork"/></a></p> -->
				<p><xsl:value-of select="/root/gui/strings/findMapsMapServers"/>: <a href="{/root/gui/locService}/mapServers.listServers"><xsl:value-of select="/root/gui/strings/gotoMapServers"/></a></p>
			</body>
		</html>
	</xsl:template>
	
</xsl:stylesheet>
