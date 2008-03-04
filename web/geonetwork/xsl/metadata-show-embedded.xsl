<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork" 
	xmlns:dc = "http://purl.org/dc/elements/1.1/" 
	xmlns:gmd="http://www.isotc211.org/2005/gmd" 
	xmlns:gco="http://www.isotc211.org/2005/gco">

	<!--
	show metadata form
	-->
	
	<xsl:include href="main.xsl"/>
	<xsl:include href="metadata.xsl"/>
	
	<xsl:variable name="host" select="/root/gui/env/server/host" />
	<xsl:variable name="port" select="/root/gui/env/server/port" />
	<xsl:variable name="serverUrl" select="concat('http://',$host,':',$port,/root/gui/locService)" />
	
	<xsl:template match="/">
		<table width="100%" height="100%">
			
			<!-- content -->
			<tr height="100%"><td>
				<xsl:call-template name="content"/>
			</td></tr>
		</table>
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
<!--					<td class="blue-content" width="150" valign="top">
						<xsl:call-template name="tab">
							<xsl:with-param name="tabLink" select="concat(/root/gui/locService,'/metadata.show')"/>
						</xsl:call-template>
					</td>-->
					<td class="content" valign="top">
						
						<xsl:variable name="mdURL" select="normalize-space(concat($serverUrl, '/metadata.show?id=', geonet:info/id))"/>
						<xsl:variable name="mdTitle" select="geonet:info/title" /> <!-- FIXME info is not available by default -->
						
						<xsl:if test="not(contains($mdURL,'localhost')) and not(contains($mdURL,'127.0.0.1'))">
							<p align="right">
								<a href="http://del.icio.us/post?url={$mdURL}&amp;title={$mdTitle}">
									<img src="{/root/gui/url}/images/delicious.gif" 
										alt="Bookmark on Delicious" title="Bookmark on Delicious" 
										style="border: 0px solid;padding:2px;"/>
								</a>
								<a href="http://digg.com/submit?url={$mdURL}&amp;title={$mdTitle}">
									<img src="{/root/gui/url}/images/digg.gif" 
										alt="Bookmark on Digg" title="Bookmark on Digg" 
										style="border: 0px solid;padding:2px;"/>
								</a>
								<a href="http://www.facebook.com/sharer.php?u={$mdURL}">
									<img src="{/root/gui/url}/images/facebook.gif" 
										alt="Bookmark on Facebook" title="Bookmark on Facebook" 
										style="border: 0px solid;padding:2px;"/>
								</a>
								<a href="http://www.stumbleupon.com/submit?url={$mdURL}&amp;title={$mdTitle}">
									<img src="{/root/gui/url}/images/stumbleupon.gif" 
										alt="Bookmark on StumbleUpon" title="Bookmark on StumbleUpon" 
										style="border: 0px solid;padding:2px;"/>
								</a> 
							</p>
						</xsl:if>
						
						<table width="100%">
						
							<xsl:variable name="buttons">
								<tr><td class="padded-content" height="100%" align="center" valign="top">
									<xsl:call-template name="buttons"/>
								</td></tr>
							</xsl:variable>
							<xsl:if test="$buttons!=''">
								<xsl:copy-of select="$buttons"/>
							</xsl:if>
							<tr>
								<td align="center" valign="left" class="padded-content">
								</td>
							</tr>
							<!-- subtemplate title button -->
							<xsl:if test="(string(geonet:info/isTemplate)='s')">
								<tr><td class="padded-content" height="100%" align="center" valign="top">
									<b><xsl:value-of select="geonet:info/title"/></b>
								</td></tr>
							</xsl:if>

							<tr><td class="padded-content">
								<table class="md" width="100%">
<!--									<form name="mainForm" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/metadata.edit">
										<input type="hidden" name="id" value="{geonet:info/id}"/>
										<input type="hidden" name="currTab" value="{/root/gui/currTab}"/>
-->										
										<xsl:choose>
											<xsl:when test="$currTab='xml'">
												<xsl:apply-templates mode="xmlDocument" select="."/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:apply-templates mode="elementEP" select="."/>
											</xsl:otherwise>
										</xsl:choose>
										
<!--									</form>-->
								</table>
							</td></tr>
							
							<xsl:if test="$buttons!=''">
								<xsl:copy-of select="$buttons"/>
							</xsl:if>
							
						</table>
					</td>
				</tr>
			</xsl:for-each>
			<tr>
				<td class="blue-content" />
			</tr>
		</table>
	</xsl:template>
	
</xsl:stylesheet>
