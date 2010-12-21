<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork" 
	xmlns:exslt = "http://exslt.org/common"
	xmlns:dc = "http://purl.org/dc/elements/1.1/" 
	xmlns:gmd="http://www.isotc211.org/2005/gmd" 
	xmlns:gco="http://www.isotc211.org/2005/gco"
	exclude-result-prefixes="#all">
	<xsl:output method="html"/>

	<!--
	show metadata form
	-->
	
	<xsl:include href="../../../../xsl/main.xsl"/>
	<xsl:include href="../../../../xsl/metadata.xsl"/>
	
	<xsl:variable name="baseurl" select="//geonet:info/baseUrl"/>
	<xsl:variable name="locserv" select="//geonet:info/locService"/>

	<xsl:template match="/">
		<xsl:apply-templates mode="doit" select="*"/>
	</xsl:template>

	<xsl:template mode="doit" match="*">
		<html>
			<head>
				<xsl:call-template name="myheader"/>
			</head>
			<body>
				<table width="100%">
						<!-- banner -->
						<tr><td>
							<xsl:call-template name="mybanner"/>
						</td></tr>
						<tr><td align="center">
							<a class="content" href="{$baseurl}" target="_blank"  alt="Opens new window">Go to the catalog that holds this metadata record</a>
						</td></tr>

						<!-- content -->
						<tr><td>
								<xsl:call-template name="content"/>
						</td></tr>
				</table>
			</body>
		</html>
	</xsl:template>
		
	<xsl:template name="myheader">
		<link href="{$baseurl}/favicon.ico" rel="shortcut icon" type="image/x-icon" />
		<link href="{$baseurl}/favicon.ico" rel="icon" type="image/x-icon" />

		<!-- stylesheet -->
		<link rel="stylesheet" type="text/css" href="{$baseurl}/geonetwork.css"/>
		<link rel="stylesheet" type="text/css" href="{$baseurl}/modalbox.css"/>
	</xsl:template>

	<xsl:template name="mybanner">
		<table width="100%">
			<tr class="banner">
				<td class="banner">
					<img src="{$baseurl}/images/header-left.jpg" alt="World picture" align="top" />
				</td>
				<td align="right" class="banner">
					<img src="{$baseurl}/images/header-right.gif" alt="GeoNetwork opensource logo" align="top" />
				</td>
			</tr>
		</table>
	</xsl:template>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:param name="schema">
			<xsl:apply-templates mode="schema" select="."/>
		</xsl:param>
		
		<table  width="100%">
				<tr>
					<td class="content" valign="top">
						
					<xsl:variable name="md">
						<xsl:apply-templates mode="brief" select="."/>
					</xsl:variable>
					<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
					
					<table width="100%">
						<tr>
							<td class="padded-content">
							<table class="md" width="100%">
								<xsl:apply-templates mode="elementEP" select=".">
									<xsl:with-param name="embedded" select="true()" />
								</xsl:apply-templates>
							</table>
						</td></tr>
					</table>
				</td>
			</tr>
			<tr>
				<td align="center">
					<a class="content" href="{$baseurl}" target="_blank"  alt="Opens new window">Go to the catalog that holds this metadata record</a>
				</td>
			</tr>
			<tr>
				<td class="blue-content"/>
			</tr>
		</table>
	</xsl:template>
	
</xsl:stylesheet>
