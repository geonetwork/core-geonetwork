<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="modal.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/info/heading"/>
			<xsl:with-param name="content">
				<xsl:value-of select="/root/gui/info/message"/>
				<xsl:text>&#160;</xsl:text>
				<xsl:choose>
					<xsl:when test="/root/gui/info/text"><xsl:value-of select="/root/gui/info/text"/></xsl:when>
					<xsl:otherwise><xsl:value-of select="/root/response"/></xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
</xsl:stylesheet>
