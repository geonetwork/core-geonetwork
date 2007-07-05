<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/error/heading"/>
			<xsl:with-param name="content">
				<font class="error"><xsl:value-of select="/root/gui/error/message"/></font>
				<p/>
				<xsl:value-of select="/root/error/exception/message"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="JavaScript:goBack()"><xsl:value-of select="/root/gui/strings/backToPreviousPage"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
</xsl:stylesheet>
