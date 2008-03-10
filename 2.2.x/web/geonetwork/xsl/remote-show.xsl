<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork">

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
							<xsl:variable name="server" select="geonet:info/server"/>
							<xsl:variable name="name" select="/root/gui/repositories/Collection[@collection_dn=$server]/@collection_name"/>
							
							<tr><td class="padded-content"><h1><xsl:value-of select="geonet:info[server]/id"/><xsl:text> - </xsl:text><xsl:value-of select="$name"/></h1></td></tr>
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
