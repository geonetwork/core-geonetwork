<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>
	<xsl:include href="user-update-utils.xsl"/>
	
	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
<script type="text/javascript" language="JavaScript">
			
			function update1()
			{
				var invalid = " "; // Invalid character is a space
				var minLength = 6; // Minimum length
	
				if (document.userupdateform.username.value.length == 0)
				{
					alert("<xsl:value-of select="/root/gui/strings/usernameMandatory"/>");
					return;
				}

				var pw1 = document.userupdateform.password.value;
				var pw2 = document.userupdateform.password2.value;
	
				// check for a value in both fields.
				if (pw1 == '' || pw2 == '')
				{
					alert("<xsl:value-of select="/root/gui/strings/passwordEntry"/>");
					return;
				}
				// check for minimum length
				if (document.userupdateform.password.value.length &lt; minLength)
				{
					alert("<xsl:value-of select="/root/gui/strings/passwordLength"/>");
					return;
				}
				// check for spaces
				if (document.userupdateform.password.value.indexOf(invalid) &gt; -1)
				{
					alert("<xsl:value-of select="/root/gui/strings/passwordSpace"/>");
					return;
				}
				// check for bad password confirmation
				if (pw1 != pw2)
				{
					alert ("<xsl:value-of select="/root/gui/strings/passwordDoNotMatch"/>");
					return;
				}

				// check surname specified
				if (document.userupdateform.surname.value == '') {
						alert("<xsl:value-of select="/root/gui/strings/surnameMandatory"/>");
						return;
				}

				// check firstname specified
				if (document.userupdateform.name.value == '') {
						alert("<xsl:value-of select="/root/gui/strings/firstnameMandatory"/>");
						return;
				}

				// check email specified
				if (document.userupdateform.email.value == '') {
						alert("<xsl:value-of select="/root/gui/strings/emailMandatory"/>");
						return;
				}
				
				
				// all ok, proceed
				document.userupdateform.submit();
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
				<xsl:value-of select="/root/gui/strings/insert"/>
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
		<form name="userupdateform" accept-charset="UTF-8" action="{/root/gui/locService}/user.update?operation=newuser" method="post">
			<input type="hidden" name="id" value="{/root/response/record/id}"/>
			<table class="text-aligned-left">
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/username"/> (*)</th>
					<td class="padded"><input class="content" type="text" name="username" value="{/root/response/record/username}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/password"/> (*)</th>
					<td class="padded"><input class="content" type="password" name="password" value=""/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/confirmPassword"/> (*)</th>
					<td class="padded"><input class="content" type="password" name="password2" value=""/></td>
				</tr>
				<xsl:call-template name="userinfofields"/>
			</table>
		</form>
	</xsl:template>
	
</xsl:stylesheet>

