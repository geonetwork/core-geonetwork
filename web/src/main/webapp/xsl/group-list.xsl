<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	
	<xsl:include href="main.xsl"/>

	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
		<script type="text/javascript" language="JavaScript">
			function delete1(url)
			{
				if(confirm("<xsl:value-of select="/root/gui/strings/deleteGroup"/>"))
					load(url);
			}
		</script>
	</xsl:template>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/groupManagement"/>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="load('{/root/gui/locService}/admin')"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="load('{/root/gui/locService}/group.get')"><xsl:value-of select="/root/gui/strings/newGroup"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	form
	-->
	<xsl:template name="form">
		<div class="important"><xsl:value-of select="/root/gui/strings/localizationHelp"/></div>
		<table width="70%" class="text-aligned-left">
			<tr>
				<th class="padded, bottom_border"><xsl:value-of select="/root/gui/strings/name"/></th>
				<th class="padded, bottom_border"><xsl:value-of select="/root/gui/strings/descriptionTab"/></th>
				<th class="padded, bottom_border"><xsl:value-of select="/root/gui/strings/emailAddress"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/strings/operation"/></th>
			</tr>
			<xsl:for-each select="/root/response/record">
				<xsl:sort select="name"/>
				<tr>
					<td class="padded, bottom_border"><xsl:value-of select="name"/></td>
					<td class="padded, bottom_border" width="150"><xsl:value-of select="description"/></td>
					<td class="padded, bottom_border"><xsl:value-of select="email"/></td>
					<td class="padded" width="150px">
						<button class="content" onclick="load('{/root/gui/locService}/group.get?id={id}')"><xsl:value-of select="/root/gui/strings/edit"/></button>
						&#160;
						<button class="content" onclick="delete1('{/root/gui/locService}/group.remove?id={id}')"><xsl:value-of select="/root/gui/strings/delete"/></button>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	
</xsl:stylesheet>
