<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="edit.xsl"/>

	<!--
	additional scripts
	-->
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<script type="text/javascript" language="JavaScript">
			
			function updateUserPw()
			{
				var invalid = " "; // Invalid character is a space
        var minLength = 6; // Minimum length

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
	
				// all ok, proceed
				document.userupdateform.submit();
			}//update
			
		</script>
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title">
				<xsl:value-of select="'Reset Password'"/>
			</xsl:with-param>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="updateUserPw()"><xsl:value-of select="/root/gui/strings/save"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	form
	-->
	<xsl:template name="form">
		<form name="userupdateform" accept-charset="UTF-8" action="{/root/gui/locService}/user.update" method="post">
			<input type="hidden" name="id" value="{/root/response/record/id}"/>
			<input type="hidden" name="operation" value="resetpw"/>
			<input type="hidden" name="username" value="tweedledee"/>
			<input type="hidden" name="surname" value="tweedledum"/>
			<input type="hidden" name="name" value="alice"/>
			<input type="hidden" name="profile" value="cheshire"/>
			<input type="hidden" name="groups" value="cat"/>
			<table>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/username"/></th>
					<td class="padded"><xsl:value-of select="/root/response/record/username"/></td>
				</tr>
				<tr>
			  	<th class="padded"><xsl:value-of select="/root/gui/strings/password"/></th>
					<td class="padded"><input class="content" type="password" name="password"/></td>
				</tr>
				<tr>
				  <th class="padded"><xsl:value-of select="/root/gui/strings/confirmPassword"/></th>
					<td class="padded"><input class="content" type="password" name="password2"/></td>
				</tr>

				<!-- Add groups -->				

				<xsl:variable name="lang" select="/root/gui/language"/>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/groups"/></th>
					<td class="padded">
							<xsl:for-each select="/root/gui/groups/record">
								<xsl:variable name="aGroupName" select="label/child::*[name() = $lang]"/>
								<xsl:variable name="aGroup" select="id"/>
								<xsl:for-each select="/root/response/groups/id">
									<xsl:if test="$aGroup=(.)">
										<xsl:value-of select="$aGroupName"/><br/>
									</xsl:if>
								</xsl:for-each>
							</xsl:for-each>
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>

</xsl:stylesheet>

