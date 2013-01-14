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
			<body width="100%" onload="init();">
			
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


	<!--
	main page
	-->
	<xsl:template match="/">
		<html>
			<head>
				<xsl:call-template name="header"/>
				
				<style type="text/css">
					body {
						height:100%;
					}
				</style>
			</head>
			<body>
				<div id="header">
					<xsl:call-template name="banner"/>
				</div>
			
				<div id="content_container" >
					<xsl:call-template name="content"/>
				</div>
			</body>
		</html>
	</xsl:template>

	<xsl:template mode="css" match="/"/>

	<xsl:template name="formLayout">
		<xsl:param name="title"/>
		<xsl:param name="content"/>
		<xsl:param name="buttons"/>
		<xsl:param name="indent" select="100"/>
		
		<table  width="100%" height="100%">
	
			<!-- title -->
			<xsl:if test="$title">
				<xsl:call-template name="formTitle">
					<xsl:with-param name="title" select="$title"/>
					<xsl:with-param name="indent" select="$indent"/>
				</xsl:call-template>
				<xsl:call-template name="formSeparator"/>
			</xsl:if>
			
			<!-- content -->
			<xsl:call-template name="formContent">
				<xsl:with-param name="content" select="$content"/>
				<xsl:with-param name="indent" select="$indent"/>
			</xsl:call-template>
			
			<!-- buttons -->
			<xsl:if test="$buttons">
				<xsl:call-template name="formSeparator"/>
				<xsl:choose>
					<xsl:when test="/root/request/modal">
						<!-- remove the back button -->
						<xsl:variable name="buttonsTmp" select="exslt:node-set($buttons)"/>
						<xsl:variable name="buttonsNoBack">
							<xsl:apply-templates mode="buttons" select="$buttonsTmp/*"/>
						</xsl:variable>
						<xsl:call-template name="formContent">
							<xsl:with-param name="content" select="$buttonsNoBack"/>
							<xsl:with-param name="indent" select="$indent"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="formContent">
							<xsl:with-param name="content" select="$buttons"/>
							<xsl:with-param name="indent" select="$indent"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
			
			<!-- footer -->
			<xsl:if test="not(/root/request/modal)">
				<xsl:call-template name="formFiller">
					<xsl:with-param name="indent" select="$indent"/>
				</xsl:call-template>
				<tr><td class="blue-content" colspan="3"/></tr>
			</xsl:if>
		</table>
	</xsl:template>

	<xsl:template name="formTitle">
		<xsl:param name="title"/>
		<xsl:param name="indent" select="100"/>
		
		<tr>
			<xsl:choose>
				<xsl:when test="not(/root/request/modal)">
					<td class="padded-content" width="{$indent}"/>
					<td class="dots"/>
					<td class="padded-content">
						<h1><xsl:value-of select="$title"/></h1>
					</td>
				</xsl:when>
				<xsl:otherwise>
					<td class="padded-content" colspan="3" align="center">
						<h1><xsl:value-of select="$title"/></h1>
					</td>
				</xsl:otherwise>
			</xsl:choose>
		</tr>
	</xsl:template>
	
	<xsl:template name="formContent">
		<xsl:param name="content"/>
		<xsl:param name="indent" select="100"/>
		
		<tr>
			<xsl:choose>
				<xsl:when test="not(/root/request/modal)">
					<td class="padded-content" width="{$indent}"/>
    				<td class="dots"/>
					<td class="padded-content" align="center">
						<xsl:copy-of select="$content"/>
					</td>
				</xsl:when>
				<xsl:otherwise>
					<td class="padded-content" colspan="3" align="center">
						<xsl:copy-of select="$content"/>
					</td>
				</xsl:otherwise>
			</xsl:choose>
		</tr>
	</xsl:template>
	
	<xsl:template name="formSection">
		<xsl:param name="title"/>
		<xsl:param name="indent" select="100"/>
		
		<tr>
			<td class="content" width="{$indent}" valign="bottom">
				<table width="100%"><tr><td class="dots"/></tr></table>
			</td>
			<td class="content" valign="bottom" colspan="2">
				<table width="100%"><tr>
					<td class="green-content"><xsl:copy-of select="$title"/></td>
					<td class="content" width="100%" valign="bottom">
						<table width="100%"><tr><td class="dots"/></tr></table>
					</td>
				</tr></table>
			</td>
		</tr>
	</xsl:template>
	
	<xsl:template name="formSeparator">
		<tr><td class="dots" colspan="3"/></tr>
	</xsl:template>
	
	<xsl:template name="formFiller">
		<xsl:param name="indent" select="100"/>
		
		<tr height="100%">
			<td class="padded-content" width="{$indent}"/>
			<td class="dots"/>
			<td class="padded-content">
			</td>
		</tr>
	</xsl:template>

	<!-- when displaying a page using modalbox, it makes no sense to have 
		 a back button so copy everything in the supplied buttons except
			 the back button -->

	<xsl:variable name="backButtonName" select="/root/gui/strings/back"/>

	<xsl:template mode="buttons" match="button[normalize-space()=$backButtonName]" priority="20"/>

	<xsl:template mode="buttons" match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates mode="buttons" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>


</xsl:stylesheet>
