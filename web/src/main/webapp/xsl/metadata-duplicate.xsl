<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>

	<!-- ============================================================================= -->
	<!-- page content -->
	<!-- ============================================================================= -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/create/title"/>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="document.createform.submit();"><xsl:value-of select="/root/gui/strings/create"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template name="form">
		<form name="createform" accept-charset="UTF-8" action="metadata.create" method="post">
			<input name="id" type="hidden" value="{/root/response/id}"/>
			<input name="uuid" type="hidden" value="{/root/response/uuid}"/>
			<input name="child" type="hidden" value="{/root/response/child}"/>
			<table>
				<tr>
					<td colspan="2">
						<xsl:value-of select="/root/gui/create/message"/>
					</td>
				</tr>

				<!-- groups -->
				<xsl:variable name="lang" select="/root/gui/language"/>

				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/groups"/></th>
					<td class="padded">
							<select class="content" name="group" id="groups">
							<xsl:for-each select="/root/gui/groups/record">
								<xsl:sort select="label/child::*[name() = $lang]"/>
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
