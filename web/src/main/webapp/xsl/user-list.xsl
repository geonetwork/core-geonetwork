<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	

	<xsl:include href="main.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/userManagement"/>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="load('{/root/gui/locService}/admin')"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="load('{/root/gui/locService}/user.get')"><xsl:value-of select="/root/gui/strings/newUser"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	form
	-->
	<xsl:template name="form">
		<script type="text/javascript">
			function deleteUser(service, message, id){
				var cswContactId = '<xsl:value-of select="/root/gui/env/csw/contactId"/>';
				
				if (id == cswContactId) {
					if (!confirm("<xsl:value-of select="/root/gui/strings/delUserCsw"/>"))
						return null;
				}
				doConfirm(service, message);
				
			}
		</script>
		
		<table border="0">
			<tr>
				<th class="padded" style="width:100px;"><b><xsl:value-of select="/root/gui/strings/username"/></b></th>
				<th class="padded" style="width:100px;"><b><xsl:value-of select="/root/gui/strings/surName"/></b></th>
				<th class="padded" style="width:100px;"><b><xsl:value-of select="/root/gui/strings/firstName"/></b></th>
				<th class="padded" style="width:100px;"><b><xsl:value-of select="/root/gui/strings/profile"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/strings/operation"/></b></th>
			</tr>
			<xsl:for-each select="/root/response/record">
				<xsl:sort select="username"/>
				
				<xsl:variable name="profileId">
					<xsl:value-of select="profile"/>
				</xsl:variable>
				
				<tr>
					<td class="padded"><xsl:value-of select="username"/></td>
					<td class="padded"><xsl:value-of select="surname"/></td>
					<td class="padded"><xsl:value-of select="name"/></td>
					<td class="padded"><xsl:value-of select="/root/gui/strings/profileChoice[@value=$profileId]"/></td>
					<td class="padded">
						<button class="content" onclick="load('{/root/gui/locService}/user.edit?id={id}')"><xsl:value-of select="/root/gui/strings/edit"/></button>
						&#160;
						<button class="content" onclick="load('{/root/gui/locService}/user.resetpw?id={id}')"><xsl:value-of select="/root/gui/strings/resetPassword"/></button>
						&#160;
						<xsl:if test="/root/gui/session/userId != id">
							<button class="content" onclick="deleteUser('{/root/gui/locService}/user.remove?id={id}','{/root/gui/strings/delUserConf}', {id})"><xsl:value-of select="/root/gui/strings/delete"/></button>
						</xsl:if>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
</xsl:stylesheet>
