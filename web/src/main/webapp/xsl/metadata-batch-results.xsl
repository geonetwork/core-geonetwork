<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:include href="modal.xsl"/>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<table style="width: 100%;">
			<tr>
				<td><xsl:value-of select="/root/gui/info/updated"/></td>
				<td style="text-align: center;"><xsl:value-of select="/root/response/done"/></td>
			</tr>
			<tr>
				<td><xsl:value-of select="/root/gui/info/notowner"/></td>
				<td style="text-align: center;"><xsl:value-of select="/root/response/notOwner"/></td>
			</tr>
			<tr>
				<td><xsl:value-of select="/root/gui/info/notfound"/></td>
				<td style="text-align: center;"><xsl:value-of select="/root/response/notFound"/></td>
			</tr>
			<xsl:if test="/root/gui/reqService='metadata.batch.processing'">
				<tr>
					<td><xsl:value-of select="/root/gui/info/notProcessFound"/></td>
					<td style="text-align: center;"><xsl:value-of select="/root/response/notProcessFound"/></td>
				</tr>
			</xsl:if>
		</table>
	</xsl:template>
	
	<!--
	title
	-->
	<xsl:template mode="title" match="/" priority="20">
			<xsl:value-of select='/root/gui/info/resultstitle'/>
	</xsl:template>

</xsl:stylesheet>

