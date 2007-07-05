<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	
	<xsl:include href="main.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/categories"/>
			<xsl:with-param name="content">

				<form name="update" accept-charset="UTF-8" action="{/root/gui/locService}/metadata.category" method="post">
					<input name="id" type="hidden" value="{/root/response/id}"/>
					<table>
						<tr>
							<th class="padded"><xsl:value-of select="/root/gui/strings/categories"/></th>
							<th class="padded"><xsl:value-of select="/root/gui/strings/assigned"/></th>
						</tr>

						<xsl:variable name="lang" select="/root/gui/language"/>
			
						<!-- loop on all categories -->

						<xsl:for-each select="/root/response/categories/category">
							<xsl:variable name="categId" select="id"/>
							<tr>
								<td class="padded"><xsl:value-of select="label/child::*[name() = $lang]"/></td>
								<td class="padded" align="center">
									<input type="checkbox" name="_{$categId}">
										<xsl:if test="on">
											<xsl:attribute name="checked"/>
										</xsl:if>
									</input>
								</td>
							</tr>
						</xsl:for-each>				
					</table>
				</form>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="goSubmit('update')"><xsl:value-of select="/root/gui/strings/submit"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
</xsl:stylesheet>
