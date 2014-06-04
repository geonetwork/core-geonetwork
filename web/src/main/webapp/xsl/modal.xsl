<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="header.xsl"/>
	<xsl:import href="utils.xsl"/>

	<!--
	main page
	-->
	<xsl:template match="/">
		<html>
			<body>
				<xsl:call-template name="content"/>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template mode="script" match="/"/>
	<xsl:template mode="css" match="/"/>

	<xsl:template name="formLayout">
		<xsl:param name="content"/>
		<xsl:param name="buttons"/>
		
		<!-- content -->
		<xsl:call-template name="formContent">
			<xsl:with-param name="content" select="$content"/>
			<xsl:with-param name="buttons" select="$buttons"/>
		</xsl:call-template>

	</xsl:template>

	<xsl:template name="formContent">
		<xsl:param name="content"/>
		<xsl:param name="buttons"/>
		<div>
			<xsl:copy-of select="$content"/>
			<xsl:if test="$buttons">
				<xsl:copy-of select="$buttons"/>
			</xsl:if>
		</div>
	</xsl:template>
	
</xsl:stylesheet>
