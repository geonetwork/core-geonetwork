<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="geonet">

	<xsl:include href="main.xsl"/>
	<xsl:include href="metadata.xsl"/>

	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
		<script language="JavaScript1.2" type="text/javascript">
			
			function doAction(action)
			{
				// alert("In doAction(" + action + ")"); // DEBUG
				document.mainForm.action = action;
				goSubmit('mainForm');
			}
			
			function doTabAction(action, tab)
			{
				// alert("In doTabAction(" + action + ", " + tab + ")"); // DEBUG
				document.mainForm.currTab.value = tab;
				doAction(action);
			}
		</script>
	</xsl:template>
	
	<!--
	page content
	-->
	<xsl:template name="content">
	
		<xsl:apply-templates mode="remoteHit" select="/root/*[name(.)!='gui' and name(.)!='request']"/> <!-- just one -->
		
	</xsl:template>

	<xsl:template mode="remoteHit" match="*">
		<table  width="100%" height="100%">
		
			<tr height="100%">
				<td class="blue-content" width="150" valign="top">
					<xsl:call-template name="tab">
						<xsl:with-param name="tabLink" select="concat(/root/gui/locService,'/remote.show')"/>
					</xsl:call-template>
				</td>
				<td class="content" valign="top">
					<table width="100%">
							<xsl:variable name="collection" select="geonet:info/collection"/>
							<xsl:variable name="repocode" select="substring-before(geonet:info/server,':')"/>
							<xsl:variable name="name" select="/root/gui/repositories/z3950repositories/repository[id/@code=$collection and id/@serverCode=$repocode]/label"/>
							
							<tr><td class="padded-content">
							<h1>
								<xsl:value-of select="geonet:info[server]/id"/><xsl:text> - </xsl:text><xsl:value-of select="$name"/>
								<xsl:if test="geonet:info/html">
									<xsl:text> - </xsl:text>
									<xsl:choose>
										<xsl:when test="geonet:info/html/@error"> 
											&#160;
											<img src="{/root/gui/url}/images/important.png" onclick="$('html.error').toggle()">
											<span id="html.error" class="searchHelpFrame" style="display:none;z-index:1000;">
												<font class="error">
													<xsl:value-of select="geonet:info/html/@error"/>
												</font>
											</span>
											</img>
										</xsl:when>
										<xsl:otherwise>
											<a href="{geonet:info/html}"><xsl:value-of select="/root/gui/strings/showRemoteHTML"/></a> 
										</xsl:otherwise>
									</xsl:choose>
								</xsl:if>
							</h1></td></tr>
							<tr><td class="dots"/></tr>
							<tr><td class="padded-content">
								<table class="md" width="100%">
									
									<form name="mainForm" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/remote.show">
										<input type="hidden" name="id" value="{geonet:info[server]/id}"/>
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
									
									<!--
									<xsl:apply-templates mode="elementEP" select="."/>
									-->
									
								</table>
							</td></tr>
					</table>
				</td>
			</tr>
			<tr><td class="blue-content" colspan="3"/></tr>
		</table>
	</xsl:template>

</xsl:stylesheet>
