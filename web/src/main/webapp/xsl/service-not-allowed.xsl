<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:exslt="http://exslt.org/common">

	<xsl:output
		omit-xml-declaration="yes" 
		method="html" 
		doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
		doctype-system="http://www.w3.org/TR/html4/loose.dtd"
		indent="yes"
		encoding="UTF-8" />
	
    <xsl:include href="main.xsl"/>

	<!--
	main page
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/error"/>
			<xsl:with-param name="content">
				<h1><xsl:value-of select="replace(/root/gui/strings/serviceNotAllowed, '\{1\}',/root/request/referer)" /></h1>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="css" match="/"/>

</xsl:stylesheet>
