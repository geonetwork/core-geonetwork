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
		<table border="0">
			<tr>
				<th class="padded"><xsl:value-of select="/root/gui/strings/username"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/strings/surName"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/strings/firstName"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/strings/profile"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/strings/operation"/></th>
			</tr>
			<xsl:for-each select="/root/response/record">
				<tr>
					<td class="padded"><xsl:value-of select="username"/></td>
					<td class="padded"><xsl:value-of select="surname"/></td>
					<td class="padded"><xsl:value-of select="name"/></td>
					<td class="padded"><xsl:value-of select="profile"/></td>
					<td class="padded">
						<button class="content" onclick="load('{/root/gui/locService}/user.get?id={id}')"><xsl:value-of select="/root/gui/strings/edit"/></button>
						&#160;
						<xsl:if test="/root/gui/session/userId != id">
							<button class="content" onclick="doConfirm('{/root/gui/locService}/user.remove?id={id}','{/root/gui/strings/delUserConf}')"><xsl:value-of select="/root/gui/strings/delete"/></button>
						</xsl:if>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
</xsl:stylesheet>
