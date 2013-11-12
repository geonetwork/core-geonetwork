<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="../main.xsl"/>
	<xsl:include href="virtualcsw-update-utils.xsl"/>
	
	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
<script type="text/javascript" language="JavaScript">
			
			function update1()
			{
				var invalid = " "; // Invalid character is a space
				var minLength = 6; // Minimum length
	
				if (document.virtualcswupdateform.servicename.value.length == 0)
				{
					alert("<xsl:value-of select="/root/gui/strings/virtualcswServicenameMandatory"/>");
					return;
				}

				// all ok, proceed
				document.virtualcswupdateform.submit();
			}//update
</script>
		
		<xsl:call-template name="user-admin-js"/>
	</xsl:template>
	
	<!--
	page content
	-->
		
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title">
				<xsl:value-of select="/root/gui/strings/virtualcswUpdateService"/>
			</xsl:with-param>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="update1()"><xsl:value-of select="/root/gui/strings/save"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	form
	-->
	<xsl:template name="form">
	
		<form name="virtualcswupdateform" accept-charset="UTF-8" action="{/root/gui/locService}/virtualcsw.config.update?operation=updateservice" method="post">
			
			<input type="hidden" name="id" value="{/root/response/id}"/>

			
			<table class="text-aligned-left">
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/virtualcswServiceName"/> (*)</th>
					<td class="padded"><input class="content" type="text" name="servicename" value="{if (/root/gui/services/name != '') then /root/gui/services/name else 'csw-'}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/virtualcswServiceDescription"/></th>
					<td class="padded"><input class="content" size="40" type="text" name="servicedescription" value="{/root/gui/services/description}"/></td>
				</tr>
				
				<tr><td colspan="2"><br/><hr style="border:1px solid; color:#eee"/><br/></td></tr>
				
				<input class="content" type="hidden" name="classname" value=".services.main.CswDiscoveryDispatcher"/>
				
				<xsl:call-template name="virtualcswinfofields"/>
			</table>
		</form>
	</xsl:template>
	
</xsl:stylesheet>

