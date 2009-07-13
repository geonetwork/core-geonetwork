<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:include href="res.xsl"/>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<b><xsl:value-of select='/root/gui/validation/message'/></b>
		<br/><br/>
		<b><xsl:value-of select='/root/gui/validation/schemaTronValid'/></b>
		<br/>
	</xsl:template>
	
	<!--
	title
	-->
	<xsl:template mode="title" match="/">
			<xsl:value-of select='/root/gui/validation/heading'/>
	</xsl:template>

</xsl:stylesheet>

