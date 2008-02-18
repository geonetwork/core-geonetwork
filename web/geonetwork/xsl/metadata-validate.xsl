<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:include href="res.xsl"/>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:value-of select='/root/gui/validation/message'/>
		<br/><br/>
		<xsl:choose>
			<xsl:when test="/root/response/geonet:schematronerrors/geonet:errorFound">
				<font color="FF0000"><b>
				<xsl:value-of select='/root/gui/validation/schemaTronError'/>
				</b></font>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select='/root/gui/validation/schemaTronValid'/>
			</xsl:otherwise>
		</xsl:choose>
		<br/><br/>
		<a href="{/root/gui/url}/schematronCache/SchematronReport{/root/response/id}/schematron-frame.html">
			<xsl:value-of select="/root/gui/validation/schemaTronReport"/>
		</a>
		<br/>
	</xsl:template>
	
	<!--
	title
	-->
	<xsl:template mode="title" match="/">
			<xsl:value-of select='/root/gui/validation/heading'/>
	</xsl:template>

</xsl:stylesheet>

