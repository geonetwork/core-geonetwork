<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="main.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/download"/>
			<xsl:with-param name="content">
				<h2><xsl:value-of select="/root/gui/strings/messageDownload"/></h2>
				<p/>
				<xsl:copy-of select="/root/gui/strings/moreinfo"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="load('{/root/gui/locService}/metadata.show?id={/root/request/id}')"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="load('{/root/gui/locService}/resources.get?access=private&amp;id={/root/request/id}&amp;fname={/root/request/fname}')"><xsl:value-of select="/root/gui/strings/accept"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
</xsl:stylesheet>
