<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/batchImport"/>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="goSubmit('xmlbatch')"><xsl:value-of select="/root/gui/strings/upload"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="form">
		<form name="xmlbatch" accept-charset="UTF-8" action="{/root/gui/locService}/util.import" method="post">
			<input type="submit" style="display: none;" />
			<table>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/directory"/></th>
					<td class="padded"><input class="content" type="text" name="dir"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/schema"/></th>
					<td class="padded">
						<select class="content" name="schema" size="1">
							<xsl:for-each select="/root/gui/schemas/name">
								<option value="{.}">
									<xsl:value-of select="."/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/validate"/></th>
					<td><input class="content" type="checkbox" name="validate"/></td>
				</tr>
				
				<!-- groups -->
				
				<xsl:variable name="lang" select="/root/gui/language"/>

				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/group"/></th>
					<td class="padded">
						<select class="content" name="group" size="1">
							<xsl:for-each select="/root/gui/groups/record">
								<option value="{id}">
									<xsl:value-of select="label/child::*[name() = $lang]"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>

				<!-- categories -->

				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/category"/></th>
					<td class="padded">
						<select class="content" name="category" size="1">
							<option value="_none_">
								<xsl:value-of select="/root/gui/strings/none"/>
							</option>
							<xsl:for-each select="/root/gui/categories/record">
								<xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>
								<option value="{id}">
									<xsl:value-of select="label/child::*[name() = $lang]"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>

				<!-- transformation stylesheet -->

				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/styleSheet"/></th>
					<td class="padded">
						<select class="content" name="styleSheet" size="1">
							<option value="_none_">
								<xsl:value-of select="/root/gui/strings/none"/>
							</option>
							<xsl:for-each select="/root/gui/importStyleSheets/record">
								<option value="{id}">
									<xsl:value-of select="name"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>

</xsl:stylesheet>
