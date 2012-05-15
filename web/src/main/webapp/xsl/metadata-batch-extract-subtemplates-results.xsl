<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:output method="html"/>

	<xsl:include href="modal.xsl"/>
	
	<!--
	page content
	-->

	<xsl:template name="content">
		<xsl:choose>
			<xsl:when test="normalize-space(/root/request/doChanges)='on'">

		<table style="width: 100%;">
			<tr>
				<td><xsl:value-of select="/root/gui/info/updated"/></td>
				<td style="text-align: center;"><xsl:value-of select="/root/response/done"/></td>
			</tr>
			<tr>
				<td><xsl:value-of select="/root/gui/info/subtemplates"/></td>
				<td style="text-align: center;"><xsl:value-of select="/root/response/subtemplates"/></td>
			</tr>
			<tr>
				<td><xsl:value-of select="/root/gui/info/notowner"/></td>
				<td style="text-align: center;"><xsl:value-of select="/root/response/notOwner"/></td>
			</tr>
			<tr>
				<td><xsl:value-of select="/root/gui/info/notfound"/></td>
				<td style="text-align: center;"><xsl:value-of select="/root/response/notFound"/></td>
			</tr>
		</table>

			</xsl:when>
			<xsl:otherwise>

		<!-- show some test results if in test mode -->
		<br/><b>XPath</b><br/>
		<p><xsl:value-of select="/root/response/xpath"/></p>
		<br/><b>XPath Selected</b><br/>
		<p><xsl:value-of select="/root/response/xpathReturned"/></p>
		<br/><b>Subtemplate</b><br/>
		<pre><xsl:value-of select="/root/response/subtemplate"/></pre>
		<br/>
		<br/><b>Title</b><br/>
		<pre><xsl:value-of select="/root/response/title"/></pre>
		<br/>
		<br/><b>Replaced Element</b><br/>
		<pre><xsl:value-of select="/root/response/replacedElement"/></pre>
		<br/>

			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
	
	<!--
	title
	-->
	<xsl:template mode="title" match="/" priority="20">
			<xsl:value-of select='/root/gui/info/resultstitle'/>
	</xsl:template>

</xsl:stylesheet>

