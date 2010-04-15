<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="main.xsl"/>

	<!--
	main page
	-->
	<xsl:template match="/">
		<html>
			<head>
				<xsl:choose>
					<xsl:when test="contains(/root/gui/reqService,'metadata.show') or contains(/root/gui/reqService,'metadata.edit')">
						<xsl:call-template name="header">
							<xsl:with-param name="title" select="/root/gui/strings/editorViewer"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="header"/>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:apply-templates mode="script" select="/"/>
			</head>
			<body onload="init();initCalendar();validateMetadataFields();">
				<table width="100%">
					
					<!-- content -->
					<tr><td>
						<xsl:call-template name="content"/>
					</td></tr>
				</table>
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>
