<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:exslt="http://exslt.org/common">

	<xsl:output
		omit-xml-declaration="yes" 
		method="html" 
		doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
		doctype-system="http://www.w3.org/TR/html4/loose.dtd"
		indent="yes"
		encoding="UTF-8" />
	
	<xsl:include href="header.xsl"/>
	<xsl:include href="banner.xsl"/>
	<xsl:include href="utils.xsl"/>

	<!--
	main page
	-->
	<xsl:template match="/">
	<html>
		<head>
			<xsl:call-template name="header"/>
		</head>
		<body>
			<table width="100%">
	
				<!-- title -->
				<tr class="banner">
					<td class="banner">
						<img src="{/root/gui/url}/images/header-left.jpg" alt="World picture" align="top" />
					</td>
					<td align="right" class="banner">
						<img src="{/root/gui/url}/images/header-right.gif" alt="GeoNetwork opensource logo" align="top" />
					</td>
				</tr>
			</table>
			
			<table width="100%">
				<tr>
					<xsl:choose>
						<xsl:when test="/root/gui/startupError/error">
							<td style="color: #ffffff; background: #ff0000; font-weight: bold; padding-top: 2px; padding-bottom: 2px; padding-left: 4px; padding-right: 4px;" colspan="3">GeoNetwork Initialization Failed</td>
						</xsl:when>
						<xsl:otherwise>
							<td style="color: #ffffff; background: #beb800; font-weight: bold; padding-top: 2px; padding-bottom: 2px; padding-left: 4px; padding-right: 4px;" colspan="3">GeoNetwork Initialization Successful</td>
						</xsl:otherwise>
					</xsl:choose>
				</tr>

				<xsl:apply-templates mode="showError" select="/root/gui/startupError/error/Error"/>
				<xsl:apply-templates mode="showError" select="/root/gui/startupError/error/*[name()!='Error' and name()!='Stack']"/>
				<xsl:apply-templates mode="showError" select="/root/gui/startupError/error/Stack"/>
			</table>

		</body>
	</html>
	</xsl:template>

	<xsl:template mode="showError" match="*">
		<tr>
				<td width="15%" class="padded-content">
					<b><code><xsl:value-of select="name(.)"/></code></b>
				</td>
				<td class="dots"/>
				<td class="padded-content">
					<code><xsl:value-of select="string(.)"/></code>
				</td>
		</tr>
	</xsl:template>

	<xsl:template mode="css" match="/"/>

</xsl:stylesheet>
