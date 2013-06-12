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
				var maxLength = 48; //Maximum length
				var regexp = new RegExp( "[ÀÂÇÈÉÊËÎÔÙÛàâçèéêëîôùû]", "gi" ) ;


				// check service name specified
				if (document.virtualcswupdateform.servicename.value.length == 0)
				{
					alert("<xsl:value-of select="/root/gui/strings/virtualcswServicenameMandatory"/>");
					return;
				}

				// check special characters				
				var resultat = document.virtualcswupdateform.servicename.value.match(regexp) ;
				if(resultat) {
				  	alert("<xsl:value-of select="/root/gui/strings/virtualcswServicenameSpecialChars"/>");
				  	return;
				  }
				
				// check service name length
				if (document.virtualcswupdateform.servicename.value.length >= 48)
				{
					alert("<xsl:value-of select="/root/gui/strings/virtualcswServicenameTooLong"/>");
					return;
				}
				
				if (document.virtualcswupdateform.servicename.value.substring(0,4)!='csw-')
				{
					alert("<xsl:value-of select="/root/gui/strings/virtualcswServiceNameStartsWith"/>");
					return;
				}
				
				
				if (document.virtualcswupdateform.servicename.value.indexOf(invalid) > -1) {
					alert(translate('spacesNot'));
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
				<xsl:value-of select="/root/gui/strings/virtualcswInsert"/>
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
	
		<form name="virtualcswupdateform" accept-charset="UTF-8" action="{/root/gui/locService}/virtualcsw.config.update?operation=newservice" method="post">
			
			<input type="hidden" name="id" value="{/root/response/record/id}"/>
			
			<table class="text-aligned-left">
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/virtualcswServiceName"/> (*)</th>
					<td class="padded"><input class="content" type="text" name="servicename" value="csw-"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/virtualcswServiceDescription"/></th>
					<td class="padded"><input class="content" size="40" type="text" name="servicedescription" value=""/></td>
				</tr>
				
				<tr><td colspan="2"><br/><hr style="border:1px solid; color:#eee"/><br/></td></tr>

				<input class="content" type="hidden" name="classname" value=".services.main.CswDiscoveryDispatcher"/>
				
				<xsl:call-template name="virtualcswinfofields"/>
			</table>
		</form>
	</xsl:template>
	
</xsl:stylesheet>

