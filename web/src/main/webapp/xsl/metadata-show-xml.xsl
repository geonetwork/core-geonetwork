<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork" 
	xmlns:exslt="http://exslt.org/common"
	xmlns:dc = "http://purl.org/dc/elements/1.1/" 
	xmlns:gmd="http://www.isotc211.org/2005/gmd" 
	xmlns:gco="http://www.isotc211.org/2005/gco"
	exclude-result-prefixes="gco gmd dc exslt geonet">

	<!--
	show metadata form
	-->
	

	<xsl:output
		omit-xml-declaration="yes" 
		method="xml" 
		indent="no"
		encoding="UTF-8" />
	
	<xsl:include href="header.xsl"/>
	<xsl:include href="banner.xsl"/>
	<xsl:include href="utils.xsl"/>
	<xsl:include href="metadata.xsl"/>
    <xsl:include href="mapfish_includes.xsl"/>

    <xsl:variable name="protocol" select="/root/gui/env/server/protocol" />
	<xsl:variable name="host" select="/root/gui/env/server/host" />
	<xsl:variable name="port" select="/root/gui/env/server/port" />
	<xsl:variable name="baseURL" select="concat($protocol,'://',$host,':',$port,/root/gui/url)" />
	<xsl:variable name="serverUrl" select="concat($protocol,'://',$host,':',$port,/root/gui/locService)" />
	<xsl:template match="*" mode="js-translations" priority="100">
	</xsl:template>
	
	<xsl:template mode="css" match="/">
		<xsl:call-template name="geoCssHeader"/>
		<xsl:call-template name="ext-ux-css"/>
	</xsl:template>
	<xsl:template match="/">
		<html>
			<head>
				<xsl:call-template name="header"/>
				
				<style type="text/css">
					body {
						height:100%;
						width:100%;
						border:0px;
						padding:0px;
					}
				</style>
			</head>
			<body width="100%" >
			
		        <table width="100%">
		
		            <!-- print banner -->
		            <tr id="banner-img1" class="banner doprint" style="display:block;white-space:nowrap">
		                <td class="banner" colspan="2" width="100%"><div style="width:1024px">
		                    <img src="{/root/gui/url}/images/geocat_logo_li.gif" alt="geocat.ch logo"/>
		                    <img src="{/root/gui/url}/images/header-background-print.jpg" alt="geocat.ch logo"/>
		                    <img src="{/root/gui/url}/images/bg_kopf_geocat.gif" alt="geocat.ch logo"/>
		                </div></td>
		            </tr>
		
					</table>
				<div id="content_container">
					<xsl:call-template name="content"/>
				</div>

			</body>
		</html>
	</xsl:template>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:param name="schema">
			<xsl:apply-templates mode="schema" select="."/>
		</xsl:param>

		<table  width="100%" height="100%">
			<xsl:for-each select="/root/*[name(.)!='gui' and name(.)!='request']"> <!-- just one -->
				<tr height="100%">
					<td class="content" valign="top">
						
						<xsl:variable name="md">
							<xsl:apply-templates mode="brief" select="."/>
						</xsl:variable>
						<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
						<xsl:variable name="mdURL" select="normalize-space(concat($baseURL, '?uuid=', geonet:info/uuid))"/>
						
						<table width="100%">
							<tr><td class="padded-content">
								<table class="md" width="100%">
									<form name="mainForm" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/metadata.edit">
										<input type="hidden" name="id" value="{geonet:info/id}"/>
										<input type="hidden" name="currTab" value="{/root/gui/currTab}"/>
										
										<xsl:choose>
											<xsl:when test="$currTab='xml'">
												<xsl:apply-templates mode="xmlDocument" select="."/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:apply-templates mode="elementEP" select="."/>
											</xsl:otherwise>
										</xsl:choose>
										
									</form>
								</table>
							</td></tr>
						</table>
					</td>
				</tr>
			</xsl:for-each>
			<tr><td class="blue-content" colspan="3"/></tr>
		</table>
	</xsl:template>



</xsl:stylesheet>
