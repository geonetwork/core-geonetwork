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
				if (document.categoryUpdateForm.name.value.length &lt; 1)
				{
					alert("<xsl:value-of select="/root/gui/strings/categoryNameMandatory"/>");
					return;
				}
				document.categoryUpdateForm.submit()
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
					<xsl:when test="/root/response/record/id">
						<xsl:value-of select="/root/gui/strings/updateCategory"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="/root/gui/strings/newCategory"/>
					</xsl:otherwise>
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
		<div class="important"><xsl:copy-of select="/root/gui/strings/localizationHelp"/></div>
		<form name="categoryUpdateForm" accept-charset="UTF-8" action="{/root/gui/locService}/category.update" method="post">
			<input type="submit" style="display: none;" />
			<xsl:if test="/root/response/record/id">
				<input type="hidden" name="id" value="{/root/response/record/id}"/>
			</xsl:if>
			<table>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/name"/></th>
					<td class="padded"><input class="content" type="text" name="name" value="{/root/response/record/name}" size="60"/></td>
				</tr>
			</table>
		</form>
	</xsl:template>
	
</xsl:stylesheet>
