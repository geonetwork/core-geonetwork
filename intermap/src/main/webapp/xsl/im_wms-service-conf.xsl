<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- Change the layer style -->
<xsl:template match="/">
	<xsl:variable name="name" select="/root/response/service/@name" />
	<html>
		<head>
			<title>
				<xsl:value-of select="/root/gui/strings/title" />
			</title>
			<link rel="stylesheet" type="text/css" href="../../intermap.css" />
			<script language="JavaScript" src="{/root/gui/url}/scripts/intermap.js"/>
		</head>
		<body style="margin:10px;" onLoad="javascript:window.focus();">
			<xsl:apply-templates select="//Layer[Name=$name]" />
		</body>
	</html>
</xsl:template>

<xsl:template match="Layer">
	<h1>
		<xsl:value-of select="Title"/>
	</h1>
	<p>
		<b><xsl:value-of select="/root/gui/strings/abstract"/>: </b><xsl:value-of select="Abstract"/>
	</p>
	<form name="form" method="post" action="{/root/gui/locService}/map.service.wmsConfig">
		<p>
			<xsl:choose>
				<xsl:when test="./ancestor-or-self::Layer/Style">
				<xsl:value-of select="/root/gui/strings/style" /><xsl:text>: </xsl:text>
					<select name="style">
						<xsl:apply-templates select="./ancestor-or-self::Layer/Style" />
					</select>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="/root/gui/strings/noStyle" />
				</xsl:otherwise>
			</xsl:choose>
		</p>
		<p>
			<xsl:apply-templates select="/root/response/extents" />
		</p>
		<p>
			<center>
				<button class="content" onclick="javascript:document.form.submit();"><xsl:value-of select="/root/gui/strings/ok"/></button>
			</center>
		</p>
		<p class="emphasize" align="center"><xsl:value-of select="/root/gui/strings/submit/refreshNotification"/></p>
		<input type="hidden" name="id" value="{/root/response/id}" />
	</form>
</xsl:template>

<!-- Styles menu -->
<xsl:template match="Style">
	<option value="{Name}">
		<xsl:if test="/root/response/service/@style = Name">
			<xsl:attribute name="selected">selected</xsl:attribute>
		</xsl:if>
		<xsl:value-of select="Title" />
	</option>
</xsl:template>

<!-- Extent menus -->
<xsl:template match="extents">
	<xsl:if test="extent">
		<xsl:value-of select="/root/gui/strings/extents" />
	</xsl:if>
	<xsl:for-each select="extent">
		<xsl:variable name="default" select="@default" />
		<xsl:value-of select="@name" />
		<select name="{@name}">
			<xsl:for-each select="value">
				<option value="{.}">
					<xsl:if test="$default = .">
						<xsl:attribute name="selected">selected</xsl:attribute>
					</xsl:if>
					<xsl:value-of select="." />
				</option>
			</xsl:for-each>
		</select>
	</xsl:for-each>
	<option value="{name}">
		<xsl:if test="/root/response/service/@style = Name">
			<xsl:attribute name="selected">selected</xsl:attribute>
		</xsl:if>
		<xsl:value-of select="Title" />
	</option>
</xsl:template>

</xsl:stylesheet>
