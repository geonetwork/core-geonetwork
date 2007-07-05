<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="res.xsl"/>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:variable name="schema" select="string(/root/response/schema)"/>
		<xsl:variable name="name"   select="string(/root/response/name)"/>
		
		<xsl:choose>
			<xsl:when test="$name!=''">
				<table width="100%">
					<xsl:for-each select="/root/gui/*[name(.)=$schema]/*[name(.)=$name and string(@help)!='']">
						<xsl:call-template name="row">
							<xsl:with-param name="name" select="$name"/>
						</xsl:call-template>
					</xsl:for-each>
				</table>
				<p>
					<button class="content" onclick="document.location.href='{/root/gui/locService}/help.metadata?schema={$schema}#{$name}'"><xsl:value-of select="/root/gui/strings/helpComplete"/></button>
				</p>
			</xsl:when>
			<xsl:otherwise>
				<table width="100%">
					<xsl:for-each select="/root/gui/*[name(.)=$schema]/*[string(@help)!='']">
						<xsl:variable name="name" select="name(.)"/>
						<xsl:call-template name="row">
							<xsl:with-param name="name" select="$name"/>
						</xsl:call-template>
					</xsl:for-each>
				</table>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="row">
		<xsl:param name="name"/>
		
		<tr>
			<th class="padded" valign="top">
				<a name="{$name}"><xsl:value-of select="string(.)"/></a>
			</th>
			<td class="padded" valign="top">
				<xsl:value-of select="@help"/>
				<xsl:choose>
					<xsl:when test="string(@condition)='conditional'">
						<br/>
						<font class="warning">
							<xsl:value-of select="/root/gui/strings/mandatoryIf"/>
							<xsl:text> </xsl:text>
							<xsl:value-of select="@consdesc"/>
						</font>
					</xsl:when>
					<xsl:when test="string(@condition)='mandatory'">
						<br/>
						<font class="warning">
							<xsl:value-of select="/root/gui/strings/mandatory"/>
						</font>
					</xsl:when>
				</xsl:choose>
			</td>
		</tr>
	</xsl:template>
	
	<!--
	title
	-->
	<xsl:template mode="title" match="/">
		<xsl:value-of select="/root/gui/strings/help"/>
	</xsl:template>

</xsl:stylesheet>
