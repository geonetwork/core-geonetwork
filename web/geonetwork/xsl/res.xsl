<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="main.xsl"/>
	
	<!--
	html page
	-->
	<xsl:template match="/">
		<html>
			<head>
				<title><xsl:apply-templates select="/" mode="title"/></title>
				<link rel="stylesheet" type="text/css" href="{/root/gui/url}/geonetwork.css"/>
			</head>
			<body>
				<table width="100%" height="100%">
					<tr class="banner">
						<td class="banner">
							<img src="{/root/gui/url}/images/header-left.jpg" alt="GeoNetwork opensource" align="top" />
						</td>
						<td align="right" class="banner">
							<img src="{/root/gui/url}/images/header-right.gif" alt="World picture" align="top" />
						</td>
					</tr>
        			<tr height="100%">
						<td class="content" colspan="3">
							<xsl:call-template name="formLayout">
								<xsl:with-param name="title">
									<h1><xsl:apply-templates select="/" mode="title"/></h1>
								</xsl:with-param>
								<xsl:with-param name="content">
									<xsl:call-template name="content"/>
								</xsl:with-param>
							</xsl:call-template>
						</td>
        			
        				<!--
						<td class="padded-content" colspan="3">
							<h1><xsl:apply-templates select="/" mode="title"/></h1>
							<xsl:call-template name="content"/>
						</td>
						-->
        			</tr>
        		</table>
			</body>
		</html>
	</xsl:template>
	
	<!--
	title
	-->
	<xsl:template mode="title" match="/">
		<xsl:value-of select="/root/gui/strings/title"/>
	</xsl:template>
	
</xsl:stylesheet>
