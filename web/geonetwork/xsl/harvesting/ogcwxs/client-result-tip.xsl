<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate the result tooltip for the harvesting entry list -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/node">
		<xsl:choose>
			<xsl:when test="info/result">
				<xsl:apply-templates select="info/result"/>
			</xsl:when>
			
			<xsl:otherwise>
				<span><xsl:value-of select="/root/strings/notRun"/></span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="result">
		<table>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/total"/></td>
				<td class="tipCell"><xsl:value-of select="total"/></td>
			</tr>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/added"/></td>
				<td class="tipCell"><xsl:value-of select="added"/></td>
			</tr>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/removed"/></td>
				<td class="tipCell"><xsl:value-of select="removed"/></td>
			</tr>
			

			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/datasetAdded"/></td>
				<td class="tipCell"><xsl:value-of select="layer"/></td>
			</tr>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/datasetExist"/></td>
				<td class="tipCell"><xsl:value-of select="layerUuidExist"/></td>
			</tr>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/datasetMdUrl"/></td>
				<td class="tipCell"><xsl:value-of select="layerUsingMdUrl"/></td>
			</tr>
			
			
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/thumbnails"/></td>
				<td class="tipCell"><xsl:value-of select="thumbnails"/></td>
			</tr>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/thumbnailsFailed"/></td>
				<td class="tipCell"><xsl:value-of select="thumbnailsFailed"/></td>
			</tr>


			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/unknownSchema"/></td>
				<td class="tipCell"><xsl:value-of select="unknownSchema"/></td>					
			</tr>

			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/unretrievable"/></td>
				<td class="tipCell"><xsl:value-of select="unretrievable"/></td>					
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
