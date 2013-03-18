<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="main.xsl"/>

	<!--
	additional scripts
	-->
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<script type="text/javascript" src="{/root/gui/url}/scripts/geonetwork.js"/>
		<script type="text/javascript" language="JavaScript">
			
			function updateUserPw()
			{
				var invalid = " "; // Invalid character is a space
        var minLength = 6; // Minimum length

        var pw1 = document.changepwdform.password.value;
        var pw2 = document.changepwdform.password2.value;

        // check for a value in both fields.
        if (pw1 == '' || pw2 == '')
        {
          alert('Please enter your password twice.');
          return;
        }
        // check for minimum length
        if (document.changepwdform.password.value.length &lt; minLength)
        {
          alert('Your password must be at least ' + minLength + ' characters long. Try again.');
          return;
        }
        // check for spaces
        if (document.changepwdform.password.value.indexOf(invalid) &gt; -1)
        {
          alert("Sorry, spaces are not allowed.");
          return;
        }
        // check for bad password confirmation
        if (pw1 != pw2)
        {
          alert ("You did not enter the same new password twice. Please re-enter your password.");
          return;
        }
	
				// all ok, proceed
				document.changepwdform.submit();
			}//update
			
		</script>
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title">
				<xsl:value-of select="/root/gui/strings/changePassword"/>
			</xsl:with-param>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
			<!--
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160; -->
				<button class="content" onclick="updateUserPw('{/root/gui/locService}/password.change.submit')"><xsl:value-of select="/root/gui/strings/save"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	form
	-->
	<xsl:template name="form">
		<form name="changepwdform" accept-charset="UTF-8" action="{/root/gui/locService}/password.change.submit" method="post">
			<input type="hidden" name="username" size="-1" value="{/root/request/username}"/>
			<input type="hidden" name="changeKey" size="-1" value="{/root/request/changeKey}"/>
			<table>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/username"/></th>
					<td class="padded"><xsl:value-of select="/root/request/username"/></td>
				</tr>
				<tr>
			  	<th class="padded"><xsl:value-of select="/root/gui/strings/password"/></th>
					<td class="padded"><input class="content" type="password" name="password"/></td>
				</tr>
				<tr>
				  <th class="padded"><xsl:value-of select="/root/gui/strings/confirmPassword"/></th>
					<td class="padded"><input class="content" type="password" name="password2"/></td>
				</tr>
			</table>
		</form>
	</xsl:template>

</xsl:stylesheet>

