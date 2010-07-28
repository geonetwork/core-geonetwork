<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/searchUnused"/>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="goSubmit('searchForm')"><xsl:value-of select="/root/gui/unused/search"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="form">
		<form name="searchForm" accept-charset="UTF-8" action="{/root/gui/locService}/metadata.searchunused" method="post">
			<input type="submit" style="display: none;" />
			<table width="70%" class="text-aligned-left">
				<tr>
					<td colspan="2">
						<xsl:value-of select="/root/gui/unused/maxTimeMsg"/>
					</td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/unused/maxDiff"/></th>
					<td class="padded" width="80%">
						<select class="content" name="maxDiff" size="1">
							<xsl:for-each select="/root/gui/unused/diffList/item">
								<option value="{@id}">
									<xsl:value-of select="."/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>

</xsl:stylesheet>
