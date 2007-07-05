<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="res.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<font class="error"><xsl:value-of select="/root/gui/error/message"/></font>
		<p/>
		<xsl:value-of select="/root/error/exception/message"/>
	</xsl:template>
	
	<!--
	title
	-->
	<xsl:template mode="title" match="/">
		<xsl:value-of select="/root/gui/error/heading"/>
	</xsl:template>

</xsl:stylesheet>

