<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:include href="../modal.xsl"/>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<table style="width: 100%;">
			<tr>
				<td><xsl:value-of select="/root/gui/harvesting/historyDeleted"/></td>
				<td style="text-align: center;"><xsl:value-of select="/root/response"/></td>
			</tr>
		</table>
	</xsl:template>
	
</xsl:stylesheet>

