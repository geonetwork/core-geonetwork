<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="res.xsl"/>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:value-of select="/root/gui/validation/message"/>
	</xsl:template>
	
	<!--
	title
	-->
	<xsl:template mode="title" match="/">
		<xsl:value-of select="/root/gui/validation/heading"/>
	</xsl:template>

</xsl:stylesheet>

