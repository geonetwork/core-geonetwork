<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>
	
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
		<form name="userupdateform" accept-charset="UTF-8" action="{/root/gui/locService}/user.infoupdate" method="post">
			<input type="submit" style="display: none;" />
			<table>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/username"/></th>
					<td class="padded"><xsl:value-of select="/root/response/record/username"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/surName"/></th>
					<td class="padded"><input class="content" type="text" name="surname" value="{/root/response/record/surname}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/firstName"/></th>
					<td class="padded"><input class="content" type="text" name="name" value="{/root/response/record/name}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/address"/></th>
					<td class="padded"><input class="content" type="text" name="address" value="{/root/response/record/address}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/state"/></th>
					<td class="padded">
						<input class="content" type="text" name="state" value="{/root/response/record/state}" size="8"/>
						<b><xsl:value-of select="/root/gui/strings/zip"/></b>
						<input class="content" type="text" name="zip" value="{/root/response/record/zip}" size="8"/>
					</td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/country"/></th>
					<td class="padded">
						<select class="content" size="1" name="country">
							<xsl:if test="string(/root/response/record/country)=''">
								<option value=""/>
							</xsl:if>
							<xsl:for-each select="/root/gui/countries/country">
								<option value="{@iso2}">
									<xsl:if test="string(/root/response/record/country)=@iso2">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="."/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/email"/></th>
					<td class="padded"><input class="content" type="text" name="email" value="{/root/response/record/email}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/organisation"/></th>
					<td class="padded"><input class="content" type="text" name="org" value="{/root/response/record/organisation}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/kind"/></th>
					<td class="padded">
						<select class="content" size="1" name="kind">
							<xsl:for-each select="/root/gui/strings/kindChoice">
								<option value="{@value}">
									<xsl:if test="string(/root/response/record/kind)=@value">
										<xsl:attribute name="selected"/>
									</xsl:if>
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

