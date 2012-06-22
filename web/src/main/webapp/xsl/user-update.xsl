<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>
	<xsl:include href="user-update-utils.xsl"/>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/update"/>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="document.userupdateform.submit();"><xsl:value-of select="/root/gui/strings/save"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	form
	-->
	<xsl:template name="form">
		<form name="userupdateform" accept-charset="UTF-8" action="{/root/gui/locService}/user.update?operation=editinfo" method="post">
			<input type="hidden" name="id" value="{/root/response/record/id}"/>
			<input type="hidden" name="username" value="{/root/response/record/username}"/>
			<input type="hidden" name="password" value="password"/>
			<input type="submit" style="display: none;" />
			<table class="text-aligned-left">
				<tr>
          <th class="padded"><xsl:value-of select="/root/gui/strings/username"/></th>
          <td class="padded"><xsl:value-of select="/root/response/record/username"/></td>
        </tr>
				<xsl:call-template name="userinfofields"/>
			</table>
		</form>
	</xsl:template>
	
</xsl:stylesheet>

