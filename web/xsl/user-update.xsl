<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>
	
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
					alert('The username field is mandatory.');
					return;
				}

				var pw1 = document.userupdateform.password.value;
				var pw2 = document.userupdateform.password2.value;
	
				// check for a value in both fields.
				if (pw1 == '' || pw2 == '')
				{
					alert('Please enter your password twice.');
					return;
				}
				// check for minimum length
				if (document.userupdateform.password.value.length &lt; minLength)
				{
					alert('Your password must be at least ' + minLength + ' characters long. Try again.');
					return;
				}
				// check for spaces
				if (document.userupdateform.password.value.indexOf(invalid) &gt; -1)
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

				// check at least one group selected
				if ($F('groups') == '')
				{
					alert('Please, select at least one group');
					return;
				}
				
				// all ok, proceed
				document.userupdateform.submit();
			}//update

			function profileChanged()
			{
				var profile = $F('user.profile');

				if (profile == 'Administrator')
					Element.hide('group.list');
				else
					Element.show('group.list');
			}
			
			function init()
			{
				profileChanged();
			}
</script>
	</xsl:template>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title">
				<xsl:choose>
					<xsl:when test="/root/response/record/id"><xsl:value-of select="/root/gui/strings/update"/></xsl:when>
					<xsl:otherwise><xsl:value-of select="/root/gui/strings/insert"/></xsl:otherwise>
				</xsl:choose>
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
		<form name="userupdateform" accept-charset="UTF-8" action="{/root/gui/locService}/user.update" method="post">
			<xsl:if test="/root/response/record/id">
				<input type="hidden" name="id" size="-1" value="{/root/response/record/id}"/>
			</xsl:if>
			<table>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/username"/> (*)</th>
					<td class="padded"><input class="content" type="text" name="username" value="{/root/response/record/username}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/password"/> (*)</th>
					<td class="padded"><input class="content" type="password" name="password" value="{/root/response/record/password}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/confirmPassword"/> (*)</th>
					<td class="padded"><input class="content" type="password" name="password2" value="{/root/response/record/password}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/surName"/></th>
					<td class="padded"><input class="content" type="text" name="surname" value="{/root/response/record/surname}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/firstName"/></th>
					<td class="padded"><input class="content" type="text" name="name" value="{/root/response/record/name}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/address"/></th>
					<td class="padded"><input class="content" type="text" name="address" value="{/root/response/record/address}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/state"/></th>
					<td class="padded">
						<input class="content" type="text" name="state" value="{/root/response/record/state}" size="8"/>
						<b><xsl:value-of select="/root/gui/strings/zip"/></b>
						<input class="content" type="text" name="zip" value="{/root/response/record/zip}" size="8"/>
					</td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/country"/></th>
					<td class="padded">
						<select class="content" size="1" name="country">
							<xsl:if test="string(/root/response/record/country)=''">
								<option value=""/>
							</xsl:if>
							<xsl:for-each select="/root/gui/countries/country">
								<option value="{@iso2}">
									<xsl:if test="string(/root/response/record/country)=@iso2">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="."/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/email"/></th>
					<td class="padded"><input class="content" type="text" name="email" value="{/root/response/record/email}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/organisation"/></th>
					<td class="padded"><input class="content" type="text" name="org" value="{/root/response/record/organisation}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/kind"/></th>
					<td class="padded">
						<select class="content" size="1" name="kind">
							<xsl:for-each select="/root/gui/strings/kindChoice">
								<option value="{@value}">
									<xsl:if test="string(/root/response/record/kind)=@value">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="."/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/profile"/></th>
					<td class="padded">
						<select class="content" size="1" name="profile" onchange="profileChanged()" id="user.profile">
							<!--  When adding a new user, make Editor the default selected profile -->
							<!-- <xsl:for-each select="/root/gui/profiles/*">
							<option value="{name(.)}">
							<xsl:if test="/root/response/record/profile=name(.)">
							<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:if test="count(/root/response/record)=0 and name(.)='Editor'">
							<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:value-of select="name(.)"/>
							</option>
							</xsl:for-each> -->
							<xsl:for-each select="/root/gui/profiles/Administrator">
								<option value="{name(.)}">
									<xsl:if test="/root/response/record/profile=name(.)">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="/root/gui/strings/Administrator"/>
								</option>
							</xsl:for-each>
							<xsl:for-each select="/root/gui/profiles/UserAdmin">
								<option value="{name(.)}">
									<xsl:if test="/root/response/record/profile=name(.)">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="/root/gui/strings/UserAdmin"/>
								</option>
							</xsl:for-each>
							<xsl:for-each select="/root/gui/profiles/Editor">
								<option value="{name(.)}">
									<xsl:if test="/root/response/record/profile=name(.) or (count(/root/response/record)=0 and name(.)='Editor')">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="/root/gui/strings/Editor"/>
								</option>
							</xsl:for-each>
							<xsl:for-each select="/root/gui/profiles/RegisteredUser">
								<option value="{name(.)}">
									<xsl:if test="/root/response/record/profile=name(.)">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="/root/gui/strings/RegisteredUser"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
				
				<!-- Add groups -->
				
				<xsl:variable name="lang" select="/root/gui/language"/>

				<tr id="group.list">
					<th class="padded"><xsl:value-of select="/root/gui/strings/groups"/></th>
					<td class="padded">
						<select class="content" size="7" name="groups" multiple="" id="groups">
							<xsl:for-each select="/root/gui/groups/record">
								<option value="{id}">
									<xsl:variable name="aGroup" select="id"/>
									<xsl:for-each select="/root/response/groups/id">
										<xsl:if test="$aGroup=(.)">
											<xsl:attribute name="selected"/>
										</xsl:if>
									</xsl:for-each>
									<xsl:value-of select="label/child::*[name() = $lang]"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>
	
</xsl:stylesheet>

