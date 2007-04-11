<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/create/title"/>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="goSubmit('create')"><xsl:value-of select="/root/gui/create/button"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="form">
		<form name="create" accept-charset="UTF-8" action="{/root/gui/locService}/metadata.create" method="post">
			<input type="submit" style="display: none;" />
			<table>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/template"/></th>
					<td class="padded">
						<select class="content" name="id" size="1">
							<xsl:for-each select="/root/gui/templates/record">
								<option value="{id}">
									<xsl:value-of select="name"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
			
				<!-- groups -->

				<xsl:variable name="lang" select="/root/gui/language"/>

				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/groups"/></th>
					<td class="padded">
						<select class="content" name="group" size="10" multiple="">
							<xsl:for-each select="/root/gui/groups/record">
								<option value="{id}">
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
