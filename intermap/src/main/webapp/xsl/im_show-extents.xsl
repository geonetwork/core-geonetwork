<?xml version="1.0" encoding="UTF-8"?>
<!-- Change the selected time (time dimension/temporal extent) -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<xsl:variable name="name" select="/root/response/service/@name"/>
		<html>
			<head>
				<title>
					<xsl:value-of select="/root/gui/strings/title"/>
				</title>
				<link rel="stylesheet" type="text/css" href="../../intermap.css"/>
				<script language="JavaScript" src="../../scripts/intermap.js"/>
			</head>
			<body style="margin:10px;" onLoad="javascript:window.focus();">
				<xsl:apply-templates select="//Layer[Name=$name]"/>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="Layer">
		<h1>
			<xsl:value-of select="Title"/>
		</h1>
		<p>
			<b>
				<xsl:value-of select="/root/gui/strings/abstract"/>: </b>
			<xsl:value-of select="Abstract"/>
		</p>
		<form name="form" method="post" action="{/root/gui/locService}/map.service.setExtents">
			<p>
				<xsl:apply-templates select="/root/response/extents"/>
			</p>
			<p>
				<center>
					<button class="content" onclick="document.form.submit();"><xsl:value-of select="/root/gui/strings/ok"/></button>
				</center>
			</p>
			<p class="emphasize" align="center"><xsl:value-of select="/root/gui/strings/refreshNotification"/></p>
			<input type="hidden" name="id" value="{/root/response/id}"/>
		</form>
	</xsl:template>
	
	<!-- Extent menus -->
	<xsl:template match="extents">
		<xsl:if test="extent">
			<b>
				<xsl:value-of select="/root/gui/strings/extents"/>: </b>
		</xsl:if>
		<p align="center">
			<xsl:for-each select="extent">
				<xsl:variable name="default" select="@default"/>
				<xsl:variable name="name" select="@name"/>
				<!--		<b><xsl:value-of select="@name" />:<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></b><br/> -->
				<select name="{@name}">
					<xsl:for-each select="value">
						<option value="{.}">
							<!-- no extent selected -->
							<xsl:if test="$default = . and not(/root/response/service/selectedExtents/*[name() = $name])">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							<!-- extent selected -->
							<xsl:if test=". = /root/response/service/selectedExtents/*[name() = $name]/text()">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
					<xsl:variable name="date"><xsl:value-of select="."/></xsl:variable>
					<xsl:choose>
						<xsl:when test="$date='9999-01-01'">January</xsl:when>
						<xsl:when test="$date='9999-02-01'">February</xsl:when>
						<xsl:when test="$date='9999-03-01'">March</xsl:when>
						<xsl:when test="$date='9999-04-01'">April</xsl:when>
						<xsl:when test="$date='9999-05-01'">May</xsl:when>
						<xsl:when test="$date='9999-06-01'">June</xsl:when>
						<xsl:when test="$date='9999-07-01'">July</xsl:when>
						<xsl:when test="$date='9999-08-01'">August</xsl:when>
						<xsl:when test="$date='9999-09-01'">September</xsl:when>
						<xsl:when test="$date='9999-10-01'">October</xsl:when>
						<xsl:when test="$date='9999-11-01'">November</xsl:when>
						<xsl:when test="$date='9999-12-01'">December</xsl:when>
						<xsl:otherwise><xsl:value-of select="." /></xsl:otherwise>
					</xsl:choose>
						</option>
					</xsl:for-each>
				</select>
			</xsl:for-each>
		</p>
	</xsl:template>
</xsl:stylesheet>
